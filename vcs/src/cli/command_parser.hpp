#ifndef COMMAND_PARSER_HPP
#define COMMAND_PARSER_HPP

#include <string>
#include <vector>
#include <map>
#include <functional>

struct CommandOption {
    std::string short_name;
    std::string long_name;
    std::string description;
    bool has_value;
    std::string default_value;
};

struct ParsedCommand {
    std::string command;
    std::vector<std::string> arguments;
    std::map<std::string, std::string> options;
    bool help_requested;
};

class CommandParser {
private:
    std::map<std::string, std::vector<CommandOption>> command_options;
    std::map<std::string, std::string> command_descriptions;
    
public:
    CommandParser();
    
    void addCommand(const std::string& name, const std::string& description);
    void addOption(const std::string& command, const CommandOption& option);
    
    ParsedCommand parse(int argc, char* argv[]);
    void showHelp(const std::string& command = "");
    
private:
    bool isOption(const std::string& arg);
    std::string getOptionName(const std::string& arg);
    CommandOption* findOption(const std::string& command, const std::string& name);
};

#endif // COMMAND_PARSER_HPP