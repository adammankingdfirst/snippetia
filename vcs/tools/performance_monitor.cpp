#include "../src/core/performance_monitor.hpp"
#include <iostream>
#include <iomanip>
#include <thread>
#include <chrono>

using namespace svcs;

void print_usage() {
    std::cout << "SVCS Performance Monitor Tool\n\n";
    std::cout << "Usage: svcs_perf_monitor [options]\n\n";
    std::cout << "Options:\n";
    std::cout << "  --report              Generate performance report\n";
    std::cout << "  --detailed            Include detailed metrics\n";
    std::cout << "  --clear               Clear all metrics\n";
    std::cout << "  --monitor <seconds>   Monitor for specified duration\n";
    std::cout << "  --threshold <ms>      Set slow operation threshold\n";
    std::cout << "  --memory-limit <mb>   Set memory usage threshold\n";
    std::cout << "  --cache-report        Generate cache performance report\n";
    std::cout << "  --optimize            Generate optimization suggestions\n";
    std::cout << "  --help                Show this help message\n\n";
    std::cout << "Examples:\n";
    std::cout << "  svcs_perf_monitor --report --detailed\n";
    std::cout << "  svcs_perf_monitor --monitor 60 --threshold 500\n";
    std::cout << "  svcs_perf_monitor --cache-report --optimize\n";
}

void simulate_operations() {
    std::cout << "Simulating repository operations for monitoring...\n";
    
    // Simulate various operations
    {
        PROFILE_OPERATION("repository_init");
        std::this_thread::sleep_for(std::chrono::milliseconds(50));
    }
    
    {
        PROFILE_OPERATION("file_operations");
        for (int i = 0; i < 10; i++) {
            PROFILE_OPERATION("add_file");
            std::this_thread::sleep_for(std::chrono::milliseconds(10));
            
            // Simulate memory allocation
            PerformanceMonitor::instance().track_memory_allocation(1024 * (i + 1));
        }
    }
    
    {
        PROFILE_OPERATION("commit_creation");
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
        
        // Simulate disk I/O
        PerformanceMonitor::instance().track_disk_write(1024 * 1024); // 1MB
    }
    
    {
        PROFILE_OPERATION("branch_operations");
        std::this_thread::sleep_for(std::chrono::milliseconds(30));
    }
    
    {
        PROFILE_OPERATION("merge_operation");
        std::this_thread::sleep_for(std::chrono::milliseconds(200));
        
        // Simulate network I/O
        PerformanceMonitor::instance().track_network_send(512 * 1024); // 512KB
    }
    
    // Clean up memory allocations
    for (int i = 0; i < 10; i++) {
        PerformanceMonitor::instance().track_memory_deallocation(1024 * (i + 1));
    }
    
    std::cout << "Simulation completed.\n\n";
}

void monitor_for_duration(int seconds) {
    std::cout << "Monitoring performance for " << seconds << " seconds...\n";
    std::cout << "Press Ctrl+C to stop early.\n\n";
    
    auto start_time = std::chrono::steady_clock::now();
    auto end_time = start_time + std::chrono::seconds(seconds);
    
    int iteration = 0;
    while (std::chrono::steady_clock::now() < end_time) {
        // Simulate ongoing operations
        {
            ScopedProfiler profiler("monitoring_iteration_" + std::to_string(iteration));
            profiler.add_custom_metric("iteration_number", iteration);
            
            std::this_thread::sleep_for(std::chrono::milliseconds(100));
            
            // Simulate varying workload
            if (iteration % 5 == 0) {
                std::this_thread::sleep_for(std::chrono::milliseconds(200)); // Slow operation
            }
        }
        
        iteration++;
        
        // Print progress every 10 seconds
        auto elapsed = std::chrono::steady_clock::now() - start_time;
        auto elapsed_seconds = std::chrono::duration_cast<std::chrono::seconds>(elapsed).count();
        
        if (elapsed_seconds > 0 && elapsed_seconds % 10 == 0) {
            static int last_reported = 0;
            if (elapsed_seconds != last_reported) {
                std::cout << "Monitoring... " << elapsed_seconds << "s elapsed\n";
                last_reported = elapsed_seconds;
            }
        }
    }
    
    std::cout << "Monitoring completed.\n\n";
}

void generate_cache_report() {
    std::cout << "=== Cache Performance Report ===\n\n";
    
    // Simulate cache operations for demonstration
    CacheMonitor cache_monitor;
    
    // Simulate object cache
    for (int i = 0; i < 100; i++) {
        if (i % 4 == 0) {
            cache_monitor.record_miss("object_cache");
        } else {
            cache_monitor.record_hit("object_cache");
        }
    }
    cache_monitor.update_size("object_cache", 25 * 1024 * 1024, 50 * 1024 * 1024);
    
    // Simulate tree cache
    for (int i = 0; i < 50; i++) {
        if (i % 6 == 0) {
            cache_monitor.record_miss("tree_cache");
        } else {
            cache_monitor.record_hit("tree_cache");
        }
    }
    cache_monitor.update_size("tree_cache", 10 * 1024 * 1024, 20 * 1024 * 1024);
    
    // Simulate blob cache
    for (int i = 0; i < 200; i++) {
        if (i % 3 == 0) {
            cache_monitor.record_miss("blob_cache");
        } else {
            cache_monitor.record_hit("blob_cache");
        }
        
        if (i % 20 == 0) {
            cache_monitor.record_eviction("blob_cache");
        }
    }
    cache_monitor.update_size("blob_cache", 80 * 1024 * 1024, 100 * 1024 * 1024);
    
    std::cout << cache_monitor.generate_cache_report() << std::endl;
}

void generate_optimization_suggestions() {
    std::cout << "=== Performance Optimization Suggestions ===\n\n";
    
    // Get current performance data
    auto& monitor = PerformanceMonitor::instance();
    
    // Create mock profiles for demonstration
    std::vector<OperationProfile> profiles;
    
    // Simulate some performance data
    OperationProfile slow_op;
    slow_op.operation_name = "slow_merge_operation";
    slow_op.metrics.execution_time = std::chrono::milliseconds(2000);
    slow_op.metrics.memory_usage = 150 * 1024 * 1024; // 150MB
    slow_op.metrics.disk_io_bytes = 100 * 1024 * 1024; // 100MB
    profiles.push_back(slow_op);
    
    OperationProfile memory_heavy_op;
    memory_heavy_op.operation_name = "memory_intensive_operation";
    memory_heavy_op.metrics.execution_time = std::chrono::milliseconds(500);
    memory_heavy_op.metrics.memory_usage = 200 * 1024 * 1024; // 200MB
    memory_heavy_op.metrics.disk_io_bytes = 10 * 1024 * 1024; // 10MB
    profiles.push_back(memory_heavy_op);
    
    // Generate suggestions
    auto suggestions = PerformanceOptimizer::analyze_performance(profiles);
    std::string report = PerformanceOptimizer::generate_optimization_report(suggestions);
    
    std::cout << report << std::endl;
    
    // Additional general recommendations
    std::cout << "General Recommendations:\n";
    std::cout << "1. Enable performance monitoring in production for continuous optimization\n";
    std::cout << "2. Use appropriate cache sizes based on available memory\n";
    std::cout << "3. Consider parallel processing for CPU-intensive operations\n";
    std::cout << "4. Monitor memory usage patterns to detect leaks early\n";
    std::cout << "5. Use compression for network operations to reduce bandwidth usage\n";
    std::cout << "6. Implement proper error handling to avoid performance degradation\n";
    std::cout << "7. Regular performance regression testing in CI/CD pipeline\n\n";
}

int main(int argc, char* argv[]) {
    auto& monitor = PerformanceMonitor::instance();
    monitor.set_enabled(true);
    
    if (argc == 1) {
        print_usage();
        return 0;
    }
    
    bool generate_report = false;
    bool detailed_report = false;
    bool clear_metrics = false;
    bool cache_report = false;
    bool optimize = false;
    int monitor_duration = 0;
    int threshold_ms = 1000;
    int memory_limit_mb = 100;
    
    // Parse command line arguments
    for (int i = 1; i < argc; i++) {
        std::string arg = argv[i];
        
        if (arg == "--help") {
            print_usage();
            return 0;
        } else if (arg == "--report") {
            generate_report = true;
        } else if (arg == "--detailed") {
            detailed_report = true;
        } else if (arg == "--clear") {
            clear_metrics = true;
        } else if (arg == "--cache-report") {
            cache_report = true;
        } else if (arg == "--optimize") {
            optimize = true;
        } else if (arg == "--monitor" && i + 1 < argc) {
            monitor_duration = std::stoi(argv[++i]);
        } else if (arg == "--threshold" && i + 1 < argc) {
            threshold_ms = std::stoi(argv[++i]);
        } else if (arg == "--memory-limit" && i + 1 < argc) {
            memory_limit_mb = std::stoi(argv[++i]);
        } else {
            std::cerr << "Unknown option: " << arg << std::endl;
            print_usage();
            return 1;
        }
    }
    
    // Set thresholds
    monitor.set_slow_operation_threshold(std::chrono::milliseconds(threshold_ms));
    monitor.set_memory_threshold(memory_limit_mb * 1024 * 1024);
    
    std::cout << "SVCS Performance Monitor Tool v2.0.0\n";
    std::cout << "=====================================\n\n";
    
    if (clear_metrics) {
        std::cout << "Clearing all performance metrics...\n";
        monitor.clear_metrics();
        std::cout << "Metrics cleared.\n\n";
    }
    
    if (monitor_duration > 0) {
        monitor_for_duration(monitor_duration);
    } else {
        // Run simulation if no monitoring duration specified
        simulate_operations();
    }
    
    if (generate_report) {
        std::cout << "=== Performance Report ===\n\n";
        std::string report = monitor.generate_report(detailed_report);
        std::cout << report << std::endl;
        
        // Show slow operations
        auto slow_ops = monitor.get_slow_operations();
        if (!slow_ops.empty()) {
            std::cout << "Slow Operations (>" << threshold_ms << "ms):\n";
            for (const auto& op : slow_ops) {
                std::cout << "  " << std::setw(30) << op.operation_name 
                         << ": " << std::setw(8) << op.metrics.execution_time.count() << "ms"
                         << " (Memory: " << std::setw(10) << (op.metrics.memory_usage / 1024) << "KB)"
                         << std::endl;
            }
            std::cout << std::endl;
        }
    }
    
    if (cache_report) {
        generate_cache_report();
    }
    
    if (optimize) {
        generate_optimization_suggestions();
    }
    
    // Final summary
    std::cout << "Performance monitoring session completed.\n";
    std::cout << "Current memory usage: " << (monitor.get_current_memory_usage() / 1024) << " KB\n";
    
    return 0;
}