#include "merge_engine.hpp"
#include "terminal_ui.hpp"
#include <algorithm>
#include <sstream>
#include <fstream>

namespace svcs {
namespace core {

MergeEngine::MergeEngine(svcs_repository_t* repo) : repository(repo) {
    if (repo) {
        dag = std::make_unique<CommitDAG>(repo);
        dag->load_from_repository();
    }
}

MergeResult MergeEngine::merge_branches(const std::string& source_branch, const std::string& target_branch) {
    MergeResult result;
    
    if (!repository || !dag) {
        result.error_message = "Repository not initialized";
        return result;
    }
    
    // Get branch commit hashes
    auto source_commit = dag->resolve_reference(source_branch);
    auto target_commit = dag->resolve_reference(target_branch);
    
    if (!source_commit || !target_commit) {
        result.error_message = "Branch not found";
        return result;
    }
    
    // Check if fast-forward is possible
    if (can_fast_forward(source_branch, target_branch)) {
        return fast_forward_merge(source_branch, target_branch);
    }
    
    // Find merge base
    auto merge_base = find_merge_base(source_commit->hash, target_commit->hash);
    if (!merge_base) {
        result.error_message = "No common ancestor found";
        return result;
    }
    
    // Perform three-way merge
    return perform_recursive_merge(merge_base->hash, target_commit->hash, source_commit->hash);
}

MergeResult MergeEngine::fast_forward_merge(const std::string& source_branch, const std::string& target_branch) {
    MergeResult result;
    
    // Update target branch to point to source branch commit
    auto source_commit = dag->resolve_reference(source_branch);
    if (!source_commit) {
        result.error_message = "Source branch not found";
        return result;
    }
    
    // Update branch reference
    char branch_file[SVCS_MAX_PATH];
    snprintf(branch_file, sizeof(branch_file), "%s/refs/heads/%s", repository->git_dir, target_branch.c_str());
    
    char hash_str[SVCS_HASH_HEX_SIZE];
    svcs_hash_to_string(&source_commit->hash, hash_str);
    
    std::string content = std::string(hash_str) + "\n";
    if (svcs_file_write(branch_file, content.c_str(), content.length()) == SVCS_OK) {
        result.success = true;
        result.is_fast_forward = true;
        result.merge_commit_hash = source_commit->hash;
        
        // Count commits merged
        auto target_commit = dag->resolve_reference(target_branch);
        if (target_commit) {
            result.files_changed = count_commits_between(target_commit->hash, source_commit->hash);
        }
    } else {
        result.error_message = "Failed to update branch reference";
    }
    
    return result;
}

bool MergeEngine::can_fast_forward(const std::string& source_branch, const std::string& target_branch) {
    auto source_commit = dag->resolve_reference(source_branch);
    auto target_commit = dag->resolve_reference(target_branch);
    
    if (!source_commit || !target_commit) {
        return false;
    }
    
    // Check if target is ancestor of source
    return is_ancestor(target_commit->hash, source_commit->hash);
}

std::shared_ptr<CommitNode> MergeEngine::find_merge_base(const svcs_hash_t& commit1, const svcs_hash_t& commit2) {
    // Simple implementation - find lowest common ancestor
    auto node1 = dag->get_commit(std::string(reinterpret_cast<const char*>(&commit1), sizeof(commit1)));
    auto node2 = dag->get_commit(std::string(reinterpret_cast<const char*>(&commit2), sizeof(commit2)));
    
    if (!node1 || !node2) {
        return nullptr;
    }
    
    // Get all ancestors of commit1
    std::set<std::string> ancestors1;
    std::function<void(std::shared_ptr<CommitNode>)> collect_ancestors = [&](std::shared_ptr<CommitNode> node) {
        if (!node) return;
        ancestors1.insert(node->hash_string());
        for (auto& parent : node->parents) {
            collect_ancestors(parent);
        }
    };
    collect_ancestors(node1);
    
    // Find first common ancestor in commit2's ancestry
    std::function<std::shared_ptr<CommitNode>(std::shared_ptr<CommitNode>)> find_common = 
        [&](std::shared_ptr<CommitNode> node) -> std::shared_ptr<CommitNode> {
        if (!node) return nullptr;
        
        if (ancestors1.count(node->hash_string())) {
            return node;
        }
        
        for (auto& parent : node->parents) {
            auto result = find_common(parent);
            if (result) return result;
        }
        
        return nullptr;
    };
    
    return find_common(node2);
}

ThreeWayMergeResult MergeEngine::three_way_merge_files(const std::string& base_content,
                                                      const std::string& our_content,
                                                      const std::string& their_content) {
    auto base_lines = split_into_lines(base_content);
    auto our_lines = split_into_lines(our_content);
    auto their_lines = split_into_lines(their_content);
    
    return three_way_merge_lines(base_lines, our_lines, their_lines);
}

ThreeWayMergeResult MergeEngine::three_way_merge_lines(const std::vector<std::string>& base_lines,
                                                      const std::vector<std::string>& our_lines,
                                                      const std::vector<std::string>& their_lines) {
    ThreeWayMergeResult result;
    
    // Simple three-way merge algorithm
    std::vector<std::string> merged_lines;
    size_t base_idx = 0, our_idx = 0, their_idx = 0;
    
    while (base_idx < base_lines.size() || our_idx < our_lines.size() || their_idx < their_lines.size()) {
        // If all three have the same line, use it
        if (base_idx < base_lines.size() && our_idx < our_lines.size() && their_idx < their_lines.size() &&
            base_lines[base_idx] == our_lines[our_idx] && our_lines[our_idx] == their_lines[their_idx]) {
            merged_lines.push_back(our_lines[our_idx]);
            base_idx++; our_idx++; their_idx++;
            continue;
        }
        
        // If our version matches base, use their version
        if (base_idx < base_lines.size() && our_idx < our_lines.size() &&
            base_lines[base_idx] == our_lines[our_idx] && their_idx < their_lines.size()) {
            merged_lines.push_back(their_lines[their_idx]);
            base_idx++; our_idx++; their_idx++;
            continue;
        }
        
        // If their version matches base, use our version
        if (base_idx < base_lines.size() && their_idx < their_lines.size() &&
            base_lines[base_idx] == their_lines[their_idx] && our_idx < our_lines.size()) {
            merged_lines.push_back(our_lines[our_idx]);
            base_idx++; our_idx++; their_idx++;
            continue;
        }
        
        // Conflict detected
        MergeConflict conflict;
        conflict.type = ConflictType::CONTENT;
        conflict.our_line_start = static_cast<int>(our_idx);
        conflict.their_line_start = static_cast<int>(their_idx);
        
        // Collect conflicting lines
        std::vector<std::string> our_conflict_lines;
        std::vector<std::string> their_conflict_lines;
        
        // Simple heuristic: take next few lines as conflict
        int conflict_size = 1;
        for (int i = 0; i < conflict_size && our_idx + i < our_lines.size(); i++) {
            our_conflict_lines.push_back(our_lines[our_idx + i]);
        }
        for (int i = 0; i < conflict_size && their_idx + i < their_lines.size(); i++) {
            their_conflict_lines.push_back(their_lines[their_idx + i]);
        }
        
        conflict.our_content = join_lines(our_conflict_lines);
        conflict.their_content = join_lines(their_conflict_lines);
        conflict.our_line_end = conflict.our_line_start + static_cast<int>(our_conflict_lines.size()) - 1;
        conflict.their_line_end = conflict.their_line_start + static_cast<int>(their_conflict_lines.size()) - 1;
        
        result.conflicts.push_back(conflict);
        result.has_conflicts = true;
        
        // Add conflict markers to merged content
        merged_lines.push_back(\"<<<<<<< HEAD\");
        merged_lines.insert(merged_lines.end(), our_conflict_lines.begin(), our_conflict_lines.end());
        merged_lines.push_back(\"=======\");
        merged_lines.insert(merged_lines.end(), their_conflict_lines.begin(), their_conflict_lines.end());
        merged_lines.push_back(\">>>>>>> \" + std::string(\"branch\"));
        
        our_idx += conflict_size;
        their_idx += conflict_size;
        base_idx += conflict_size;
    }
    
    result.merged_content = join_lines(merged_lines);
    result.success = true;
    
    return result;
}

MergeResult MergeEngine::perform_recursive_merge(const svcs_hash_t& base_commit,
                                                const svcs_hash_t& our_commit,
                                                const svcs_hash_t& their_commit) {
    MergeResult result;
    
    // Get file trees for all three commits
    auto base_tree = get_file_tree(base_commit);
    auto our_tree = get_file_tree(our_commit);
    auto their_tree = get_file_tree(their_commit);
    
    // Collect all files from all trees
    std::set<std::string> all_files;
    for (const auto& [path, hash] : base_tree) all_files.insert(path);
    for (const auto& [path, hash] : our_tree) all_files.insert(path);
    for (const auto& [path, hash] : their_tree) all_files.insert(path);
    
    std::map<std::string, std::string> merged_files;
    
    for (const std::string& file_path : all_files) {
        bool in_base = base_tree.count(file_path) > 0;
        bool in_ours = our_tree.count(file_path) > 0;
        bool in_theirs = their_tree.count(file_path) > 0;
        
        if (in_base && in_ours && in_theirs) {
            // File exists in all three - three-way merge
            std::string base_content = \"// Base content for \" + file_path;
            std::string our_content = \"// Our content for \" + file_path;
            std::string their_content = \"// Their content for \" + file_path;
            
            auto merge_result = three_way_merge_files(base_content, our_content, their_content);
            
            if (merge_result.has_conflicts) {
                result.conflicts.insert(result.conflicts.end(), 
                                      merge_result.conflicts.begin(), 
                                      merge_result.conflicts.end());
            }
            
            merged_files[file_path] = merge_result.merged_content;
            
        } else if (!in_base && in_ours && in_theirs) {
            // Both added the same file - potential conflict
            MergeConflict conflict;
            conflict.file_path = file_path;
            conflict.type = ConflictType::ADD_ADD;
            conflict.our_content = \"// Our version of \" + file_path;
            conflict.their_content = \"// Their version of \" + file_path;
            result.conflicts.push_back(conflict);
            
        } else if (in_base && in_ours && !in_theirs) {
            // We modified, they deleted
            MergeConflict conflict;
            conflict.file_path = file_path;
            conflict.type = ConflictType::MODIFY_DELETE;
            conflict.our_content = \"// Our modified version\";
            conflict.their_content = \"\"; // Deleted
            result.conflicts.push_back(conflict);
            
        } else if (in_base && !in_ours && in_theirs) {
            // We deleted, they modified
            MergeConflict conflict;
            conflict.file_path = file_path;
            conflict.type = ConflictType::DELETE_MODIFY;
            conflict.our_content = \"\"; // Deleted
            conflict.their_content = \"// Their modified version\";
            result.conflicts.push_back(conflict);
            
        } else if (!in_base && in_ours && !in_theirs) {
            // Only we added it
            merged_files[file_path] = \"// Our new file: \" + file_path;
            
        } else if (!in_base && !in_ours && in_theirs) {
            // Only they added it
            merged_files[file_path] = \"// Their new file: \" + file_path;
        }
    }
    
    // Apply changes to working tree
    if (result.conflicts.empty()) {
        result.success = apply_changes_to_working_tree(merged_files);
        result.files_changed = static_cast<int>(merged_files.size());
        
        if (result.success) {
            // Create merge commit
            std::string merge_message = format_merge_message(\"source\", \"target\");
            
            svcs_error_t err = svcs_commit_create(repository, merge_message.c_str(), 
                                                \"Merger <merger@example.com>\", &result.merge_commit_hash);
            result.success = (err == SVCS_OK);
        }
    }
    
    return result;
}

std::string MergeEngine::generate_conflict_markers(const MergeConflict& conflict) {
    std::ostringstream oss;
    
    oss << \"<<<<<<< HEAD\\n\";
    oss << conflict.our_content;
    if (!conflict.our_content.empty() && conflict.our_content.back() != '\\n') {
        oss << \"\\n\";
    }
    oss << \"=======\\n\";
    oss << conflict.their_content;
    if (!conflict.their_content.empty() && conflict.their_content.back() != '\\n') {
        oss << \"\\n\";
    }
    oss << \">>>>>>> branch\\n\";
    
    return oss.str();
}

bool MergeEngine::is_ancestor(const svcs_hash_t& ancestor, const svcs_hash_t& descendant) {
    auto ancestor_node = dag->get_commit(std::string(reinterpret_cast<const char*>(&ancestor), sizeof(ancestor)));
    auto descendant_node = dag->get_commit(std::string(reinterpret_cast<const char*>(&descendant), sizeof(descendant)));
    
    if (!ancestor_node || !descendant_node) {
        return false;
    }
    
    // BFS to check if ancestor is reachable from descendant
    std::queue<std::shared_ptr<CommitNode>> queue;
    std::set<std::string> visited;
    
    queue.push(descendant_node);
    visited.insert(descendant_node->hash_string());
    
    while (!queue.empty()) {
        auto current = queue.front();
        queue.pop();
        
        if (current->hash_string() == ancestor_node->hash_string()) {
            return true;
        }
        
        for (auto& parent : current->parents) {
            if (visited.find(parent->hash_string()) == visited.end()) {
                visited.insert(parent->hash_string());
                queue.push(parent);
            }
        }
    }
    
    return false;
}

std::vector<std::string> MergeEngine::split_into_lines(const std::string& content) {
    std::vector<std::string> lines;
    std::istringstream iss(content);
    std::string line;
    
    while (std::getline(iss, line)) {
        lines.push_back(line);
    }
    
    return lines;
}

std::string MergeEngine::join_lines(const std::vector<std::string>& lines) {
    std::ostringstream oss;
    for (size_t i = 0; i < lines.size(); ++i) {
        oss << lines[i];
        if (i < lines.size() - 1) {
            oss << \"\\n\";
        }
    }
    return oss.str();
}

std::map<std::string, svcs_hash_t> MergeEngine::get_file_tree(const svcs_hash_t& commit_hash) {
    std::map<std::string, svcs_hash_t> file_tree;
    
    // Simplified implementation - in a real system, you'd parse the commit's tree object
    // For now, return empty map
    
    return file_tree;
}

bool MergeEngine::apply_changes_to_working_tree(const std::map<std::string, std::string>& file_changes) {
    for (const auto& [file_path, content] : file_changes) {
        if (svcs_file_write(file_path.c_str(), content.c_str(), content.length()) != SVCS_OK) {
            return false;
        }
    }
    return true;
}

std::string MergeEngine::format_merge_message(const std::string& source_branch, const std::string& target_branch) {
    return \"Merge branch '\" + source_branch + \"' into \" + target_branch;
}

int MergeEngine::count_commits_between(const svcs_hash_t& base, const svcs_hash_t& head) {
    // Simplified implementation
    return 1;
}

// InteractiveMergeResolver implementation
InteractiveMergeResolver::InteractiveMergeResolver(MergeEngine* engine) : merge_engine(engine) {}

bool InteractiveMergeResolver::resolve_conflicts_interactively(std::vector<MergeConflict>& conflicts) {
    using namespace svcs::ui;
    
    TerminalUI ui;
    ui.print_header(\"Merge Conflicts Detected\");
    ui.print_info(\"Found \" + std::to_string(conflicts.size()) + \" conflicts to resolve\");
    
    for (auto& conflict : conflicts) {
        ui.print_separator();
        ui.print_styled(StyledText(\"Conflict in: \" + conflict.file_path, Color::BRIGHT_YELLOW, Style::BOLD));
        
        show_conflict(conflict);
        
        std::string resolution = prompt_resolution(conflict);
        if (resolution == \"abort\") {
            return false;
        }
        
        conflict.resolution = resolution;
        conflict.resolved = true;
    }
    
    return true;
}

void InteractiveMergeResolver::show_conflict(const MergeConflict& conflict) {
    using namespace svcs::ui;
    
    TerminalUI ui;
    
    ui.print_styled(StyledText(\"<<<<<<< HEAD (ours)\", Color::BRIGHT_GREEN));
    ui.print_line(conflict.our_content);
    ui.print_styled(StyledText(\"=======\", Color::BRIGHT_BLUE));
    ui.print_line(conflict.their_content);
    ui.print_styled(StyledText(\">>>>>>> branch (theirs)\", Color::BRIGHT_RED));
}

std::string InteractiveMergeResolver::prompt_resolution(const MergeConflict& conflict) {
    using namespace svcs::ui;
    
    Menu resolution_menu(\"Resolve Conflict\");
    resolution_menu.add_item({\"Use ours (HEAD)\", \"Keep our version\", nullptr});
    resolution_menu.add_item({\"Use theirs (branch)\", \"Keep their version\", nullptr});
    resolution_menu.add_item({\"Edit manually\", \"Open editor to resolve\", nullptr});
    resolution_menu.add_item({\"Skip this conflict\", \"Resolve later\", nullptr});
    resolution_menu.add_separator();
    resolution_menu.add_item({\"Abort merge\", \"Cancel the entire merge\", nullptr});
    
    int choice = resolution_menu.show();
    
    switch (choice) {
        case 0: return conflict.our_content;
        case 1: return conflict.their_content;
        case 2: {
            // In a real implementation, open editor
            TerminalUI ui;
            return ui.prompt(\"Enter resolution:\", conflict.our_content);
        }
        case 3: return \"\"; // Skip
        case 4: return \"abort\";
        default: return \"abort\";
    }
}

// MergeReporter implementation
void MergeReporter::print_merge_summary(const MergeResult& result) {
    using namespace svcs::ui;
    
    TerminalUI ui;
    
    if (result.success) {
        if (result.is_fast_forward) {
            ui.print_success(\"Fast-forward merge completed\");
        } else {
            ui.print_success(\"Merge completed successfully\");
            
            char hash_str[SVCS_HASH_HEX_SIZE];
            svcs_hash_to_string(&result.merge_commit_hash, hash_str);
            ui.print_info(\"Merge commit: \" + std::string(hash_str, 7));
        }
        
        print_merge_stats(result);
    } else {
        ui.print_error(\"Merge failed: \" + result.error_message);
        
        if (!result.conflicts.empty()) {
            print_conflict_summary(result.conflicts);
        }
    }
}

void MergeReporter::print_conflict_summary(const std::vector<MergeConflict>& conflicts) {
    using namespace svcs::ui;
    
    TerminalUI ui;
    ui.print_warning(\"Conflicts found in \" + std::to_string(conflicts.size()) + \" files:\");
    
    for (const auto& conflict : conflicts) {
        ui.print_line(\"  \" + conflict.file_path);
    }
    
    ui.print_info(\"Resolve conflicts and run 'svcs commit' to complete the merge\");
}

void MergeReporter::print_merge_stats(const MergeResult& result) {
    using namespace svcs::ui;
    
    TerminalUI ui;
    
    if (result.files_changed > 0) {
        std::string stats = std::to_string(result.files_changed) + \" files changed\";
        if (result.insertions > 0) {
            stats += \", \" + std::to_string(result.insertions) + \" insertions(+)\";
        }
        if (result.deletions > 0) {
            stats += \", \" + std::to_string(result.deletions) + \" deletions(-)\";
        }
        
        ui.print_info(stats);
    }
}

} // namespace core
} // namespace svcs