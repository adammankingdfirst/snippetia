#ifndef ADVANCED_PARSER_HPP
#define ADVANCED_PARSER_HPP

#include <string>
#include <vector>
#include <map>
#include <functional>
#include <memory>
#include <optional>
#include <variant>

namespace svcs {
namespace cli {

// Argument value types
using ArgumentValue = std::variant<std::string, int, double, bool, std::vector<std::string>>;

// Option configuration
struct OptionConfig {
    std::string short_name;
    std::string long_name;
    std::string description;
    std::string help_text;
    bool required = false;
    bool has_value = false;
    bool multiple_values = false;
    ArgumentValue default_value;
    std::function<bool(const ArgumentValue&)> validator;
    std::vector<std::string> choices;
    std::string metavar;
};

// Subcommand configuration
struct SubcommandConfig {
    std::string name;
    std::string description;
    std::string help_text;
    std::vector<OptionConfig> options;
    std::vector<std::string> positional_args;
    std::function<int(const std::map<std::string, ArgumentValue>&, const std::vector<std::string>&)> handler;
};

// Parsed result
struct ParseResult {
    std::string command;
    std::string subcommand;
    std::map<std::string, ArgumentValue> options;
    std::vector<std::string> positional_args;
    bool help_requested = false;
    bool version_requested = false;
    std::string error_message;
};

class AdvancedArgumentParser {
private:
    std::string program_name;
    std::string program_description;
    std::string program_version;
    std::map<std::string, SubcommandConfig> subcommands;
    std::vector<OptionConfig> global_options;
    bool allow_unknown_options = false;
    
public:
    AdvancedArgumentParser(const std::string& name, const std::string& description, const std::string& version);
    
    // Configuration methods
    void add_global_option(const OptionConfig& option);
    void add_subcommand(const SubcommandConfig& subcommand);
    void set_allow_unknown_options(bool allow);
    
    // Parsing methods
    ParseResult parse(int argc, char* argv[]);
    ParseResult parse(const std::vector<std::string>& args);
    
    // Help and usage
    void print_help(const std::string& subcommand = "") const;
    void print_version() const;
    std::string get_usage_string(const std::string& subcommand = "") const;
    
    // Validation
    bool validate_arguments(const ParseResult& result) const;
    
private:
    std::optional<OptionConfig> find_option(const std::string& name, const std::string& subcommand = "") const;
    bool is_option(const std::string& arg) const;
    std::string extract_option_name(const std::string& arg) const;
    ArgumentValue parse_option_value(const std::string& value, const OptionConfig& option) const;
    void print_subcommand_help(const SubcommandConfig& subcmd) const;
    std::string format_option_help(const OptionConfig& option) const;
};

// Builder pattern for easier configuration
class ArgumentParserBuilder {
private:
    std::unique_ptr<AdvancedArgumentParser> parser;
    
public:
    ArgumentParserBuilder(const std::string& name, const std::string& description, const std::string& version);
    
    ArgumentParserBuilder& global_option(const OptionConfig& option);
    ArgumentParserBuilder& subcommand(const SubcommandConfig& subcommand);
    ArgumentParserBuilder& allow_unknown(bool allow = true);
    
    std::unique_ptr<AdvancedArgumentParser> build();
};

// Utility functions for common option types
OptionConfig make_flag_option(const std::string& short_name, const std::string& long_name, 
                             const std::string& description);
OptionConfig make_string_option(const std::string& short_name, const std::string& long_name,
                               const std::string& description, bool required = false,
                               const std::string& default_value = "");
OptionConfig make_int_option(const std::string& short_name, const std::string& long_name,
                            const std::string& description, bool required = false,
                            int default_value = 0);
OptionConfig make_choice_option(const std::string& short_name, const std::string& long_name,
                               const std::string& description, const std::vector<std::string>& choices,
                               const std::string& default_choice = "");

} // namespace cli
} // namespace svcs

#endif // ADVANCED_PARSER_HPP