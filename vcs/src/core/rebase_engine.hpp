#pragma once

#include <string>
#include <vector>
#include <map>
#include <functional>

namespace svcs {

struct RebaseStep {
    enum Type {
        PICK,
        REWORD,
        EDIT,
        SQUASH,
        FIXUP,
        DROP,
        EXEC
    } type;
    
    std::string commit_hash;
    std::string message;
    std::string command; // For EXEC type
    bool completed = false;
};

struct RebaseState {
    std::string onto_commit;
    std::string original_head;
    std::vector<RebaseStep> steps;
    int current_step = 0;
    bool in_progress = false;
    std::string rebase_dir;
    std::map<std::string, std::string> metadata;
};

class RebaseEngine {
public:
    // Interactive rebase
    static bool start_interactive_rebase(
        const std::string& upstream,
        const std::string& branch = "HEAD",
        const std::vector<RebaseStep>& custom_steps = {}
    );
    
    // Continue rebase after resolving conflicts
    static bool continue_rebase();
    
    // Abort rebase and return to original state
    static bool abort_rebase();
    
    // Skip current commit during rebase
    static bool skip_rebase_step();
    
    // Edit rebase todo list
    static bool edit_rebase_todo(const std::vector<RebaseStep>& new_steps);
    
    // Get current rebase state
    static RebaseState get_rebase_state();
    
    // Check if rebase is in progress
    static bool is_rebase_in_progress();
    
    // Automatic rebase (non-interactive)
    static bool rebase_onto(
        const std::string& upstream,
        const std::string& branch = "HEAD",
        bool preserve_merges = false
    );
    
    // Cherry-pick commits
    static bool cherry_pick(
        const std::vector<std::string>& commits,
        bool no_commit = false
    );
    
    // Squash commits
    static bool squash_commits(
        const std::vector<std::string>& commits,
        const std::string& message
    );
    
    // Rewrite commit history
    static bool rewrite_history(
        const std::string& start_commit,
        const std::function<std::string(const std::string&)>& rewriter
    );
    
private:
    static bool save_rebase_state(const RebaseState& state);
    static RebaseState load_rebase_state();
    static bool execute_rebase_step(const RebaseStep& step);
    static std::vector<std::string> get_commits_to_rebase(
        const std::string& upstream,
        const std::string& branch
    );
};

// Advanced commit manipulation
class CommitManipulator {
public:
    // Split a commit into multiple commits
    static std::vector<std::string> split_commit(
        const std::string& commit_hash,
        const std::vector<std::vector<std::string>>& file_groups
    );
    
    // Combine multiple commits into one
    static std::string combine_commits(
        const std::vector<std::string>& commits,
        const std::string& message
    );
    
    // Reorder commits
    static bool reorder_commits(
        const std::vector<std::string>& commit_order,
        const std::string& base_commit
    );
    
    // Edit commit message
    static bool edit_commit_message(
        const std::string& commit_hash,
        const std::string& new_message
    );
    
    // Amend last commit
    static bool amend_commit(
        const std::vector<std::string>& additional_files = {},
        const std::string& new_message = ""
    );
    
    // Reset commit (soft, mixed, hard)
    enum class ResetType { SOFT, MIXED, HARD };
    static bool reset_to_commit(
        const std::string& commit_hash,
        ResetType type = ResetType::MIXED
    );
};

}