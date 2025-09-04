#include "performance_monitor.hpp"
#include <iostream>
#include <sstream>
#include <algorithm>
#include <iomanip>

#ifdef __linux__
#include <sys/resource.h>
#include <unistd.h>
#include <fstream>
#endif

namespace svcs {

PerformanceMonitor& PerformanceMonitor::instance() {
    static PerformanceMonitor instance;
    return instance;
}

std::shared_ptr<OperationProfile> PerformanceMonitor::start_operation(const std::string& name) {
    if (!monitoring_enabled_) {
        return nullptr;
    }
    
    auto profile = std::make_shared<OperationProfile>();
    profile->operation_name = name;
    profile->start_time = std::chrono::steady_clock::now();
    
    return profile;
}

void PerformanceMonitor::end_operation(std::shared_ptr<OperationProfile> profile) {
    if (!profile || !monitoring_enabled_) {
        return;
    }
    
    profile->end_time = std::chrono::steady_clock::now();
    profile->metrics.execution_time = std::chrono::duration_cast<std::chrono::milliseconds>(
        profile->end_time - profile->start_time
    );
    
    // Get current memory usage
    profile->metrics.memory_usage = get_current_memory_usage();
    
    std::lock_guard<std::mutex> lock(metrics_mutex_);
    
    // Add to completed operations
    completed_operations_.push_back(*profile);
    
    // Update operation summaries
    auto& summary = operation_summaries_[profile->operation_name];
    summary.execution_time = std::max(summary.execution_time, profile->metrics.execution_time);
    summary.memory_usage = std::max(summary.memory_usage, profile->metrics.memory_usage);
    
    // Merge custom metrics
    for (const auto& [key, value] : profile->metrics.custom_metrics) {
        summary.custom_metrics[key] = std::max(summary.custom_metrics[key], value);
    }
}

std::string PerformanceMonitor::generate_report(bool detailed) {
    std::lock_guard<std::mutex> lock(metrics_mutex_);
    std::ostringstream oss;
    
    oss << "=== Performance Report ===\n\n";
    
    // System information
    oss << "System Information:\n";
    oss << "  Current Memory Usage: " << format_bytes(get_current_memory_usage()) << "\n";
    oss << "  Total Disk Reads: " << format_bytes(total_disk_reads_) << "\n";
    oss << "  Total Disk Writes: " << format_bytes(total_disk_writes_) << "\n";
    oss << "  Total Network Sent: " << format_bytes(total_network_sent_) << "\n";
    oss << "  Total Network Received: " << format_bytes(total_network_received_) << "\n\n";
    
    // Operation summaries
    oss << "Operation Summaries:\n";
    oss << std::setw(25) << "Operation" 
        << std::setw(15) << "Max Time (ms)"
        << std::setw(15) << "Max Memory"
        << std::setw(10) << "Count" << "\n";
    oss << std::string(65, '-') << "\n";
    
    std::map<std::string, int> operation_counts;
    for (const auto& op : completed_operations_) {
        operation_counts[op.operation_name]++;
    }
    
    for (const auto& [name, metrics] : operation_summaries_) {
        oss << std::setw(25) << name
            << std::setw(15) << metrics.execution_time.count()
            << std::setw(15) << format_bytes(metrics.memory_usage)
            << std::setw(10) << operation_counts[name] << "\n";
    }
    
    // Slow operations
    auto slow_ops = get_slow_operations();
    if (!slow_ops.empty()) {
        oss << "\nSlow Operations (>" << slow_threshold_.count() << "ms):\n";
        for (const auto& op : slow_ops) {
            oss << "  " << op.operation_name << ": " 
                << op.metrics.execution_time.count() << "ms\n";
        }
    }
    
    if (detailed) {
        oss << "\nDetailed Operation History:\n";
        for (const auto& op : completed_operations_) {
            oss << "  " << op.operation_name 
                << " - " << op.metrics.execution_time.count() << "ms"
                << " - " << format_bytes(op.metrics.memory_usage) << "\n";
            
            for (const auto& [key, value] : op.metrics.custom_metrics) {
                oss << "    " << key << ": " << value << "\n";
            }
        }
    }
    
    return oss.str();
}

PerformanceMetrics PerformanceMonitor::get_operation_metrics(const std::string& operation_name) {
    std::lock_guard<std::mutex> lock(metrics_mutex_);
    
    auto it = operation_summaries_.find(operation_name);
    if (it != operation_summaries_.end()) {
        return it->second;
    }
    
    return PerformanceMetrics{};
}

void PerformanceMonitor::clear_metrics() {
    std::lock_guard<std::mutex> lock(metrics_mutex_);
    completed_operations_.clear();
    operation_summaries_.clear();
    current_memory_usage_ = 0;
    total_disk_reads_ = 0;
    total_disk_writes_ = 0;
    total_network_sent_ = 0;
    total_network_received_ = 0;
}

void PerformanceMonitor::set_slow_operation_threshold(std::chrono::milliseconds threshold) {
    slow_threshold_ = threshold;
}

void PerformanceMonitor::set_memory_threshold(size_t bytes) {
    memory_threshold_ = bytes;
}

std::vector<OperationProfile> PerformanceMonitor::get_slow_operations() {
    std::lock_guard<std::mutex> lock(metrics_mutex_);
    std::vector<OperationProfile> slow_ops;
    
    for (const auto& op : completed_operations_) {
        if (op.metrics.execution_time >= slow_threshold_) {
            slow_ops.push_back(op);
        }
    }
    
    // Sort by execution time (slowest first)
    std::sort(slow_ops.begin(), slow_ops.end(),
              [](const OperationProfile& a, const OperationProfile& b) {
                  return a.metrics.execution_time > b.metrics.execution_time;
              });
    
    return slow_ops;
}

void PerformanceMonitor::track_memory_allocation(size_t bytes) {
    current_memory_usage_ += bytes;
}

void PerformanceMonitor::track_memory_deallocation(size_t bytes) {
    current_memory_usage_ -= bytes;
}

size_t PerformanceMonitor::get_current_memory_usage() const {
    return current_memory_usage_;
}

void PerformanceMonitor::track_disk_read(size_t bytes) {
    total_disk_reads_ += bytes;
}

void PerformanceMonitor::track_disk_write(size_t bytes) {
    total_disk_writes_ += bytes;
}

void PerformanceMonitor::track_network_send(size_t bytes) {
    total_network_sent_ += bytes;
}

void PerformanceMonitor::track_network_receive(size_t bytes) {
    total_network_received_ += bytes;
}

std::string PerformanceMonitor::format_bytes(size_t bytes) {
    const char* units[] = {"B", "KB", "MB", "GB", "TB"};
    int unit_index = 0;
    double size = static_cast<double>(bytes);
    
    while (size >= 1024.0 && unit_index < 4) {
        size /= 1024.0;
        unit_index++;
    }
    
    std::ostringstream oss;
    oss << std::fixed << std::setprecision(2) << size << " " << units[unit_index];
    return oss.str();
}

// ScopedProfiler implementation
ScopedProfiler::ScopedProfiler(const std::string& operation_name) {
    profile_ = PerformanceMonitor::instance().start_operation(operation_name);
}

ScopedProfiler::~ScopedProfiler() {
    if (profile_) {
        PerformanceMonitor::instance().end_operation(profile_);
    }
}

void ScopedProfiler::add_custom_metric(const std::string& name, double value) {
    if (profile_) {
        profile_->metrics.custom_metrics[name] = value;
    }
}

// PerformanceOptimizer implementation
std::vector<PerformanceOptimizer::Suggestion> PerformanceOptimizer::analyze_performance(
    const std::vector<OperationProfile>& profiles
) {
    std::vector<Suggestion> suggestions;
    
    analyze_memory_usage(profiles, suggestions);
    analyze_disk_io(profiles, suggestions);
    analyze_operation_patterns(profiles, suggestions);
    
    // Sort by priority (highest first)
    std::sort(suggestions.begin(), suggestions.end(),
              [](const Suggestion& a, const Suggestion& b) {
                  return a.priority > b.priority;
              });
    
    return suggestions;
}

std::string PerformanceOptimizer::generate_optimization_report(
    const std::vector<Suggestion>& suggestions
) {
    std::ostringstream oss;
    
    oss << "=== Performance Optimization Suggestions ===\n\n";
    
    std::map<std::string, std::vector<Suggestion>> categorized;
    for (const auto& suggestion : suggestions) {
        categorized[suggestion.category].push_back(suggestion);
    }
    
    for (const auto& [category, category_suggestions] : categorized) {
        oss << category << ":\n";
        for (const auto& suggestion : category_suggestions) {
            oss << "  Priority " << suggestion.priority << ": " 
                << suggestion.description << "\n";
            oss << "    Recommendation: " << suggestion.recommendation << "\n\n";
        }
    }
    
    return oss.str();
}

void PerformanceOptimizer::analyze_memory_usage(
    const std::vector<OperationProfile>& profiles,
    std::vector<Suggestion>& suggestions
) {
    size_t max_memory = 0;
    std::string memory_heavy_operation;
    
    for (const auto& profile : profiles) {
        if (profile.metrics.memory_usage > max_memory) {
            max_memory = profile.metrics.memory_usage;
            memory_heavy_operation = profile.operation_name;
        }
    }
    
    if (max_memory > 100 * 1024 * 1024) { // 100MB threshold
        Suggestion suggestion;
        suggestion.category = "Memory Usage";
        suggestion.description = "High memory usage detected in " + memory_heavy_operation;
        suggestion.recommendation = "Consider using memory pools, streaming, or chunked processing";
        suggestion.priority = 8;
        suggestions.push_back(suggestion);
    }
}

void PerformanceOptimizer::analyze_disk_io(
    const std::vector<OperationProfile>& profiles,
    std::vector<Suggestion>& suggestions
) {
    size_t total_disk_io = 0;
    
    for (const auto& profile : profiles) {
        total_disk_io += profile.metrics.disk_io_bytes;
    }
    
    if (total_disk_io > 50 * 1024 * 1024) { // 50MB threshold
        Suggestion suggestion;
        suggestion.category = "Disk I/O";
        suggestion.description = "High disk I/O detected: " + 
                               PerformanceMonitor::instance().format_bytes(total_disk_io);
        suggestion.recommendation = "Consider using buffered I/O, compression, or caching";
        suggestion.priority = 7;
        suggestions.push_back(suggestion);
    }
}

void PerformanceOptimizer::analyze_operation_patterns(
    const std::vector<OperationProfile>& profiles,
    std::vector<Suggestion>& suggestions
) {
    std::map<std::string, int> operation_counts;
    std::map<std::string, std::chrono::milliseconds> total_times;
    
    for (const auto& profile : profiles) {
        operation_counts[profile.operation_name]++;
        total_times[profile.operation_name] += profile.metrics.execution_time;
    }
    
    // Look for frequently called slow operations
    for (const auto& [operation, count] : operation_counts) {
        if (count > 100) { // Called more than 100 times
            auto avg_time = total_times[operation] / count;
            if (avg_time > std::chrono::milliseconds(100)) { // Average > 100ms
                Suggestion suggestion;
                suggestion.category = "Operation Patterns";
                suggestion.description = "Frequently called slow operation: " + operation;
                suggestion.recommendation = "Consider caching results or optimizing the algorithm";
                suggestion.priority = 9;
                suggestions.push_back(suggestion);
            }
        }
    }
}

// CacheMonitor implementation
void CacheMonitor::record_hit(const std::string& cache_name) {
    std::lock_guard<std::mutex> lock(stats_mutex_);
    cache_stats_[cache_name].hits++;
}

void CacheMonitor::record_miss(const std::string& cache_name) {
    std::lock_guard<std::mutex> lock(stats_mutex_);
    cache_stats_[cache_name].misses++;
}

void CacheMonitor::record_eviction(const std::string& cache_name) {
    std::lock_guard<std::mutex> lock(stats_mutex_);
    cache_stats_[cache_name].evictions++;
}

void CacheMonitor::update_size(const std::string& cache_name, size_t current, size_t max) {
    std::lock_guard<std::mutex> lock(stats_mutex_);
    auto& stats = cache_stats_[cache_name];
    stats.current_size = current;
    stats.max_size = max;
}

CacheMonitor::CacheStats CacheMonitor::get_stats(const std::string& cache_name) const {
    std::lock_guard<std::mutex> lock(stats_mutex_);
    auto it = cache_stats_.find(cache_name);
    return it != cache_stats_.end() ? it->second : CacheStats{};
}

std::map<std::string, CacheMonitor::CacheStats> CacheMonitor::get_all_stats() const {
    std::lock_guard<std::mutex> lock(stats_mutex_);
    return cache_stats_;
}

std::string CacheMonitor::generate_cache_report() const {
    std::lock_guard<std::mutex> lock(stats_mutex_);
    std::ostringstream oss;
    
    oss << "=== Cache Performance Report ===\n\n";
    oss << std::setw(15) << "Cache Name"
        << std::setw(10) << "Hits"
        << std::setw(10) << "Misses"
        << std::setw(12) << "Hit Ratio"
        << std::setw(12) << "Evictions"
        << std::setw(15) << "Size Usage" << "\n";
    oss << std::string(74, '-') << "\n";
    
    for (const auto& [name, stats] : cache_stats_) {
        double size_ratio = stats.max_size > 0 ? 
                           (double)stats.current_size / stats.max_size : 0.0;
        
        oss << std::setw(15) << name
            << std::setw(10) << stats.hits
            << std::setw(10) << stats.misses
            << std::setw(12) << std::fixed << std::setprecision(3) << stats.hit_ratio()
            << std::setw(12) << stats.evictions
            << std::setw(14) << std::fixed << std::setprecision(1) << (size_ratio * 100) << "%\n";
    }
    
    return oss.str();
}

}