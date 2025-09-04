#pragma once

#include <string>
#include <vector>
#include <map>
#include <chrono>
#include <memory>

namespace svcs {

// Repository analytics and insights
class RepositoryAnalytics {
public:
    struct CommitStats {
        int total_commits = 0;
        int commits_last_week = 0;
        int commits_last_month = 0;
        std::map<std::string, int> commits_by_author;
        std::map<std::string, int> commits_by_day_of_week;
        std::map<std::string, int> commits_by_hour;
        double average_commits_per_day = 0.0;
    };
    
    struct FileStats {
        int total_files = 0;
        int active_files = 0; // Modified in last 30 days
        std::map<std::string, int> files_by_extension;
        std::map<std::string, int> lines_by_language;
        std::vector<std::pair<std::string, int>> most_modified_files;
        std::vector<std::pair<std::string, int>> largest_files;
    };
    
    struct AuthorStats {
        std::map<std::string, int> commits_by_author;
        std::map<std::string, int> lines_added_by_author;
        std::map<std::string, int> lines_removed_by_author;
        std::map<std::string, std::vector<std::string>> files_by_author;
        std::map<std::string, std::chrono::system_clock::time_point> last_activity_by_author;
    };
    
    struct BranchStats {
        int total_branches = 0;
        int active_branches = 0; // Modified in last 30 days
        std::map<std::string, int> commits_by_branch;
        std::map<std::string, std::chrono::system_clock::time_point> last_activity_by_branch;
        std::vector<std::string> stale_branches; // No activity in 90+ days
        std::vector<std::string> merged_branches;
    };
    
    struct CodeQualityMetrics {
        double average_function_length = 0.0;
        double average_file_length = 0.0;
        int total_functions = 0;
        int total_classes = 0;
        std::map<std::string, int> complexity_by_file;
        std::vector<std::string> files_needing_refactoring;
        double test_coverage_percentage = 0.0;
        int total_test_files = 0;
    };
    
    struct RepositoryHealth {
        double health_score = 0.0; // 0-100
        std::vector<std::string> issues;
        std::vector<std::string> recommendations;
        
        struct {
            bool has_readme = false;
            bool has_license = false;
            bool has_gitignore = false;
            bool has_ci_config = false;
            bool has_tests = false;
        } project_structure;
        
        struct {
            int days_since_last_commit = 0;
            int open_merge_conflicts = 0;
            int uncommitted_changes = 0;
            int untracked_files = 0;
        } maintenance_status;
    };
    
    // Main analytics functions
    static CommitStats analyze_commits(const std::string& repo_path, 
                                     int days_back = 365);
    
    static FileStats analyze_files(const std::string& repo_path);
    
    static AuthorStats analyze_authors(const std::string& repo_path,
                                     int days_back = 365);
    
    static BranchStats analyze_branches(const std::string& repo_path);
    
    static CodeQualityMetrics analyze_code_quality(const std::string& repo_path);
    
    static RepositoryHealth assess_repository_health(const std::string& repo_path);
    
    // Trend analysis
    struct TrendData {
        std::vector<std::pair<std::chrono::system_clock::time_point, int>> data_points;
        double trend_slope = 0.0; // Positive = increasing, negative = decreasing
        std::string trend_description;
    };
    
    static TrendData analyze_commit_trends(const std::string& repo_path, 
                                         int days_back = 90);
    
    static TrendData analyze_code_growth_trends(const std::string& repo_path,
                                              int days_back = 90);
    
    static TrendData analyze_contributor_trends(const std::string& repo_path,
                                              int days_back = 90);
    
    // Productivity metrics
    struct ProductivityMetrics {
        double commits_per_day = 0.0;
        double lines_per_commit = 0.0;
        double files_per_commit = 0.0;
        std::map<std::string, double> productivity_by_author;
        std::vector<std::pair<std::string, int>> most_productive_days;
        std::vector<std::pair<int, int>> most_productive_hours; // hour, commit_count
    };
    
    static ProductivityMetrics analyze_productivity(const std::string& repo_path,
                                                  int days_back = 30);
    
    // Collaboration metrics
    struct CollaborationMetrics {
        int total_contributors = 0;
        int active_contributors = 0; // Active in last 30 days
        double average_contributors_per_file = 0.0;
        std::map<std::string, std::vector<std::string>> file_ownership; // file -> authors
        std::vector<std::pair<std::string, std::string>> collaboration_pairs; // frequent co-authors
        std::map<std::string, int> merge_conflicts_by_author_pair;
    };
    
    static CollaborationMetrics analyze_collaboration(const std::string& repo_path,
                                                    int days_back = 90);
    
    // Risk assessment
    struct RiskAssessment {
        std::vector<std::string> high_risk_files; // Files with many recent changes
        std::vector<std::string> single_contributor_files; // Bus factor = 1
        std::vector<std::string> complex_files; // High cyclomatic complexity
        std::vector<std::string> large_files; // Files that might need splitting
        std::vector<std::string> stale_branches; // Branches that might be abandoned
        double overall_risk_score = 0.0; // 0-100, higher = more risky
    };
    
    static RiskAssessment assess_risks(const std::string& repo_path);
    
    // Report generation
    static std::string generate_analytics_report(const std::string& repo_path,
                                                bool include_trends = true,
                                                bool include_recommendations = true);
    
    static std::string generate_health_report(const std::string& repo_path);
    
    static std::string generate_productivity_report(const std::string& repo_path,
                                                  int days_back = 30);
    
    // Export capabilities
    static bool export_analytics_json(const std::string& repo_path,
                                     const std::string& output_file);
    
    static bool export_analytics_csv(const std::string& repo_path,
                                   const std::string& output_file);
    
private:
    // Helper functions
    static std::vector<std::string> get_commit_hashes(const std::string& repo_path,
                                                    int days_back = 365);
    
    static std::map<std::string, std::string> get_commit_info(const std::string& repo_path,
                                                            const std::string& commit_hash);
    
    static std::vector<std::string> get_changed_files(const std::string& repo_path,
                                                    const std::string& commit_hash);
    
    static int count_lines_in_file(const std::string& file_path);
    
    static std::string detect_file_language(const std::string& file_path);
    
    static int calculate_cyclomatic_complexity(const std::string& file_path);
    
    static bool is_test_file(const std::string& file_path);
    
    static double calculate_trend_slope(const std::vector<std::pair<std::chrono::system_clock::time_point, int>>& data);
};

// Real-time analytics dashboard
class AnalyticsDashboard {
public:
    struct DashboardData {
        RepositoryAnalytics::CommitStats commit_stats;
        RepositoryAnalytics::FileStats file_stats;
        RepositoryAnalytics::AuthorStats author_stats;
        RepositoryAnalytics::RepositoryHealth health;
        std::chrono::system_clock::time_point last_updated;
    };
    
    // Dashboard management
    static void start_dashboard(const std::string& repo_path, int update_interval_seconds = 300);
    static void stop_dashboard();
    static DashboardData get_current_data();
    static void refresh_data();
    
    // Real-time notifications
    static void set_notification_callback(std::function<void(const std::string&)> callback);
    
    // Alerts
    struct Alert {
        std::string type; // "warning", "error", "info"
        std::string message;
        std::chrono::system_clock::time_point timestamp;
        bool acknowledged = false;
    };
    
    static std::vector<Alert> get_active_alerts();
    static void acknowledge_alert(const std::string& alert_id);
    
private:
    static std::thread dashboard_thread_;
    static std::atomic<bool> dashboard_running_;
    static DashboardData current_data_;
    static std::mutex data_mutex_;
    static std::function<void(const std::string&)> notification_callback_;
    
    static void dashboard_loop(const std::string& repo_path, int update_interval);
    static void check_for_alerts(const DashboardData& data);
};

// Performance analytics
class PerformanceAnalytics {
public:
    struct OperationMetrics {
        std::string operation_name;
        std::chrono::milliseconds average_duration{0};
        std::chrono::milliseconds max_duration{0};
        std::chrono::milliseconds min_duration{0};
        int execution_count = 0;
        size_t average_memory_usage = 0;
        size_t max_memory_usage = 0;
    };
    
    static std::vector<OperationMetrics> get_operation_metrics(int days_back = 7);
    
    static std::string generate_performance_report(int days_back = 7);
    
    static std::vector<std::string> identify_performance_bottlenecks();
    
    static std::map<std::string, double> calculate_performance_trends(int days_back = 30);
    
private:
    static void collect_performance_data();
    static std::vector<OperationMetrics> load_historical_metrics(int days_back);
};

// Custom analytics queries
class AnalyticsQueryEngine {
public:
    struct QueryResult {
        std::vector<std::string> column_names;
        std::vector<std::vector<std::string>> rows;
        std::string query_execution_time;
    };
    
    // SQL-like query interface for repository data
    static QueryResult execute_query(const std::string& repo_path, 
                                   const std::string& query);
    
    // Predefined useful queries
    static QueryResult get_top_contributors(const std::string& repo_path, int limit = 10);
    
    static QueryResult get_files_by_change_frequency(const std::string& repo_path, 
                                                   int days_back = 90);
    
    static QueryResult get_commit_activity_by_time(const std::string& repo_path,
                                                 const std::string& time_grouping = "day");
    
    static QueryResult get_language_distribution(const std::string& repo_path);
    
    static QueryResult get_branch_activity(const std::string& repo_path, 
                                         int days_back = 30);
    
private:
    static bool validate_query(const std::string& query);
    static std::vector<std::string> parse_query_tokens(const std::string& query);
};

}