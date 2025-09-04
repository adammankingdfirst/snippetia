#include <iostream>
#include <string>
#include <vector>
#include "svcs.h"

extern "C" {
    // Declare C functions for Snippetia integration
    svcs_error_t svcs_snippetia_configure(svcs_repository_t *repo, const char *api_url, 
                                         const char *auth_token, const char *user_id);
    svcs_error_t svcs_snippetia_link(svcs_repository_t *repo, const char *snippet_id);
    svcs_error_t svcs_snippetia_sync(svcs_repository_t *repo, int force_push);
    svcs_error_t svcs_snippetia_status(svcs_repository_t *repo);
}

class SnippetiaCommand {
public:
    static int execute(svcs_repository_t* repo, const std::vector<std::string>& args) {
        if (args.empty()) {
            showUsage();
            return 1;
        }
        
        const std::string& subcommand = args[0];
        
        if (subcommand == "config") {
            return handleConfig(repo, args);
        } else if (subcommand == "link") {
            return handleLink(repo, args);
        } else if (subcommand == "sync") {
            return handleSync(repo, args);
        } else if (subcommand == "status") {
            return handleStatus(repo, args);
        } else if (subcommand == "push") {
            return handlePush(repo, args);
        } else if (subcommand == "pull") {
            return handlePull(repo, args);
        } else {
            std::cerr << "Unknown Snippetia command: " << subcommand << std::endl;
            showUsage();
            return 1;
        }
    }
    
private:
    static void showUsage() {
        std::cout << "Snippetia Integration Commands:" << std::endl;
        std::cout << std::endl;
        std::cout << "  svcs snippetia config <api-url> <auth-token> [user-id]" << std::endl;
        std::cout << "      Configure Snippetia API connection" << std::endl;
        std::cout << std::endl;
        std::cout << "  svcs snippetia link <snippet-id>" << std::endl;
        std::cout << "      Link repository to a Snippetia snippet" << std::endl;
        std::cout << std::endl;
        std::cout << "  svcs snippetia sync [--force]" << std::endl;
        std::cout << "      Sync local changes with remote snippet" << std::endl;
        std::cout << std::endl;
        std::cout << "  svcs snippetia status" << std::endl;
        std::cout << "      Show sync status with remote snippet" << std::endl;
        std::cout << std::endl;
        std::cout << "  svcs snippetia push [--force]" << std::endl;
        std::cout << "      Push local changes to remote snippet" << std::endl;
        std::cout << std::endl;
        std::cout << "  svcs snippetia pull" << std::endl;
        std::cout << "      Pull remote changes from snippet" << std::endl;
        std::cout << std::endl;
        std::cout << "Examples:" << std::endl;
        std::cout << "  svcs snippetia config http://localhost:8080 your-auth-token" << std::endl;
        std::cout << "  svcs snippetia link 12345" << std::endl;
        std::cout << "  svcs snippetia sync" << std::endl;
    }
    
    static int handleConfig(svcs_repository_t* repo, const std::vector<std::string>& args) {
        if (args.size() < 3) {
            std::cerr << "Usage: svcs snippetia config <api-url> <auth-token> [user-id]" << std::endl;
            return 1;
        }
        
        const std::string& api_url = args[1];
        const std::string& auth_token = args[2];
        const std::string user_id = (args.size() > 3) ? args[3] : "";
        
        svcs_error_t err = svcs_snippetia_configure(repo, api_url.c_str(), 
                                                   auth_token.c_str(), 
                                                   user_id.empty() ? nullptr : user_id.c_str());
        
        if (err != SVCS_OK) {
            std::cerr << "Failed to configure Snippetia integration" << std::endl;
            return 1;
        }
        
        std::cout << "Snippetia integration configured successfully!" << std::endl;
        std::cout << "API URL: " << api_url << std::endl;
        if (!user_id.empty()) {
            std::cout << "User ID: " << user_id << std::endl;
        }
        
        return 0;
    }
    
    static int handleLink(svcs_repository_t* repo, const std::vector<std::string>& args) {
        if (args.size() < 2) {
            std::cerr << "Usage: svcs snippetia link <snippet-id>" << std::endl;
            return 1;
        }
        
        const std::string& snippet_id = args[1];
        
        svcs_error_t err = svcs_snippetia_link(repo, snippet_id.c_str());
        
        if (err != SVCS_OK) {
            std::cerr << "Failed to link repository to snippet" << std::endl;
            return 1;
        }
        
        return 0;
    }
    
    static int handleSync(svcs_repository_t* repo, const std::vector<std::string>& args) {
        bool force = false;
        
        // Check for --force flag
        for (const auto& arg : args) {
            if (arg == "--force" || arg == "-f") {
                force = true;
                break;
            }
        }
        
        svcs_error_t err = svcs_snippetia_sync(repo, force ? 1 : 0);
        
        if (err != SVCS_OK) {
            std::cerr << "Sync failed" << std::endl;
            return 1;
        }
        
        return 0;
    }
    
    static int handleStatus(svcs_repository_t* repo, const std::vector<std::string>& args) {
        (void)args; // Unused parameter
        
        svcs_error_t err = svcs_snippetia_status(repo);
        
        if (err != SVCS_OK) {
            std::cerr << "Failed to get status" << std::endl;
            return 1;
        }
        
        return 0;
    }
    
    static int handlePush(svcs_repository_t* repo, const std::vector<std::string>& args) {
        bool force = false;
        
        // Check for --force flag
        for (const auto& arg : args) {
            if (arg == "--force" || arg == "-f") {
                force = true;
                break;
            }
        }
        
        std::cout << "Pushing local changes to Snippetia..." << std::endl;
        
        svcs_error_t err = svcs_snippetia_sync(repo, force ? 1 : 0);
        
        if (err != SVCS_OK) {
            std::cerr << "Push failed" << std::endl;
            return 1;
        }
        
        return 0;
    }
    
    static int handlePull(svcs_repository_t* repo, const std::vector<std::string>& args) {
        (void)args; // Unused parameter
        
        std::cout << "Pulling changes from Snippetia..." << std::endl;
        std::cout << "Pull functionality coming soon!" << std::endl;
        
        // TODO: Implement pull functionality
        // This would involve:
        // 1. Fetching latest snippet content from API
        // 2. Comparing with local version
        // 3. Merging changes or detecting conflicts
        // 4. Updating local files
        
        return 0;
    }
};

// C interface for the CLI
extern "C" int handle_snippetia_command(svcs_repository_t* repo, int argc, char* argv[]) {
    std::vector<std::string> args;
    for (int i = 0; i < argc; i++) {
        args.push_back(argv[i]);
    }
    
    return SnippetiaCommand::execute(repo, args);
}