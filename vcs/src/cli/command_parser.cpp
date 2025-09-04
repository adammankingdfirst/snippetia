#include "command_parser.hpp"
#include <iostream>
#include <algorithm>

CommandParser::CommandParser() {
    // Initialize common commands
    addCommand("init", "Initialize a new repository");
    addCommand("add", "Add files to the staging area");
    addCommand("commit", "Record changes to the repository");
    addCommand("status", "Show the working tree status");
    addCommand("log", "Show commit logs");
    addCommand("branch", "List, create, or delete branches");
    addCommand("checkout", "Switch branches or restore working tree files");
    addCommand("diff", "Show changes between commits, commit and working tree, etc");
    addCommand("merge", "Join two or more development histories together");
    addCommand("remote", "Manage set of tracked repositories");
    
    // Add options for specific commands
    addOption("commit", {"-m", "--message", "Commit message", true, ""});
    addOption("commit", {"-a", "--all", "Automatically stage modified files", false, ""});
    addOption("commit", {"", "--amend", "Amend the previous commit", false, ""});
    
    addOption("log", {"", "--oneline", "Show each commit on a single line", false, ""});
    addOption("log", {"", "--graph", "Show a text-based graphical representation", false, ""});
    addOption("log", {"-n", "--max-count", "Limit the number of commits", true, "10"});
    
    addOption("branch", {"-d", "--delete", "Delete a branch", false, ""});
    addOption("branch", {"-D", "--delete-force", "Force delete a branch", false, ""});
    addOption("branch", {"-r", "--remotes", "List remote branches", false, ""});
    
    addOption("checkout", {"-b", "--create", "Create a new branch", false, ""});
    addOption("checkout", {"-f", "--force", "Force checkout", false, ""});
    
    addOption("diff", {"", "--cached", "Show changes in the index", false, ""});
    addOption("diff", {"", "--stat", "Show diffstat", false, ""});
    
    addOption("remote", {"-v", "--verbose", "Show remote URLs", false, ""});
}

void CommandParser::addCommand(const std::string& name, const std::string& description) {
    command_descriptions[name] = description;
    command_options[name] = std::vector<CommandOption>();
}

void CommandParser::addOption(const std::string& command, const CommandOption& option) {
    if (command_options.find(command) != command_options.end()) {
        command_options[command].push_back(option);
    }
}

ParsedCommand CommandParser::parse(int argc, char* argv[]) {
    ParsedCommand result;
    result.help_requested = false;
    
    if (argc < 2) {
        result.help_requested = true;
        return result;
    }
    
    result.command = argv[1];
    
    // Check for help request
    if (result.command == "help" || result.command == "--help" || result.command == "-h") {
        result.help_requested = true;
        if (argc > 2) {
            result.command = argv[2];
        }
        return result;
    }
    
    // Parse arguments and options
    for (int i = 2; i < argc; i++) {
        std::string arg = argv[i];
        
        if (isOption(arg)) {
            std::string option_name = getOptionName(arg);
            CommandOption* option = findOption(result.command, option_name);
            
            if (option && option->has_value) {
                if (i + 1 < argc && !isOption(argv[i + 1])) {
                    result.options[option_name] = argv[i + 1];
                    i++; // Skip the value
                } else {
                    result.options[option_name] = option->default_value;
                }
            } else {
                result.options[option_name] = "true";
            }
        } else {
            result.arguments.push_back(arg);
        }
    }
    
    return result;
}

void CommandParser::showHelp(const std::string& command) {
    if (command.empty()) {
        std::cout << "SnippetVCS - A Git-like Version Control System" << std::endl;
        std::cout << std::endl;
        std::cout << "Usage: svcs <command> [options]" << std::endl;
        std::cout << std::endl;
        std::cout << "Available commands:" << std::endl;
        
        for (const auto& cmd : command_descriptions) {
            std::cout << "  " << cmd.first;
            // Pad to align descriptions
            for (size_t i = cmd.first.length(); i < 15; i++) {
                std::cout << " ";
            }
            std::cout << cmd.second << std::endl;
        }
        
        std::cout << std::endl;
        std::cout << "Use 'svcs help <command>' for more information on a specific command." << std::endl;
    } else {
        auto desc_it = command_descriptions.find(command);
        if (desc_it != command_descriptions.end()) {
            std::cout << "svcs " << command << " - " << desc_it->second << std::endl;
            std::cout << std::endl;
            
            auto opts_it = command_options.find(command);
            if (opts_it != command_options.end() && !opts_it->second.empty()) {
                std::cout << "Options:" << std::endl;
                for (const auto& option : opts_it->second) {
                    std::cout << "  ";
                    if (!option.short_name.empty()) {
                        std::cout << option.short_name;
                        if (!option.long_name.empty()) {
                            std::cout << ", ";
                        }
                    }
                    if (!option.long_name.empty()) {
                        std::cout << option.long_name;
                    }
                    if (option.has_value) {
                        std::cout << " <value>";
                    }
                    std::cout << std::endl;
                    std::cout << "      " << option.description << std::endl;
                }
            }
        } else {
            std::cout << "Unknown command: " << command << std::endl;
        }
    }
}

bool CommandParser::isOption(const std::string& arg) {
    return arg.length() > 1 && arg[0] == '-';
}

std::string CommandParser::getOptionName(const std::string& arg) {
    if (arg.length() > 2 && arg.substr(0, 2) == "--") {
        return arg.substr(2);
    } else if (arg.length() > 1 && arg[0] == '-') {
        return arg.substr(1);
    }
    return arg;
}

CommandOption* CommandParser::findOption(const std::string& command, const std::string& name) {
    auto it = command_options.find(command);
    if (it == command_options.end()) {
        return nullptr;
    }
    
    for (auto& option : it->second) {
        if (option.short_name == "-" + name || 
            option.long_name == "--" + name ||
            option.short_name.substr(1) == name ||
            option.long_name.substr(2) == name) {
            return &option;
        }
    }
    
    return nullptr;
}