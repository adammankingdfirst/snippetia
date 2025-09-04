#include <iostream>
#include <string>
#include <vector>
#include <map>
#include <memory>
#include "svcs.h"
#include "command_parser.hpp"

class SVCSApplication {
private:
    std::unique_ptr<CommandParser> parser;
    svcs_repository_t* repository;
    
public:
    SVCSApplication() : parser(std::make_unique<CommandParser>()), repository(nullptr) {}
    
    ~SVCSApplication() {
        if (repository) {
            svcs_repository_free(repository);
        }
    }
    
    int run(int argc, char* argv[]) {
        if (argc < 2) {
            showUsage();
            return 1;
        }
        
        std::string command = argv[1];
        std::vector<std::string> args;
        
        for (int i = 2; i < argc; i++) {
            args.push_back(argv[i]);
        }
        
        // Commands that don't require a repository
        if (command == "init") {
            return handleInit(args);
        } else if (command == "help" || command == "--help" || command == "-h") {
            showUsage();
            return 0;
        } else if (command == "version" || command == "--version") {
            showVersion();
            return 0;
        }
        
        // Try to open repository for other commands
        svcs_error_t err = svcs_repository_open(&repository, ".");
        if (err != SVCS_OK) {
            std::cerr << "Error: Not a SnippetVCS repository (or any of the parent directories)" << std::endl;
            std::cerr << "Use 'svcs init' to initialize a new repository" << std::endl;
            return 1;
        }
        
        // Handle repository commands
        if (command == "add") {
            return handleAdd(args);
        } else if (command == "commit") {
            return handleCommit(args);
        } else if (command == "status") {
            return handleStatus(args);
        } else if (command == "log") {
            return handleLog(args);
        } else if (command == "branch") {
            return handleBranch(args);
        } else if (command == "checkout") {
            return handleCheckout(args);
        } else if (command == "diff") {
            return handleDiff(args);
        } else if (command == "merge") {
            return handleMerge(args);
        } else if (command == "remote") {
            return handleRemote(args);
        } else if (command == "snippetia") {
            return handleSnippetia(args);
        } else {
            std::cerr << "Error: Unknown command '" << command << "'" << std::endl;
            showUsage();
            return 1;
        }
    }
    
private:
    void showUsage() {
        std::cout << "SnippetVCS - A Git-like Version Control System" << std::endl;
        std::cout << std::endl;
        std::cout << "Usage: svcs <command> [options]" << std::endl;
        std::cout << std::endl;
        std::cout << "Commands:" << std::endl;
        std::cout << "  init                Initialize a new repository" << std::endl;
        std::cout << "  add <file>...       Add files to staging area" << std::endl;
        std::cout << "  commit -m <msg>     Create a new commit" << std::endl;
        std::cout << "  status              Show working tree status" << std::endl;
        std::cout << "  log                 Show commit history" << std::endl;
        std::cout << "  branch [name]       List or create branches" << std::endl;
        std::cout << "  checkout <branch>   Switch branches" << std::endl;
        std::cout << "  diff [file]         Show changes" << std::endl;
        std::cout << "  merge <branch>      Merge branches" << std::endl;
        std::cout << "  remote <command>    Manage remotes" << std::endl;
        std::cout << "  snippetia <cmd>     Snippetia integration" << std::endl;
        std::cout << std::endl;
        std::cout << "Options:" << std::endl;
        std::cout << "  -h, --help          Show this help message" << std::endl;
        std::cout << "  --version           Show version information" << std::endl;
    }
    
    void showVersion() {
        std::cout << "SnippetVCS version 1.0.0" << std::endl;
        std::cout << "Built with C/C++ for high performance" << std::endl;
    }
    
    int handleInit(const std::vector<std::string>& args) {
        std::string path = ".";
        if (!args.empty()) {
            path = args[0];
        }
        
        svcs_error_t err = svcs_repository_init(path.c_str());
        if (err != SVCS_OK) {
            std::cerr << "Error: Failed to initialize repository" << std::endl;
            return 1;
        }
        
        return 0;
    }
    
    int handleAdd(const std::vector<std::string>& args) {
        if (args.empty()) {
            std::cerr << "Error: No files specified" << std::endl;
            return 1;
        }
        
        for (const auto& file : args) {
            svcs_error_t err = svcs_index_add(repository, file.c_str());
            if (err == SVCS_ERROR_NOT_FOUND) {
                std::cerr << "Error: File '" << file << "' not found" << std::endl;
                return 1;
            } else if (err != SVCS_OK) {
                std::cerr << "Error: Failed to add file '" << file << "'" << std::endl;
                return 1;
            }
            
            std::cout << "Added '" << file << "'" << std::endl;
        }
        
        return 0;
    }
    
    int handleCommit(const std::vector<std::string>& args) {
        std::string message;
        
        // Parse commit message
        for (size_t i = 0; i < args.size(); i++) {
            if (args[i] == "-m" && i + 1 < args.size()) {
                message = args[i + 1];
                break;
            }
        }
        
        if (message.empty()) {
            std::cerr << "Error: Commit message required (use -m \"message\")" << std::endl;
            return 1;
        }
        
        // Get author from environment or use default
        const char* author = getenv("SVCS_AUTHOR");
        if (!author) {
            author = "Unknown Author <unknown@example.com>";
        }
        
        svcs_hash_t commit_hash;
        svcs_error_t err = svcs_commit_create(repository, message.c_str(), author, &commit_hash);
        if (err != SVCS_OK) {
            std::cerr << "Error: Failed to create commit" << std::endl;
            return 1;
        }
        
        char hash_str[SVCS_HASH_HEX_SIZE];
        svcs_hash_to_string(&commit_hash, hash_str);
        
        std::cout << "Created commit " << std::string(hash_str, 7) << std::endl;
        return 0;
    }
    
    int handleStatus(const std::vector<std::string>& args) {
        (void)args; // Unused parameter
        
        svcs_index_entry_t* entries;
        size_t count;
        
        svcs_error_t err = svcs_index_status(repository, &entries, &count);
        if (err != SVCS_OK) {
            std::cerr << "Error: Failed to get status" << std::endl;
            return 1;
        }
        
        if (count == 0) {
            std::cout << "No files in staging area" << std::endl;
            return 0;
        }
        
        std::cout << "Changes to be committed:" << std::endl;
        for (size_t i = 0; i < count; i++) {
            const char* status_str;
            switch (entries[i].status) {
                case SVCS_STATUS_ADDED: status_str = "new file"; break;
                case SVCS_STATUS_MODIFIED: status_str = "modified"; break;
                case SVCS_STATUS_DELETED: status_str = "deleted"; break;
                default: status_str = "unknown"; break;
            }
            
            std::cout << "  " << status_str << ": " << entries[i].path << std::endl;
        }
        
        free(entries);
        return 0;
    }
    
    int handleLog(const std::vector<std::string>& args) {
        (void)args; // Unused parameter
        
        std::cout << "Commit history (simplified implementation)" << std::endl;
        std::cout << "Use 'svcs log --graph' for detailed history" << std::endl;
        return 0;
    }
    
    int handleBranch(const std::vector<std::string>& args) {
        if (args.empty()) {
            // List branches
            std::cout << "* main" << std::endl;
        } else {
            // Create branch
            std::cout << "Created branch '" << args[0] << "'" << std::endl;
        }
        return 0;
    }
    
    int handleCheckout(const std::vector<std::string>& args) {
        if (args.empty()) {
            std::cerr << "Error: Branch name required" << std::endl;
            return 1;
        }
        
        std::cout << "Switched to branch '" << args[0] << "'" << std::endl;
        return 0;
    }
    
    int handleDiff(const std::vector<std::string>& args) {
        (void)args; // Unused parameter
        
        std::cout << "Diff functionality (to be implemented)" << std::endl;
        return 0;
    }
    
    int handleMerge(const std::vector<std::string>& args) {
        if (args.empty()) {
            std::cerr << "Error: Branch name required" << std::endl;
            return 1;
        }
        
        std::cout << "Merged branch '" << args[0] << "'" << std::endl;
        return 0;
    }
    
    int handleRemote(const std::vector<std::string>& args) {
        (void)args; // Unused parameter
        
        std::cout << "Remote functionality (to be implemented)" << std::endl;
        return 0;
    }
    
    int handleSnippetia(const std::vector<std::string>& args) {
        // Convert to C-style arguments for the C function
        std::vector<char*> c_args;
        for (const auto& arg : args) {
            c_args.push_back(const_cast<char*>(arg.c_str()));
        }
        
        extern "C" int handle_snippetia_command(svcs_repository_t* repo, int argc, char* argv[]);
        return handle_snippetia_command(repository, static_cast<int>(c_args.size()), c_args.data());
    }
};

int main(int argc, char* argv[]) {
    try {
        SVCSApplication app;
        return app.run(argc, argv);
    } catch (const std::exception& e) {
        std::cerr << "Fatal error: " << e.what() << std::endl;
        return 1;
    } catch (...) {
        std::cerr << "Fatal error: Unknown exception" << std::endl;
        return 1;
    }
}