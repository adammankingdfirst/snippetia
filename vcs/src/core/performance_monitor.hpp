#pragma once

#include <string>
#include <chrono>
#include <map>
#include <vector>
#include <memory>
#include <atomic>

namespace svcs {

struct PerformanceMetrics {
    std::chrono::milliseconds execution_time{0};
    size_t memory_usage = 0;
    size_t disk_io_bytes = 0;
    size_t network_io_bytes = 0;
    int cpu_usage_percent = 0;
    std::map<std::string, double> custom_metrics;
};

struct OperationProfile {
    std::string operation_name;
    std::chrono::steady_clock::time_point start_time;
    std::chrono::steady_clock::time_point end_time;
    PerformanceMetrics metrics;
    std::vector<std::shared_ptr<OperationProfile>> sub_operations;
};

class PerformanceMonitor {
public:
    // Singleton instance
    static PerformanceMonitor& instance();
    
    // Start monitoring an operation
    std::shared_ptr<OperationProfile> start_operation(const std::string& name);
    
    // End monitoring an operation
    void end_operation(std::shared_ptr<OperationProfile> profile);
    
    // Get performance report
    std::string generate_report(bool detailed = false);
    
    // Get metrics for specific operation
    PerformanceMetrics get_operation_metrics(const std::string& operation_name);
    
    // Clear all collected data
    void clear_metrics();
    
    // Enable/disable monitoring
    void set_enabled(bool enabled) { monitoring_enabled_ = enabled; }
    bool is_enabled() const { return monitoring_enabled_; }
    
    // Set monitoring thresholds
    void set_slow_operation_threshold(std::chrono::milliseconds threshold);
    void set_memory_threshold(size_t bytes);
    
    // Get slow operations
    std::vector<OperationProfile> get_slow_operations();
    
    // Memory tracking
    void track_memory_allocation(size_t bytes);
    void track_memory_deallocation(size_t bytes);
    size_t get_current_memory_usage() const;
    
    // Disk I/O tracking
    void track_disk_read(size_t bytes);
    void track_disk_write(size_t bytes);
    
    // Network I/O tracking
    void track_network_send(size_t bytes);
    void track_network_receive(size_t bytes);
    
private:
    PerformanceMonitor() = default;
    
    std::atomic<bool> monitoring_enabled_{true};
    std::atomic<size_t> current_memory_usage_{0};
    std::atomic<size_t> total_disk_reads_{0};
    std::atomic<size_t> total_disk_writes_{0};
    std::atomic<size_t> total_network_sent_{0};
    std::atomic<size_t> total_network_received_{0};
    
    std::chrono::milliseconds slow_threshold_{1000};
    size_t memory_threshold_{100 * 1024 * 1024}; // 100MB
    
    std::vector<OperationProfile> completed_operations_;
    std::map<std::string, PerformanceMetrics> operation_summaries_;
    
    mutable std::mutex metrics_mutex_;
};

// RAII helper for automatic operation profiling
class ScopedProfiler {
public:
    explicit ScopedProfiler(const std::string& operation_name);
    ~ScopedProfiler();
    
    void add_custom_metric(const std::string& name, double value);
    
private:
    std::shared_ptr<OperationProfile> profile_;
};

// Macros for easy profiling
#define PROFILE_OPERATION(name) \
    svcs::ScopedProfiler _prof(name)

#define PROFILE_FUNCTION() \
    svcs::ScopedProfiler _prof(__FUNCTION__)

// Performance optimization suggestions
class PerformanceOptimizer {
public:
    struct Suggestion {
        std::string category;
        std::string description;
        std::string recommendation;
        int priority; // 1-10, 10 being highest
    };
    
    static std::vector<Suggestion> analyze_performance(
        const std::vector<OperationProfile>& profiles
    );
    
    static std::string generate_optimization_report(
        const std::vector<Suggestion>& suggestions
    );
    
private:
    static void analyze_memory_usage(
        const std::vector<OperationProfile>& profiles,
        std::vector<Suggestion>& suggestions
    );
    
    static void analyze_disk_io(
        const std::vector<OperationProfile>& profiles,
        std::vector<Suggestion>& suggestions
    );
    
    static void analyze_operation_patterns(
        const std::vector<OperationProfile>& profiles,
        std::vector<Suggestion>& suggestions
    );
};

// Cache performance monitor
class CacheMonitor {
public:
    struct CacheStats {
        size_t hits = 0;
        size_t misses = 0;
        size_t evictions = 0;
        size_t current_size = 0;
        size_t max_size = 0;
        double hit_ratio() const {
            return hits + misses > 0 ? (double)hits / (hits + misses) : 0.0;
        }
    };
    
    void record_hit(const std::string& cache_name);
    void record_miss(const std::string& cache_name);
    void record_eviction(const std::string& cache_name);
    void update_size(const std::string& cache_name, size_t current, size_t max);
    
    CacheStats get_stats(const std::string& cache_name) const;
    std::map<std::string, CacheStats> get_all_stats() const;
    
    std::string generate_cache_report() const;
    
private:
    std::map<std::string, CacheStats> cache_stats_;
    mutable std::mutex stats_mutex_;
};

}