#include "../src/core/repository_analytics.hpp"
#include <iostream>
#include <iomanip>
#include <filesystem>

using namespace svcs;

void print_usage() {
    std::cout << "SVCS Repository Analytics Tool\n\n";
    std::cout << "Usage: svcs_analytics [options] <repository_path>\n\n";
    std::cout << "Options:\n";
    std::cout << "  --commits             Analyze commit statistics\n";
    std::cout << "  --files               Analyze file statistics\n";
    std::cout << "  --authors             Analyze author contributions\n";
    std::cout << "  --branches            Analyze branch activity\n";
    std::cout << "  --quality             Analyze code quality metrics\n";
    std::cout << "  --health              Assess repository health\n";
    std::cout << "  --trends              Analyze trends over time\n";
    std::cout << "  --productivity        Analyze productivity metrics\n";
    std::cout << "  --collaboration       Analyze collaboration patterns\n";
    std::cout << "  --risks               Assess repository risks\n";
    std::cout << "  --all                 Run all analyses\n";
    std::cout << "  --days <n>            Analyze last n days (default: 90)\n";
    std::cout << "  --export-json <file>  Export results to JSON\n";
    std::cout << "  --export-csv <file>   Export results to CSV\n";
    std::cout << "  --report              Generate comprehensive report\n";
    std::cout << "  --help                Show this help message\n\n";
    std::cout << "Examples:\n";
    std::cout << "  svcs_analytics --all /path/to/repo\n";
    std::cout << "  svcs_analytics --commits --authors --days 30 /path/to/repo\n";
    std::cout << "  svcs_analytics --health --report /path/to/repo\n";
}

void print_commit_stats(const RepositoryAnalytics::CommitStats& stats) {
    std::cout << "=== Commit Statistics ===\n\n";
    std::cout << "Total commits: " << stats.total_commits << "\n";
    std::cout << "Commits last week: " << stats.commits_last_week << "\n";
    std::cout << "Commits last month: " << stats.commits_last_month << "\n";
    std::cout << "Average commits per day: " << std::fixed << std::setprecision(2) 
              << stats.average_commits_per_day << "\n\n";
    
    if (!stats.commits_by_author.empty()) {
        std::cout << "Top contributors:\n";
        std::vector<std::pair<std::string, int>> sorted_authors(
            stats.commits_by_author.begin(), stats.commits_by_author.end());
        std::sort(sorted_authors.begin(), sorted_authors.end(),
                 [](const auto& a, const auto& b) { return a.second > b.second; });
        
        int count = 0;
        for (const auto& [author, commits] : sorted_authors) {
            if (count++ >= 10) break; // Top 10
            std::cout << "  " << std::setw(30) << author 
                     << ": " << std::setw(5) << commits << " commits\n";
        }
        std::cout << "\n";
    }
    
    if (!stats.commits_by_day_of_week.empty()) {
        std::cout << "Commits by day of week:\n";
        const std::vector<std::string> days = {"Sunday", "Monday", "Tuesday", "Wednesday", 
                                             "Thursday", "Friday", "Saturday"};
        for (int i = 0; i < 7; i++) {
            std::string day = std::to_string(i);
            auto it = stats.commits_by_day_of_week.find(day);
            int commits = (it != stats.commits_by_day_of_week.end()) ? it->second : 0;
            std::cout << "  " << std::setw(10) << days[i] 
                     << ": " << std::setw(5) << commits << " commits\n";
        }
        std::cout << "\n";
    }
}

void print_file_stats(const RepositoryAnalytics::FileStats& stats) {
    std::cout << "=== File Statistics ===\n\n";
    std::cout << "Total files: " << stats.total_files << "\n";
    std::cout << "Active files (modified in last 30 days): " << stats.active_files << "\n\n";
    
    if (!stats.files_by_extension.empty()) {
        std::cout << "Files by extension:\n";
        std::vector<std::pair<std::string, int>> sorted_extensions(
            stats.files_by_extension.begin(), stats.files_by_extension.end());
        std::sort(sorted_extensions.begin(), sorted_extensions.end(),
                 [](const auto& a, const auto& b) { return a.second > b.second; });
        
        for (const auto& [ext, count] : sorted_extensions) {
            std::cout << "  " << std::setw(10) << ext 
                     << ": " << std::setw(5) << count << " files\n";
        }
        std::cout << "\n";
    }
    
    if (!stats.lines_by_language.empty()) {
        std::cout << "Lines of code by language:\n";
        std::vector<std::pair<std::string, int>> sorted_languages(
            stats.lines_by_language.begin(), stats.lines_by_language.end());
        std::sort(sorted_languages.begin(), sorted_languages.end(),
                 [](const auto& a, const auto& b) { return a.second > b.second; });
        
        for (const auto& [lang, lines] : sorted_languages) {
            std::cout << "  " << std::setw(15) << lang 
                     << ": " << std::setw(8) << lines << " lines\n";
        }
        std::cout << "\n";
    }
    
    if (!stats.most_modified_files.empty()) {
        std::cout << "Most frequently modified files:\n";
        int count = 0;
        for (const auto& [file, modifications] : stats.most_modified_files) {
            if (count++ >= 10) break; // Top 10
            std::cout << "  " << std::setw(40) << file 
                     << ": " << std::setw(3) << modifications << " modifications\n";
        }
        std::cout << "\n";
    }
}

void print_author_stats(const RepositoryAnalytics::AuthorStats& stats) {
    std::cout << "=== Author Statistics ===\n\n";
    
    if (!stats.commits_by_author.empty()) {
        std::cout << "Author contributions:\n";
        std::cout << std::setw(25) << "Author" 
                 << std::setw(10) << "Commits"
                 << std::setw(12) << "Lines Added"
                 << std::setw(12) << "Lines Removed" << "\n";
        std::cout << std::string(59, '-') << "\n";
        
        std::vector<std::pair<std::string, int>> sorted_authors(
            stats.commits_by_author.begin(), stats.commits_by_author.end());
        std::sort(sorted_authors.begin(), sorted_authors.end(),
                 [](const auto& a, const auto& b) { return a.second > b.second; });
        
        for (const auto& [author, commits] : sorted_authors) {
            int lines_added = 0, lines_removed = 0;
            
            auto added_it = stats.lines_added_by_author.find(author);
            if (added_it != stats.lines_added_by_author.end()) {
                lines_added = added_it->second;
            }
            
            auto removed_it = stats.lines_removed_by_author.find(author);
            if (removed_it != stats.lines_removed_by_author.end()) {
                lines_removed = removed_it->second;
            }
            
            std::cout << std::setw(25) << author
                     << std::setw(10) << commits
                     << std::setw(12) << lines_added
                     << std::setw(12) << lines_removed << "\n";
        }
        std::cout << "\n";
    }
}

void print_health_assessment(const RepositoryAnalytics::RepositoryHealth& health) {
    std::cout << "=== Repository Health Assessment ===\n\n";
    std::cout << "Overall Health Score: " << std::fixed << std::setprecision(1) 
              << health.health_score << "/100\n\n";
    
    std::cout << "Project Structure:\n";
    std::cout << "  README file: " << (health.project_structure.has_readme ? "✓" : "✗") << "\n";
    std::cout << "  License file: " << (health.project_structure.has_license ? "✓" : "✗") << "\n";
    std::cout << "  .gitignore file: " << (health.project_structure.has_gitignore ? "✓" : "✗") << "\n";
    std::cout << "  CI configuration: " << (health.project_structure.has_ci_config ? "✓" : "✗") << "\n";
    std::cout << "  Test files: " << (health.project_structure.has_tests ? "✓" : "✗") << "\n\n";
    
    std::cout << "Maintenance Status:\n";
    std::cout << "  Days since last commit: " << health.maintenance_status.days_since_last_commit << "\n";
    std::cout << "  Open merge conflicts: " << health.maintenance_status.open_merge_conflicts << "\n";
    std::cout << "  Uncommitted changes: " << health.maintenance_status.uncommitted_changes << "\n";
    std::cout << "  Untracked files: " << health.maintenance_status.untracked_files << "\n\n";
    
    if (!health.issues.empty()) {
        std::cout << "Issues:\n";
        for (const auto& issue : health.issues) {
            std::cout << "  ⚠️  " << issue << "\n";
        }
        std::cout << "\n";
    }
    
    if (!health.recommendations.empty()) {
        std::cout << "Recommendations:\n";
        for (const auto& rec : health.recommendations) {
            std::cout << "  💡 " << rec << "\n";
        }
        std::cout << "\n";
    }
}

void print_productivity_metrics(const RepositoryAnalytics::ProductivityMetrics& metrics) {
    std::cout << "=== Productivity Metrics ===\n\n";
    std::cout << "Commits per day: " << std::fixed << std::setprecision(2) 
              << metrics.commits_per_day << "\n";
    std::cout << "Lines per commit: " << std::fixed << std::setprecision(1) 
              << metrics.lines_per_commit << "\n";
    std::cout << "Files per commit: " << std::fixed << std::setprecision(1) 
              << metrics.files_per_commit << "\n\n";
    
    if (!metrics.productivity_by_author.empty()) {
        std::cout << "Productivity by author (commits/day):\n";
        std::vector<std::pair<std::string, double>> sorted_productivity(
            metrics.productivity_by_author.begin(), metrics.productivity_by_author.end());
        std::sort(sorted_productivity.begin(), sorted_productivity.end(),
                 [](const auto& a, const auto& b) { return a.second > b.second; });
        
        for (const auto& [author, productivity] : sorted_productivity) {
            std::cout << "  " << std::setw(25) << author 
                     << ": " << std::fixed << std::setprecision(2) << productivity << "\n";
        }
        std::cout << "\n";
    }
    
    if (!metrics.most_productive_hours.empty()) {
        std::cout << "Most productive hours:\n";
        for (const auto& [hour, commits] : metrics.most_productive_hours) {
            std::cout << "  " << std::setw(2) << hour << ":00"
                     << ": " << std::setw(3) << commits << " commits\n";
        }
        std::cout << "\n";
    }
}

void print_trend_data(const std::string& title, const RepositoryAnalytics::TrendData& trend) {
    std::cout << "=== " << title << " ===\n\n";
    std::cout << "Trend: " << trend.trend_description << "\n";
    std::cout << "Slope: " << std::fixed << std::setprecision(4) << trend.trend_slope << "\n";
    std::cout << "Data points: " << trend.data_points.size() << "\n\n";
    
    if (!trend.data_points.empty() && trend.data_points.size() <= 30) {
        std::cout << "Recent data points:\n";
        for (const auto& [timestamp, value] : trend.data_points) {
            auto time_t = std::chrono::system_clock::to_time_t(timestamp);
            std::cout << "  " << std::put_time(std::localtime(&time_t), "%Y-%m-%d")
                     << ": " << value << "\n";
        }
        std::cout << "\n";
    }
}

int main(int argc, char* argv[]) {
    if (argc < 2) {
        print_usage();
        return 1;
    }
    
    std::string repo_path;
    bool analyze_commits = false;
    bool analyze_files = false;
    bool analyze_authors = false;
    bool analyze_branches = false;
    bool analyze_quality = false;
    bool analyze_health = false;
    bool analyze_trends = false;
    bool analyze_productivity = false;
    bool analyze_collaboration = false;
    bool analyze_risks = false;
    bool analyze_all = false;
    bool generate_report = false;
    int days_back = 90;
    std::string export_json;
    std::string export_csv;
    
    // Parse command line arguments
    for (int i = 1; i < argc; i++) {
        std::string arg = argv[i];
        
        if (arg == "--help") {
            print_usage();
            return 0;
        } else if (arg == "--commits") {
            analyze_commits = true;
        } else if (arg == "--files") {
            analyze_files = true;
        } else if (arg == "--authors") {
            analyze_authors = true;
        } else if (arg == "--branches") {
            analyze_branches = true;
        } else if (arg == "--quality") {
            analyze_quality = true;
        } else if (arg == "--health") {
            analyze_health = true;
        } else if (arg == "--trends") {
            analyze_trends = true;
        } else if (arg == "--productivity") {
            analyze_productivity = true;
        } else if (arg == "--collaboration") {
            analyze_collaboration = true;
        } else if (arg == "--risks") {
            analyze_risks = true;
        } else if (arg == "--all") {
            analyze_all = true;
        } else if (arg == "--report") {
            generate_report = true;
        } else if (arg == "--days" && i + 1 < argc) {
            days_back = std::stoi(argv[++i]);
        } else if (arg == "--export-json" && i + 1 < argc) {
            export_json = argv[++i];
        } else if (arg == "--export-csv" && i + 1 < argc) {
            export_csv = argv[++i];
        } else if (arg[0] != '-') {
            repo_path = arg;
        } else {
            std::cerr << "Unknown option: " << arg << std::endl;
            print_usage();
            return 1;
        }
    }
    
    if (repo_path.empty()) {
        std::cerr << "Error: Repository path is required\n";
        print_usage();
        return 1;
    }
    
    if (!std::filesystem::exists(repo_path)) {
        std::cerr << "Error: Repository path does not exist: " << repo_path << std::endl;
        return 1;
    }
    
    std::cout << "SVCS Repository Analytics Tool v2.0.0\n";
    std::cout << "======================================\n\n";
    std::cout << "Analyzing repository: " << repo_path << "\n";
    std::cout << "Analysis period: last " << days_back << " days\n\n";
    
    if (analyze_all) {
        analyze_commits = analyze_files = analyze_authors = analyze_branches = 
        analyze_quality = analyze_health = analyze_trends = analyze_productivity = 
        analyze_collaboration = analyze_risks = true;
    }
    
    try {
        if (analyze_commits) {
            auto stats = RepositoryAnalytics::analyze_commits(repo_path, days_back);
            print_commit_stats(stats);
        }
        
        if (analyze_files) {
            auto stats = RepositoryAnalytics::analyze_files(repo_path);
            print_file_stats(stats);
        }
        
        if (analyze_authors) {
            auto stats = RepositoryAnalytics::analyze_authors(repo_path, days_back);
            print_author_stats(stats);
        }
        
        if (analyze_branches) {
            auto stats = RepositoryAnalytics::analyze_branches(repo_path);
            std::cout << "=== Branch Statistics ===\n\n";
            std::cout << "Total branches: " << stats.total_branches << "\n";
            std::cout << "Active branches: " << stats.active_branches << "\n";
            std::cout << "Stale branches: " << stats.stale_branches.size() << "\n";
            std::cout << "Merged branches: " << stats.merged_branches.size() << "\n\n";
        }
        
        if (analyze_quality) {
            auto metrics = RepositoryAnalytics::analyze_code_quality(repo_path);
            std::cout << "=== Code Quality Metrics ===\n\n";
            std::cout << "Average function length: " << std::fixed << std::setprecision(1) 
                     << metrics.average_function_length << " lines\n";
            std::cout << "Average file length: " << std::fixed << std::setprecision(1) 
                     << metrics.average_file_length << " lines\n";
            std::cout << "Total functions: " << metrics.total_functions << "\n";
            std::cout << "Total classes: " << metrics.total_classes << "\n";
            std::cout << "Test coverage: " << std::fixed << std::setprecision(1) 
                     << metrics.test_coverage_percentage << "%\n";
            std::cout << "Total test files: " << metrics.total_test_files << "\n\n";
        }
        
        if (analyze_health) {
            auto health = RepositoryAnalytics::assess_repository_health(repo_path);
            print_health_assessment(health);
        }
        
        if (analyze_trends) {
            auto commit_trends = RepositoryAnalytics::analyze_commit_trends(repo_path, days_back);
            print_trend_data("Commit Trends", commit_trends);
            
            auto growth_trends = RepositoryAnalytics::analyze_code_growth_trends(repo_path, days_back);
            print_trend_data("Code Growth Trends", growth_trends);
        }
        
        if (analyze_productivity) {
            auto metrics = RepositoryAnalytics::analyze_productivity(repo_path, days_back);
            print_productivity_metrics(metrics);
        }
        
        if (analyze_collaboration) {
            auto metrics = RepositoryAnalytics::analyze_collaboration(repo_path, days_back);
            std::cout << "=== Collaboration Metrics ===\n\n";
            std::cout << "Total contributors: " << metrics.total_contributors << "\n";
            std::cout << "Active contributors: " << metrics.active_contributors << "\n";
            std::cout << "Average contributors per file: " << std::fixed << std::setprecision(2) 
                     << metrics.average_contributors_per_file << "\n\n";
        }
        
        if (analyze_risks) {
            auto risks = RepositoryAnalytics::assess_risks(repo_path);
            std::cout << "=== Risk Assessment ===\n\n";
            std::cout << "Overall risk score: " << std::fixed << std::setprecision(1) 
                     << risks.overall_risk_score << "/100\n\n";
            
            if (!risks.high_risk_files.empty()) {
                std::cout << "High-risk files:\n";
                for (const auto& file : risks.high_risk_files) {
                    std::cout << "  ⚠️  " << file << "\n";
                }
                std::cout << "\n";
            }
            
            if (!risks.single_contributor_files.empty()) {
                std::cout << "Single contributor files (bus factor = 1):\n";
                for (const auto& file : risks.single_contributor_files) {
                    std::cout << "  🚌 " << file << "\n";
                }
                std::cout << "\n";
            }
        }
        
        if (generate_report) {
            std::cout << "=== Comprehensive Report ===\n\n";
            auto report = RepositoryAnalytics::generate_analytics_report(repo_path, true, true);
            std::cout << report << std::endl;
        }
        
        // Export functionality
        if (!export_json.empty()) {
            std::cout << "Exporting analytics to JSON: " << export_json << "\n";
            bool success = RepositoryAnalytics::export_analytics_json(repo_path, export_json);
            if (success) {
                std::cout << "JSON export completed successfully.\n";
            } else {
                std::cout << "JSON export failed.\n";
            }
        }
        
        if (!export_csv.empty()) {
            std::cout << "Exporting analytics to CSV: " << export_csv << "\n";
            bool success = RepositoryAnalytics::export_analytics_csv(repo_path, export_csv);
            if (success) {
                std::cout << "CSV export completed successfully.\n";
            } else {
                std::cout << "CSV export failed.\n";
            }
        }
        
    } catch (const std::exception& e) {
        std::cerr << "Error during analysis: " << e.what() << std::endl;
        return 1;
    }
    
    std::cout << "Analysis completed successfully.\n";
    return 0;
}