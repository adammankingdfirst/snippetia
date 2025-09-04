#include "patch_engine.hpp"
#include "diff.h"
#include "utils.h"
#include <algorithm>
#include <sstream>
#include <regex>
#include <iostream>

namespace svcs {

std::vector<Patch> PatchEngine::generate_patches(
    const std::string& old_tree,
    const std::string& new_tree,
    const std::map<std::string, std::string>& options
) {
    std::vector<Patch> patches;
    
    // Get file lists from both trees
    auto old_files = get_tree_files(old_tree.c_str());
    auto new_files = get_tree_files(new_tree.c_str());
    
    std::set<std::string> all_files;
    for (const auto& file : old_files) all_files.insert(file);
    for (const auto& file : new_files) all_files.insert(file);
    
    for (const std::string& file : all_files) {
        Patch patch;
        patch.old_file = file;
        patch.new_file = file;
        
        bool in_old = std::find(old_files.begin(), old_files.end(), file) != old_files.end();
        bool in_new = std::find(new_files.begin(), new_files.end(), file) != new_files.end();
        
        if (!in_old && in_new) {
            // New file
            patch.is_new_file = true;
            auto content = read_file_from_tree(new_tree, file);
            auto lines = split_lines(content);
            
            PatchHunk hunk;
            hunk.old_start = 0;
            hunk.old_count = 0;
            hunk.new_start = 1;
            hunk.new_count = lines.size();
            
            for (const auto& line : lines) {
                hunk.lines.push_back("+" + line);
            }
            patch.hunks.push_back(hunk);
            
        } else if (in_old && !in_new) {
            // Deleted file
            patch.is_deleted_file = true;
            auto content = read_file_from_tree(old_tree, file);
            auto lines = split_lines(content);
            
            PatchHunk hunk;
            hunk.old_start = 1;
            hunk.old_count = lines.size();
            hunk.new_start = 0;
            hunk.new_count = 0;
            
            for (const auto& line : lines) {
                hunk.lines.push_back("-" + line);
            }
            patch.hunks.push_back(hunk);
            
        } else if (in_old && in_new) {
            // Modified file
            auto old_content = read_file_from_tree(old_tree, file);
            auto new_content = read_file_from_tree(new_tree, file);
            
            if (old_content != new_content) {
                auto old_lines = split_lines(old_content);
                auto new_lines = split_lines(new_content);
                
                auto diff_lines = generate_diff_lines(old_lines, new_lines);
                if (!diff_lines.empty()) {
                    // Parse diff lines into hunks
                    PatchHunk current_hunk;
                    bool in_hunk = false;
                    
                    for (const auto& line : diff_lines) {
                        if (line.starts_with("@@")) {
                            if (in_hunk) {
                                patch.hunks.push_back(current_hunk);
                            }
                            // Parse hunk header
                            std::regex hunk_regex(R"(@@ -(\d+),(\d+) \+(\d+),(\d+) @@)");
                            std::smatch match;
                            if (std::regex_search(line, match, hunk_regex)) {
                                current_hunk = PatchHunk{};
                                current_hunk.old_start = std::stoi(match[1]);
                                current_hunk.old_count = std::stoi(match[2]);
                                current_hunk.new_start = std::stoi(match[3]);
                                current_hunk.new_count = std::stoi(match[4]);
                                in_hunk = true;
                            }
                        } else if (in_hunk) {
                            current_hunk.lines.push_back(line);
                        }
                    }
                    
                    if (in_hunk) {
                        patch.hunks.push_back(current_hunk);
                    }
                }
            }
        }
        
        if (!patch.hunks.empty() || patch.is_new_file || patch.is_deleted_file) {
            patches.push_back(patch);
        }
    }
    
    return patches;
}

bool PatchEngine::apply_patches(
    const std::vector<Patch>& patches,
    const std::string& target_dir,
    bool dry_run
) {
    for (const auto& patch : patches) {
        std::string target_file = target_dir + "/" + patch.new_file;
        
        if (patch.is_new_file) {
            if (!dry_run) {
                std::string content;
                for (const auto& hunk : patch.hunks) {
                    for (const auto& line : hunk.lines) {
                        if (line.starts_with("+")) {
                            content += line.substr(1) + "\n";
                        }
                    }
                }
                write_file(target_file, content);
            }
        } else if (patch.is_deleted_file) {
            if (!dry_run) {
                remove_file(target_file);
            }
        } else {
            // Apply hunks to existing file
            auto current_content = read_file(target_file);
            auto lines = split_lines(current_content);
            
            for (const auto& hunk : patch.hunks) {
                if (!validate_patch(patch, target_file)) {
                    return false;
                }
                
                // Apply hunk
                int line_offset = hunk.old_start - 1;
                std::vector<std::string> new_lines;
                
                // Copy lines before hunk
                for (int i = 0; i < line_offset; i++) {
                    if (i < lines.size()) {
                        new_lines.push_back(lines[i]);
                    }
                }
                
                // Apply hunk changes
                for (const auto& line : hunk.lines) {
                    if (line.starts_with("+")) {
                        new_lines.push_back(line.substr(1));
                    }
                    // Skip deleted lines (those starting with -)
                }
                
                // Copy remaining lines
                int skip_count = hunk.old_count;
                for (int i = line_offset + skip_count; i < lines.size(); i++) {
                    new_lines.push_back(lines[i]);
                }
                
                lines = new_lines;
            }
            
            if (!dry_run) {
                std::string new_content;
                for (const auto& line : lines) {
                    new_content += line + "\n";
                }
                write_file(target_file, new_content);
            }
        }
    }
    
    return true;
}

PatchEngine::PatchStats PatchEngine::calculate_stats(const std::vector<Patch>& patches) {
    PatchStats stats;
    
    for (const auto& patch : patches) {
        if (patch.is_binary) {
            stats.binary_files++;
        } else {
            stats.files_changed++;
            
            for (const auto& hunk : patch.hunks) {
                for (const auto& line : hunk.lines) {
                    if (line.starts_with("+")) {
                        stats.insertions++;
                    } else if (line.starts_with("-")) {
                        stats.deletions++;
                    }
                }
            }
        }
    }
    
    return stats;
}

std::string PatchEngine::format_patch(const Patch& patch, bool color) {
    std::ostringstream oss;
    
    // Header
    if (patch.is_new_file) {
        oss << "new file mode 100644\n";
    } else if (patch.is_deleted_file) {
        oss << "deleted file mode 100644\n";
    }
    
    oss << "--- " << (patch.is_new_file ? "/dev/null" : patch.old_file) << "\n";
    oss << "+++ " << (patch.is_deleted_file ? "/dev/null" : patch.new_file) << "\n";
    
    // Hunks
    for (const auto& hunk : patch.hunks) {
        oss << "@@ -" << hunk.old_start << "," << hunk.old_count
            << " +" << hunk.new_start << "," << hunk.new_count << " @@\n";
        
        for (const auto& line : hunk.lines) {
            if (color) {
                if (line.starts_with("+")) {
                    oss << "\033[32m" << line << "\033[0m\n";
                } else if (line.starts_with("-")) {
                    oss << "\033[31m" << line << "\033[0m\n";
                } else {
                    oss << line << "\n";
                }
            } else {
                oss << line << "\n";
            }
        }
    }
    
    return oss.str();
}

std::vector<std::string> PatchEngine::generate_diff_lines(
    const std::vector<std::string>& old_lines,
    const std::vector<std::string>& new_lines,
    int context_size
) {
    // Simple LCS-based diff implementation
    std::vector<std::string> result;
    
    // This is a simplified implementation
    // In a real system, you'd use a more sophisticated diff algorithm
    
    int old_idx = 0, new_idx = 0;
    int hunk_start_old = 1, hunk_start_new = 1;
    
    while (old_idx < old_lines.size() || new_idx < new_lines.size()) {
        if (old_idx < old_lines.size() && new_idx < new_lines.size() &&
            old_lines[old_idx] == new_lines[new_idx]) {
            // Lines match
            old_idx++;
            new_idx++;
        } else {
            // Start a new hunk
            std::vector<std::string> hunk_lines;
            int hunk_old_count = 0, hunk_new_count = 0;
            
            // Add context before changes
            for (int i = std::max(0, old_idx - context_size); i < old_idx; i++) {
                hunk_lines.push_back(" " + old_lines[i]);
                hunk_old_count++;
                hunk_new_count++;
            }
            
            // Add deletions
            while (old_idx < old_lines.size() && 
                   (new_idx >= new_lines.size() || old_lines[old_idx] != new_lines[new_idx])) {
                hunk_lines.push_back("-" + old_lines[old_idx]);
                hunk_old_count++;
                old_idx++;
            }
            
            // Add insertions
            while (new_idx < new_lines.size() && 
                   (old_idx >= old_lines.size() || old_lines[old_idx] != new_lines[new_idx])) {
                hunk_lines.push_back("+" + new_lines[new_idx]);
                hunk_new_count++;
                new_idx++;
            }
            
            // Add context after changes
            int context_end = std::min((int)old_lines.size(), old_idx + context_size);
            for (int i = old_idx; i < context_end; i++) {
                hunk_lines.push_back(" " + old_lines[i]);
                hunk_old_count++;
                hunk_new_count++;
            }
            
            // Add hunk header
            result.push_back("@@ -" + std::to_string(hunk_start_old) + "," + 
                           std::to_string(hunk_old_count) + " +" + 
                           std::to_string(hunk_start_new) + "," + 
                           std::to_string(hunk_new_count) + " @@");
            
            // Add hunk lines
            result.insert(result.end(), hunk_lines.begin(), hunk_lines.end());
            
            hunk_start_old = old_idx + 1;
            hunk_start_new = new_idx + 1;
        }
    }
    
    return result;
}

bool PatchEngine::validate_patch(const Patch& patch, const std::string& target_file) {
    if (!file_exists(target_file)) {
        return patch.is_new_file;
    }
    
    auto content = read_file(target_file);
    auto lines = split_lines(content);
    
    for (const auto& hunk : patch.hunks) {
        int line_idx = hunk.old_start - 1;
        
        for (const auto& patch_line : hunk.lines) {
            if (patch_line.starts_with(" ") || patch_line.starts_with("-")) {
                if (line_idx >= lines.size() || 
                    lines[line_idx] != patch_line.substr(1)) {
                    return false;
                }
                line_idx++;
            }
        }
    }
    
    return true;
}

}