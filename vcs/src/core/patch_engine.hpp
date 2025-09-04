#pragma once

#include <string>
#include <vector>
#include <map>
#include <memory>
#include <functional>

namespace svcs {

struct PatchHunk {
    int old_start;
    int old_count;
    int new_start;
    int new_count;
    std::vector<std::string> lines;
    std::string context;
};

struct Patch {
    std::string old_file;
    std::string new_file;
    std::vector<PatchHunk> hunks;
    std::map<std::string, std::string> metadata;
    bool is_binary = false;
    bool is_new_file = false;
    bool is_deleted_file = false;
};

class PatchEngine {
public:
    // Generate patches
    static std::vector<Patch> generate_patches(
        const std::string& old_tree,
        const std::string& new_tree,
        const std::map<std::string, std::string>& options = {}
    );
    
    // Apply patches
    static bool apply_patches(
        const std::vector<Patch>& patches,
        const std::string& target_dir,
        bool dry_run = false
    );
    
    // Patch validation
    static bool validate_patch(const Patch& patch, const std::string& target_file);
    
    // Smart patching with conflict resolution
    static std::vector<std::string> apply_with_conflicts(
        const Patch& patch,
        const std::string& target_content
    );
    
    // Patch statistics
    struct PatchStats {
        int files_changed = 0;
        int insertions = 0;
        int deletions = 0;
        int binary_files = 0;
    };
    
    static PatchStats calculate_stats(const std::vector<Patch>& patches);
    
    // Format patches for display
    static std::string format_patch(const Patch& patch, bool color = true);
    static std::string format_unified_diff(const Patch& patch);
    
    // Parse patches from text
    static std::vector<Patch> parse_patches(const std::string& patch_text);
    
private:
    static std::vector<std::string> generate_diff_lines(
        const std::vector<std::string>& old_lines,
        const std::vector<std::string>& new_lines,
        int context_size = 3
    );
    
    static bool fuzzy_match_hunk(
        const PatchHunk& hunk,
        const std::vector<std::string>& target_lines,
        int& match_offset
    );
};

// Advanced merge strategies
class AdvancedMergeEngine {
public:
    enum class MergeStrategy {
        RECURSIVE,
        OCTOPUS,
        OURS,
        THEIRS,
        SUBTREE,
        RESOLVE
    };
    
    struct MergeOptions {
        MergeStrategy strategy = MergeStrategy::RECURSIVE;
        bool ignore_whitespace = false;
        bool ignore_case = false;
        int rename_threshold = 50;
        bool find_renames = true;
        std::string merge_base_hint;
    };
    
    static bool merge_commits(
        const std::string& base_commit,
        const std::string& our_commit,
        const std::string& their_commit,
        const MergeOptions& options = {}
    );
    
    static std::vector<std::string> find_merge_bases(
        const std::string& commit1,
        const std::string& commit2
    );
    
    static bool is_fast_forward(
        const std::string& from_commit,
        const std::string& to_commit
    );
};

}