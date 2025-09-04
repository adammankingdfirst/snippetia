# Performance Optimization Guide

This guide provides comprehensive information on optimizing SVCS performance for various use cases and environments.

## Table of Contents

1. [Performance Monitoring](#performance-monitoring)
2. [Memory Optimization](#memory-optimization)
3. [Disk I/O Optimization](#disk-io-optimization)
4. [Network Optimization](#network-optimization)
5. [Cache Optimization](#cache-optimization)
6. [Large Repository Handling](#large-repository-handling)
7. [Benchmarking](#benchmarking)
8. [Troubleshooting](#troubleshooting)

## Performance Monitoring

### Setting Up Monitoring

```cpp
#include "performance_monitor.hpp"

// Enable monitoring globally
auto& monitor = PerformanceMonitor::instance();
monitor.set_enabled(true);

// Set performance thresholds
monitor.set_slow_operation_threshold(std::chrono::milliseconds(1000)); // 1 second
monitor.set_memory_threshold(500 * 1024 * 1024); // 500MB
```

### Profiling Operations

```cpp
// Profile critical operations
void commit_files() {
    PROFILE_FUNCTION();
    
    {
        PROFILE_OPERATION("index_files");
        // Index file operations
    }
    
    {
        PROFILE_OPERATION("create_tree");
        // Tree creation
    }
    
    {
        PROFILE_OPERATION("write_commit");
        // Commit writing
    }
}
```

### Analyzing Performance Data

```cpp
// Generate performance report
std::string report = monitor.generate_report(true);
std::cout << report << std::endl;

// Get specific operation metrics
auto commit_metrics = monitor.get_operation_metrics("commit_operation");
std::cout << "Average commit time: " << commit_metrics.execution_time.count() << "ms" << std::endl;

// Identify slow operations
auto slow_ops = monitor.get_slow_operations();
for (const auto& op : slow_ops) {
    std::cout << "Slow operation: " << op.operation_name 
              << " took " << op.metrics.execution_time.count() << "ms" << std::endl;
}
```

### Custom Performance Metrics

```cpp
void process_large_file(const std::string& filename) {
    ScopedProfiler profiler("process_large_file");
    
    // Add custom metrics
    size_t file_size = get_file_size(filename);
    profiler.add_custom_metric("file_size_mb", file_size / (1024.0 * 1024.0));
    
    // Process file
    auto lines = read_file_lines(filename);
    profiler.add_custom_metric("line_count", lines.size());
    
    // More processing...
}
```

## Memory Optimization

### Memory Tracking

```cpp
// Track memory allocations
class MemoryTracker {
public:
    void* allocate(size_t size) {
        void* ptr = malloc(size);
        if (ptr) {
            PerformanceMonitor::instance().track_memory_allocation(size);
            allocations_[ptr] = size;
        }
        return ptr;
    }
    
    void deallocate(void* ptr) {
        auto it = allocations_.find(ptr);
        if (it != allocations_.end()) {
            PerformanceMonitor::instance().track_memory_deallocation(it->second);
            allocations_.erase(it);
            free(ptr);
        }
    }
    
private:
    std::unordered_map<void*, size_t> allocations_;
};
```

### Memory Pool Usage

```cpp
// Use memory pools for frequent allocations
class ObjectPool {
public:
    template<typename T>
    T* acquire() {
        if (pool_.empty()) {
            return new T();
        }
        
        T* obj = static_cast<T*>(pool_.back());
        pool_.pop_back();
        return obj;
    }
    
    template<typename T>
    void release(T* obj) {
        obj->reset(); // Reset object state
        pool_.push_back(obj);
    }
    
private:
    std::vector<void*> pool_;
};
```

### Memory-Efficient Data Structures

```cpp
// Use compact data structures for large datasets
struct CompactCommit {
    uint32_t hash_prefix;     // First 4 bytes of hash
    uint32_t timestamp;       // Unix timestamp
    uint16_t author_id;       // ID instead of full name
    uint16_t message_offset;  // Offset in message pool
    
    // Full hash stored separately in hash table
};

// String interning for repeated strings
class StringPool {
public:
    const char* intern(const std::string& str) {
        auto it = pool_.find(str);
        if (it != pool_.end()) {
            return it->second.c_str();
        }
        
        auto result = pool_.emplace(str, str);
        return result.first->second.c_str();
    }
    
private:
    std::unordered_map<std::string, std::string> pool_;
};
```

## Disk I/O Optimization

### Buffered I/O

```cpp
class BufferedWriter {
public:
    BufferedWriter(const std::string& filename, size_t buffer_size = 64 * 1024)
        : file_(filename, std::ios::binary), buffer_size_(buffer_size) {
        buffer_.reserve(buffer_size_);
    }
    
    void write(const void* data, size_t size) {
        const char* bytes = static_cast<const char*>(data);
        
        if (buffer_.size() + size > buffer_size_) {
            flush();
        }
        
        buffer_.insert(buffer_.end(), bytes, bytes + size);
        
        // Track I/O
        PerformanceMonitor::instance().track_disk_write(size);
    }
    
    void flush() {
        if (!buffer_.empty()) {
            file_.write(buffer_.data(), buffer_.size());
            buffer_.clear();
        }
    }
    
private:
    std::ofstream file_;
    std::vector<char> buffer_;
    size_t buffer_size_;
};
```

### Asynchronous I/O

```cpp
class AsyncFileWriter {
public:
    AsyncFileWriter(const std::string& filename) : filename_(filename) {
        writer_thread_ = std::thread(&AsyncFileWriter::writer_loop, this);
    }
    
    ~AsyncFileWriter() {
        {
            std::lock_guard<std::mutex> lock(queue_mutex_);
            should_stop_ = true;
        }
        queue_cv_.notify_one();
        writer_thread_.join();
    }
    
    void write_async(std::vector<char> data) {
        {
            std::lock_guard<std::mutex> lock(queue_mutex_);
            write_queue_.push(std::move(data));
        }
        queue_cv_.notify_one();
    }
    
private:
    void writer_loop() {
        std::ofstream file(filename_, std::ios::binary);
        
        while (true) {
            std::unique_lock<std::mutex> lock(queue_mutex_);
            queue_cv_.wait(lock, [this] { return !write_queue_.empty() || should_stop_; });
            
            if (should_stop_ && write_queue_.empty()) {
                break;
            }
            
            while (!write_queue_.empty()) {
                auto data = std::move(write_queue_.front());
                write_queue_.pop();
                lock.unlock();
                
                file.write(data.data(), data.size());
                PerformanceMonitor::instance().track_disk_write(data.size());
                
                lock.lock();
            }
        }
    }
    
    std::string filename_;
    std::thread writer_thread_;
    std::queue<std::vector<char>> write_queue_;
    std::mutex queue_mutex_;
    std::condition_variable queue_cv_;
    bool should_stop_ = false;
};
```

### File System Optimization

```cpp
// Batch file operations
class BatchFileOperations {
public:
    void add_file_operation(const std::string& src, const std::string& dst) {
        operations_.emplace_back(src, dst);
    }
    
    void execute_batch() {
        PROFILE_OPERATION("batch_file_operations");
        
        // Sort operations by source directory to improve locality
        std::sort(operations_.begin(), operations_.end(),
                 [](const auto& a, const auto& b) {
                     return std::filesystem::path(a.first).parent_path() <
                            std::filesystem::path(b.first).parent_path();
                 });
        
        for (const auto& [src, dst] : operations_) {
            std::filesystem::copy_file(src, dst);
        }
        
        operations_.clear();
    }
    
private:
    std::vector<std::pair<std::string, std::string>> operations_;
};
```

## Network Optimization

### Connection Pooling

```cpp
class ConnectionPool {
public:
    class Connection {
    public:
        void send_data(const std::vector<char>& data) {
            // Send data and track network I/O
            PerformanceMonitor::instance().track_network_send(data.size());
        }
        
        std::vector<char> receive_data() {
            std::vector<char> data = /* receive from network */;
            PerformanceMonitor::instance().track_network_receive(data.size());
            return data;
        }
    };
    
    std::shared_ptr<Connection> acquire_connection(const std::string& host) {
        std::lock_guard<std::mutex> lock(pool_mutex_);
        
        auto it = pools_.find(host);
        if (it != pools_.end() && !it->second.empty()) {
            auto conn = it->second.back();
            it->second.pop_back();
            return conn;
        }
        
        return std::make_shared<Connection>(/* create new connection */);
    }
    
    void release_connection(const std::string& host, std::shared_ptr<Connection> conn) {
        std::lock_guard<std::mutex> lock(pool_mutex_);
        pools_[host].push_back(conn);
    }
    
private:
    std::unordered_map<std::string, std::vector<std::shared_ptr<Connection>>> pools_;
    std::mutex pool_mutex_;
};
```

### Compression

```cpp
class CompressionEngine {
public:
    std::vector<char> compress(const std::vector<char>& data) {
        PROFILE_OPERATION("compress_data");
        
        // Use fast compression algorithm (e.g., LZ4)
        std::vector<char> compressed = lz4_compress(data);
        
        // Track compression ratio
        double ratio = (double)compressed.size() / data.size();
        
        ScopedProfiler profiler("compression_stats");
        profiler.add_custom_metric("compression_ratio", ratio);
        profiler.add_custom_metric("original_size", data.size());
        profiler.add_custom_metric("compressed_size", compressed.size());
        
        return compressed;
    }
    
    std::vector<char> decompress(const std::vector<char>& compressed_data) {
        PROFILE_OPERATION("decompress_data");
        return lz4_decompress(compressed_data);
    }
};
```

### Bandwidth Management

```cpp
class BandwidthLimiter {
public:
    BandwidthLimiter(int max_kbps) : max_bytes_per_second_(max_kbps * 1024) {}
    
    void wait_for_bandwidth(size_t bytes) {
        auto now = std::chrono::steady_clock::now();
        
        // Reset counter every second
        if (now - last_reset_ >= std::chrono::seconds(1)) {
            bytes_this_second_ = 0;
            last_reset_ = now;
        }
        
        bytes_this_second_ += bytes;
        
        if (bytes_this_second_ > max_bytes_per_second_) {
            auto sleep_time = std::chrono::seconds(1) - (now - last_reset_);
            std::this_thread::sleep_for(sleep_time);
        }
    }
    
private:
    size_t max_bytes_per_second_;
    size_t bytes_this_second_ = 0;
    std::chrono::steady_clock::time_point last_reset_;
};
```

## Cache Optimization

### Multi-Level Caching

```cpp
template<typename Key, typename Value>
class MultiLevelCache {
public:
    MultiLevelCache(size_t l1_size, size_t l2_size) 
        : l1_cache_(l1_size), l2_cache_(l2_size) {}
    
    std::optional<Value> get(const Key& key) {
        CacheMonitor& monitor = /* get cache monitor */;
        
        // Try L1 cache first
        auto l1_result = l1_cache_.get(key);
        if (l1_result) {
            monitor.record_hit("l1_cache");
            return l1_result;
        }
        
        // Try L2 cache
        auto l2_result = l2_cache_.get(key);
        if (l2_result) {
            monitor.record_hit("l2_cache");
            // Promote to L1
            l1_cache_.put(key, *l2_result);
            return l2_result;
        }
        
        monitor.record_miss("l1_cache");
        monitor.record_miss("l2_cache");
        return std::nullopt;
    }
    
    void put(const Key& key, const Value& value) {
        l1_cache_.put(key, value);
    }
    
private:
    LRUCache<Key, Value> l1_cache_;
    LRUCache<Key, Value> l2_cache_;
};
```

### Cache Warming

```cpp
class CacheWarmer {
public:
    void warm_object_cache() {
        PROFILE_OPERATION("warm_object_cache");
        
        // Pre-load frequently accessed objects
        auto recent_commits = get_recent_commits(100);
        
        for (const auto& commit_hash : recent_commits) {
            // Load commit object into cache
            load_commit_object(commit_hash);
            
            // Load associated tree objects
            auto tree_hash = get_commit_tree(commit_hash);
            load_tree_object(tree_hash);
        }
    }
    
    void warm_diff_cache() {
        PROFILE_OPERATION("warm_diff_cache");
        
        // Pre-compute diffs for recent commits
        auto recent_commits = get_recent_commits(50);
        
        for (size_t i = 1; i < recent_commits.size(); ++i) {
            compute_diff(recent_commits[i-1], recent_commits[i]);
        }
    }
};
```

### Adaptive Cache Sizing

```cpp
class AdaptiveCacheManager {
public:
    void adjust_cache_sizes() {
        auto& cache_monitor = /* get cache monitor */;
        
        auto object_stats = cache_monitor.get_stats("object_cache");
        auto tree_stats = cache_monitor.get_stats("tree_cache");
        auto blob_stats = cache_monitor.get_stats("blob_cache");
        
        // Increase cache size for low hit ratios
        if (object_stats.hit_ratio() < 0.8) {
            increase_cache_size("object_cache", 1.2);
        }
        
        // Decrease cache size for very high hit ratios (over-provisioned)
        if (tree_stats.hit_ratio() > 0.95) {
            decrease_cache_size("tree_cache", 0.9);
        }
    }
    
private:
    void increase_cache_size(const std::string& cache_name, double factor) {
        // Implementation to resize cache
    }
    
    void decrease_cache_size(const std::string& cache_name, double factor) {
        // Implementation to resize cache
    }
};
```

## Large Repository Handling

### Chunked Processing

```cpp
class ChunkedProcessor {
public:
    void process_large_repository(const std::string& repo_path) {
        PROFILE_OPERATION("process_large_repository");
        
        auto all_files = get_all_files(repo_path);
        const size_t chunk_size = 1000;
        
        for (size_t i = 0; i < all_files.size(); i += chunk_size) {
            size_t end = std::min(i + chunk_size, all_files.size());
            
            std::vector<std::string> chunk(all_files.begin() + i, all_files.begin() + end);
            process_file_chunk(chunk);
            
            // Allow other operations to run
            std::this_thread::yield();
        }
    }
    
private:
    void process_file_chunk(const std::vector<std::string>& files) {
        PROFILE_OPERATION("process_file_chunk");
        
        for (const auto& file : files) {
            process_single_file(file);
        }
    }
};
```

### Parallel Processing

```cpp
class ParallelProcessor {
public:
    ParallelProcessor(size_t num_threads = std::thread::hardware_concurrency()) 
        : thread_pool_(num_threads) {}
    
    void process_files_parallel(const std::vector<std::string>& files) {
        PROFILE_OPERATION("parallel_file_processing");
        
        std::vector<std::future<void>> futures;
        
        for (const auto& file : files) {
            futures.push_back(
                thread_pool_.enqueue([this, file] {
                    PROFILE_OPERATION("process_single_file_parallel");
                    process_single_file(file);
                })
            );
        }
        
        // Wait for all tasks to complete
        for (auto& future : futures) {
            future.wait();
        }
    }
    
private:
    ThreadPool thread_pool_;
    
    void process_single_file(const std::string& file) {
        // File processing logic
    }
};
```

### Memory-Mapped Files

```cpp
class MemoryMappedFile {
public:
    MemoryMappedFile(const std::string& filename) {
        file_descriptor_ = open(filename.c_str(), O_RDONLY);
        if (file_descriptor_ == -1) {
            throw std::runtime_error("Failed to open file");
        }
        
        struct stat file_stat;
        if (fstat(file_descriptor_, &file_stat) == -1) {
            close(file_descriptor_);
            throw std::runtime_error("Failed to get file size");
        }
        
        file_size_ = file_stat.st_size;
        
        mapped_data_ = mmap(nullptr, file_size_, PROT_READ, MAP_PRIVATE, file_descriptor_, 0);
        if (mapped_data_ == MAP_FAILED) {
            close(file_descriptor_);
            throw std::runtime_error("Failed to map file");
        }
    }
    
    ~MemoryMappedFile() {
        if (mapped_data_ != MAP_FAILED) {
            munmap(mapped_data_, file_size_);
        }
        if (file_descriptor_ != -1) {
            close(file_descriptor_);
        }
    }
    
    const char* data() const { return static_cast<const char*>(mapped_data_); }
    size_t size() const { return file_size_; }
    
private:
    int file_descriptor_ = -1;
    void* mapped_data_ = MAP_FAILED;
    size_t file_size_ = 0;
};
```

## Benchmarking

### Benchmark Framework

```cpp
class Benchmark {
public:
    template<typename Func>
    static void run_benchmark(const std::string& name, Func&& func, int iterations = 1000) {
        std::vector<std::chrono::nanoseconds> times;
        times.reserve(iterations);
        
        // Warm up
        for (int i = 0; i < 10; ++i) {
            func();
        }
        
        // Actual benchmark
        for (int i = 0; i < iterations; ++i) {
            auto start = std::chrono::high_resolution_clock::now();
            func();
            auto end = std::chrono::high_resolution_clock::now();
            
            times.push_back(std::chrono::duration_cast<std::chrono::nanoseconds>(end - start));
        }
        
        // Calculate statistics
        auto min_time = *std::min_element(times.begin(), times.end());
        auto max_time = *std::max_element(times.begin(), times.end());
        auto avg_time = std::accumulate(times.begin(), times.end(), std::chrono::nanoseconds(0)) / iterations;
        
        std::cout << "Benchmark: " << name << std::endl;
        std::cout << "  Min: " << min_time.count() << " ns" << std::endl;
        std::cout << "  Max: " << max_time.count() << " ns" << std::endl;
        std::cout << "  Avg: " << avg_time.count() << " ns" << std::endl;
    }
};
```

### Performance Regression Testing

```cpp
class RegressionTester {
public:
    void run_regression_tests() {
        // Load baseline performance data
        load_baseline_data();
        
        // Run current benchmarks
        run_current_benchmarks();
        
        // Compare and report
        compare_and_report();
    }
    
private:
    void load_baseline_data() {
        // Load from performance_baseline.json
        std::ifstream file("performance_baseline.json");
        // Parse JSON and populate baseline_data_
    }
    
    void run_current_benchmarks() {
        Benchmark::run_benchmark("commit_operation", []() {
            // Commit benchmark
        });
        
        Benchmark::run_benchmark("diff_operation", []() {
            // Diff benchmark
        });
        
        // Store results in current_data_
    }
    
    void compare_and_report() {
        for (const auto& [operation, baseline_time] : baseline_data_) {
            auto current_time = current_data_[operation];
            double regression = (double)current_time / baseline_time;
            
            if (regression > 1.1) { // 10% regression threshold
                std::cout << "REGRESSION: " << operation 
                         << " is " << (regression - 1.0) * 100 << "% slower" << std::endl;
            }
        }
    }
    
    std::map<std::string, std::chrono::nanoseconds> baseline_data_;
    std::map<std::string, std::chrono::nanoseconds> current_data_;
};
```

## Troubleshooting

### Performance Debugging

```cpp
class PerformanceDebugger {
public:
    void debug_slow_operation(const std::string& operation_name) {
        auto& monitor = PerformanceMonitor::instance();
        
        // Get detailed metrics
        auto metrics = monitor.get_operation_metrics(operation_name);
        
        std::cout << "Operation: " << operation_name << std::endl;
        std::cout << "Execution time: " << metrics.execution_time.count() << "ms" << std::endl;
        std::cout << "Memory usage: " << metrics.memory_usage << " bytes" << std::endl;
        std::cout << "Disk I/O: " << metrics.disk_io_bytes << " bytes" << std::endl;
        std::cout << "Network I/O: " << metrics.network_io_bytes << " bytes" << std::endl;
        
        // Analyze custom metrics
        for (const auto& [name, value] : metrics.custom_metrics) {
            std::cout << "Custom metric " << name << ": " << value << std::endl;
        }
        
        // Generate optimization suggestions
        auto suggestions = analyze_bottlenecks(metrics);
        for (const auto& suggestion : suggestions) {
            std::cout << "Suggestion: " << suggestion << std::endl;
        }
    }
    
private:
    std::vector<std::string> analyze_bottlenecks(const PerformanceMetrics& metrics) {
        std::vector<std::string> suggestions;
        
        if (metrics.memory_usage > 100 * 1024 * 1024) { // 100MB
            suggestions.push_back("Consider reducing memory usage or using streaming");
        }
        
        if (metrics.disk_io_bytes > 50 * 1024 * 1024) { // 50MB
            suggestions.push_back("Consider using buffered I/O or compression");
        }
        
        if (metrics.execution_time > std::chrono::milliseconds(5000)) { // 5 seconds
            suggestions.push_back("Consider parallelization or algorithm optimization");
        }
        
        return suggestions;
    }
};
```

### Memory Leak Detection

```cpp
class MemoryLeakDetector {
public:
    void start_monitoring() {
        initial_memory_ = get_current_memory_usage();
        monitoring_ = true;
    }
    
    void stop_monitoring() {
        if (!monitoring_) return;
        
        final_memory_ = get_current_memory_usage();
        monitoring_ = false;
        
        size_t leaked = final_memory_ - initial_memory_;
        if (leaked > leak_threshold_) {
            std::cout << "MEMORY LEAK DETECTED: " << leaked << " bytes leaked" << std::endl;
            
            // Generate leak report
            generate_leak_report();
        }
    }
    
private:
    size_t get_current_memory_usage() {
        return PerformanceMonitor::instance().get_current_memory_usage();
    }
    
    void generate_leak_report() {
        // Analyze allocation patterns and generate report
        std::cout << "Leak analysis:" << std::endl;
        std::cout << "Initial memory: " << initial_memory_ << " bytes" << std::endl;
        std::cout << "Final memory: " << final_memory_ << " bytes" << std::endl;
        std::cout << "Difference: " << (final_memory_ - initial_memory_) << " bytes" << std::endl;
    }
    
    size_t initial_memory_ = 0;
    size_t final_memory_ = 0;
    size_t leak_threshold_ = 1024; // 1KB threshold
    bool monitoring_ = false;
};
```

### Performance Profiling Tools

```cpp
// Integration with external profiling tools
class ExternalProfiler {
public:
    void start_profiling() {
        #ifdef ENABLE_PERF_PROFILING
        // Start perf profiling
        system("perf record -g -p " + std::to_string(getpid()) + " &");
        #endif
        
        #ifdef ENABLE_VALGRIND_PROFILING
        // Valgrind integration
        CALLGRIND_START_INSTRUMENTATION;
        #endif
    }
    
    void stop_profiling() {
        #ifdef ENABLE_PERF_PROFILING
        system("pkill perf");
        #endif
        
        #ifdef ENABLE_VALGRIND_PROFILING
        CALLGRIND_STOP_INSTRUMENTATION;
        CALLGRIND_DUMP_STATS;
        #endif
    }
};
```

## Best Practices Summary

1. **Always Profile First**: Use performance monitoring before optimizing
2. **Optimize Hot Paths**: Focus on frequently executed code
3. **Memory Management**: Use pools, avoid frequent allocations
4. **I/O Optimization**: Use buffering, async I/O, and compression
5. **Cache Effectively**: Implement multi-level caching with monitoring
6. **Parallel Processing**: Utilize multiple cores for independent tasks
7. **Monitor Continuously**: Set up automated performance regression testing
8. **Measure Everything**: Track custom metrics relevant to your use case

## Configuration Examples

### High-Performance Configuration

```cpp
// Configure for maximum performance
PerformanceMonitor::instance().set_enabled(true);

// Large caches
configure_cache("object_cache", 100 * 1024 * 1024); // 100MB
configure_cache("tree_cache", 50 * 1024 * 1024);    // 50MB
configure_cache("blob_cache", 200 * 1024 * 1024);   // 200MB

// Parallel processing
set_thread_pool_size(std::thread::hardware_concurrency());

// Aggressive compression
enable_compression(true, CompressionLevel::FAST);

// Large I/O buffers
set_io_buffer_size(1024 * 1024); // 1MB
```

### Memory-Constrained Configuration

```cpp
// Configure for low memory usage
configure_cache("object_cache", 10 * 1024 * 1024);  // 10MB
configure_cache("tree_cache", 5 * 1024 * 1024);     // 5MB
configure_cache("blob_cache", 20 * 1024 * 1024);    // 20MB

// Smaller thread pool
set_thread_pool_size(2);

// Streaming processing
enable_streaming_mode(true);

// Smaller I/O buffers
set_io_buffer_size(64 * 1024); // 64KB
```