#pragma once

#include <string>
#include <vector>
#include <map>
#include <functional>
#include <future>
#include <memory>

namespace svcs {

struct SyncConflict {
    std::string file_path;
    std::string local_hash;
    std::string remote_hash;
    std::string base_hash;
    std::chrono::system_clock::time_point local_timestamp;
    std::chrono::system_clock::time_point remote_timestamp;
    std::string conflict_type; // "content", "rename", "delete"
};

struct SyncStatus {
    enum State {
        IDLE,
        SYNCING,
        CONFLICT,
        ERROR,
        OFFLINE
    } state = IDLE;
    
    int files_to_sync = 0;
    int files_synced = 0;
    std::vector<SyncConflict> conflicts;
    std::string last_error;
    std::chrono::system_clock::time_point last_sync;
};

class CloudSyncEngine {
public:
    // Configuration
    struct SyncConfig {
        std::string server_url;
        std::string auth_token;
        std::string repository_id;
        bool auto_sync = true;
        int sync_interval_seconds = 300; // 5 minutes
        bool sync_on_commit = true;
        bool sync_on_branch_switch = true;
        std::vector<std::string> ignore_patterns;
    };
    
    explicit CloudSyncEngine(const SyncConfig& config);
    
    // Sync operations
    std::future<bool> sync_repository();
    std::future<bool> push_changes();
    std::future<bool> pull_changes();
    
    // Conflict resolution
    bool resolve_conflict(const SyncConflict& conflict, const std::string& resolution);
    std::vector<SyncConflict> get_pending_conflicts();
    
    // Status and monitoring
    SyncStatus get_sync_status() const;
    void set_sync_callback(std::function<void(const SyncStatus&)> callback);
    
    // Auto-sync control
    void start_auto_sync();
    void stop_auto_sync();
    bool is_auto_sync_enabled() const;
    
    // Offline mode
    void enable_offline_mode();
    void disable_offline_mode();
    bool is_offline_mode() const;
    
    // Selective sync
    void set_sync_filters(const std::vector<std::string>& include_patterns,
                         const std::vector<std::string>& exclude_patterns);
    
    // Bandwidth optimization
    void enable_compression(bool enable = true);
    void set_bandwidth_limit(int kbps); // 0 = unlimited
    
    // Collaboration features
    struct CollaboratorInfo {
        std::string user_id;
        std::string username;
        std::string email;
        std::chrono::system_clock::time_point last_active;
        std::vector<std::string> active_branches;
    };
    
    std::vector<CollaboratorInfo> get_active_collaborators();
    bool lock_file(const std::string& file_path, const std::string& reason = "");
    bool unlock_file(const std::string& file_path);
    std::map<std::string, std::string> get_locked_files();
    
    // Real-time sync
    void enable_real_time_sync();
    void disable_real_time_sync();
    
private:
    SyncConfig config_;
    SyncStatus status_;
    std::function<void(const SyncStatus&)> status_callback_;
    
    bool auto_sync_enabled_ = false;
    bool offline_mode_ = false;
    bool real_time_sync_ = false;
    
    std::thread auto_sync_thread_;
    std::atomic<bool> should_stop_auto_sync_{false};
    
    // Internal sync methods
    bool upload_changes();
    bool download_changes();
    bool detect_conflicts();
    bool merge_remote_changes();
    
    // Network operations
    bool authenticate();
    std::string make_api_request(const std::string& endpoint, 
                                const std::string& method = "GET",
                                const std::string& data = "");
    
    // File operations
    std::vector<std::string> get_changed_files();
    std::string calculate_file_hash(const std::string& file_path);
    bool upload_file(const std::string& file_path);
    bool download_file(const std::string& file_path);
};

// Snippetia-specific integration
class SnippetiaIntegration {
public:
    struct SnippetMetadata {
        std::string snippet_id;
        std::string title;
        std::string description;
        std::vector<std::string> tags;
        std::string language;
        bool is_public = false;
        std::string author_id;
        std::chrono::system_clock::time_point created_at;
        std::chrono::system_clock::time_point updated_at;
    };
    
    // Snippet operations
    std::future<std::string> create_snippet(const SnippetMetadata& metadata,
                                           const std::string& content);
    std::future<bool> update_snippet(const std::string& snippet_id,
                                   const SnippetMetadata& metadata,
                                   const std::string& content);
    std::future<bool> delete_snippet(const std::string& snippet_id);
    
    // Repository integration
    std::future<bool> sync_repository_as_snippets();
    std::future<bool> import_snippets_to_repository();
    
    // Version tracking
    std::future<bool> create_snippet_version(const std::string& snippet_id,
                                           const std::string& commit_hash);
    std::vector<std::string> get_snippet_versions(const std::string& snippet_id);
    
    // Social features
    std::future<bool> share_repository();
    std::future<bool> fork_repository(const std::string& source_repo_id);
    std::future<std::vector<std::string>> get_repository_forks();
    
    // Analytics integration
    void track_repository_activity(const std::string& action,
                                 const std::map<std::string, std::string>& metadata = {});
    
private:
    std::string api_base_url_;
    std::string auth_token_;
    
    std::string make_snippetia_request(const std::string& endpoint,
                                     const std::string& method = "GET",
                                     const std::string& data = "");
};

// Backup and restore
class BackupManager {
public:
    struct BackupInfo {
        std::string backup_id;
        std::string repository_id;
        std::chrono::system_clock::time_point created_at;
        size_t size_bytes;
        std::string description;
        bool is_incremental;
    };
    
    // Create backups
    std::future<std::string> create_full_backup(const std::string& description = "");
    std::future<std::string> create_incremental_backup(const std::string& base_backup_id);
    
    // Restore operations
    std::future<bool> restore_from_backup(const std::string& backup_id,
                                         const std::string& target_path = "");
    
    // Backup management
    std::vector<BackupInfo> list_backups();
    bool delete_backup(const std::string& backup_id);
    
    // Automated backups
    void schedule_automatic_backups(int interval_hours = 24);
    void stop_automatic_backups();
    
    // Backup verification
    bool verify_backup(const std::string& backup_id);
    
private:
    std::string backup_storage_path_;
    std::thread backup_scheduler_;
    std::atomic<bool> should_stop_scheduler_{false};
};

}