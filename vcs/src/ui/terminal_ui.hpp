#ifndef TERMINAL_UI_HPP
#define TERMINAL_UI_HPP

#include <string>
#include <vector>
#include <memory>
#include <functional>
#include <map>
#include <chrono>

namespace svcs {
namespace ui {

// Color codes for terminal output
enum class Color {
    RESET = 0,
    BLACK = 30,
    RED = 31,
    GREEN = 32,
    YELLOW = 33,
    BLUE = 34,
    MAGENTA = 35,
    CYAN = 36,
    WHITE = 37,
    BRIGHT_BLACK = 90,
    BRIGHT_RED = 91,
    BRIGHT_GREEN = 92,
    BRIGHT_YELLOW = 93,
    BRIGHT_BLUE = 94,
    BRIGHT_MAGENTA = 95,
    BRIGHT_CYAN = 96,
    BRIGHT_WHITE = 97
};

// Text styles
enum class Style {
    NORMAL = 0,
    BOLD = 1,
    DIM = 2,
    ITALIC = 3,
    UNDERLINE = 4,
    BLINK = 5,
    REVERSE = 7,
    STRIKETHROUGH = 9
};

// Terminal capabilities
class TerminalCapabilities {
public:
    static bool supports_color();
    static bool supports_unicode();
    static std::pair<int, int> get_terminal_size();
    static bool is_interactive();
    static void enable_raw_mode();
    static void disable_raw_mode();
    static void clear_screen();
    static void move_cursor(int row, int col);
    static void hide_cursor();
    static void show_cursor();
};

// Styled text class
class StyledText {
private:
    std::string text;
    Color foreground = Color::RESET;
    Color background = Color::RESET;
    Style style = Style::NORMAL;
    
public:
    StyledText(const std::string& text);
    StyledText(const std::string& text, Color fg);
    StyledText(const std::string& text, Color fg, Color bg);
    StyledText(const std::string& text, Color fg, Style style);
    StyledText(const std::string& text, Color fg, Color bg, Style style);
    
    std::string render() const;
    size_t length() const { return text.length(); }
    
    // Fluent interface
    StyledText& fg(Color color) { foreground = color; return *this; }
    StyledText& bg(Color color) { background = color; return *this; }
    StyledText& bold() { style = Style::BOLD; return *this; }
    StyledText& italic() { style = Style::ITALIC; return *this; }
    StyledText& underline() { style = Style::UNDERLINE; return *this; }
};

// Progress bar widget
class ProgressBar {
private:
    int width;
    char fill_char;
    char empty_char;
    std::string prefix;
    std::string suffix;
    Color bar_color;
    
public:
    ProgressBar(int width = 50, char fill = '█', char empty = '░');
    
    void set_prefix(const std::string& text) { prefix = text; }
    void set_suffix(const std::string& text) { suffix = text; }
    void set_color(Color color) { bar_color = color; }
    
    std::string render(double progress) const;  // progress: 0.0 to 1.0
};

// Table widget
class Table {
public:
    struct Column {
        std::string header;
        int width = -1;  // -1 = auto-size
        enum Alignment { LEFT, CENTER, RIGHT } alignment = LEFT;
        Color header_color = Color::BRIGHT_WHITE;
        Style header_style = Style::BOLD;
    };
    
    struct Cell {
        std::string content;
        Color color = Color::RESET;
        Style style = Style::NORMAL;
    };
    
private:
    std::vector<Column> columns;
    std::vector<std::vector<Cell>> rows;
    bool show_headers = true;
    bool show_borders = true;
    char border_char = '│';
    char header_separator = '─';
    
public:
    Table(const std::vector<Column>& cols);
    
    void add_row(const std::vector<std::string>& row_data);
    void add_row(const std::vector<Cell>& row_cells);
    void set_show_headers(bool show) { show_headers = show; }
    void set_show_borders(bool show) { show_borders = show; }
    
    std::string render() const;
    void print() const;
    
private:
    std::vector<int> calculate_column_widths() const;
    std::string render_separator(const std::vector<int>& widths) const;
    std::string render_row(const std::vector<Cell>& row, const std::vector<int>& widths) const;
};

// Interactive menu system
class Menu {
public:
    struct MenuItem {
        std::string text;
        std::string description;
        std::function<void()> action;
        bool enabled = true;
        char hotkey = '\0';
    };
    
private:
    std::string title;
    std::vector<MenuItem> items;
    int selected_index = 0;
    bool show_descriptions = true;
    Color selected_color = Color::BRIGHT_BLUE;
    Color disabled_color = Color::BRIGHT_BLACK;
    
public:
    Menu(const std::string& title);
    
    void add_item(const MenuItem& item);
    void add_separator();
    void set_show_descriptions(bool show) { show_descriptions = show; }
    
    int show();  // Returns selected index, -1 if cancelled
    void run();  // Runs the menu loop with actions
    
private:
    void render() const;
    char get_key() const;
    void handle_key(char key);
};

// Status line widget
class StatusLine {
private:
    std::map<std::string, std::string> sections;
    Color background_color = Color::BLUE;
    Color text_color = Color::WHITE;
    
public:
    void set_section(const std::string& name, const std::string& content);
    void remove_section(const std::string& name);
    void set_colors(Color bg, Color fg);
    
    std::string render() const;
    void print() const;
};

// Pager for long output
class Pager {
private:
    std::vector<std::string> lines;
    int current_line = 0;
    int lines_per_page;
    bool show_line_numbers = false;
    
public:
    Pager(const std::vector<std::string>& content);
    Pager(const std::string& content);
    
    void set_show_line_numbers(bool show) { show_line_numbers = show; }
    void show();  // Interactive paging
    
private:
    void render_page() const;
    void show_help() const;
};

// Spinner for long operations
class Spinner {
private:
    std::vector<std::string> frames;
    std::string message;
    std::chrono::milliseconds delay;
    bool running = false;
    
public:
    Spinner(const std::string& msg = "Loading...", 
           const std::vector<std::string>& spinner_frames = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"});
    
    void start();
    void stop();
    void set_message(const std::string& msg) { message = msg; }
    
private:
    void spin();
};

// Input widgets
class InputWidget {
public:
    static std::string get_line(const std::string& prompt = "", const std::string& default_value = "");
    static std::string get_password(const std::string& prompt = "Password: ");
    static bool get_confirmation(const std::string& prompt = "Continue? (y/N): ");
    static int get_choice(const std::string& prompt, const std::vector<std::string>& options);
};

// Diff viewer
class DiffViewer {
public:
    struct DiffLine {
        enum Type { CONTEXT, ADDED, REMOVED, HEADER } type;
        std::string content;
        int old_line_num = -1;
        int new_line_num = -1;
    };
    
private:
    std::vector<DiffLine> lines;
    bool show_line_numbers = true;
    bool syntax_highlighting = false;
    
public:
    DiffViewer(const std::vector<DiffLine>& diff_lines);
    
    void set_show_line_numbers(bool show) { show_line_numbers = show; }
    void set_syntax_highlighting(bool enable) { syntax_highlighting = enable; }
    
    void show() const;
    std::string render() const;
    
private:
    StyledText format_line(const DiffLine& line) const;
    Color get_line_color(DiffLine::Type type) const;
};

// Log viewer with filtering
class LogViewer {
public:
    struct LogEntry {
        std::string hash;
        std::string message;
        std::string author;
        std::chrono::system_clock::time_point timestamp;
        std::vector<std::string> tags;
        bool is_merge = false;
    };
    
    struct FilterOptions {
        std::string author_filter;
        std::string message_filter;
        std::chrono::system_clock::time_point since;
        std::chrono::system_clock::time_point until;
        bool show_merges = true;
        int max_entries = -1;
    };
    
private:
    std::vector<LogEntry> entries;
    FilterOptions filters;
    bool show_graph = false;
    bool show_stats = false;
    
public:
    LogViewer(const std::vector<LogEntry>& log_entries);
    
    void set_filters(const FilterOptions& opts) { filters = opts; }
    void set_show_graph(bool show) { show_graph = show; }
    void set_show_stats(bool show) { show_stats = show; }
    
    void show() const;
    std::string render() const;
    
private:
    std::vector<LogEntry> apply_filters() const;
    std::string format_entry(const LogEntry& entry) const;
    std::string generate_graph_line(const LogEntry& entry) const;
};

// Main UI controller
class TerminalUI {
private:
    bool color_enabled;
    bool interactive_mode;
    int terminal_width;
    int terminal_height;
    
public:
    TerminalUI();
    
    // Configuration
    void set_color_enabled(bool enabled) { color_enabled = enabled; }
    void set_interactive_mode(bool interactive) { interactive_mode = interactive; }
    
    // Output methods
    void print(const std::string& text) const;
    void print_line(const std::string& text = "") const;
    void print_error(const std::string& text) const;
    void print_warning(const std::string& text) const;
    void print_success(const std::string& text) const;
    void print_info(const std::string& text) const;
    
    // Styled output
    void print_styled(const StyledText& text) const;
    void print_header(const std::string& text) const;
    void print_separator(char ch = '─') const;
    
    // Interactive elements
    std::string prompt(const std::string& message, const std::string& default_value = "") const;
    bool confirm(const std::string& message) const;
    int choose(const std::string& message, const std::vector<std::string>& options) const;
    
    // Progress indication
    void show_progress(const std::string& message, double progress) const;
    void show_spinner(const std::string& message) const;
    void hide_spinner() const;
    
    // Paging and viewing
    void page_text(const std::string& content) const;
    void show_diff(const std::vector<DiffViewer::DiffLine>& diff) const;
    void show_log(const std::vector<LogViewer::LogEntry>& entries) const;
    void show_table(const Table& table) const;
    
    // Utility
    void clear_screen() const;
    void pause(const std::string& message = "Press any key to continue...") const;
    
private:
    StyledText colorize_by_type(const std::string& text, const std::string& type) const;
};

} // namespace ui
} // namespace svcs

#endif // TERMINAL_UI_HPP