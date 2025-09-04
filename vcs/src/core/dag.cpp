#include "dag.hpp"
#include <algorithm>
#include <sstream>
#include <iomanip>
#include <chrono>

namespace svcs {
namespace core {

// CommitNode implementation
CommitNode::CommitNode(const svcs_hash_t& commit_hash, const std::string& commit_message, 
                      const std::string& commit_author, time_t commit_time)
    : hash(commit_hash), message(commit_message), author(commit_author), timestamp(commit_time) {
}

std::string CommitNode::hash_string() const {
    char hash_str[SVCS_HASH_HEX_SIZE];
    svcs_hash_to_string(&hash, hash_str);
    return std::string(hash_str);
}

std::string CommitNode::short_hash() const {
    return hash_string().substr(0, 7);
}

// CommitDAG implementation
CommitDAG::CommitDAG(svcs_repository_t* repo) : repository(repo) {
}

svcs_error_t CommitDAG::load_from_repository() {
    if (!repository) {
        return SVCS_ERROR_INVALID;
    }
    
    clear();
    
    // Load all commits from repository
    // This is a simplified implementation - in a real system, you'd walk the commit graph
    char refs_dir[SVCS_MAX_PATH];
    snprintf(refs_dir, sizeof(refs_dir), "%s/refs/heads", repository->git_dir);
    
    DIR* dir = opendir(refs_dir);
    if (!dir) {
        return SVCS_ERROR_NOT_FOUND;
    }
    
    struct dirent* entry;
    while ((entry = readdir(dir)) != NULL) {
        if (entry->d_name[0] == '.') continue;
        
        char branch_file[SVCS_MAX_PATH];
        snprintf(branch_file, sizeof(branch_file), "%s/%s", refs_dir, entry->d_name);
        
        void* branch_data;
        size_t branch_size;
        if (svcs_file_read(branch_file, &branch_data, &branch_size) == SVCS_OK) {
            char* hash_str = (char*)branch_data;
            char* newline = strchr(hash_str, '\n');
            if (newline) *newline = '\0';
            
            // Load commit chain from this branch head
            svcs_hash_t current_hash;
            if (svcs_hash_from_string(&current_hash, hash_str) == SVCS_OK) {
                load_commit_chain(current_hash, entry->d_name);
            }
            
            free(branch_data);
        }
    }
    
    closedir(dir);
    
    calculate_depths();
    return SVCS_OK;
}

svcs_error_t CommitDAG::add_commit(const svcs_hash_t& hash, const std::string& message,
                                  const std::string& author, time_t timestamp,
                                  const std::vector<svcs_hash_t>& parent_hashes) {
    std::string hash_str;
    char hash_cstr[SVCS_HASH_HEX_SIZE];
    svcs_hash_to_string(&hash, hash_cstr);
    hash_str = hash_cstr;
    
    // Check if commit already exists
    if (nodes.find(hash_str) != nodes.end()) {
        return SVCS_OK;  // Already exists
    }
    
    // Create new commit node
    auto commit_node = std::make_shared<CommitNode>(hash, message, author, timestamp);
    
    // Link to parents
    for (const auto& parent_hash : parent_hashes) {
        char parent_hash_str[SVCS_HASH_HEX_SIZE];
        svcs_hash_to_string(&parent_hash, parent_hash_str);
        std::string parent_str = parent_hash_str;
        
        auto parent_it = nodes.find(parent_str);
        if (parent_it != nodes.end()) {
            commit_node->parents.push_back(parent_it->second);
            parent_it->second->children.push_back(commit_node);
        }
    }
    
    // Add to nodes map
    nodes[hash_str] = commit_node;
    
    // Update roots and heads
    if (commit_node->is_root_commit()) {
        roots.push_back(commit_node);
    }
    
    // Remove parents from heads list if they exist there
    for (auto& parent : commit_node->parents) {
        auto head_it = std::find(heads.begin(), heads.end(), parent);
        if (head_it != heads.end()) {
            heads.erase(head_it);
        }
    }
    
    // Add this commit to heads if it has no children
    if (commit_node->is_leaf_commit()) {
        heads.push_back(commit_node);
    }
    
    return SVCS_OK;
}

std::shared_ptr<CommitNode> CommitDAG::get_commit(const std::string& hash_or_ref) const {
    // Try direct hash lookup first
    auto it = nodes.find(hash_or_ref);
    if (it != nodes.end()) {
        return it->second;
    }
    
    // Try to resolve as reference
    return resolve_reference(hash_or_ref);
}

std::vector<std::shared_ptr<CommitNode>> CommitDAG::get_commits_in_range(const CommitRange& range) const {
    std::vector<std::shared_ptr<CommitNode>> result;
    
    // Get all commits first
    std::vector<std::shared_ptr<CommitNode>> all_commits;
    for (const auto& [hash, node] : nodes) {
        all_commits.push_back(node);
    }
    
    // Apply filters
    if (!range.include_merges) {
        all_commits.erase(
            std::remove_if(all_commits.begin(), all_commits.end(),
                          [](const std::shared_ptr<CommitNode>& node) {
                              return node->is_merge_commit();
                          }),
            all_commits.end()
        );
    }
    
    // Sort according to specified order
    switch (range.order) {
        case TraversalOrder::CHRONOLOGICAL:
            std::sort(all_commits.begin(), all_commits.end(),
                     [](const std::shared_ptr<CommitNode>& a, const std::shared_ptr<CommitNode>& b) {
                         return a->timestamp > b->timestamp;  // Newest first
                     });
            break;
            
        case TraversalOrder::TOPOLOGICAL:
            all_commits = topological_sort();
            break;
            
        case TraversalOrder::DEPTH_FIRST:
            all_commits = dfs_traversal(range.start_commit);
            break;
            
        case TraversalOrder::BREADTH_FIRST:
            all_commits = bfs_traversal(range.start_commit);
            break;
    }
    
    // Apply count limit
    if (range.max_count > 0 && all_commits.size() > static_cast<size_t>(range.max_count)) {
        all_commits.resize(range.max_count);
    }
    
    return all_commits;
}

std::vector<std::shared_ptr<CommitNode>> CommitDAG::topological_sort() const {
    std::vector<std::shared_ptr<CommitNode>> result;
    std::unordered_map<std::string, int> in_degree;
    std::queue<std::shared_ptr<CommitNode>> queue;
    
    // Calculate in-degrees
    for (const auto& [hash, node] : nodes) {
        in_degree[hash] = static_cast<int>(node->parents.size());
        if (in_degree[hash] == 0) {
            queue.push(node);
        }
    }
    
    // Process nodes with zero in-degree
    while (!queue.empty()) {
        auto current = queue.front();
        queue.pop();
        result.push_back(current);
        
        // Reduce in-degree of children
        for (auto& weak_child : current->children) {
            if (auto child = weak_child.lock()) {
                std::string child_hash = child->hash_string();
                in_degree[child_hash]--;
                if (in_degree[child_hash] == 0) {
                    queue.push(child);
                }
            }
        }
    }
    
    return result;
}

std::vector<std::shared_ptr<CommitNode>> CommitDAG::chronological_sort() const {
    std::vector<std::shared_ptr<CommitNode>> result;
    for (const auto& [hash, node] : nodes) {
        result.push_back(node);
    }
    
    std::sort(result.begin(), result.end(),
             [](const std::shared_ptr<CommitNode>& a, const std::shared_ptr<CommitNode>& b) {
                 return a->timestamp > b->timestamp;
             });
    
    return result;
}

DAGStatistics CommitDAG::get_statistics() const {
    DAGStatistics stats;
    
    stats.total_commits = nodes.size();
    stats.merge_commits = 0;
    stats.root_commits = roots.size();
    stats.leaf_commits = heads.size();
    stats.max_depth = 0;
    
    if (!nodes.empty()) {
        stats.earliest_commit = std::numeric_limits<time_t>::max();
        stats.latest_commit = 0;
        
        for (const auto& [hash, node] : nodes) {
            if (node->is_merge_commit()) {
                stats.merge_commits++;
            }
            
            stats.max_depth = std::max(stats.max_depth, node->depth);
            stats.earliest_commit = std::min(stats.earliest_commit, node->timestamp);
            stats.latest_commit = std::max(stats.latest_commit, node->timestamp);
        }
    }
    
    return stats;
}

std::string CommitDAG::generate_ascii_graph(int max_commits) const {
    return GraphVisualizer::generate_ascii_tree(*this, {.max_commits = max_commits});
}

void CommitDAG::clear() {
    nodes.clear();
    roots.clear();
    heads.clear();
}

void CommitDAG::calculate_depths() {
    // Reset all depths
    for (const auto& [hash, node] : nodes) {
        node->depth = 0;
        node->visited = false;
    }
    
    // BFS from roots to calculate depths
    std::queue<std::shared_ptr<CommitNode>> queue;
    for (auto& root : roots) {
        root->depth = 0;
        root->visited = true;
        queue.push(root);
    }
    
    while (!queue.empty()) {
        auto current = queue.front();
        queue.pop();
        
        for (auto& weak_child : current->children) {
            if (auto child = weak_child.lock()) {
                if (!child->visited) {
                    child->depth = current->depth + 1;
                    child->visited = true;
                    queue.push(child);
                }
            }
        }
    }
}

std::shared_ptr<CommitNode> CommitDAG::resolve_reference(const std::string& ref) const {
    // Try to resolve branch reference
    if (repository) {
        char branch_file[SVCS_MAX_PATH];
        snprintf(branch_file, sizeof(branch_file), "%s/refs/heads/%s", repository->git_dir, ref.c_str());
        
        void* branch_data;
        size_t branch_size;
        if (svcs_file_read(branch_file, &branch_data, &branch_size) == SVCS_OK) {
            char* hash_str = (char*)branch_data;
            char* newline = strchr(hash_str, '\n');
            if (newline) *newline = '\0';
            
            auto it = nodes.find(hash_str);
            free(branch_data);
            
            if (it != nodes.end()) {
                return it->second;
            }
        }
    }
    
    // Try partial hash match
    for (const auto& [hash, node] : nodes) {
        if (hash.substr(0, ref.length()) == ref) {
            return node;
        }
    }
    
    return nullptr;
}

// Helper method to load commit chain (simplified implementation)
svcs_error_t CommitDAG::load_commit_chain(const svcs_hash_t& start_hash, const std::string& branch_name) {
    // This is a simplified implementation
    // In a real system, you'd recursively load commits and their parents
    
    std::string hash_str;
    char hash_cstr[SVCS_HASH_HEX_SIZE];
    svcs_hash_to_string(&start_hash, hash_cstr);
    hash_str = hash_cstr;
    
    // Check if already loaded
    if (nodes.find(hash_str) != nodes.end()) {
        return SVCS_OK;
    }
    
    // Create a mock commit for demonstration
    auto commit_node = std::make_shared<CommitNode>(
        start_hash, 
        "Sample commit message", 
        "Author <author@example.com>", 
        time(nullptr)
    );
    commit_node->branch_name = branch_name;
    
    nodes[hash_str] = commit_node;
    heads.push_back(commit_node);
    
    return SVCS_OK;
}

// GraphVisualizer implementation
std::string GraphVisualizer::generate_ascii_tree(const CommitDAG& dag, const VisualizationOptions& options) {
    std::ostringstream oss;
    
    auto commits = dag.chronological_sort();
    if (commits.size() > static_cast<size_t>(options.max_commits)) {
        commits.resize(options.max_commits);
    }
    
    for (size_t i = 0; i < commits.size(); ++i) {
        auto& commit = commits[i];
        
        // Generate graph characters
        std::string graph_part = "* ";
        if (commit->is_merge_commit() && options.show_merge_commits) {
            graph_part = "M ";
        }
        
        // Add commit info
        std::string commit_info = format_commit_info(commit, options);
        
        oss << graph_part << commit_info << std::endl;
        
        // Add connection lines for parents (simplified)
        if (i < commits.size() - 1) {
            oss << "| " << std::endl;
        }
    }
    
    return oss.str();
}

std::string GraphVisualizer::format_commit_info(std::shared_ptr<CommitNode> commit, const VisualizationOptions& options) {
    std::ostringstream oss;
    
    oss << commit->short_hash();
    
    if (options.show_commit_messages) {
        oss << " " << commit->message;
    }
    
    if (options.show_authors) {
        oss << " (" << commit->author << ")";
    }
    
    if (options.show_timestamps) {
        auto tm = *std::localtime(&commit->timestamp);
        oss << " [" << std::put_time(&tm, "%Y-%m-%d %H:%M") << "]";
    }
    
    return oss.str();
}

std::string GraphVisualizer::generate_compact_log(const CommitDAG& dag, const CommitRange& range) {
    std::ostringstream oss;
    
    auto commits = dag.get_commits_in_range(range);
    
    for (const auto& commit : commits) {
        oss << commit->short_hash() << " " << commit->message << std::endl;
    }
    
    return oss.str();
}

} // namespace core
} // namespace svcs