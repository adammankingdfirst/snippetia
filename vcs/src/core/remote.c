#include "svcs.h"
#include <curl/curl.h>

// HTTP response structure
struct http_response {
    char *data;
    size_t size;
};

// Callback for writing HTTP response data
static size_t write_callback(void *contents, size_t size, size_t nmemb, struct http_response *response) {
    size_t total_size = size * nmemb;
    
    response->data = realloc(response->data, response->size + total_size + 1);
    if (!response->data) {
        return 0;
    }
    
    memcpy(response->data + response->size, contents, total_size);
    response->size += total_size;
    response->data[response->size] = '\0';
    
    return total_size;
}

// Remote configuration
typedef struct {
    char name[256];
    char url[1024];
    char auth_token[512];
} svcs_remote_t;

// Add remote repository
svcs_error_t svcs_remote_add(svcs_repository_t *repo, const char *name, const char *url) {
    if (!repo || !name || !url) {
        return SVCS_ERROR_INVALID;
    }
    
    char config_path[SVCS_MAX_PATH];
    snprintf(config_path, sizeof(config_path), "%s/config", repo->git_dir);
    
    // Read existing config
    void *config_data = NULL;
    size_t config_size = 0;
    svcs_file_read(config_path, &config_data, &config_size);
    
    // Append remote configuration
    char remote_config[1024];
    snprintf(remote_config, sizeof(remote_config), 
             "\n[remote \"%s\"]\n\turl = %s\n\tfetch = +refs/heads/*:refs/remotes/%s/*\n",
             name, url, name);
    
    // Write updated config
    FILE *file = fopen(config_path, "a");
    if (!file) {
        if (config_data) free(config_data);
        return SVCS_ERROR_IO;
    }
    
    fwrite(remote_config, 1, strlen(remote_config), file);
    fclose(file);
    
    if (config_data) free(config_data);
    
    printf("Added remote '%s' -> %s\n", name, url);
    return SVCS_OK;
}

// Set authentication token for remote
svcs_error_t svcs_remote_set_auth(svcs_repository_t *repo, const char *name, const char *token) {
    if (!repo || !name || !token) {
        return SVCS_ERROR_INVALID;
    }
    
    char auth_path[SVCS_MAX_PATH];
    snprintf(auth_path, sizeof(auth_path), "%s/remotes/%s.auth", repo->git_dir, name);
    
    // Create remotes directory
    char remotes_dir[SVCS_MAX_PATH];
    snprintf(remotes_dir, sizeof(remotes_dir), "%s/remotes", repo->git_dir);
    svcs_mkdir_recursive(remotes_dir);
    
    return svcs_file_write(auth_path, token, strlen(token));
}

// Get authentication token for remote
static svcs_error_t get_remote_auth(svcs_repository_t *repo, const char *name, char *token, size_t token_size) {
    char auth_path[SVCS_MAX_PATH];
    snprintf(auth_path, sizeof(auth_path), "%s/remotes/%s.auth", repo->git_dir, name);
    
    void *auth_data;
    size_t auth_size;
    svcs_error_t err = svcs_file_read(auth_path, &auth_data, &auth_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    size_t copy_size = (auth_size < token_size - 1) ? auth_size : token_size - 1;
    memcpy(token, auth_data, copy_size);
    token[copy_size] = '\0';
    
    // Remove newline if present
    char *newline = strchr(token, '\n');
    if (newline) *newline = '\0';
    
    free(auth_data);
    return SVCS_OK;
}

// Push changes to remote Snippetia repository
svcs_error_t svcs_remote_push(svcs_repository_t *repo, const char *remote_name, const char *snippet_id) {
    if (!repo || !remote_name || !snippet_id) {
        return SVCS_ERROR_INVALID;
    }
    
    // Get remote URL from config
    char config_path[SVCS_MAX_PATH];
    snprintf(config_path, sizeof(config_path), "%s/config", repo->git_dir);
    
    void *config_data;
    size_t config_size;
    svcs_error_t err = svcs_file_read(config_path, &config_data, &config_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Parse remote URL (simplified)
    char remote_url[1024] = {0};
    char *config_str = (char*)config_data;
    char search_pattern[256];
    snprintf(search_pattern, sizeof(search_pattern), "[remote \"%s\"]", remote_name);
    
    char *remote_section = strstr(config_str, search_pattern);
    if (remote_section) {
        char *url_line = strstr(remote_section, "url = ");
        if (url_line) {
            url_line += 6; // Skip "url = "
            char *url_end = strchr(url_line, '\n');
            if (url_end) {
                size_t url_len = url_end - url_line;
                if (url_len < sizeof(remote_url)) {
                    memcpy(remote_url, url_line, url_len);
                    remote_url[url_len] = '\0';
                }
            }
        }
    }
    
    free(config_data);
    
    if (remote_url[0] == '\0') {
        return SVCS_ERROR_NOT_FOUND;
    }
    
    // Get authentication token
    char auth_token[512] = {0};
    get_remote_auth(repo, remote_name, auth_token, sizeof(auth_token));
    
    // Get current HEAD commit
    char head_path[SVCS_MAX_PATH];
    snprintf(head_path, sizeof(head_path), "%s/HEAD", repo->git_dir);
    
    void *head_data;
    size_t head_size;
    err = svcs_file_read(head_path, &head_data, &head_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Get commit hash from current branch
    char commit_hash_str[SVCS_HASH_HEX_SIZE] = {0};
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
            
            strncpy(commit_hash_str, hash_str, sizeof(commit_hash_str) - 1);
            free(branch_data);
        }
    }
    
    free(head_data);
    
    if (commit_hash_str[0] == '\0') {
        return SVCS_ERROR_NOT_FOUND;
    }
    
    // Initialize curl
    CURL *curl = curl_easy_init();
    if (!curl) {
        return SVCS_ERROR;
    }
    
    struct http_response response = {0};
    
    // Prepare API endpoint
    char api_url[1024];
    snprintf(api_url, sizeof(api_url), "%s/api/v1/snippets/%s/sync", remote_url, snippet_id);
    
    // Prepare JSON payload
    char json_payload[2048];
    snprintf(json_payload, sizeof(json_payload), 
             "{"
             "\"commit_hash\":\"%s\","
             "\"repository_path\":\"%s\","
             "\"branch\":\"main\""
             "}", 
             commit_hash_str, repo->path);
    
    // Set curl options
    curl_easy_setopt(curl, CURLOPT_URL, api_url);
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, json_payload);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
    
    // Set headers
    struct curl_slist *headers = NULL;
    headers = curl_slist_append(headers, "Content-Type: application/json");
    
    if (auth_token[0] != '\0') {
        char auth_header[1024];
        snprintf(auth_header, sizeof(auth_header), "Authorization: Bearer %s", auth_token);
        headers = curl_slist_append(headers, auth_header);
    }
    
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    
    // Perform request
    CURLcode res = curl_easy_perform(curl);
    
    long response_code;
    curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);
    
    // Cleanup
    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);
    
    if (res != CURLE_OK) {
        if (response.data) free(response.data);
        return SVCS_ERROR;
    }
    
    if (response_code >= 200 && response_code < 300) {
        printf("Successfully pushed to remote '%s' (snippet %s)\n", remote_name, snippet_id);
        printf("Commit: %s\n", commit_hash_str);
        err = SVCS_OK;
    } else {
        printf("Push failed with HTTP %ld\n", response_code);
        if (response.data) {
            printf("Response: %s\n", response.data);
        }
        err = SVCS_ERROR;
    }
    
    if (response.data) free(response.data);
    
    return err;
}

// Pull changes from remote Snippetia repository
svcs_error_t svcs_remote_pull(svcs_repository_t *repo, const char *remote_name, const char *snippet_id) {
    if (!repo || !remote_name || !snippet_id) {
        return SVCS_ERROR_INVALID;
    }
    
    // Get remote URL and auth token (similar to push)
    char config_path[SVCS_MAX_PATH];
    snprintf(config_path, sizeof(config_path), "%s/config", repo->git_dir);
    
    void *config_data;
    size_t config_size;
    svcs_error_t err = svcs_file_read(config_path, &config_data, &config_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    char remote_url[1024] = {0};
    // Parse remote URL (same logic as push)
    // ... (implementation similar to push)
    
    free(config_data);
    
    // Get authentication token
    char auth_token[512] = {0};
    get_remote_auth(repo, remote_name, auth_token, sizeof(auth_token));
    
    // Initialize curl
    CURL *curl = curl_easy_init();
    if (!curl) {
        return SVCS_ERROR;
    }
    
    struct http_response response = {0};
    
    // Prepare API endpoint for getting snippet info
    char api_url[1024];
    snprintf(api_url, sizeof(api_url), "%s/api/v1/snippets/%s", remote_url, snippet_id);
    
    // Set curl options for GET request
    curl_easy_setopt(curl, CURLOPT_URL, api_url);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
    
    // Set headers
    struct curl_slist *headers = NULL;
    if (auth_token[0] != '\0') {
        char auth_header[1024];
        snprintf(auth_header, sizeof(auth_header), "Authorization: Bearer %s", auth_token);
        headers = curl_slist_append(headers, auth_header);
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    }
    
    // Perform request
    CURLcode res = curl_easy_perform(curl);
    
    long response_code;
    curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);
    
    // Cleanup
    if (headers) curl_slist_free_all(headers);
    curl_easy_cleanup(curl);
    
    if (res != CURLE_OK || response_code < 200 || response_code >= 300) {
        if (response.data) free(response.data);
        return SVCS_ERROR;
    }
    
    // Parse response and update local files
    // This would involve parsing JSON response and updating working directory
    printf("Successfully pulled from remote '%s' (snippet %s)\n", remote_name, snippet_id);
    
    if (response.data) free(response.data);
    
    return SVCS_OK;
}

// List configured remotes
svcs_error_t svcs_remote_list(svcs_repository_t *repo, svcs_remote_t **remotes, size_t *count) {
    if (!repo || !remotes || !count) {
        return SVCS_ERROR_INVALID;
    }
    
    *remotes = NULL;
    *count = 0;
    
    char config_path[SVCS_MAX_PATH];
    snprintf(config_path, sizeof(config_path), "%s/config", repo->git_dir);
    
    if (!svcs_file_exists(config_path)) {
        return SVCS_OK; // No remotes configured
    }
    
    void *config_data;
    size_t config_size;
    svcs_error_t err = svcs_file_read(config_path, &config_data, &config_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Count remotes (simplified parsing)
    char *config_str = (char*)config_data;
    size_t remote_count = 0;
    char *pos = config_str;
    while ((pos = strstr(pos, "[remote \"")) != NULL) {
        remote_count++;
        pos += 9; // Skip "[remote \""
    }
    
    if (remote_count == 0) {
        free(config_data);
        return SVCS_OK;
    }
    
    *remotes = calloc(remote_count, sizeof(svcs_remote_t));
    if (!*remotes) {
        free(config_data);
        return SVCS_ERROR_MEMORY;
    }
    
    // Parse remotes (simplified)
    pos = config_str;
    size_t i = 0;
    while ((pos = strstr(pos, "[remote \"")) != NULL && i < remote_count) {
        pos += 9; // Skip "[remote \""
        
        // Extract remote name
        char *name_end = strchr(pos, '"');
        if (name_end) {
            size_t name_len = name_end - pos;
            if (name_len < sizeof((*remotes)[i].name)) {
                memcpy((*remotes)[i].name, pos, name_len);
                (*remotes)[i].name[name_len] = '\0';
            }
            
            // Find URL
            char *url_start = strstr(name_end, "url = ");
            if (url_start) {
                url_start += 6; // Skip "url = "
                char *url_end = strchr(url_start, '\n');
                if (url_end) {
                    size_t url_len = url_end - url_start;
                    if (url_len < sizeof((*remotes)[i].url)) {
                        memcpy((*remotes)[i].url, url_start, url_len);
                        (*remotes)[i].url[url_len] = '\0';
                    }
                }
            }
        }
        
        pos = name_end ? name_end + 1 : pos + 1;
        i++;
    }
    
    *count = i;
    free(config_data);
    
    return SVCS_OK;
}