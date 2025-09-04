#ifndef DAG_HPP
#define DAG_HPP

#include <string>
#include <vector>
#include <unordered_map>
#include <unordered_set>
#include <memory>
#include <functional>
#include <queue>
#include <stack>
#include "svcs.h"

namespace svcs {
namespace core {

// Forward declarations
class CommitNode;
class CommitDAG;

// Commit node in the DAG
class CommitNode {
public:
    svcs_hash_t hash;
    std::string message;
    std::string author;
    time_t timestamp;
    std::vector<std::shared_ptr<CommitNode>> parents;
    std::vector<std::weak_ptr<CommitNode>> children;
    
    // Metadata
    int depth = 0;  // Distance from root
    bool visited = false;  // For traversal algorithms
    std::string branch_name;
    
    CommitNode(const svcs_hash_t& commit_hash, const std::string& commit_message, 
               const std::string& commit_author, time_t commit_time);
    
    // Utility methods
    bool is_merge_commit() const { return parents.size() > 1; }
    bool is_root_commit() const { return parents.empty(); }
    bool is_leaf_commit() const { return children.empty(); }
    std::string hash_string() const;
    std::string short_hash() const;
};

// Traversal order options
enum class TraversalOrder {
    CHRONOLOGICAL,      // By timestamp
    TOPOLOGICAL,        // Topological sort (parents before children)
    DEPTH_FIRST,        // Depth-first traversal
    BREADTH_FIRST       // Breadth-first traversal
};

// Commit range specification
struct CommitRange {
    std::string start_commit;  // Empty means from beginning
    std::string end_commit;    // Empty means to HEAD
    bool include_merges = true;
    int max_count = -1;        // -1 means no limit
    TraversalOrder order = TraversalOrder::CHRONOLOGICAL;
};

// DAG statistics
struct DAGStatistics {
    size_t total_commits = 0;
    size_t merge_commits = 0;
    size_t root_commits = 0;
    size_t leaf_commits = 0;
    int max_depth = 0;
    std::vector<std::string> branches;
    time_t earliest_commit = 0;
    time_t latest_commit = 0;
};

// Commit DAG implementation
class CommitDAG {
private:
    std::unordered_map<std::string, std::shared_ptr<CommitNode>> nodes;
    std::vector<std::shared_ptr<CommitNode>> roots;  // Commits with no parents
    std::vector<std::shared_ptr<CommitNode>> heads;  // Commits with no children
    svcs_repository_t* repository;
    
public:
    explicit CommitDAG(svcs_repository_t* repo);
    ~CommitDAG() = default;
    
    // Building the DAG
    svcs_error_t load_from_repository();
    svcs_error_t add_commit(const svcs_hash_t& hash, const std::string& message,
                           const std::string& author, time_t timestamp,
                           const std::vector<svcs_hash_t>& parent_hashes);
    svcs_error_t rebuild();
    
    // Querying
    std::shared_ptr<CommitNode> get_commit(const std::string& hash_or_ref) const;
    std::vector<std::shared_ptr<CommitNode>> get_commits_in_range(const CommitRange& range) const;
    std::vector<std::shared_ptr<CommitNode>> get_path_between(const std::string& from, const std::string& to) const;
    std::vector<std::shared_ptr<CommitNode>> get_ancestors(const std::string& commit_hash, int max_depth = -1) const;
    std::vector<std::shared_ptr<CommitNode>> get_descendants(const std::string& commit_hash, int max_depth = -1) const;
    
    // Branch operations
    std::vector<std::shared_ptr<CommitNode>> get_branch_commits(const std::string& branch_name) const;
    std::shared_ptr<CommitNode> get_merge_base(const std::string& commit1, const std::string& commit2) const;
    std::vector<std::shared_ptr<CommitNode>> get_commits_between_branches(const std::string& base_branch, 
                                                                         const std::string& feature_branch) const;
    
    // Traversal
    void traverse(std::function<bool(std::shared_ptr<CommitNode>)> visitor, 
                 TraversalOrder order = TraversalOrder::CHRONOLOGICAL,
                 const std::string& start_commit = "") const;
    
    std::vector<std::shared_ptr<CommitNode>> topological_sort() const;
    std::vector<std::shared_ptr<CommitNode>> chronological_sort() const;
    
    // Analysis
    DAGStatistics get_statistics() const;
    bool has_cycles() const;
    std::vector<std::string> find_unreachable_commits() const;
    
    // Visualization helpers
    std::string generate_ascii_graph(int max_commits = 50) const;
    std::string generate_dot_graph() const;  // GraphViz DOT format
    
    // Utility
    size_t size() const { return nodes.size(); }
    bool empty() const { return nodes.empty(); }
    void clear();
    
private:
    // Helper methods
    void reset_visited_flags() const;
    void calculate_depths();
    std::shared_ptr<CommitNode> resolve_reference(const std::string& ref) const;
    std::vector<std::shared_ptr<CommitNode>> dfs_traversal(const std::string& start_commit = "") const;
    std::vector<std::shared_ptr<CommitNode>> bfs_traversal(const std::string& start_commit = "") const;
    bool has_cycles_util(std::shared_ptr<CommitNode> node, 
                        std::unordered_set<std::string>& visited,
                        std::unordered_set<std::string>& rec_stack) const;
};

// Graph visualization utilities
class GraphVisualizer {
public:
    struct VisualizationOptions {
        int max_width = 80;
        int max_commits = 50;
        bool show_merge_commits = true;
        bool show_commit_messages = true;
        bool show_timestamps = false;
        bool show_authors = false;
        bool color_branches = true;
    };
    
    static std::string generate_ascii_tree(const CommitDAG& dag, const VisualizationOptions& options = {});
    static std::string generate_compact_log(const CommitDAG& dag, const CommitRange& range = {});
    static std::string generate_branch_graph(const CommitDAG& dag, const std::vector<std::string>& branches);
    
private:
    struct GraphLine {
        std::string content;
        std::vector<int> branch_positions;
        bool is_merge = false;
        bool is_branch = false;
    };
    
    static std::vector<GraphLine> build_graph_lines(const CommitDAG& dag, const VisualizationOptions& options);
    static std::string format_commit_info(std::shared_ptr<CommitNode> commit, const VisualizationOptions& options);
};

// Merge analysis utilities
class MergeAnalyzer {
public:
    struct MergeInfo {
        std::shared_ptr<CommitNode> merge_commit;
        std::vector<std::shared_ptr<CommitNode>> merged_commits;
        std::shared_ptr<CommitNode> merge_base;
        std::string source_branch;
        std::string target_branch;
        bool is_fast_forward = false;
        int commits_ahead = 0;
        int commits_behind = 0;
    };
    
    static MergeInfo analyze_merge(const CommitDAG& dag, const std::string& merge_commit_hash);
    static std::vector<MergeInfo> find_all_merges(const CommitDAG& dag);
    static bool can_fast_forward(const CommitDAG& dag, const std::string& from_branch, const std::string& to_branch);
    static std::vector<std::shared_ptr<CommitNode>> get_merge_conflicts(const CommitDAG& dag, 
                                                                       const std::string& branch1, 
                                                                       const std::string& branch2);
};

} // namespace core
} // namespace svcs

#endif // DAG_HPP