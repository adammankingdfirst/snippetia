# Advanced Features Guide

This guide covers the advanced features of the SVCS (Simple Version Control System) including performance monitoring, patch management, cloud synchronization, and more.

## Table of Contents

1. [Performance Monitoring](#performance-monitoring)
2. [Patch Engine](#patch-engine)
3. [Rebase Engine](#rebase-engine)
4. [Cloud Synchronization](#cloud-synchronization)
5. [Backup Management](#backup-management)
6. [Advanced Merge Strategies](#advanced-merge-strategies)
7. [Snippetia Integration](#snippetia-integration)

## Performance Monitoring

SVCS includes comprehensive performance monitoring capabilities to help optimize your workflow and identify bottlenecks.

### Basic Usage

```cpp
#include "performance_monitor.hpp"

// Profile a function
void my_function() {
    PROFILE_FUNCTION();
    // Your code here
}

// Profile a specific operation
{
    PROFILE_OPERATION("custom_operation");
    // Your code here
}

// Manual profiling
auto& monitor = PerformanceMonitor::instance();
auto profile = monitor.start_operation("manual_operation");
// Your code here
monitor.end_operation(profile);
```

### Memory Tracking

```cpp
auto& monitor = PerformanceMonitor::instance();

// Track memory allocations
monitor.track_memory_allocation(1024);
monitor.track_memory_deallocation(512);

// Get current memory usage
size_t current_usage = monitor.get_current_memory_usage();
```

### Performance Reports

```cpp
// Generate detailed performance report
std::string report = monitor.generate_report(true);
std::cout << report << std::endl;

// Get metrics for specific operation
auto metrics = monitor.get_operation_metrics("commit_operation");
std::cout << "Execution time: " << metrics.execution_time.count() << "ms" << std::endl;
```

### Cache Monitoring

```cpp
CacheMonitor cache_monitor;

// Record cache operations
cache_monitor.record_hit("object_cache");
cache_monitor.record_miss("object_cache");
cache_monitor.record_eviction("object_cache");

// Get cache statistics
auto stats = cache_monitor.get_stats("object_cache");
std::cout << "Hit ratio: " << stats.hit_ratio() << std::endl;
```

## Patch Engine

The patch engine provides advanced diff and patch capabilities for fine-grained change management.

### Generating Patches

```cpp
#include "patch_engine.hpp"

// Generate patches between two trees
auto patches = PatchEngine::generate_patches("old_tree_hash", "new_tree_hash");

// Calculate patch statistics
auto stats = PatchEngine::calculate_stats(patches);
std::cout << "Files changed: " << stats.files_changed << std::endl;
std::cout << "Insertions: " << stats.insertions << std::endl;
std::cout << "Deletions: " << stats.deletions << std::endl;
```

### Applying Patches

```cpp
// Apply patches to target directory
bool success = PatchEngine::apply_patches(patches, "/path/to/target", false);

// Dry run (don't actually apply)
bool would_succeed = PatchEngine::apply_patches(patches, "/path/to/target", true);
```

### Patch Formatting

```cpp
// Format patch for display
for (const auto& patch : patches) {
    std::string formatted = PatchEngine::format_patch(patch, true); // with colors
    std::cout << formatted << std::endl;
}

// Generate unified diff format
std::string unified_diff = PatchEngine::format_unified_diff(patch);
```

### Advanced Merge Strategies

```cpp
AdvancedMergeEngine::MergeOptions options;
options.strategy = AdvancedMergeEngine::MergeStrategy::RECURSIVE;
options.ignore_whitespace = true;
options.find_renames = true;
options.rename_threshold = 70;

bool success = AdvancedMergeEngine::merge_commits(
    "base_commit", "our_commit", "their_commit", options
);
```

## Rebase Engine

Interactive and automatic rebasing capabilities for clean commit history management.

### Interactive Rebase

```cpp
#include "rebase_engine.hpp"

// Start interactive rebase
std::vector<RebaseStep> steps = {
    {RebaseStep::PICK, "commit1", "First commit"},
    {RebaseStep::SQUASH, "commit2", "Second commit"},
    {RebaseStep::REWORD, "commit3", "New commit message"}
};

bool success = RebaseEngine::start_interactive_rebase("upstream_branch", "HEAD", steps);
```

### Rebase Control

```cpp
// Continue rebase after resolving conflicts
RebaseEngine::continue_rebase();

// Abort rebase and return to original state
RebaseEngine::abort_rebase();

// Skip current problematic commit
RebaseEngine::skip_rebase_step();

// Check rebase status
if (RebaseEngine::is_rebase_in_progress()) {
    auto state = RebaseEngine::get_rebase_state();
    std::cout << "Step " << state.current_step << " of " << state.steps.size() << std::endl;
}
```

### Commit Manipulation

```cpp
// Split a commit into multiple commits
std::vector<std::vector<std::string>> file_groups = {
    {"file1.cpp", "file1.hpp"},
    {"file2.cpp", "file2.hpp"}
};
auto new_commits = CommitManipulator::split_commit("commit_hash", file_groups);

// Combine multiple commits
std::string combined = CommitManipulator::combine_commits(
    {"commit1", "commit2", "commit3"}, 
    "Combined commit message"
);

// Amend last commit
CommitManipulator::amend_commit({"new_file.txt"}, "Updated commit message");
```

## Cloud Synchronization

Advanced cloud synchronization with conflict resolution and collaboration features.

### Basic Setup

```cpp
#include "cloud_sync_engine.hpp"

CloudSyncEngine::SyncConfig config;
config.server_url = "https://api.snippetia.com";
config.auth_token = "your_auth_token";
config.repository_id = "your_repo_id";
config.auto_sync = true;
config.sync_interval_seconds = 300; // 5 minutes

CloudSyncEngine sync_engine(config);
```

### Synchronization Operations

```cpp
// Manual sync
auto sync_future = sync_engine.sync_repository();
bool success = sync_future.get();

// Push only
auto push_future = sync_engine.push_changes();

// Pull only
auto pull_future = sync_engine.pull_changes();
```

### Conflict Resolution

```cpp
// Get pending conflicts
auto conflicts = sync_engine.get_pending_conflicts();

for (const auto& conflict : conflicts) {
    std::cout << "Conflict in: " << conflict.file_path << std::endl;
    std::cout << "Type: " << conflict.conflict_type << std::endl;
    
    // Resolve conflict (use local version)
    sync_engine.resolve_conflict(conflict, "use_local");
    
    // Or use remote version
    // sync_engine.resolve_conflict(conflict, "use_remote");
    
    // Or merge manually
    // sync_engine.resolve_conflict(conflict, "manual_merge");
}
```

### Auto-sync and Monitoring

```cpp
// Start auto-sync
sync_engine.start_auto_sync();

// Set up status callback
sync_engine.set_sync_callback([](const SyncStatus& status) {
    switch (status.state) {
        case SyncStatus::SYNCING:
            std::cout << "Syncing: " << status.files_synced 
                     << "/" << status.files_to_sync << std::endl;
            break;
        case SyncStatus::CONFLICT:
            std::cout << "Conflicts detected: " << status.conflicts.size() << std::endl;
            break;
        case SyncStatus::ERROR:
            std::cout << "Sync error: " << status.last_error << std::endl;
            break;
    }
});
```

### Collaboration Features

```cpp
// Get active collaborators
auto collaborators = sync_engine.get_active_collaborators();
for (const auto& collab : collaborators) {
    std::cout << collab.username << " active on: ";
    for (const auto& branch : collab.active_branches) {
        std::cout << branch << " ";
    }
    std::cout << std::endl;
}

// Lock files for exclusive editing
sync_engine.lock_file("important_file.cpp", "Critical bug fix in progress");

// Get locked files
auto locked_files = sync_engine.get_locked_files();
```

## Backup Management

Automated and manual backup capabilities with incremental backup support.

### Creating Backups

```cpp
#include "cloud_sync_engine.hpp"

BackupManager backup_manager;

// Create full backup
auto backup_future = backup_manager.create_full_backup("Weekly backup");
std::string backup_id = backup_future.get();

// Create incremental backup
auto incremental_future = backup_manager.create_incremental_backup(backup_id);
std::string incremental_id = incremental_future.get();
```

### Backup Management

```cpp
// List all backups
auto backups = backup_manager.list_backups();
for (const auto& backup : backups) {
    std::cout << "Backup: " << backup.backup_id << std::endl;
    std::cout << "Created: " << backup.created_at << std::endl;
    std::cout << "Size: " << backup.size_bytes << " bytes" << std::endl;
    std::cout << "Incremental: " << (backup.is_incremental ? "Yes" : "No") << std::endl;
}

// Verify backup integrity
bool is_valid = backup_manager.verify_backup(backup_id);
```

### Restore Operations

```cpp
// Restore from backup
auto restore_future = backup_manager.restore_from_backup(backup_id, "/restore/path");
bool success = restore_future.get();

// Schedule automatic backups
backup_manager.schedule_automatic_backups(24); // Every 24 hours
```

## Snippetia Integration

Deep integration with the Snippetia platform for code snippet management and sharing.

### Snippet Operations

```cpp
SnippetiaIntegration integration;

// Create snippet metadata
SnippetiaIntegration::SnippetMetadata metadata;
metadata.title = "Useful Algorithm";
metadata.description = "A fast sorting algorithm implementation";
metadata.language = "cpp";
metadata.tags = {"algorithm", "sorting", "performance"};
metadata.is_public = true;

// Create snippet
auto create_future = integration.create_snippet(metadata, source_code);
std::string snippet_id = create_future.get();

// Update snippet
metadata.description = "Updated description";
auto update_future = integration.update_snippet(snippet_id, metadata, updated_code);
```

### Repository Integration

```cpp
// Sync entire repository as snippets
auto sync_future = integration.sync_repository_as_snippets();
bool success = sync_future.get();

// Import snippets back to repository
auto import_future = integration.import_snippets_to_repository();
```

### Version Tracking

```cpp
// Create snippet version linked to commit
auto version_future = integration.create_snippet_version(snippet_id, "commit_hash");

// Get all versions
auto versions = integration.get_snippet_versions(snippet_id);
```

### Social Features

```cpp
// Share repository publicly
auto share_future = integration.share_repository();

// Fork repository
auto fork_future = integration.fork_repository("source_repo_id");

// Get repository forks
auto forks_future = integration.get_repository_forks();
auto forks = forks_future.get();
```

### Analytics Integration

```cpp
// Track repository activity
std::map<std::string, std::string> metadata;
metadata["commit_count"] = "5";
metadata["files_changed"] = "12";

integration.track_repository_activity("bulk_commit", metadata);
```

## Performance Optimization

### Best Practices

1. **Use Performance Monitoring**: Always profile critical operations to identify bottlenecks.

2. **Cache Management**: Monitor cache hit ratios and adjust cache sizes accordingly.

3. **Memory Management**: Track memory usage to prevent leaks and optimize allocation patterns.

4. **Batch Operations**: Group related operations to reduce overhead.

5. **Selective Sync**: Use sync filters to avoid syncing unnecessary files.

### Configuration Tuning

```cpp
// Optimize sync performance
CloudSyncEngine::SyncConfig config;
config.sync_interval_seconds = 600; // Reduce sync frequency
sync_engine.enable_compression(true); // Enable compression
sync_engine.set_bandwidth_limit(1000); // Limit to 1MB/s

// Optimize cache settings
CacheMonitor cache_monitor;
cache_monitor.update_size("object_cache", current_size, 50 * 1024 * 1024); // 50MB max
```

### Performance Monitoring Setup

```cpp
// Set up performance thresholds
auto& monitor = PerformanceMonitor::instance();
monitor.set_slow_operation_threshold(std::chrono::milliseconds(500));
monitor.set_memory_threshold(100 * 1024 * 1024); // 100MB

// Generate optimization suggestions
auto profiles = /* get operation profiles */;
auto suggestions = PerformanceOptimizer::analyze_performance(profiles);
std::string report = PerformanceOptimizer::generate_optimization_report(suggestions);
```

## Troubleshooting

### Common Issues

1. **Sync Conflicts**: Use the conflict resolution API to handle merge conflicts automatically or manually.

2. **Performance Issues**: Enable performance monitoring to identify slow operations.

3. **Memory Leaks**: Use memory tracking to detect and fix memory leaks.

4. **Network Issues**: Enable offline mode during network outages.

### Debug Mode

```cpp
// Enable detailed logging
PerformanceMonitor::instance().set_enabled(true);

// Enable sync debugging
CloudSyncEngine::SyncConfig config;
config.debug_mode = true; // If available in implementation
```

### Error Handling

```cpp
try {
    auto result = sync_engine.sync_repository().get();
} catch (const std::exception& e) {
    std::cerr << "Sync error: " << e.what() << std::endl;
    
    // Check sync status for more details
    auto status = sync_engine.get_sync_status();
    if (status.state == SyncStatus::ERROR) {
        std::cerr << "Last error: " << status.last_error << std::endl;
    }
}
```

## API Reference

For complete API documentation, see:
- [Performance Monitor API](api/performance_monitor.md)
- [Patch Engine API](api/patch_engine.md)
- [Cloud Sync API](api/cloud_sync.md)
- [Backup Manager API](api/backup_manager.md)
- [Snippetia Integration API](api/snippetia_integration.md)