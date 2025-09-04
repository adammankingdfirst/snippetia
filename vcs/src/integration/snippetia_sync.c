#include "svcs.h"
#include <curl/curl.h>
#include <json-c/json.h>

// Snippetia integration configuration
typedef struct {
    char api_base_url[1024];
    char auth_token[512];
    char user_id[64];
    int auto_sync;
} snippetia_config_t;

// Snippet tracking information
typedef struct {
    char snippet_id[64];
    char remote_hash[SVCS_HASH_HEX_SIZE];
    char local_hash[SVCS_HASH_HEX_SIZE];
    time_t last_sync;
    int has_conflicts;
} snippet_track_t;

// Load Snippetia configuration
static svcs_error_t load_snippetia_config(svcs_repository_t *repo, snippetia_config_t *config) {
    char config_path[SVCS_MAX_PATH];
    snprintf(config_path, sizeof(config_path), "%s/snippetia.config", repo->git_dir);
    
    if (!svcs_file_exists(config_path)) {
        // Create default config
        const char *default_config = 
            "api_base_url=http://localhost:8080\n"
            "auth_token=\n"
            "user_id=\n"
            "auto_sync=1\n";
        
        svcs_file_write(config_path, default_config, strlen(default_config));
        
        // Set defaults
        strcpy(config->api_base_url, "http://localhost:8080");
        config->auth_token[0] = '\0';
        config->user_id[0] = '\0';
        config->auto_sync = 1;
        
        return SVCS_OK;
    }
    
    void *config_data;
    size_t config_size;
    svcs_error_t err = svcs_file_read(config_path, &config_data, &config_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Parse configuration (simple key=value format)
    char *config_str = (char*)config_data;
    char *line = strtok(config_str, "\n");
    
    while (line) {
        char *equals = strchr(line, '=');
        if (equals) {
            *equals = '\0';
            char *key = line;
            char *value = equals + 1;
            
            if (strcmp(key, "api_base_url") == 0) {
                strncpy(config->api_base_url, value, sizeof(config->api_base_url) - 1);
            } else if (strcmp(key, "auth_token") == 0) {
                strncpy(config->auth_token, value, sizeof(config->auth_token) - 1);
            } else if (strcmp(key, "user_id") == 0) {
                strncpy(config->user_id, value, sizeof(config->user_id) - 1);
            } else if (strcmp(key, "auto_sync") == 0) {
                config->auto_sync = atoi(value);
            }
        }
        
        line = strtok(NULL, "\n");
    }
    
    free(config_data);
    return SVCS_OK;
}

// Save Snippetia configuration
svcs_error_t svcs_snippetia_configure(svcs_repository_t *repo, const char *api_url, 
                                     const char *auth_token, const char *user_id) {
    if (!repo) {
        return SVCS_ERROR_INVALID;
    }
    
    char config_path[SVCS_MAX_PATH];
    snprintf(config_path, sizeof(config_path), "%s/snippetia.config", repo->git_dir);
    
    char config_content[2048];
    snprintf(config_content, sizeof(config_content),
             "api_base_url=%s\n"
             "auth_token=%s\n"
             "user_id=%s\n"
             "auto_sync=1\n",
             api_url ? api_url : "http://localhost:8080",
             auth_token ? auth_token : "",
             user_id ? user_id : "");
    
    return svcs_file_write(config_path, config_content, strlen(config_content));
}

// Link local repository to remote snippet
svcs_error_t svcs_snippetia_link(svcs_repository_t *repo, const char *snippet_id) {
    if (!repo || !snippet_id) {
        return SVCS_ERROR_INVALID;
    }
    
    snippetia_config_t config;
    svcs_error_t err = load_snippetia_config(repo, &config);
    if (err != SVCS_OK) {
        return err;
    }
    
    if (config.auth_token[0] == '\0') {
        printf("Error: No authentication token configured. Use 'svcs snippetia config' first.\n");
        return SVCS_ERROR_INVALID;
    }
    
    // Create tracking file
    char track_path[SVCS_MAX_PATH];
    snprintf(track_path, sizeof(track_path), "%s/snippetia.track", repo->git_dir);
    
    snippet_track_t track = {0};
    strncpy(track.snippet_id, snippet_id, sizeof(track.snippet_id) - 1);
    track.last_sync = time(NULL);
    track.has_conflicts = 0;
    
    // Get current HEAD commit hash
    char head_path[SVCS_MAX_PATH];
    snprintf(head_path, sizeof(head_path), "%s/HEAD", repo->git_dir);
    
    void *head_data;
    size_t head_size;
    if (svcs_file_read(head_path, &head_data, &head_size) == SVCS_OK) {
        char *head_content = (char*)head_data;
        if (strncmp(head_content, "ref: refs/heads/", 16) == 0) {
            char *branch_name = head_content + 16;
            char *newline = strchr(branch_name, '\n');
            if (newline) *newline = '\0';
            
            char branch_path[SVCS_MAX_PATH];
            snprintf(branch_path, sizeof(branch_path), "%s/refs/heads/%s", repo->git_dir, branch_name);
            
            void *branch_data;
            size_t branch_size;
            if (svcs_file_read(branch_path, &branch_data, &branch_size) == SVCS_OK) {
                char *hash_str = (char*)branch_data;
                char *hash_newline = strchr(hash_str, '\n');
                if (hash_newline) *hash_newline = '\0';
                
                strncpy(track.local_hash, hash_str, sizeof(track.local_hash) - 1);
                strncpy(track.remote_hash, hash_str, sizeof(track.remote_hash) - 1);
                
                free(branch_data);
            }
        }
        free(head_data);
    }
    
    // Save tracking information
    err = svcs_file_write(track_path, &track, sizeof(track));
    if (err != SVCS_OK) {
        return err;
    }
    
    printf("Linked repository to Snippetia snippet: %s\n", snippet_id);
    return SVCS_OK;
}

// Sync with remote snippet
svcs_error_t svcs_snippetia_sync(svcs_repository_t *repo, int force_push) {
    if (!repo) {
        return SVCS_ERROR_INVALID;
    }
    
    // Load configuration
    snippetia_config_t config;
    svcs_error_t err = load_snippetia_config(repo, &config);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Load tracking information
    char track_path[SVCS_MAX_PATH];
    snprintf(track_path, sizeof(track_path), "%s/snippetia.track", repo->git_dir);
    
    if (!svcs_file_exists(track_path)) {
        printf("Error: Repository not linked to any snippet. Use 'svcs snippetia link <snippet-id>' first.\n");
        return SVCS_ERROR_NOT_FOUND;
    }
    
    void *track_data;
    size_t track_size;
    err = svcs_file_read(track_path, &track_data, &track_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    snippet_track_t *track = (snippet_track_t*)track_data;
    
    // Get current local commit hash
    char current_hash[SVCS_HASH_HEX_SIZE] = {0};
    char head_path[SVCS_MAX_PATH];
    snprintf(head_path, sizeof(head_path), "%s/HEAD", repo->git_dir);
    
    void *head_data;
    size_t head_size;
    if (svcs_file_read(head_path, &head_data, &head_size) == SVCS_OK) {
        char *head_content = (char*)head_data;
        if (strncmp(head_content, "ref: refs/heads/", 16) == 0) {
            char *branch_name = head_content + 16;
            char *newline = strchr(branch_name, '\n');
            if (newline) *newline = '\0';
            
            char branch_path[SVCS_MAX_PATH];
            snprintf(branch_path, sizeof(branch_path), "%s/refs/heads/%s", repo->git_dir, branch_name);
            
            void *branch_data;
            size_t branch_size;
            if (svcs_file_read(branch_path, &branch_data, &branch_size) == SVCS_OK) {
                char *hash_str = (char*)branch_data;
                char *hash_newline = strchr(hash_str, '\n');
                if (hash_newline) *hash_newline = '\0';
                
                strncpy(current_hash, hash_str, sizeof(current_hash) - 1);
                free(branch_data);
            }
        }
        free(head_data);
    }
    
    // Check if local changes exist
    int has_local_changes = (strcmp(current_hash, track->local_hash) != 0);
    
    if (has_local_changes || force_push) {
        // Push local changes to Snippetia
        printf("Syncing local changes to Snippetia...\n");
        
        CURL *curl = curl_easy_init();
        if (!curl) {
            free(track_data);
            return SVCS_ERROR;
        }
        
        // Prepare API request
        char api_url[1024];
        snprintf(api_url, sizeof(api_url), "%s/api/v1/snippets/%s/sync", 
                config.api_base_url, track->snippet_id);
        
        // Read main file content (assume main.* or README.* or first file)
        char main_file[SVCS_MAX_PATH] = {0};
        DIR *dir = opendir(repo->work_dir);
        if (dir) {
            struct dirent *entry;
            while ((entry = readdir(dir)) != NULL) {
                if (entry->d_name[0] != '.' && 
                    (strstr(entry->d_name, "main.") || 
                     strstr(entry->d_name, "README.") ||
                     strstr(entry->d_name, "index."))) {
                    snprintf(main_file, sizeof(main_file), "%s/%s", repo->work_dir, entry->d_name);
                    break;
                }
            }
            closedir(dir);
        }
        
        // If no main file found, use first non-hidden file
        if (main_file[0] == '\0') {
            dir = opendir(repo->work_dir);
            if (dir) {
                struct dirent *entry;
                while ((entry = readdir(dir)) != NULL) {
                    if (entry->d_name[0] != '.' && entry->d_type == DT_REG) {
                        snprintf(main_file, sizeof(main_file), "%s/%s", repo->work_dir, entry->d_name);
                        break;
                    }
                }
                closedir(dir);
            }
        }
        
        char *file_content = "";
        if (main_file[0] != '\0') {
            void *content_data;
            size_t content_size;
            if (svcs_file_read(main_file, &content_data, &content_size) == SVCS_OK) {
                file_content = (char*)content_data;
            }
        }
        
        // Create JSON payload
        json_object *json_obj = json_object_new_object();
        json_object *content_obj = json_object_new_string(file_content);
        json_object *hash_obj = json_object_new_string(current_hash);
        json_object *timestamp_obj = json_object_new_int64(time(NULL));
        
        json_object_object_add(json_obj, "content", content_obj);
        json_object_object_add(json_obj, "commit_hash", hash_obj);
        json_object_object_add(json_obj, "timestamp", timestamp_obj);
        
        const char *json_string = json_object_to_json_string(json_obj);
        
        // Set curl options
        curl_easy_setopt(curl, CURLOPT_URL, api_url);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, json_string);
        
        // Set headers
        struct curl_slist *headers = NULL;
        headers = curl_slist_append(headers, "Content-Type: application/json");
        
        char auth_header[1024];
        snprintf(auth_header, sizeof(auth_header), "Authorization: Bearer %s", config.auth_token);
        headers = curl_slist_append(headers, auth_header);
        
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        
        // Perform request
        CURLcode res = curl_easy_perform(curl);
        
        long response_code;
        curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);
        
        // Cleanup
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);
        json_object_put(json_obj);
        
        if (res == CURLE_OK && response_code >= 200 && response_code < 300) {
            // Update tracking information
            strncpy(track->local_hash, current_hash, sizeof(track->local_hash) - 1);
            strncpy(track->remote_hash, current_hash, sizeof(track->remote_hash) - 1);
            track->last_sync = time(NULL);
            track->has_conflicts = 0;
            
            svcs_file_write(track_path, track, sizeof(snippet_track_t));
            
            printf("Successfully synced to Snippetia snippet %s\n", track->snippet_id);
            printf("Commit: %s\n", current_hash);
        } else {
            printf("Sync failed with HTTP %ld\n", response_code);
            free(track_data);
            return SVCS_ERROR;
        }
    } else {
        printf("No local changes to sync.\n");
    }
    
    free(track_data);
    return SVCS_OK;
}

// Check sync status
svcs_error_t svcs_snippetia_status(svcs_repository_t *repo) {
    if (!repo) {
        return SVCS_ERROR_INVALID;
    }
    
    char track_path[SVCS_MAX_PATH];
    snprintf(track_path, sizeof(track_path), "%s/snippetia.track", repo->git_dir);
    
    if (!svcs_file_exists(track_path)) {
        printf("Repository not linked to any Snippetia snippet.\n");
        return SVCS_OK;
    }
    
    void *track_data;
    size_t track_size;
    svcs_error_t err = svcs_file_read(track_path, &track_data, &track_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    snippet_track_t *track = (snippet_track_t*)track_data;
    
    printf("Snippetia Integration Status:\n");
    printf("  Linked snippet: %s\n", track->snippet_id);
    printf("  Local commit:   %s\n", track->local_hash);
    printf("  Remote commit:  %s\n", track->remote_hash);
    printf("  Last sync:      %s", ctime(&track->last_sync));
    printf("  Has conflicts:  %s\n", track->has_conflicts ? "Yes" : "No");
    
    // Check if local changes exist
    char current_hash[SVCS_HASH_HEX_SIZE] = {0};
    // ... (get current hash logic similar to sync function)
    
    if (strcmp(current_hash, track->local_hash) != 0) {
        printf("  Status:         Local changes pending sync\n");
    } else {
        printf("  Status:         Up to date\n");
    }
    
    free(track_data);
    return SVCS_OK;
}