#include "terminal_ui.hpp"
#include <iostream>
#include <sstream>
#include <iomanip>
#include <algorithm>
#include <thread>
#include <atomic>

#ifdef _WIN32
#include <windows.h>
#include <conio.h>
#else
#include <sys/ioctl.h>
#include <unistd.h>
#include <termios.h>
#endif

namespace svcs {
namespace ui {

// TerminalCapabilities implementation
bool TerminalCapabilities::supports_color() {
#ifdef _WIN32
    HANDLE hOut = GetStdHandle(STD_OUTPUT_HANDLE);
    DWORD dwMode = 0;
    GetConsoleMode(hOut, &dwMode);
    return (dwMode & ENABLE_VIRTUAL_TERMINAL_PROCESSING) != 0;
#else
    const char* term = getenv("TERM");
    return term && (strstr(term, "color") || strstr(term, "xterm") || strstr(term, "screen"));
#endif
}

bool TerminalCapabilities::supports_unicode() {
#ifdef _WIN32
    return GetConsoleOutputCP() == CP_UTF8;
#else
    const char* lang = getenv("LANG");
    return lang && strstr(lang, "UTF-8");
#endif
}

std::pair<int, int> TerminalCapabilities::get_terminal_size() {
#ifdef _WIN32
    CONSOLE_SCREEN_BUFFER_INFO csbi;
    GetConsoleScreenBufferInfo(GetStdHandle(STD_OUTPUT_HANDLE), &csbi);
    return {csbi.srWindow.Right - csbi.srWindow.Left + 1, csbi.srWindow.Bottom - csbi.srWindow.Top + 1};
#else
    struct winsize w;
    ioctl(STDOUT_FILENO, TIOCGWINSZ, &w);
    return {w.ws_col, w.ws_row};
#endif
}

bool TerminalCapabilities::is_interactive() {
#ifdef _WIN32
    return _isatty(_fileno(stdout));
#else
    return isatty(STDOUT_FILENO);
#endif
}

void TerminalCapabilities::clear_screen() {
    std::cout << "\033[2J\033[H" << std::flush;
}

void TerminalCapabilities::move_cursor(int row, int col) {
    std::cout << "\033[" << row << ";" << col << "H" << std::flush;
}

void TerminalCapabilities::hide_cursor() {
    std::cout << "\033[?25l" << std::flush;
}

void TerminalCapabilities::show_cursor() {
    std::cout << "\033[?25h" << std::flush;
}

// StyledText implementation
StyledText::StyledText(const std::string& text) : text(text) {}

StyledText::StyledText(const std::string& text, Color fg) : text(text), foreground(fg) {}

StyledText::StyledText(const std::string& text, Color fg, Color bg) : text(text), foreground(fg), background(bg) {}

StyledText::StyledText(const std::string& text, Color fg, Style style) : text(text), foreground(fg), style(style) {}

StyledText::StyledText(const std::string& text, Color fg, Color bg, Style style) 
    : text(text), foreground(fg), background(bg), style(style) {}

std::string StyledText::render() const {
    if (!TerminalCapabilities::supports_color()) {
        return text;
    }
    
    std::ostringstream oss;
    
    // Apply styles
    if (style != Style::NORMAL) {
        oss << "\033[" << static_cast<int>(style) << "m";
    }
    
    // Apply foreground color
    if (foreground != Color::RESET) {
        oss << "\033[" << static_cast<int>(foreground) << "m";
    }
    
    // Apply background color
    if (background != Color::RESET) {
        oss << "\033[" << (static_cast<int>(background) + 10) << "m";
    }
    
    oss << text;
    
    // Reset formatting
    oss << "\033[0m";
    
    return oss.str();
}

// ProgressBar implementation
ProgressBar::ProgressBar(int width, char fill, char empty) 
    : width(width), fill_char(fill), empty_char(empty), bar_color(Color::GREEN) {}

std::string ProgressBar::render(double progress) const {
    progress = std::max(0.0, std::min(1.0, progress));
    
    int filled = static_cast<int>(progress * width);
    int empty = width - filled;
    
    std::ostringstream oss;
    
    if (!prefix.empty()) {
        oss << prefix << " ";
    }
    
    oss << "[";
    
    if (TerminalCapabilities::supports_color()) {
        oss << "\033[" << static_cast<int>(bar_color) << "m";
    }
    
    oss << std::string(filled, fill_char);
    
    if (TerminalCapabilities::supports_color()) {
        oss << "\033[0m";
    }
    
    oss << std::string(empty, empty_char) << "]";
    
    if (!suffix.empty()) {
        oss << " " << suffix;
    }
    
    oss << " " << std::fixed << std::setprecision(1) << (progress * 100) << "%";
    
    return oss.str();
}

// Table implementation
Table::Table(const std::vector<Column>& cols) : columns(cols) {}

void Table::add_row(const std::vector<std::string>& row_data) {
    std::vector<Cell> cells;
    for (const auto& data : row_data) {
        cells.push_back({data});
    }
    rows.push_back(cells);
}

void Table::add_row(const std::vector<Cell>& row_cells) {
    rows.push_back(row_cells);
}

std::string Table::render() const {
    std::ostringstream oss;
    
    auto widths = calculate_column_widths();
    
    // Render header
    if (show_headers) {
        std::vector<Cell> header_cells;
        for (const auto& col : columns) {
            header_cells.push_back({col.header, col.header_color, col.header_style});
        }
        oss << render_row(header_cells, widths) << "\n";
        oss << render_separator(widths) << "\n";
    }
    
    // Render rows
    for (const auto& row : rows) {
        oss << render_row(row, widths) << "\n";
    }
    
    return oss.str();
}

void Table::print() const {
    std::cout << render();
}

std::vector<int> Table::calculate_column_widths() const {
    std::vector<int> widths(columns.size(), 0);
    
    // Calculate from headers
    for (size_t i = 0; i < columns.size(); ++i) {
        if (columns[i].width > 0) {
            widths[i] = columns[i].width;
        } else {
            widths[i] = static_cast<int>(columns[i].header.length());
        }
    }
    
    // Calculate from data
    for (const auto& row : rows) {
        for (size_t i = 0; i < std::min(row.size(), widths.size()); ++i) {
            if (columns[i].width <= 0) {  // Only auto-size columns
                widths[i] = std::max(widths[i], static_cast<int>(row[i].content.length()));
            }
        }
    }
    
    return widths;
}

std::string Table::render_separator(const std::vector<int>& widths) const {
    std::ostringstream oss;
    
    if (show_borders) {
        oss << "+";
        for (int width : widths) {
            oss << std::string(width + 2, header_separator) << "+";
        }
    } else {
        for (size_t i = 0; i < widths.size(); ++i) {
            if (i > 0) oss << " ";
            oss << std::string(widths[i], header_separator);
        }
    }
    
    return oss.str();
}

std::string Table::render_row(const std::vector<Cell>& row, const std::vector<int>& widths) const {
    std::ostringstream oss;
    
    if (show_borders) {
        oss << border_char;
    }
    
    for (size_t i = 0; i < widths.size(); ++i) {
        if (show_borders) {
            oss << " ";
        } else if (i > 0) {
            oss << " ";
        }
        
        std::string content;
        Color color = Color::RESET;
        Style style = Style::NORMAL;
        
        if (i < row.size()) {
            content = row[i].content;
            color = row[i].color;
            style = row[i].style;
        }
        
        // Apply alignment
        Column::Alignment alignment = (i < columns.size()) ? columns[i].alignment : Column::LEFT;
        
        if (alignment == Column::CENTER) {
            int padding = widths[i] - static_cast<int>(content.length());
            int left_pad = padding / 2;
            int right_pad = padding - left_pad;
            content = std::string(left_pad, ' ') + content + std::string(right_pad, ' ');
        } else if (alignment == Column::RIGHT) {
            content = std::string(widths[i] - content.length(), ' ') + content;
        } else {
            content += std::string(widths[i] - content.length(), ' ');
        }
        
        // Apply styling
        if (TerminalCapabilities::supports_color() && (color != Color::RESET || style != Style::NORMAL)) {
            StyledText styled(content, color, style);
            oss << styled.render();
        } else {
            oss << content;
        }
        
        if (show_borders) {
            oss << " " << border_char;
        }
    }
    
    return oss.str();
}

// Menu implementation
Menu::Menu(const std::string& title) : title(title) {}

void Menu::add_item(const MenuItem& item) {
    items.push_back(item);
}

void Menu::add_separator() {
    items.push_back({"", "", nullptr, false});
}

int Menu::show() {
    if (!TerminalCapabilities::is_interactive()) {
        return -1;
    }
    
    while (true) {
        render();
        char key = get_key();
        
        switch (key) {
            case 'q':
            case 'Q':
            case 27:  // ESC
                return -1;
                
            case '\n':
            case '\r':
                if (selected_index >= 0 && selected_index < static_cast<int>(items.size()) &&
                    items[selected_index].enabled && items[selected_index].action) {
                    return selected_index;
                }
                break;
                
            case 'j':
            case 'J':
                if (selected_index < static_cast<int>(items.size()) - 1) {
                    selected_index++;
                    while (selected_index < static_cast<int>(items.size()) && 
                           (!items[selected_index].enabled || items[selected_index].text.empty())) {
                        selected_index++;
                    }
                }
                break;
                
            case 'k':
            case 'K':
                if (selected_index > 0) {
                    selected_index--;
                    while (selected_index >= 0 && 
                           (!items[selected_index].enabled || items[selected_index].text.empty())) {
                        selected_index--;
                    }
                }
                break;
                
            default:
                // Check for hotkeys
                for (size_t i = 0; i < items.size(); ++i) {
                    if (items[i].hotkey == key && items[i].enabled) {
                        selected_index = static_cast<int>(i);
                        return selected_index;
                    }
                }
                break;
        }
    }
}

void Menu::run() {
    int choice = show();
    if (choice >= 0 && choice < static_cast<int>(items.size()) && items[choice].action) {
        items[choice].action();
    }
}

void Menu::render() const {
    TerminalCapabilities::clear_screen();
    
    // Print title
    if (!title.empty()) {
        StyledText styled_title(title, Color::BRIGHT_WHITE, Style::BOLD);
        std::cout << styled_title.render() << "\n\n";
    }
    
    // Print menu items
    for (size_t i = 0; i < items.size(); ++i) {
        const auto& item = items[i];
        
        if (item.text.empty()) {
            std::cout << "\n";  // Separator
            continue;
        }
        
        std::string prefix = "  ";
        Color color = Color::RESET;
        
        if (static_cast<int>(i) == selected_index) {
            prefix = "> ";
            color = selected_color;
        } else if (!item.enabled) {
            color = disabled_color;
        }
        
        std::string line = prefix + item.text;
        if (item.hotkey != '\0') {
            line += " (" + std::string(1, item.hotkey) + ")";
        }
        
        StyledText styled_line(line, color);
        std::cout << styled_line.render();
        
        if (show_descriptions && !item.description.empty()) {
            StyledText desc(" - " + item.description, Color::BRIGHT_BLACK);
            std::cout << desc.render();
        }
        
        std::cout << "\n";
    }
    
    std::cout << "\nUse j/k or arrow keys to navigate, Enter to select, q to quit\n";
}

char Menu::get_key() const {
#ifdef _WIN32
    return _getch();
#else
    struct termios old_termios, new_termios;
    tcgetattr(STDIN_FILENO, &old_termios);
    new_termios = old_termios;
    new_termios.c_lflag &= ~(ICANON | ECHO);
    tcsetattr(STDIN_FILENO, TCSANOW, &new_termios);
    
    char ch = getchar();
    
    tcsetattr(STDIN_FILENO, TCSANOW, &old_termios);
    return ch;
#endif
}

// TerminalUI implementation
TerminalUI::TerminalUI() {
    color_enabled = TerminalCapabilities::supports_color();
    interactive_mode = TerminalCapabilities::is_interactive();
    auto [width, height] = TerminalCapabilities::get_terminal_size();
    terminal_width = width;
    terminal_height = height;
}

void TerminalUI::print(const std::string& text) const {
    std::cout << text;
}

void TerminalUI::print_line(const std::string& text) const {
    std::cout << text << std::endl;
}

void TerminalUI::print_error(const std::string& text) const {
    if (color_enabled) {
        StyledText styled("Error: " + text, Color::BRIGHT_RED, Style::BOLD);
        std::cerr << styled.render() << std::endl;
    } else {
        std::cerr << "Error: " << text << std::endl;
    }
}

void TerminalUI::print_warning(const std::string& text) const {
    if (color_enabled) {
        StyledText styled("Warning: " + text, Color::BRIGHT_YELLOW);
        std::cout << styled.render() << std::endl;
    } else {
        std::cout << "Warning: " << text << std::endl;
    }
}

void TerminalUI::print_success(const std::string& text) const {
    if (color_enabled) {
        StyledText styled(text, Color::BRIGHT_GREEN);
        std::cout << styled.render() << std::endl;
    } else {
        std::cout << text << std::endl;
    }
}

void TerminalUI::print_info(const std::string& text) const {
    if (color_enabled) {
        StyledText styled(text, Color::BRIGHT_BLUE);
        std::cout << styled.render() << std::endl;
    } else {
        std::cout << text << std::endl;
    }
}

void TerminalUI::print_styled(const StyledText& text) const {
    std::cout << text.render() << std::endl;
}

void TerminalUI::print_header(const std::string& text) const {
    if (color_enabled) {
        StyledText styled(text, Color::BRIGHT_WHITE, Style::BOLD);
        std::cout << styled.render() << std::endl;
    } else {
        std::cout << text << std::endl;
    }
    print_separator();
}

void TerminalUI::print_separator(char ch) const {
    std::cout << std::string(terminal_width, ch) << std::endl;
}

std::string TerminalUI::prompt(const std::string& message, const std::string& default_value) const {
    return InputWidget::get_line(message, default_value);
}

bool TerminalUI::confirm(const std::string& message) const {
    return InputWidget::get_confirmation(message);
}

int TerminalUI::choose(const std::string& message, const std::vector<std::string>& options) const {
    return InputWidget::get_choice(message, options);
}

void TerminalUI::clear_screen() const {
    if (interactive_mode) {
        TerminalCapabilities::clear_screen();
    }
}

// InputWidget implementation
std::string InputWidget::get_line(const std::string& prompt, const std::string& default_value) {
    if (!prompt.empty()) {
        std::cout << prompt;
        if (!default_value.empty()) {
            std::cout << " [" << default_value << "]";
        }
        std::cout << ": ";
    }
    
    std::string input;
    std::getline(std::cin, input);
    
    return input.empty() ? default_value : input;
}

bool InputWidget::get_confirmation(const std::string& prompt) {
    std::cout << prompt;
    std::string input;
    std::getline(std::cin, input);
    
    if (input.empty()) return false;
    
    char first = std::tolower(input[0]);
    return first == 'y' || first == '1';
}

int InputWidget::get_choice(const std::string& prompt, const std::vector<std::string>& options) {
    std::cout << prompt << std::endl;
    
    for (size_t i = 0; i < options.size(); ++i) {
        std::cout << "  " << (i + 1) << ". " << options[i] << std::endl;
    }
    
    while (true) {
        std::cout << "Enter choice (1-" << options.size() << "): ";
        std::string input;
        std::getline(std::cin, input);
        
        try {
            int choice = std::stoi(input);
            if (choice >= 1 && choice <= static_cast<int>(options.size())) {
                return choice - 1;  // Return 0-based index
            }
        } catch (const std::exception&) {
            // Invalid input, continue loop
        }
        
        std::cout << "Invalid choice. Please try again." << std::endl;
    }
}

} // namespace ui
} // namespace svcs