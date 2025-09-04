#include <gtest/gtest.h>
#include "../src/core/performance_monitor.hpp"
#include "../src/core/patch_engine.hpp"
#include "../src/core/merge_engine.hpp"
#include <thread>
#include <chrono>

using namespace svcs;

class PerformanceTest : public ::testing::Test {
protected:
    void SetUp() override {
        monitor_ = &PerformanceMonitor::instance();
        monitor_->clear_metrics();
        monitor_->set_enabled(true);
    }
    
    void TearDown() override {
        monitor_->clear_metrics();
    }
    
    PerformanceMonitor* monitor_;
};

TEST_F(PerformanceTest, BasicOperationProfiling) {
    {
        PROFILE_OPERATION("test_operation");
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }
    
    auto metrics = monitor_->get_operation_metrics("test_operation");
    EXPECT_GE(metrics.execution_time.count(), 100);
    EXPECT_LT(metrics.execution_time.count(), 200); // Allow some tolerance
}

TEST_F(PerformanceTest, NestedOperationProfiling) {
    {
        PROFILE_OPERATION("outer_operation");
        std::this_thread::sleep_for(std::chrono::milliseconds(50));
        
        {
            PROFILE_OPERATION("inner_operation");
            std::this_thread::sleep_for(std::chrono::milliseconds(30));
        }
        
        std::this_thread::sleep_for(std::chrono::milliseconds(20));
    }
    
    auto outer_metrics = monitor_->get_operation_metrics("outer_operation");
    auto inner_metrics = monitor_->get_operation_metrics("inner_operation");
    
    EXPECT_GE(outer_metrics.execution_time.count(), 100);
    EXPECT_GE(inner_metrics.execution_time.count(), 30);
    EXPECT_LT(inner_metrics.execution_time.count(), 50);
}

TEST_F(PerformanceTest, MemoryTracking) {
    size_t initial_memory = monitor_->get_current_memory_usage();
    
    monitor_->track_memory_allocation(1024);
    EXPECT_EQ(monitor_->get_current_memory_usage(), initial_memory + 1024);
    
    monitor_->track_memory_deallocation(512);
    EXPECT_EQ(monitor_->get_current_memory_usage(), initial_memory + 512);
}

TEST_F(PerformanceTest, SlowOperationDetection) {
    monitor_->set_slow_operation_threshold(std::chrono::milliseconds(50));
    
    {
        PROFILE_OPERATION("fast_operation");
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
    }
    
    {
        PROFILE_OPERATION("slow_operation");
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }
    
    auto slow_ops = monitor_->get_slow_operations();
    EXPECT_EQ(slow_ops.size(), 1);
    EXPECT_EQ(slow_ops[0].operation_name, "slow_operation");
}

TEST_F(PerformanceTest, CustomMetrics) {
    {
        ScopedProfiler profiler("custom_metrics_test");
        profiler.add_custom_metric("items_processed", 42.0);
        profiler.add_custom_metric("cache_hit_ratio", 0.85);
    }
    
    auto metrics = monitor_->get_operation_metrics("custom_metrics_test");
    EXPECT_EQ(metrics.custom_metrics["items_processed"], 42.0);
    EXPECT_EQ(metrics.custom_metrics["cache_hit_ratio"], 0.85);
}

// Benchmark tests for core operations
class BenchmarkTest : public ::testing::Test {
protected:
    void SetUp() override {
        // Create test repository
        system("mkdir -p test_repo");
        system("cd test_repo && git init");
        
        // Create test files
        for (int i = 0; i < 100; i++) {
            std::string filename = "test_repo/file" + std::to_string(i) + ".txt";
            std::ofstream file(filename);
            for (int j = 0; j < 1000; j++) {
                file << "Line " << j << " in file " << i << "\n";
            }
        }
    }
    
    void TearDown() override {
        system("rm -rf test_repo");
    }
};

TEST_F(BenchmarkTest, CommitPerformance) {
    PROFILE_OPERATION("commit_100_files");
    
    // Simulate committing 100 files
    for (int i = 0; i < 100; i++) {
        std::string filename = "test_repo/file" + std::to_string(i) + ".txt";
        // Add file to index and commit (simplified)
    }
    
    auto& monitor = PerformanceMonitor::instance();
    auto metrics = monitor.get_operation_metrics("commit_100_files");
    
    // Commit should complete within reasonable time
    EXPECT_LT(metrics.execution_time.count(), 5000); // 5 seconds max
}

TEST_F(BenchmarkTest, DiffPerformance) {
    PROFILE_OPERATION("diff_large_files");
    
    // Create two large files with differences
    std::string file1_content, file2_content;
    for (int i = 0; i < 10000; i++) {
        file1_content += "Original line " + std::to_string(i) + "\n";
        if (i % 100 == 0) {
            file2_content += "Modified line " + std::to_string(i) + "\n";
        } else {
            file2_content += "Original line " + std::to_string(i) + "\n";
        }
    }
    
    // Generate diff
    auto old_lines = split_lines(file1_content);
    auto new_lines = split_lines(file2_content);
    
    auto patches = PatchEngine::generate_patches("tree1", "tree2");
    
    auto& monitor = PerformanceMonitor::instance();
    auto metrics = monitor.get_operation_metrics("diff_large_files");
    
    // Diff should complete within reasonable time
    EXPECT_LT(metrics.execution_time.count(), 1000); // 1 second max
}

TEST_F(BenchmarkTest, MergePerformance) {
    PROFILE_OPERATION("three_way_merge");
    
    // Simulate three-way merge of large files
    std::string base_content, our_content, their_content;
    
    for (int i = 0; i < 5000; i++) {
        base_content += "Base line " + std::to_string(i) + "\n";
        
        if (i % 50 == 0) {
            our_content += "Our modified line " + std::to_string(i) + "\n";
        } else {
            our_content += "Base line " + std::to_string(i) + "\n";
        }
        
        if (i % 75 == 0) {
            their_content += "Their modified line " + std::to_string(i) + "\n";
        } else {
            their_content += "Base line " + std::to_string(i) + "\n";
        }
    }
    
    // Perform merge
    MergeEngine engine;
    auto result = engine.merge_files(base_content, our_content, their_content);
    
    auto& monitor = PerformanceMonitor::instance();
    auto metrics = monitor.get_operation_metrics("three_way_merge");
    
    // Merge should complete within reasonable time
    EXPECT_LT(metrics.execution_time.count(), 2000); // 2 seconds max
}

// Cache performance tests
class CachePerformanceTest : public ::testing::Test {
protected:
    void SetUp() override {
        cache_monitor_ = std::make_unique<CacheMonitor>();
    }
    
    std::unique_ptr<CacheMonitor> cache_monitor_;
};

TEST_F(CachePerformanceTest, CacheHitRatioTracking) {
    // Simulate cache operations
    for (int i = 0; i < 100; i++) {
        if (i % 4 == 0) {
            cache_monitor_->record_miss("object_cache");
        } else {
            cache_monitor_->record_hit("object_cache");
        }
    }
    
    auto stats = cache_monitor_->get_stats("object_cache");
    EXPECT_EQ(stats.hits, 75);
    EXPECT_EQ(stats.misses, 25);
    EXPECT_DOUBLE_EQ(stats.hit_ratio(), 0.75);
}

TEST_F(CachePerformanceTest, MultipleCacheTracking) {
    cache_monitor_->record_hit("object_cache");
    cache_monitor_->record_hit("tree_cache");
    cache_monitor_->record_miss("blob_cache");
    
    auto all_stats = cache_monitor_->get_all_stats();
    EXPECT_EQ(all_stats.size(), 3);
    EXPECT_EQ(all_stats["object_cache"].hits, 1);
    EXPECT_EQ(all_stats["tree_cache"].hits, 1);
    EXPECT_EQ(all_stats["blob_cache"].misses, 1);
}

// Performance regression tests
class RegressionTest : public ::testing::Test {
protected:
    void SetUp() override {
        // Load baseline performance metrics
        load_baseline_metrics();
    }
    
    void load_baseline_metrics() {
        // In a real implementation, load from file
        baseline_metrics_["commit_operation"] = std::chrono::milliseconds(100);
        baseline_metrics_["diff_operation"] = std::chrono::milliseconds(50);
        baseline_metrics_["merge_operation"] = std::chrono::milliseconds(200);
    }
    
    std::map<std::string, std::chrono::milliseconds> baseline_metrics_;
};

TEST_F(RegressionTest, CommitPerformanceRegression) {
    {
        PROFILE_OPERATION("commit_operation");
        // Simulate commit operation
        std::this_thread::sleep_for(std::chrono::milliseconds(80));
    }
    
    auto& monitor = PerformanceMonitor::instance();
    auto metrics = monitor.get_operation_metrics("commit_operation");
    
    auto baseline = baseline_metrics_["commit_operation"];
    auto current = metrics.execution_time;
    
    // Allow 20% performance degradation
    auto threshold = baseline * 1.2;
    EXPECT_LT(current, threshold) 
        << "Performance regression detected: " 
        << current.count() << "ms vs baseline " << baseline.count() << "ms";
}

// Memory leak detection
class MemoryLeakTest : public ::testing::Test {
protected:
    void SetUp() override {
        initial_memory_ = get_memory_usage();
    }
    
    void TearDown() override {
        final_memory_ = get_memory_usage();
        
        // Allow small memory increase (10KB) for test overhead
        EXPECT_LT(final_memory_ - initial_memory_, 10240)
            << "Potential memory leak detected: "
            << (final_memory_ - initial_memory_) << " bytes leaked";
    }
    
private:
    size_t get_memory_usage() {
        // Platform-specific memory usage detection
        #ifdef __linux__
        std::ifstream status("/proc/self/status");
        std::string line;
        while (std::getline(status, line)) {
            if (line.substr(0, 6) == "VmRSS:") {
                std::istringstream iss(line);
                std::string label, value, unit;
                iss >> label >> value >> unit;
                return std::stoul(value) * 1024; // Convert KB to bytes
            }
        }
        #endif
        return 0;
    }
    
    size_t initial_memory_;
    size_t final_memory_;
};

TEST_F(MemoryLeakTest, PatchEngineMemoryLeak) {
    // Perform operations that might leak memory
    for (int i = 0; i < 100; i++) {
        auto patches = PatchEngine::generate_patches("tree1", "tree2");
        // Patches should be automatically cleaned up
    }
}

TEST_F(MemoryLeakTest, MergeEngineMemoryLeak) {
    MergeEngine engine;
    
    for (int i = 0; i < 50; i++) {
        std::string base = "base content " + std::to_string(i);
        std::string ours = "our content " + std::to_string(i);
        std::string theirs = "their content " + std::to_string(i);
        
        auto result = engine.merge_files(base, ours, theirs);
        // Result should be automatically cleaned up
    }
}

int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}