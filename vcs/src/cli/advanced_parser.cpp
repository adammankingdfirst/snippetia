#include "advanced_parser.hpp"
#include <iostream>
#include <sstream>
#include <algorithm>
#include <iomanip>
#include <regex>

namespace svcs {
namespace cli {

AdvancedArgumentParser::AdvancedArgumentParser(const std::string& name, const std::string& description, const std::string& version)
    : program_name(name), program_description(description), program_version(version) {
    
    // Add default global options
    add_global_option(make_flag_option("h", "help", "Show help message"));
    add_global_option(make_flag_option("", "version", "Show version information"));
    add_global_option(make_flag_option("v", "verbose", "Enable verbose output"));
    add_global_option(make_flag_option("q", "quiet", "Suppress output"));
}

void AdvancedArgumentParser::add_global_option(const OptionConfig& option) {
    global_options.push_back(option);
}

void AdvancedArgumentParser::add_subcommand(const SubcommandConfig& subcommand) {
    subcommands[subcommand.name] = subcommand;
}

void AdvancedArgumentParser::set_allow_unknown_options(bool allow) {
    allow_unknown_options = allow;
}

ParseResult AdvancedArgumentParser::parse(int argc, char* argv[]) {
    std::vector<std::string> args;
    for (int i = 1; i < argc; ++i) {
        args.push_back(argv[i]);
    }
    return parse(args);
}

ParseResult AdvancedArgumentParser::parse(const std::vector<std::string>& args) {
    ParseResult result;
    
    if (args.empty()) {
        result.help_requested = true;
        return result;
    }
    
    size_t arg_index = 0;
    
    // Check for global help/version first
    for (const auto& arg : args) {
        if (arg == "-h" || arg == "--help") {
            result.help_requested = true;
            return result;
        }
        if (arg == "--version") {
            result.version_requested = true;
            return result;
        }
    }
    
    // Parse global options first
    while (arg_index < args.size() && is_option(args[arg_index])) {
        const std::string& arg = args[arg_index];
        std::string option_name = extract_option_name(arg);
        
        auto option = find_option(option_name);
        if (!option) {
            if (!allow_unknown_options) {
                result.error_message = "Unknown option: " + arg;
                return result;
            }
            ++arg_index;
            continue;
        }
        
        if (option->has_value) {
            if (arg_index + 1 >= args.size() || is_option(args[arg_index + 1])) {
                result.error_message = "Option " + arg + " requires a value";
                return result;
            }
            
            try {
                ArgumentValue value = parse_option_value(args[arg_index + 1], *option);
                result.options[option->long_name.empty() ? option->short_name : option->long_name] = value;
                arg_index += 2;
            } catch (const std::exception& e) {
                result.error_message = "Invalid value for option " + arg + ": " + e.what();
                return result;
            }
        } else {
            result.options[option->long_name.empty() ? option->short_name : option->long_name] = true;
            ++arg_index;
        }
    }
    
    // Parse subcommand
    if (arg_index < args.size() && !is_option(args[arg_index])) {
        result.subcommand = args[arg_index];
        ++arg_index;
        
        auto subcmd_it = subcommands.find(result.subcommand);
        if (subcmd_it == subcommands.end()) {
            result.error_message = "Unknown subcommand: " + result.subcommand;
            return result;
        }
        
        const SubcommandConfig& subcmd = subcmd_it->second;
        
        // Parse subcommand options
        while (arg_index < args.size() && is_option(args[arg_index])) {
            const std::string& arg = args[arg_index];
            std::string option_name = extract_option_name(arg);
            
            auto option = find_option(option_name, result.subcommand);
            if (!option) {
                if (!allow_unknown_options) {
                    result.error_message = "Unknown option for " + result.subcommand + ": " + arg;
                    return result;
                }
                ++arg_index;
                continue;
            }
            
            if (option->has_value) {
                if (arg_index + 1 >= args.size() || is_option(args[arg_index + 1])) {
                    result.error_message = "Option " + arg + " requires a value";
                    return result;
                }
                
                try {
                    ArgumentValue value = parse_option_value(args[arg_index + 1], *option);
                    result.options[option->long_name.empty() ? option->short_name : option->long_name] = value;
                    arg_index += 2;
                } catch (const std::exception& e) {
                    result.error_message = "Invalid value for option " + arg + ": " + e.what();
                    return result;
                }
            } else {
                result.options[option->long_name.empty() ? option->short_name : option->long_name] = true;
                ++arg_index;
            }
        }
        
        // Collect remaining positional arguments
        while (arg_index < args.size()) {
            result.positional_args.push_back(args[arg_index]);
            ++arg_index;
        }
        
        // Validate required options
        for (const auto& option : subcmd.options) {
            if (option.required) {
                std::string key = option.long_name.empty() ? option.short_name : option.long_name;
                if (result.options.find(key) == result.options.end()) {
                    result.error_message = "Required option missing: --" + key;
                    return result;
                }
            }
        }
    }
    
    return result;
}

void AdvancedArgumentParser::print_help(const std::string& subcommand) const {
    if (subcommand.empty()) {
        // Print general help
        std::cout << program_name << " - " << program_description << std::endl;
        std::cout << std::endl;
        std::cout << "Usage: " << program_name << " [global options] <subcommand> [options] [arguments]" << std::endl;
        std::cout << std::endl;
        
        // Global options
        if (!global_options.empty()) {
            std::cout << "Global Options:" << std::endl;
            for (const auto& option : global_options) {
                std::cout << "  " << format_option_help(option) << std::endl;
            }
            std::cout << std::endl;
        }
        
        // Subcommands
        if (!subcommands.empty()) {
            std::cout << "Available Subcommands:" << std::endl;
            size_t max_name_length = 0;
            for (const auto& [name, subcmd] : subcommands) {
                max_name_length = std::max(max_name_length, name.length());
            }
            
            for (const auto& [name, subcmd] : subcommands) {
                std::cout << "  " << std::left << std::setw(max_name_length + 2) << name 
                         << subcmd.description << std::endl;
            }
            std::cout << std::endl;
        }
        
        std::cout << "Use '" << program_name << " <subcommand> --help' for more information on a specific subcommand." << std::endl;
    } else {
        // Print subcommand help
        auto it = subcommands.find(subcommand);
        if (it != subcommands.end()) {
            print_subcommand_help(it->second);
        } else {
            std::cout << "Unknown subcommand: " << subcommand << std::endl;
        }
    }
}

void AdvancedArgumentParser::print_version() const {
    std::cout << program_name << " version " << program_version << std::endl;
}

std::string AdvancedArgumentParser::get_usage_string(const std::string& subcommand) const {
    std::ostringstream oss;
    
    if (subcommand.empty()) {
        oss << "Usage: " << program_name << " [global options] <subcommand> [options] [arguments]";
    } else {
        auto it = subcommands.find(subcommand);
        if (it != subcommands.end()) {
            const SubcommandConfig& subcmd = it->second;
            oss << "Usage: " << program_name << " " << subcommand;
            
            if (!subcmd.options.empty()) {
                oss << " [options]";
            }
            
            for (const auto& arg : subcmd.positional_args) {
                oss << " <" << arg << ">";
            }
        }
    }
    
    return oss.str();
}

std::optional<OptionConfig> AdvancedArgumentParser::find_option(const std::string& name, const std::string& subcommand) const {
    // Check subcommand options first
    if (!subcommand.empty()) {
        auto subcmd_it = subcommands.find(subcommand);
        if (subcmd_it != subcommands.end()) {
            for (const auto& option : subcmd_it->second.options) {
                if (option.short_name == name || option.long_name == name) {
                    return option;
                }
            }
        }
    }
    
    // Check global options
    for (const auto& option : global_options) {
        if (option.short_name == name || option.long_name == name) {
            return option;
        }
    }
    
    return std::nullopt;
}

bool AdvancedArgumentParser::is_option(const std::string& arg) const {
    return arg.length() > 1 && arg[0] == '-';
}

std::string AdvancedArgumentParser::extract_option_name(const std::string& arg) const {
    if (arg.length() > 2 && arg.substr(0, 2) == "--") {
        // Long option
        size_t eq_pos = arg.find('=');
        if (eq_pos != std::string::npos) {
            return arg.substr(2, eq_pos - 2);
        }
        return arg.substr(2);
    } else if (arg.length() > 1 && arg[0] == '-') {
        // Short option
        return arg.substr(1, 1);
    }
    return arg;
}

ArgumentValue AdvancedArgumentParser::parse_option_value(const std::string& value, const OptionConfig& option) const {
    // Validate choices if specified
    if (!option.choices.empty()) {
        if (std::find(option.choices.begin(), option.choices.end(), value) == option.choices.end()) {
            throw std::invalid_argument("Invalid choice. Must be one of: " + 
                [&]() {
                    std::ostringstream oss;
                    for (size_t i = 0; i < option.choices.size(); ++i) {
                        if (i > 0) oss << ", ";
                        oss << option.choices[i];
                    }
                    return oss.str();
                }());
        }
    }
    
    // Try to determine the expected type from default value
    if (std::holds_alternative<int>(option.default_value)) {
        try {
            return std::stoi(value);
        } catch (const std::exception&) {
            throw std::invalid_argument("Expected integer value");
        }
    } else if (std::holds_alternative<double>(option.default_value)) {
        try {
            return std::stod(value);
        } catch (const std::exception&) {
            throw std::invalid_argument("Expected numeric value");
        }
    } else if (std::holds_alternative<bool>(option.default_value)) {
        std::string lower_value = value;
        std::transform(lower_value.begin(), lower_value.end(), lower_value.begin(), ::tolower);
        if (lower_value == "true" || lower_value == "1" || lower_value == "yes" || lower_value == "on") {
            return true;
        } else if (lower_value == "false" || lower_value == "0" || lower_value == "no" || lower_value == "off") {
            return false;
        } else {
            throw std::invalid_argument("Expected boolean value (true/false, yes/no, 1/0, on/off)");
        }
    }
    
    // Default to string
    return value;
}

void AdvancedArgumentParser::print_subcommand_help(const SubcommandConfig& subcmd) const {
    std::cout << program_name << " " << subcmd.name << " - " << subcmd.description << std::endl;
    std::cout << std::endl;
    
    if (!subcmd.help_text.empty()) {
        std::cout << subcmd.help_text << std::endl;
        std::cout << std::endl;
    }
    
    std::cout << get_usage_string(subcmd.name) << std::endl;
    std::cout << std::endl;
    
    if (!subcmd.options.empty()) {
        std::cout << "Options:" << std::endl;
        for (const auto& option : subcmd.options) {
            std::cout << "  " << format_option_help(option) << std::endl;
        }
        std::cout << std::endl;
    }
    
    if (!subcmd.positional_args.empty()) {
        std::cout << "Arguments:" << std::endl;
        for (const auto& arg : subcmd.positional_args) {
            std::cout << "  " << arg << std::endl;
        }
        std::cout << std::endl;
    }
}

std::string AdvancedArgumentParser::format_option_help(const OptionConfig& option) const {
    std::ostringstream oss;
    
    // Format option names
    if (!option.short_name.empty()) {
        oss << "-" << option.short_name;
        if (!option.long_name.empty()) {
            oss << ", ";
        }
    }
    
    if (!option.long_name.empty()) {
        oss << "--" << option.long_name;
    }
    
    // Add metavar if option has value
    if (option.has_value) {
        std::string metavar = option.metavar.empty() ? "VALUE" : option.metavar;
        oss << " <" << metavar << ">";
    }
    
    // Pad to align descriptions
    std::string option_part = oss.str();
    const size_t padding = 25;
    if (option_part.length() < padding) {
        option_part += std::string(padding - option_part.length(), ' ');
    } else {
        option_part += "  ";
    }
    
    oss.str("");
    oss << option_part << option.description;
    
    // Add additional info
    if (option.required) {
        oss << " (required)";
    }
    
    if (!option.choices.empty()) {
        oss << " (choices: ";
        for (size_t i = 0; i < option.choices.size(); ++i) {
            if (i > 0) oss << ", ";
            oss << option.choices[i];
        }
        oss << ")";
    }
    
    return oss.str();
}

// Builder implementation
ArgumentParserBuilder::ArgumentParserBuilder(const std::string& name, const std::string& description, const std::string& version) {
    parser = std::make_unique<AdvancedArgumentParser>(name, description, version);
}

ArgumentParserBuilder& ArgumentParserBuilder::global_option(const OptionConfig& option) {
    parser->add_global_option(option);
    return *this;
}

ArgumentParserBuilder& ArgumentParserBuilder::subcommand(const SubcommandConfig& subcommand) {
    parser->add_subcommand(subcommand);
    return *this;
}

ArgumentParserBuilder& ArgumentParserBuilder::allow_unknown(bool allow) {
    parser->set_allow_unknown_options(allow);
    return *this;
}

std::unique_ptr<AdvancedArgumentParser> ArgumentParserBuilder::build() {
    return std::move(parser);
}

// Utility functions
OptionConfig make_flag_option(const std::string& short_name, const std::string& long_name, const std::string& description) {
    OptionConfig option;
    option.short_name = short_name;
    option.long_name = long_name;
    option.description = description;
    option.has_value = false;
    option.default_value = false;
    return option;
}

OptionConfig make_string_option(const std::string& short_name, const std::string& long_name,
                               const std::string& description, bool required, const std::string& default_value) {
    OptionConfig option;
    option.short_name = short_name;
    option.long_name = long_name;
    option.description = description;
    option.required = required;
    option.has_value = true;
    option.default_value = default_value;
    option.metavar = "STRING";
    return option;
}

OptionConfig make_int_option(const std::string& short_name, const std::string& long_name,
                            const std::string& description, bool required, int default_value) {
    OptionConfig option;
    option.short_name = short_name;
    option.long_name = long_name;
    option.description = description;
    option.required = required;
    option.has_value = true;
    option.default_value = default_value;
    option.metavar = "INT";
    return option;
}

OptionConfig make_choice_option(const std::string& short_name, const std::string& long_name,
                               const std::string& description, const std::vector<std::string>& choices,
                               const std::string& default_choice) {
    OptionConfig option;
    option.short_name = short_name;
    option.long_name = long_name;
    option.description = description;
    option.has_value = true;
    option.choices = choices;
    option.default_value = default_choice.empty() ? (choices.empty() ? std::string{} : choices[0]) : default_choice;
    option.metavar = "CHOICE";
    return option;
}

} // namespace cli
} // namespace svcs