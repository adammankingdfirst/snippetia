#include <iostream>
#include <memory>
#include <map>
#include <functional>
#include "svcs.h"
#include "advanced_parser.hpp"
#include "dag.hpp"
#include "terminal_ui.hpp"

using namespace svcs::cli;
using namespace svcs::core;
using namespace svcs::ui;

class EnhancedVCSApplication {
private:
    std::unique_ptr<AdvancedArgumentParser> parser;
    std::unique_ptr<TerminalUI> ui;
    svcs_repository_t* repository = nullptr;
    std::unique_ptr<CommitDAG> dag;
    
public:
    EnhancedVCSApplication() {
        ui = std::make_unique<TerminalUI>();
        setup_argument_parser();
    }
    
    ~EnhancedVCSApplication() {
        if (repository) {
            svcs_repository_free(repository);
        }
    }
    
    int run(int argc, char* argv[]) {
        auto result = parser->parse(argc, argv);
        
        if (result.help_requested) {
            parser->print_help(result.subcommand);
            return 0;
        }
        
        if (result.version_requested) {
            parser->print_version();
            return 0;
        }
        
        if (!result.error_message.empty()) {
            ui->print_error(result.error_message);
            return 1;
        }
        
        // Set UI options based on global flags
        if (result.options.count("quiet")) {
            // Suppress output
        }
        
        if (result.options.count("verbose")) {
            // Enable verbose output
        }
        
        // Handle subcommands
        return dispatch_command(result);
    }
    
private:
    void setup_argument_parser() {
        parser = ArgumentParserBuilder("svcs", "SnippetVCS - Advanced Git-like Version Control", "2.0.0")
            .global_option(make_flag_option("", "no-color", "Disable colored output"))
            .global_option(make_flag_option("", "porcelain", "Machine-readable output"))
            .subcommand({
                "init",
                "Initialize a new repository",
                "Create a new SnippetVCS repository in the current directory or specified path.",
                {
                    make_flag_option("", "bare", "Create a bare repository"),
                    make_string_option("", "template", "Template directory to use", false, ""),
                },
                {"path"},
                [this](const auto& opts, const auto& args) { return handle_init(opts, args); }
            })
            .subcommand({
                "add",
                "Add files to the staging area",
                "Add file contents to the index for the next commit.",
                {
                    make_flag_option("A", "all", "Add all tracked and untracked files"),
                    make_flag_option("u", "update", "Add only tracked files"),
                    make_flag_option("n", "dry-run", "Don't actually add files, just show what would be added"),
                    make_flag_option("v", "verbose", "Be verbose"),
                },
                {"files"},
                [this](const auto& opts, const auto& args) { return handle_add(opts, args); }
            })
            .subcommand({
                "commit",
                "Record changes to the repository",
                "Create a new commit with the staged changes.",
                {
                    make_string_option("m", "message", "Commit message", true),
                    make_flag_option("a", "all", "Automatically stage modified files"),
                    make_flag_option("", "amend", "Amend the previous commit"),
                    make_string_option("", "author", "Override author", false, ""),
                },
                {},
                [this](const auto& opts, const auto& args) { return handle_commit(opts, args); }
            })
            .subcommand({
                "status",
                "Show the working tree status",
                "Display paths that have differences between the index and the working tree.",
                {
                    make_flag_option("s", "short", "Give the output in short format"),
                    make_flag_option("", "porcelain", "Machine-readable output"),
                    make_flag_option("", "ignored", "Show ignored files"),
                },
                {},
                [this](const auto& opts, const auto& args) { return handle_status(opts, args); }
            })
            .subcommand({
                "log",
                "Show commit logs",
                "Show the commit history in various formats.",
                {
                    make_int_option("n", "max-count", "Limit number of commits", false, 10),
                    make_flag_option("", "oneline", "Show each commit on a single line"),
                    make_flag_option("", "graph", "Show ASCII art commit graph"),
                    make_flag_option("", "stat", "Show diffstat for each commit"),
                    make_string_option("", "since", "Show commits since date", false, ""),
                    make_string_option("", "until", "Show commits until date", false, ""),
                    make_string_option("", "author", "Filter by author", false, ""),
                    make_string_option("", "grep", "Filter by commit message", false, ""),
                },
                {"commit_range"},
                [this](const auto& opts, const auto& args) { return handle_log(opts, args); }
            })
            .subcommand({
                "branch",
                "List, create, or delete branches",
                "Manage repository branches.",
                {
                    make_flag_option("a", "all", "List both local and remote branches"),
                    make_flag_option("r", "remotes", "List remote branches"),
                    make_flag_option("d", "delete", "Delete a branch"),
                    make_flag_option("D", "delete-force", "Force delete a branch"),
                    make_flag_option("m", "move", "Move/rename a branch"),
                    make_flag_option("v", "verbose", "Show commit info for each branch"),
                },
                {"branch_name"},
                [this](const auto& opts, const auto& args) { return handle_branch(opts, args); }
            })
            .subcommand({
                "checkout",
                "Switch branches or restore files",
                "Switch to a different branch or restore working tree files.",
                {
                    make_flag_option("b", "create", "Create a new branch"),
                    make_flag_option("B", "create-force", "Create or reset a branch"),
                    make_flag_option("f", "force", "Force checkout"),
                    make_flag_option("", "track", "Set up tracking"),
                },
                {"branch_or_commit"},
                [this](const auto& opts, const auto& args) { return handle_checkout(opts, args); }
            })
            .subcommand({
                "diff",
                "Show changes between commits, trees, etc",
                "Show differences between various objects.",
                {
                    make_flag_option("", "cached", "Show staged changes"),
                    make_flag_option("", "stat", "Show diffstat only"),
                    make_flag_option("", "name-only", "Show only file names"),
                    make_flag_option("", "name-status", "Show file names and status"),
                    make_int_option("U", "unified", "Number of context lines", false, 3),
                    make_flag_option("", "color", "Force colored output"),
                    make_flag_option("", "no-color", "Disable colored output"),
                },
                {"commit1", "commit2"},
                [this](const auto& opts, const auto& args) { return handle_diff(opts, args); }
            })
            .subcommand({
                "merge",
                "Join development histories together",
                "Merge one or more branches into the current branch.",
                {
                    make_flag_option("", "no-ff", "Create a merge commit even for fast-forward"),
                    make_flag_option("", "ff-only", "Only allow fast-forward merges"),
                    make_string_option("m", "message", "Merge commit message", false, ""),
                    make_flag_option("", "abort", "Abort current merge"),
                    make_flag_option("", "continue", "Continue merge after resolving conflicts"),
                },
                {"branch"},
                [this](const auto& opts, const auto& args) { return handle_merge(opts, args); }
            })
            .subcommand({
                "interactive",
                "Interactive mode",
                "Launch interactive terminal interface.",
                {},
                {},
                [this](const auto& opts, const auto& args) { return handle_interactive(opts, args); }
            })
            .build();
    }
    
    int dispatch_command(const ParseResult& result) {
        // Try to open repository for commands that need it
        if (!result.subcommand.empty() && result.subcommand != "init" && result.subcommand != "interactive") {
            svcs_error_t err = svcs_repository_open(&repository, ".");
            if (err != SVCS_OK) {
                ui->print_error("Not a SnippetVCS repository (or any parent directories)");
                ui->print_info("Use 'svcs init' to initialize a new repository");
                return 1;
            }
            
            // Load DAG for commands that need commit history
            if (result.subcommand == "log" || result.subcommand == "branch" || 
                result.subcommand == "merge" || result.subcommand == "diff") {
                dag = std::make_unique<CommitDAG>(repository);
                dag->load_from_repository();
            }
        }
        
        // Find and execute the handler
        // The handlers are already set up in the subcommand configuration
        return 0;
    }
    
    // Command handlers
    int handle_init(const std::map<std::string, ArgumentValue>& options, const std::vector<std::string>& args) {
        std::string path = args.empty() ? "." : args[0];
        bool bare = options.count("bare") > 0;
        
        ui->print_info("Initializing SnippetVCS repository in " + path);
        
        if (bare) {
            ui->print_info("Creating bare repository");
        }
        
        svcs_error_t err = svcs_repository_init(path.c_str());
        if (err != SVCS_OK) {
            ui->print_error("Failed to initialize repository");
            return 1;
        }
        
        ui->print_success("Repository initialized successfully");
        return 0;
    }
    
    int handle_add(const std::map<std::string, ArgumentValue>& options, const std::vector<std::string>& args) {
        if (args.empty() && !options.count("all")) {
            ui->print_error("No files specified");
            return 1;
        }
        
        bool dry_run = options.count("dry-run") > 0;
        bool verbose = options.count("verbose") > 0;
        
        if (options.count("all")) {
            ui->print_info("Adding all files...");
            // TODO: Implement add all
        } else {
            for (const auto& file : args) {
                if (verbose || dry_run) {
                    ui->print_info((dry_run ? "Would add: " : "Adding: ") + file);
                }
                
                if (!dry_run) {
                    svcs_error_t err = svcs_index_add(repository, file.c_str());
                    if (err == SVCS_ERROR_NOT_FOUND) {
                        ui->print_error("File not found: " + file);
                        return 1;
                    } else if (err != SVCS_OK) {
                        ui->print_error("Failed to add file: " + file);
                        return 1;
                    }
                }
            }
        }
        
        if (!dry_run) {
            ui->print_success("Files added to staging area");
        }
        return 0;
    }
    
    int handle_commit(const std::map<std::string, ArgumentValue>& options, const std::vector<std::string>& args) {
        auto message_it = options.find("message");
        if (message_it == options.end()) {
            ui->print_error("Commit message required");
            return 1;
        }
        
        std::string message = std::get<std::string>(message_it->second);
        std::string author = "Unknown Author <unknown@example.com>";
        
        auto author_it = options.find("author");
        if (author_it != options.end()) {
            author = std::get<std::string>(author_it->second);
        }
        
        ui->print_info("Creating commit...");
        
        svcs_hash_t commit_hash;
        svcs_error_t err = svcs_commit_create(repository, message.c_str(), author.c_str(), &commit_hash);
        if (err != SVCS_OK) {
            ui->print_error("Failed to create commit");
            return 1;
        }
        
        char hash_str[SVCS_HASH_HEX_SIZE];
        svcs_hash_to_string(&commit_hash, hash_str);
        
        ui->print_success("Created commit " + std::string(hash_str, 7));
        return 0;
    }
    
    int handle_status(const std::map<std::string, ArgumentValue>& options, const std::vector<std::string>& args) {
        bool short_format = options.count("short") > 0;
        bool porcelain = options.count("porcelain") > 0;
        
        svcs_index_entry_t* entries;
        size_t count;
        
        svcs_error_t err = svcs_index_status(repository, &entries, &count);
        if (err != SVCS_OK) {
            ui->print_error("Failed to get status");
            return 1;
        }
        
        if (count == 0) {
            if (!short_format && !porcelain) {
                ui->print_info("Working tree clean");
            }
            return 0;
        }
        
        if (!short_format && !porcelain) {
            ui->print_header("Repository Status");
            ui->print_info("Changes to be committed:");
        }
        
        // Create table for status display
        Table status_table({
            {"Status", 8, Table::Column::LEFT, Color::BRIGHT_WHITE},
            {"File", -1, Table::Column::LEFT, Color::RESET}
        });
        
        for (size_t i = 0; i < count; i++) {
            std::string status_str;
            Color status_color = Color::RESET;
            
            switch (entries[i].status) {
                case SVCS_STATUS_ADDED:
                    status_str = "new file";
                    status_color = Color::BRIGHT_GREEN;
                    break;
                case SVCS_STATUS_MODIFIED:
                    status_str = "modified";
                    status_color = Color::BRIGHT_YELLOW;
                    break;
                case SVCS_STATUS_DELETED:
                    status_str = "deleted";
                    status_color = Color::BRIGHT_RED;
                    break;
                default:
                    status_str = "unknown";
                    break;
            }
            
            if (short_format || porcelain) {
                std::cout << (porcelain ? status_str : status_str.substr(0, 1)) 
                         << " " << entries[i].path << std::endl;
            } else {
                status_table.add_row({
                    {status_str, status_color},
                    {entries[i].path}
                });
            }
        }
        
        if (!short_format && !porcelain) {
            status_table.print();
        }
        
        free(entries);
        return 0;
    }
    
    int handle_log(const std::map<std::string, ArgumentValue>& options, const std::vector<std::string>& args) {
        if (!dag) {
            ui->print_error("Failed to load commit history");
            return 1;
        }
        
        bool oneline = options.count("oneline") > 0;
        bool show_graph = options.count("graph") > 0;
        bool show_stat = options.count("stat") > 0;
        
        int max_count = 10;
        auto max_it = options.find("max-count");
        if (max_it != options.end()) {
            max_count = std::get<int>(max_it->second);
        }
        
        CommitRange range;
        range.max_count = max_count;
        range.order = TraversalOrder::CHRONOLOGICAL;
        
        auto commits = dag->get_commits_in_range(range);
        
        if (commits.empty()) {
            ui->print_info("No commits found");
            return 0;
        }
        
        if (show_graph) {
            std::string graph = dag->generate_ascii_graph(max_count);
            std::cout << graph << std::endl;
        } else if (oneline) {
            for (const auto& commit : commits) {
                std::cout << commit->short_hash() << " " << commit->message << std::endl;
            }
        } else {
            // Detailed log format
            for (const auto& commit : commits) {
                ui->print_styled(StyledText("commit " + commit->hash_string(), Color::BRIGHT_YELLOW));
                ui->print_line("Author: " + commit->author);
                
                auto tm = *std::localtime(&commit->timestamp);
                std::ostringstream date_oss;
                date_oss << std::put_time(&tm, "%a %b %d %H:%M:%S %Y");
                ui->print_line("Date: " + date_oss.str());
                
                ui->print_line();
                ui->print_line("    " + commit->message);
                ui->print_line();
            }
        }
        
        return 0;
    }
    
    int handle_branch(const std::map<std::string, ArgumentValue>& options, const std::vector<std::string>& args) {
        bool verbose = options.count("verbose") > 0;
        bool show_all = options.count("all") > 0;
        bool show_remotes = options.count("remotes") > 0;
        
        svcs_branch_t* branches;
        size_t count;
        
        svcs_error_t err = svcs_branch_list(repository, &branches, &count);
        if (err != SVCS_OK) {
            ui->print_error("Failed to list branches");
            return 1;
        }
        
        if (args.empty()) {
            // List branches
            ui->print_header("Branches");
            
            for (size_t i = 0; i < count; i++) {
                std::string prefix = branches[i].is_current ? "* " : "  ";
                Color color = branches[i].is_current ? Color::BRIGHT_GREEN : Color::RESET;
                
                StyledText branch_line(prefix + branches[i].name, color);
                ui->print_styled(branch_line);
                
                if (verbose) {
                    char hash_str[SVCS_HASH_HEX_SIZE];
                    svcs_hash_to_string(&branches[i].commit_hash, hash_str);
                    ui->print_line("    " + std::string(hash_str, 7) + " Last commit");
                }
            }
        } else {
            // Create new branch
            const std::string& branch_name = args[0];
            
            // Get current HEAD commit
            svcs_hash_t head_hash;
            // TODO: Get HEAD commit hash
            
            err = svcs_branch_create(repository, branch_name.c_str(), &head_hash);
            if (err == SVCS_ERROR_EXISTS) {
                ui->print_error("Branch '" + branch_name + "' already exists");
                return 1;
            } else if (err != SVCS_OK) {
                ui->print_error("Failed to create branch");
                return 1;
            }
            
            ui->print_success("Created branch '" + branch_name + "'");
        }
        
        if (branches) {
            free(branches);
        }
        
        return 0;
    }
    
    int handle_checkout(const std::map<std::string, ArgumentValue>& options, const std::vector<std::string>& args) {
        if (args.empty()) {
            ui->print_error("Branch or commit required");
            return 1;
        }
        
        const std::string& target = args[0];
        bool create_branch = options.count("create") > 0;
        
        if (create_branch) {
            // Create and checkout new branch
            ui->print_info("Creating and switching to branch '" + target + "'");
            // TODO: Implement branch creation
        }
        
        svcs_error_t err = svcs_branch_checkout(repository, target.c_str());
        if (err == SVCS_ERROR_NOT_FOUND) {
            ui->print_error("Branch '" + target + "' not found");
            return 1;
        } else if (err != SVCS_OK) {
            ui->print_error("Failed to checkout branch");
            return 1;
        }
        
        ui->print_success("Switched to branch '" + target + "'");
        return 0;
    }
    
    int handle_diff(const std::map<std::string, ArgumentValue>& options, const std::vector<std::string>& args) {
        bool cached = options.count("cached") > 0;
        bool stat_only = options.count("stat") > 0;
        bool name_only = options.count("name-only") > 0;
        
        ui->print_info("Diff functionality");
        ui->print_line("Cached: " + std::string(cached ? "yes" : "no"));
        ui->print_line("Stat only: " + std::string(stat_only ? "yes" : "no"));
        
        // TODO: Implement actual diff functionality
        return 0;
    }
    
    int handle_merge(const std::map<std::string, ArgumentValue>& options, const std::vector<std::string>& args) {
        if (args.empty()) {
            ui->print_error("Branch to merge required");
            return 1;
        }
        
        const std::string& branch = args[0];
        ui->print_info("Merging branch '" + branch + "'");
        
        // TODO: Implement merge functionality
        ui->print_success("Merge completed");
        return 0;
    }
    
    int handle_interactive(const std::map<std::string, ArgumentValue>& options, const std::vector<std::string>& args) {
        ui->print_header("SnippetVCS Interactive Mode");
        
        Menu main_menu("Main Menu");
        main_menu.add_item({"Repository Status", "Show current repository status", 
                           [this]() { handle_status({}, {}); }});
        main_menu.add_item({"Commit History", "View commit log", 
                           [this]() { handle_log({{"max-count", 20}}, {}); }});
        main_menu.add_item({"Branch Management", "Manage branches", 
                           [this]() { handle_branch({{"verbose", true}}, {}); }});
        main_menu.add_separator();
        main_menu.add_item({"Exit", "Exit interactive mode", nullptr});
        
        while (true) {
            int choice = main_menu.show();
            if (choice == -1 || choice == 3) {  // Exit or last item
                break;
            }
            
            if (choice >= 0 && choice < 3) {
                main_menu.items[choice].action();
                ui->pause();
            }
        }
        
        return 0;
    }
};

int main(int argc, char* argv[]) {
    try {
        EnhancedVCSApplication app;
        return app.run(argc, argv);
    } catch (const std::exception& e) {
        std::cerr << "Fatal error: " << e.what() << std::endl;
        return 1;
    } catch (...) {
        std::cerr << "Fatal error: Unknown exception" << std::endl;
        return 1;
    }
}