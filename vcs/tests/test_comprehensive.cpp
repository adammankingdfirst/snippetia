#include <gtest/gtest.h>
#include "../src/core/performance_monitor.hpp"
#include "../src/core/patch_engine.hpp"
#include "../src/core/smart_merge.hpp"
#include "../src/core/repository_analytics.hpp"
#include "../src/integration/cloud_sync_engine.hpp"
#include <filesystem>
#include <fstream>
#include <thread>
#include <chrono>

using namespace svcs;

class ComprehensiveTest : public ::testing::Test {
protected:
    void SetUp() override {
        // Create test environment
        test_repo_path_ = "test_comprehensive_repo";
        std::filesystem::create_directories(test_repo_path_);
        
        // Initialize repository
        init_test_repository();
        
        // Setup performance monitoring
        auto& monitor = PerformanceMonitor::instance();
        monitor.clear_metrics();
        monitor.set_enabled(true);
    }
    
    void TearDown() override {
        // Cleanup
        std::filesystem::remove_all(test_repo_path_);
        PerformanceMonitor::instance().clear_metrics();
    }
    
    void init_test_repository() {
        // Create .svcs directory structure
        std::filesystem::create_directories(test_repo_path_ + "/.svcs/objects");
        std::filesystem::create_directories(test_repo_path_ + "/.svcs/refs/heads");
        std::filesystem::create_directories(test_repo_path_ + "/.svcs/refs/tags");
        
        // Create initial files
        create_test_files();
    }
    
    void create_test_files() {
        // Create various file types for comprehensive testing
        
        // C++ source file
        std::ofstream cpp_file(test_repo_path_ + "/main.cpp");
        cpp_file << "#include <iostream>\n\n";
        cpp_file << "int main() {\n";
        cpp_file << "    std::cout << \"Hello, World!\" << std::endl;\n";
        cpp_file << "    return 0;\n";
        cpp_file << "}\n";
        cpp_file.close();
        
        // Header file
        std::ofstream header_file(test_repo_path_ + "/utils.hpp");
        header_file << "#pragma once\n\n";
        header_file << "namespace utils {\n";
        header_file << "    void print_message(const std::string& msg);\n";
        header_file << "    int calculate_sum(int a, int b);\n";
        header_file << "}\n";
        header_file.close();
        
        // Python file
        std::ofstream py_file(test_repo_path_ + "/script.py");
        py_file << "#!/usr/bin/env python3\n\n";
        py_file << "def main():\n";
        py_file << "    print('Hello from Python!')\n";
        py_file << "    return 0\n\n";
        py_file << "if __name__ == '__main__':\n";
        py_file << "    main()\n";
        py_file.close();
        
        // Configuration file
        std::ofstream config_file(test_repo_path_ + "/config.json");
        config_file << "{\n";
        config_file << "  \"version\": \"1.0.0\",\n";
        config_file << "  \"name\": \"test-project\",\n";
        config_file << "  \"dependencies\": {\n";
        config_file << "    \"library1\": \"^2.0.0\",\n";
        config_file << "    \"library2\": \"~1.5.0\"\n";
        config_file << "  }\n";
        config_file << "}\n";
        config_file.close();
        
        // README file
        std::ofstream readme_file(test_repo_path_ + "/README.md");
        readme_file << "# Test Project\n\n";
        readme_file << "This is a comprehensive test project for SVCS.\n\n";
        readme_file << "## Features\n\n";
        readme_file << "- Advanced version control\n";
        readme_file << "- Performance monitoring\n";
        readme_file << "- Smart merging\n";
        readme_file << "- Cloud synchronization\n\n";
        readme_file << "## Usage\n\n";
        readme_file << "```bash\n";
        readme_file << "svcs init\n";
        readme_file << "svcs add .\n";
        readme_file << "svcs commit -m \"Initial commit\"\n";
        readme_file << "```\n";
        readme_file.close();
    }
    
    std::string test_repo_path_;
};

// Test 1: End-to-End Repository Operations
TEST_F(ComprehensiveTest, EndToEndRepositoryOperations) {
    PROFILE_OPERATION("end_to_end_operations");
    
    // Initialize repository
    EXPECT_EQ(init_repository(test_repo_path_.c_str()), 0);
    
    // Add files to index
    EXPECT_EQ(add_to_index(test_repo_path_.c_str(), "main.cpp"), 0);
    EXPECT_EQ(add_to_index(test_repo_path_.c_str(), "utils.hpp"), 0);
    EXPECT_EQ(add_to_index(test_repo_path_.c_str(), "script.py"), 0);
    EXPECT_EQ(add_to_index(test_repo_path_.c_str(), "config.json"), 0);
    EXPECT_EQ(add_to_index(test_repo_path_.c_str(), "README.md"), 0);
    
    // Create initial commit
    EXPECT_EQ(create_commit(test_repo_path_.c_str(), "Initial commit", "test@example.com"), 0);
    
    // Create and switch to feature branch
    EXPECT_EQ(create_branch(test_repo_path_.c_str(), "feature/enhancement"), 0);
    EXPECT_EQ(switch_branch(test_repo_path_.c_str(), "feature/enhancement"), 0);
    
    // Modify files
    std::ofstream cpp_file(test_repo_path_ + "/main.cpp", std::ios::app);
    cpp_file << "\n// Added feature enhancement\nvoid new_feature() {\n    // TODO: implement\n}\n";
    cpp_file.close();
    
    // Commit changes
    EXPECT_EQ(add_to_index(test_repo_path_.c_str(), "main.cpp"), 0);
    EXPECT_EQ(create_commit(test_repo_path_.c_str(), "Add new feature", "test@example.com"), 0);
    
    // Switch back to main and merge
    EXPECT_EQ(switch_branch(test_repo_path_.c_str(), "main"), 0);
    EXPECT_EQ(merge_branch(test_repo_path_.c_str(), "feature/enhancement"), 0);
    
    // Verify final state
    auto log = get_commit_log(test_repo_path_.c_str());
    EXPECT_GE(log.size(), 2);
}

// Test 2: Performance Monitoring Integration
TEST_F(ComprehensiveTest, PerformanceMonitoringIntegration) {
    auto& monitor = PerformanceMonitor::instance();
    
    // Perform various operations while monitoring
    {
        PROFILE_OPERATION("repository_initialization");
        init_repository(test_repo_path_.c_str());
    }
    
    {
        PROFILE_OPERATION("file_operations");
        for (int i = 0; i < 10; i++) {
            std::string filename = "test_file_" + std::to_string(i) + ".txt";
            std::ofstream file(test_repo_path_ + "/" + filename);
            file << "Content of file " << i << std::endl;
            file.close();
            
            add_to_index(test_repo_path_.c_str(), filename.c_str());
        }
    }
    
    {
        PROFILE_OPERATION("commit_creation");
        create_commit(test_repo_path_.c_str(), "Add test files", "test@example.com");
    }
    
    // Verify monitoring data
    auto init_metrics = monitor.get_operation_metrics("repository_initialization");
    EXPECT_GT(init_metrics.execution_time.count(), 0);
    
    auto file_metrics = monitor.get_operation_metrics("file_operations");
    EXPECT_GT(file_metrics.execution_time.count(), 0);
    
    auto commit_metrics = monitor.get_operation_metrics("commit_creation");
    EXPECT_GT(commit_metrics.execution_time.count(), 0);
    
    // Generate performance report
    std::string report = monitor.generate_report(true);
    EXPECT_FALSE(report.empty());
    EXPECT_NE(report.find("Performance Report"), std::string::npos);
}

// Test 3: Patch Engine Functionality
TEST_F(ComprehensiveTest, PatchEngineIntegration) {
    PROFILE_OPERATION("patch_engine_test");
    
    // Create two different versions of a file
    std::string original_content = 
        "Line 1\n"
        "Line 2\n"
        "Line 3\n"
        "Line 4\n"
        "Line 5\n";
    
    std::string modified_content = 
        "Line 1\n"
        "Modified Line 2\n"
        "Line 3\n"
        "New Line 3.5\n"
        "Line 4\n"
        "Line 5\n"
        "Added Line 6\n";
    
    // Write original file
    std::ofstream orig_file(test_repo_path_ + "/test_patch.txt");
    orig_file << original_content;
    orig_file.close();
    
    // Initialize and commit
    init_repository(test_repo_path_.c_str());
    add_to_index(test_repo_path_.c_str(), "test_patch.txt");
    create_commit(test_repo_path_.c_str(), "Original version", "test@example.com");
    
    // Modify file
    std::ofstream mod_file(test_repo_path_ + "/test_patch.txt");
    mod_file << modified_content;
    mod_file.close();
    
    // Generate patches (this would require implementing tree hash functions)
    // For now, test the patch formatting functionality
    PatchEngine::Patch test_patch;
    test_patch.old_file = "test_patch.txt";
    test_patch.new_file = "test_patch.txt";
    
    PatchEngine::PatchHunk hunk;
    hunk.old_start = 2;
    hunk.old_count = 1;
    hunk.new_start = 2;
    hunk.new_count = 2;
    hunk.lines = {"-Line 2", "+Modified Line 2", "+New Line 3.5"};
    
    test_patch.hunks.push_back(hunk);
    
    // Test patch formatting
    std::string formatted = PatchEngine::format_patch(test_patch, false);
    EXPECT_FALSE(formatted.empty());
    EXPECT_NE(formatted.find("test_patch.txt"), std::string::npos);
    EXPECT_NE(formatted.find("Modified Line 2"), std::string::npos);
}

// Test 4: Smart Merge Capabilities
TEST_F(ComprehensiveTest, SmartMergeIntegration) {
    PROFILE_OPERATION("smart_merge_test");
    
    // Create a conflict scenario
    SmartMergeEngine::ConflictContext context;
    context.type = SmartMergeEngine::ConflictType::CONTENT;
    context.file_path = "main.cpp";
    context.language = "cpp";
    
    // Base version
    context.base_lines = {
        "#include <iostream>",
        "",
        "int main() {",
        "    std::cout << \"Hello, World!\" << std::endl;",
        "    return 0;",
        "}"
    };
    
    // Our version (added function)
    context.our_lines = {
        "#include <iostream>",
        "",
        "void print_message() {",
        "    std::cout << \"Message from our branch\" << std::endl;",
        "}",
        "",
        "int main() {",
        "    print_message();",
        "    std::cout << \"Hello, World!\" << std::endl;",
        "    return 0;",
        "}"
    };
    
    // Their version (modified main function)
    context.their_lines = {
        "#include <iostream>",
        "#include <string>",
        "",
        "int main() {",
        "    std::string name = \"User\";",
        "    std::cout << \"Hello, \" << name << \"!\" << std::endl;",
        "    return 0;",
        "}"
    };
    
    // Test smart merge resolution
    auto resolution = SmartMergeEngine::smart_merge(context);
    
    // Should attempt to resolve automatically
    EXPECT_TRUE(resolution.confidence_score >= 0.0);
    EXPECT_FALSE(resolution.resolution_strategy.empty());
    
    // Test pattern-based resolution
    auto pattern_resolution = SmartMergeEngine::resolve_by_patterns(context);
    EXPECT_TRUE(pattern_resolution.confidence_score >= 0.0);
    
    // Test semantic resolution
    auto semantic_resolution = SmartMergeEngine::resolve_by_semantics(context);
    EXPECT_TRUE(semantic_resolution.confidence_score >= 0.0);
}

// Test 5: Repository Analytics
TEST_F(ComprehensiveTest, RepositoryAnalyticsIntegration) {
    PROFILE_OPERATION("analytics_test");
    
    // Setup repository with some history
    init_repository(test_repo_path_.c_str());
    
    // Create multiple commits with different authors
    const std::vector<std::string> authors = {
        "alice@example.com",
        "bob@example.com", 
        "charlie@example.com"
    };
    
    for (int i = 0; i < 15; i++) {
        std::string filename = "file_" + std::to_string(i) + ".txt";
        std::ofstream file(test_repo_path_ + "/" + filename);
        file << "Content of file " << i << std::endl;
        file << "Line 2 of file " << i << std::endl;
        file << "Line 3 of file " << i << std::endl;
        file.close();
        
        add_to_index(test_repo_path_.c_str(), filename.c_str());
        
        std::string author = authors[i % authors.size()];
        std::string message = "Add " + filename;
        create_commit(test_repo_path_.c_str(), message.c_str(), author.c_str());
        
        // Small delay to ensure different timestamps
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
    }
    
    // Test analytics functions
    auto commit_stats = RepositoryAnalytics::analyze_commits(test_repo_path_, 365);
    EXPECT_GT(commit_stats.total_commits, 0);
    EXPECT_FALSE(commit_stats.commits_by_author.empty());
    
    auto file_stats = RepositoryAnalytics::analyze_files(test_repo_path_);
    EXPECT_GT(file_stats.total_files, 0);
    EXPECT_FALSE(file_stats.files_by_extension.empty());
    
    auto author_stats = RepositoryAnalytics::analyze_authors(test_repo_path_, 365);
    EXPECT_FALSE(author_stats.commits_by_author.empty());
    EXPECT_EQ(author_stats.commits_by_author.size(), 3); // Three different authors
    
    auto health = RepositoryAnalytics::assess_repository_health(test_repo_path_);
    EXPECT_GT(health.health_score, 0.0);
    EXPECT_LE(health.health_score, 100.0);
    
    // Test report generation
    std::string analytics_report = RepositoryAnalytics::generate_analytics_report(test_repo_path_);
    EXPECT_FALSE(analytics_report.empty());
    
    std::string health_report = RepositoryAnalytics::generate_health_report(test_repo_path_);
    EXPECT_FALSE(health_report.empty());
}

// Test 6: Cloud Sync Engine Configuration
TEST_F(ComprehensiveTest, CloudSyncEngineConfiguration) {
    PROFILE_OPERATION("cloud_sync_test");
    
    // Test sync configuration
    CloudSyncEngine::SyncConfig config;
    config.server_url = "http://localhost:8080/api";
    config.auth_token = "test_token_12345";
    config.repository_id = "test_repo_comprehensive";
    config.auto_sync = false;
    config.sync_interval_seconds = 60;
    
    CloudSyncEngine sync_engine(config);
    
    // Test initial state
    auto status = sync_engine.get_sync_status();
    EXPECT_EQ(status.state, SyncStatus::IDLE);
    EXPECT_EQ(status.files_to_sync, 0);
    EXPECT_EQ(status.files_synced, 0);
    
    // Test offline mode
    EXPECT_FALSE(sync_engine.is_offline_mode());
    sync_engine.enable_offline_mode();
    EXPECT_TRUE(sync_engine.is_offline_mode());
    sync_engine.disable_offline_mode();
    EXPECT_FALSE(sync_engine.is_offline_mode());
    
    // Test auto-sync control
    EXPECT_FALSE(sync_engine.is_auto_sync_enabled());
    sync_engine.start_auto_sync();
    EXPECT_TRUE(sync_engine.is_auto_sync_enabled());
    sync_engine.stop_auto_sync();
    EXPECT_FALSE(sync_engine.is_auto_sync_enabled());
    
    // Test sync filters
    std::vector<std::string> include_patterns = {"*.cpp", "*.hpp", "*.py"};
    std::vector<std::string> exclude_patterns = {"*.tmp", "*.log", "build/*"};
    sync_engine.set_sync_filters(include_patterns, exclude_patterns);
    
    // Test bandwidth settings
    sync_engine.enable_compression(true);
    sync_engine.set_bandwidth_limit(1000); // 1MB/s
}

// Test 7: Memory and Performance Validation
TEST_F(ComprehensiveTest, MemoryAndPerformanceValidation) {
    auto& monitor = PerformanceMonitor::instance();
    
    // Record initial memory usage
    size_t initial_memory = monitor.get_current_memory_usage();
    
    {
        PROFILE_OPERATION("memory_intensive_operations");
        
        // Perform memory-intensive operations
        std::vector<std::string> large_data;
        for (int i = 0; i < 1000; i++) {
            std::string data(1024, 'A' + (i % 26)); // 1KB strings
            large_data.push_back(data);
            
            // Track memory allocation
            monitor.track_memory_allocation(1024);
        }
        
        // Process the data
        for (const auto& data : large_data) {
            // Simulate processing
            volatile size_t len = data.length();
            (void)len; // Suppress unused variable warning
        }
        
        // Clean up
        for (size_t i = 0; i < large_data.size(); i++) {
            monitor.track_memory_deallocation(1024);
        }
        large_data.clear();
    }
    
    // Check memory usage returned to baseline
    size_t final_memory = monitor.get_current_memory_usage();
    EXPECT_EQ(final_memory, initial_memory);
    
    // Verify performance metrics were collected
    auto metrics = monitor.get_operation_metrics("memory_intensive_operations");
    EXPECT_GT(metrics.execution_time.count(), 0);
    
    // Check for slow operations
    monitor.set_slow_operation_threshold(std::chrono::milliseconds(100));
    auto slow_ops = monitor.get_slow_operations();
    
    // Generate optimization suggestions
    std::vector<OperationProfile> profiles;
    // In a real scenario, we'd get actual profiles from the monitor
    
    auto suggestions = PerformanceOptimizer::analyze_performance(profiles);
    // Should not crash and should return valid suggestions structure
}

// Test 8: Error Handling and Edge Cases
TEST_F(ComprehensiveTest, ErrorHandlingAndEdgeCases) {
    PROFILE_OPERATION("error_handling_test");
    
    // Test operations on non-existent repository
    EXPECT_NE(add_to_index("non_existent_repo", "file.txt"), 0);
    EXPECT_NE(create_commit("non_existent_repo", "message", "author"), 0);
    
    // Test operations with invalid parameters
    EXPECT_NE(create_branch(test_repo_path_.c_str(), ""), 0); // Empty branch name
    EXPECT_NE(create_commit(test_repo_path_.c_str(), "", "author"), 0); // Empty message
    
    // Test performance monitor with disabled monitoring
    auto& monitor = PerformanceMonitor::instance();
    monitor.set_enabled(false);
    
    {
        PROFILE_OPERATION("disabled_monitoring_test");
        // This should not crash even with monitoring disabled
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
    }
    
    // Should return empty metrics when disabled
    auto disabled_metrics = monitor.get_operation_metrics("disabled_monitoring_test");
    EXPECT_EQ(disabled_metrics.execution_time.count(), 0);
    
    // Re-enable monitoring
    monitor.set_enabled(true);
    
    // Test patch engine with invalid input
    std::vector<PatchEngine::Patch> empty_patches;
    bool result = PatchEngine::apply_patches(empty_patches, test_repo_path_, true);
    EXPECT_TRUE(result); // Should succeed with empty patches
    
    // Test smart merge with minimal context
    SmartMergeEngine::ConflictContext minimal_context;
    minimal_context.type = SmartMergeEngine::ConflictType::CONTENT;
    minimal_context.file_path = "test.txt";
    
    auto resolution = SmartMergeEngine::smart_merge(minimal_context);
    EXPECT_GE(resolution.confidence_score, 0.0);
}

// Test 9: Integration Stress Test
TEST_F(ComprehensiveTest, IntegrationStressTest) {
    PROFILE_OPERATION("stress_test");
    
    // Initialize repository
    init_repository(test_repo_path_.c_str());
    
    // Create many files and commits to stress test the system
    const int num_files = 100;
    const int num_commits = 50;
    
    for (int commit_idx = 0; commit_idx < num_commits; commit_idx++) {
        // Create/modify files
        for (int file_idx = 0; file_idx < 5; file_idx++) {
            std::string filename = "stress_file_" + std::to_string(file_idx) + ".txt";
            std::ofstream file(test_repo_path_ + "/" + filename);
            
            file << "Commit " << commit_idx << " content for file " << file_idx << std::endl;
            for (int line = 0; line < 50; line++) {
                file << "Line " << line << " in commit " << commit_idx << std::endl;
            }
            file.close();
            
            add_to_index(test_repo_path_.c_str(), filename.c_str());
        }
        
        // Create commit
        std::string message = "Stress test commit " + std::to_string(commit_idx);
        create_commit(test_repo_path_.c_str(), message.c_str(), "stress@test.com");
        
        // Periodically create branches and merge
        if (commit_idx % 10 == 0 && commit_idx > 0) {
            std::string branch_name = "stress_branch_" + std::to_string(commit_idx);
            create_branch(test_repo_path_.c_str(), branch_name.c_str());
            switch_branch(test_repo_path_.c_str(), branch_name.c_str());
            
            // Make a change on the branch
            std::ofstream branch_file(test_repo_path_ + "/branch_file.txt");
            branch_file << "Branch content " << commit_idx << std::endl;
            branch_file.close();
            
            add_to_index(test_repo_path_.c_str(), "branch_file.txt");
            create_commit(test_repo_path_.c_str(), "Branch commit", "stress@test.com");
            
            // Switch back and merge
            switch_branch(test_repo_path_.c_str(), "main");
            merge_branch(test_repo_path_.c_str(), branch_name.c_str());
        }
    }
    
    // Verify repository integrity
    auto commit_log = get_commit_log(test_repo_path_.c_str());
    EXPECT_GT(commit_log.size(), num_commits);
    
    // Test analytics on the stressed repository
    auto commit_stats = RepositoryAnalytics::analyze_commits(test_repo_path_, 365);
    EXPECT_GT(commit_stats.total_commits, num_commits);
    
    auto file_stats = RepositoryAnalytics::analyze_files(test_repo_path_);
    EXPECT_GT(file_stats.total_files, 5);
    
    // Verify performance monitoring captured the stress test
    auto stress_metrics = monitor.get_operation_metrics("stress_test");
    EXPECT_GT(stress_metrics.execution_time.count(), 0);
}

// Test 10: Full System Integration
TEST_F(ComprehensiveTest, FullSystemIntegration) {
    PROFILE_OPERATION("full_system_integration");
    
    // This test combines all major components
    
    // 1. Repository setup with performance monitoring
    {
        PROFILE_OPERATION("system_setup");
        init_repository(test_repo_path_.c_str());
        create_test_files();
    }
    
    // 2. Add files and create initial commit
    {
        PROFILE_OPERATION("initial_commit");
        add_to_index(test_repo_path_.c_str(), "main.cpp");
        add_to_index(test_repo_path_.c_str(), "utils.hpp");
        add_to_index(test_repo_path_.c_str(), "script.py");
        add_to_index(test_repo_path_.c_str(), "config.json");
        add_to_index(test_repo_path_.c_str(), "README.md");
        create_commit(test_repo_path_.c_str(), "Initial system integration commit", "system@test.com");
    }
    
    // 3. Create feature branch and make changes
    {
        PROFILE_OPERATION("feature_development");
        create_branch(test_repo_path_.c_str(), "feature/system-integration");
        switch_branch(test_repo_path_.c_str(), "feature/system-integration");
        
        // Modify multiple files
        std::ofstream cpp_file(test_repo_path_ + "/main.cpp", std::ios::app);
        cpp_file << "\n// System integration feature\nvoid integration_feature() {\n    // Implementation\n}\n";
        cpp_file.close();
        
        std::ofstream py_file(test_repo_path_ + "/script.py", std::ios::app);
        py_file << "\ndef integration_function():\n    \"\"\"New integration function\"\"\"\n    pass\n";
        py_file.close();
        
        add_to_index(test_repo_path_.c_str(), "main.cpp");
        add_to_index(test_repo_path_.c_str(), "script.py");
        create_commit(test_repo_path_.c_str(), "Add integration features", "system@test.com");
    }
    
    // 4. Switch back to main and create conflicting changes
    {
        PROFILE_OPERATION("conflict_setup");
        switch_branch(test_repo_path_.c_str(), "main");
        
        std::ofstream cpp_file(test_repo_path_ + "/main.cpp", std::ios::app);
        cpp_file << "\n// Main branch feature\nvoid main_feature() {\n    // Different implementation\n}\n";
        cpp_file.close();
        
        add_to_index(test_repo_path_.c_str(), "main.cpp");
        create_commit(test_repo_path_.c_str(), "Add main branch feature", "system@test.com");
    }
    
    // 5. Attempt merge (may create conflicts)
    {
        PROFILE_OPERATION("merge_operation");
        int merge_result = merge_branch(test_repo_path_.c_str(), "feature/system-integration");
        // Accept either success or conflict
        EXPECT_TRUE(merge_result == 0 || merge_result == 1);
    }
    
    // 6. Analyze repository with analytics
    {
        PROFILE_OPERATION("analytics_analysis");
        auto commit_stats = RepositoryAnalytics::analyze_commits(test_repo_path_, 365);
        auto file_stats = RepositoryAnalytics::analyze_files(test_repo_path_);
        auto health = RepositoryAnalytics::assess_repository_health(test_repo_path_);
        
        EXPECT_GT(commit_stats.total_commits, 0);
        EXPECT_GT(file_stats.total_files, 0);
        EXPECT_GT(health.health_score, 0.0);
    }
    
    // 7. Generate comprehensive reports
    {
        PROFILE_OPERATION("report_generation");
        auto& monitor = PerformanceMonitor::instance();
        
        std::string perf_report = monitor.generate_report(true);
        std::string analytics_report = RepositoryAnalytics::generate_analytics_report(test_repo_path_);
        std::string health_report = RepositoryAnalytics::generate_health_report(test_repo_path_);
        
        EXPECT_FALSE(perf_report.empty());
        EXPECT_FALSE(analytics_report.empty());
        EXPECT_FALSE(health_report.empty());
        
        // Verify reports contain expected content
        EXPECT_NE(perf_report.find("Performance Report"), std::string::npos);
        EXPECT_NE(analytics_report.find("commit"), std::string::npos);
        EXPECT_NE(health_report.find("health"), std::string::npos);
    }
    
    // 8. Verify all systems worked together
    auto final_metrics = PerformanceMonitor::instance().get_operation_metrics("full_system_integration");
    EXPECT_GT(final_metrics.execution_time.count(), 0);
    
    // Check that we have metrics for all sub-operations
    EXPECT_GT(PerformanceMonitor::instance().get_operation_metrics("system_setup").execution_time.count(), 0);
    EXPECT_GT(PerformanceMonitor::instance().get_operation_metrics("initial_commit").execution_time.count(), 0);
    EXPECT_GT(PerformanceMonitor::instance().get_operation_metrics("feature_development").execution_time.count(), 0);
}

int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}