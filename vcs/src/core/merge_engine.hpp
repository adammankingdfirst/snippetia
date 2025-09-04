#ifndef MERGE_ENGINE_HPP
#define MERGE_ENGINE_HPP

#include <string>
#include <vector>
#include <memory>
#include <map>
#include <optional>
#include "svcs.h"
#include "dag.hpp"

namespace svcs {
namespace core {

// Merge conflict types
enum class ConflictType {
    CONTENT,        // File content conflicts
    ADD_ADD,        // Both branches added same file
    MODIFY_DELETE,  // One modified, one deleted
    DELETE_MODIFY,  // One deleted, one modified
    RENAME_RENAME,  // Both renamed to different names
    MODE_CHANGE     // File mode conflicts
};

// Conflict resolution strategies
enum class MergeStrategy {
    RECURSIVE,      // Default Git-style recursive merge
    OCTOPUS,        // Multi-branch merge
    OURS,           // Always take our version
    THEIRS,         // Always take their version
    SUBTREE         // Subtree merge strategy
};

// Merge conflict representation
struct MergeConflict {
    std::string file_path;
    ConflictType type;
    std::string our_content;
    std::string their_content;
    std::string base_content;
    int our_line_start = -1;
    int our_line_end = -1;
    int their_line_start = -1;
    int their_line_end = -1;
    std::string resolution;  // User-provided resolution
    bool resolved = false;
};

// Three-way merge result
struct ThreeWayMergeResult {
    std::string merged_content;
    std::vector<MergeConflict> conflicts;
    bool has_conflicts = false;
    bool success = true;
    std::string error_message;
};

// Merge operation result
struct MergeResult {
    bool success = false;
    bool is_fast_forward = false;
    svcs_hash_t merge_commit_hash;
    std::vector<MergeConflict> conflicts;
    std::vector<std::string> merged_files;
    std::string error_message;
    
    // Statistics
    int files_changed = 0;
    int insertions = 0;
    int deletions = 0;
};

// Advanced merge engine
class MergeEngine {
private:
    svcs_repository_t* repository;
    std::unique_ptr<CommitDAG> dag;
    MergeStrategy strategy = MergeStrategy::RECURSIVE;
    
public:
    explicit MergeEngine(svcs_repository_t* repo);
    ~MergeEngine() = default;
    
    // Configuration
    void set_strategy(MergeStrategy strat) { strategy = strat; }
    MergeStrategy get_strategy() const { return strategy; }
    
    // Main merge operations
    MergeResult merge_branches(const std::string& source_branch, const std::string& target_branch);
    MergeResult merge_commits(const svcs_hash_t& source_commit, const svcs_hash_t& target_commit);
    MergeResult fast_forward_merge(const std::string& source_branch, const std::string& target_branch);
    
    // Conflict resolution
    bool can_fast_forward(const std::string& source_branch, const std::string& target_branch);
    std::vector<MergeConflict> detect_conflicts(const svcs_hash_t& base_commit, 
                                               const svcs_hash_t& our_commit,
                                               const svcs_hash_t& their_commit);
    
    // Three-way merge algorithms
    ThreeWayMergeResult three_way_merge_files(const std::string& base_content,
                                             const std::string& our_content,
                                             const std::string& their_content);
    
    ThreeWayMergeResult three_way_merge_lines(const std::vector<std::string>& base_lines,
                                             const std::vector<std::string>& our_lines,
                                             const std::vector<std::string>& their_lines);
    
    // Conflict resolution helpers
    std::string generate_conflict_markers(const MergeConflict& conflict);
    bool resolve_conflict(MergeConflict& conflict, const std::string& resolution);
    void resolve_all_conflicts(std::vector<MergeConflict>& conflicts, const std::string& strategy);
    
    // Merge base detection
    std::shared_ptr<CommitNode> find_merge_base(const svcs_hash_t& commit1, const svcs_hash_t& commit2);
    std::vector<std::shared_ptr<CommitNode>> find_merge_bases(const svcs_hash_t& commit1, const svcs_hash_t& commit2);
    
    // Advanced merge operations
    MergeResult cherry_pick(const svcs_hash_t& commit_hash, const std::string& target_branch);
    MergeResult revert_commit(const svcs_hash_t& commit_hash);
    MergeResult squash_merge(const std::string& source_branch, const std::string& target_branch);
    
    // Merge analysis
    bool is_ancestor(const svcs_hash_t& ancestor, const svcs_hash_t& descendant);
    int count_commits_between(const svcs_hash_t& base, const svcs_hash_t& head);
    std::vector<std::shared_ptr<CommitNode>> get_commits_to_merge(const std::string& source_branch, 
                                                                 const std::string& target_branch);
    
    // Utilities
    std::string format_merge_message(const std::string& source_branch, const std::string& target_branch);
    void cleanup_merge_state();
    
private:
    // Internal merge helpers
    MergeResult perform_recursive_merge(const svcs_hash_t& base_commit,
                                       const svcs_hash_t& our_commit,
                                       const svcs_hash_t& their_commit);
    
    std::vector<std::string> split_into_lines(const std::string& content);
    std::string join_lines(const std::vector<std::string>& lines);
    
    // Longest Common Subsequence for merge algorithms
    std::vector<std::vector<int>> compute_lcs_table(const std::vector<std::string>& seq1,
                                                    const std::vector<std::string>& seq2);
    
    std::vector<std::pair<int, int>> find_common_subsequence(const std::vector<std::string>& seq1,
                                                            const std::vector<std::string>& seq2);
    
    // File tree operations
    std::map<std::string, svcs_hash_t> get_file_tree(const svcs_hash_t& commit_hash);
    bool apply_changes_to_working_tree(const std::map<std::string, std::string>& file_changes);
};

// Merge conflict resolver with interactive capabilities
class InteractiveMergeResolver {
private:
    MergeEngine* merge_engine;
    std::vector<MergeConflict> conflicts;
    
public:
    explicit InteractiveMergeResolver(MergeEngine* engine);
    
    // Interactive resolution
    bool resolve_conflicts_interactively(std::vector<MergeConflict>& conflicts);
    void show_conflict(const MergeConflict& conflict);
    std::string prompt_resolution(const MergeConflict& conflict);
    
    // Automated resolution strategies
    void resolve_with_ours(std::vector<MergeConflict>& conflicts);
    void resolve_with_theirs(std::vector<MergeConflict>& conflicts);
    void resolve_with_base(std::vector<MergeConflict>& conflicts);
    
    // Conflict analysis
    void analyze_conflicts(const std::vector<MergeConflict>& conflicts);
    std::string suggest_resolution(const MergeConflict& conflict);
};

// Merge statistics and reporting
class MergeReporter {
public:
    static void print_merge_summary(const MergeResult& result);
    static void print_conflict_summary(const std::vector<MergeConflict>& conflicts);
    static void print_merge_stats(const MergeResult& result);
    static std::string format_merge_report(const MergeResult& result);
};

} // namespace core
} // namespace svcs

#endif // MERGE_ENGINE_HPP