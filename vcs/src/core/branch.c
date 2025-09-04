#include "svcs.h"

svcs_error_t svcs_branch_create(svcs_repository_t *repo, const char *name, const svcs_hash_t *commit_hash) {
    if (!repo || !name || !commit_hash) {
        return SVCS_ERROR_INVALID;
    }
    
    char branch_path[SVCS_MAX_PATH];
    snprintf(branch_path, sizeof(branch_path), "%s/refs/heads/%s", repo->git_dir, name);
    
    // Check if branch already exists
    if (svcs_file_exists(branch_path)) {
        return SVCS_ERROR_EXISTS;
    }
    
    // Create refs/heads directory if it doesn't exist
    char refs_heads_dir[SVCS_MAX_PATH];
    snprintf(refs_heads_dir, sizeof(refs_heads_dir), "%s/refs/heads", repo->git_dir);
    svcs_mkdir_recursive(refs_heads_dir);
    
    // Write commit hash to branch file
    char hash_str[SVCS_HASH_HEX_SIZE];
    svcs_hash_to_string(commit_hash, hash_str);
    
    char content[SVCS_HASH_HEX_SIZE + 1];
    snprintf(content, sizeof(content), "%s\n", hash_str);
    
    return svcs_file_write(branch_path, content, strlen(content));
}

svcs_error_t svcs_branch_list(svcs_repository_t *repo, svcs_branch_t **branches, size_t *count) {
    if (!repo || !branches || !count) {
        return SVCS_ERROR_INVALID;
    }
    
    *branches = NULL;
    *count = 0;
    
    char refs_heads_dir[SVCS_MAX_PATH];
    snprintf(refs_heads_dir, sizeof(refs_heads_dir), "%s/refs/heads", repo->git_dir);
    
    DIR *dir = opendir(refs_heads_dir);
    if (!dir) {
        return SVCS_OK; // No branches yet
    }
    
    // Count branches first
    size_t branch_count = 0;
    struct dirent *entry;
    while ((entry = readdir(dir)) != NULL) {
        if (entry->d_name[0] != '.') {
            branch_count++;
        }
    }
    
    if (branch_count == 0) {
        closedir(dir);
        return SVCS_OK;
    }
    
    // Allocate memory for branches
    *branches = calloc(branch_count, sizeof(svcs_branch_t));
    if (!*branches) {
        closedir(dir);
        return SVCS_ERROR_MEMORY;
    }
    
    // Get current branch from HEAD
    char head_path[SVCS_MAX_PATH];
    snprintf(head_path, sizeof(head_path), "%s/HEAD", repo->git_dir);
    
    char current_branch[256] = {0};
    void *head_data;
    size_t head_size;
    if (svcs_file_read(head_path, &head_data, &head_size) == SVCS_OK) {
        char *head_content = (char*)head_data;
        if (strncmp(head_content, "ref: refs/heads/", 16) == 0) {
            char *branch_name = head_content + 16;
            char *newline = strchr(branch_name, '\n');
            if (newline) *newline = '\0';
            strncpy(current_branch, branch_name, sizeof(current_branch) - 1);
        }
        free(head_data);
    }
    
    // Read branch information
    rewinddir(dir);
    size_t i = 0;
    while ((entry = readdir(dir)) != NULL && i < branch_count) {
        if (entry->d_name[0] == '.') continue;
        
        strncpy((*branches)[i].name, entry->d_name, sizeof((*branches)[i].name) - 1);
        (*branches)[i].is_current = (strcmp(entry->d_name, current_branch) == 0);
        
        // Read commit hash
        char branch_file[SVCS_MAX_PATH];
        snprintf(branch_file, sizeof(branch_file), "%s/%s", refs_heads_dir, entry->d_name);
        
        void *branch_data;
        size_t branch_size;
        if (svcs_file_read(branch_file, &branch_data, &branch_size) == SVCS_OK) {
            char *hash_str = (char*)branch_data;
            char *newline = strchr(hash_str, '\n');
            if (newline) *newline = '\0';
            
            svcs_hash_from_string(&(*branches)[i].commit_hash, hash_str);
            free(branch_data);
        }
        
        i++;
    }
    
    *count = i;
    closedir(dir);
    
    return SVCS_OK;
}

svcs_error_t svcs_branch_checkout(svcs_repository_t *repo, const char *name) {
    if (!repo || !name) {
        return SVCS_ERROR_INVALID;
    }
    
    char branch_path[SVCS_MAX_PATH];
    snprintf(branch_path, sizeof(branch_path), "%s/refs/heads/%s", repo->git_dir, name);
    
    // Check if branch exists
    if (!svcs_file_exists(branch_path)) {
        return SVCS_ERROR_NOT_FOUND;
    }
    
    // Update HEAD to point to the branch
    char head_path[SVCS_MAX_PATH];
    snprintf(head_path, sizeof(head_path), "%s/HEAD", repo->git_dir);
    
    char head_content[SVCS_MAX_PATH];
    snprintf(head_content, sizeof(head_content), "ref: refs/heads/%s\n", name);
    
    svcs_error_t err = svcs_file_write(head_path, head_content, strlen(head_content));
    if (err != SVCS_OK) {
        return err;
    }
    
    // TODO: Update working directory files to match branch state
    // This would involve reading the commit tree and updating files
    
    return SVCS_OK;
}

svcs_error_t svcs_branch_delete(svcs_repository_t *repo, const char *name) {
    if (!repo || !name) {
        return SVCS_ERROR_INVALID;
    }
    
    // Don't allow deleting current branch
    char head_path[SVCS_MAX_PATH];
    snprintf(head_path, sizeof(head_path), "%s/HEAD", repo->git_dir);
    
    void *head_data;
    size_t head_size;
    if (svcs_file_read(head_path, &head_data, &head_size) == SVCS_OK) {
        char *head_content = (char*)head_data;
        if (strncmp(head_content, "ref: refs/heads/", 16) == 0) {
            char *current_branch = head_content + 16;
            char *newline = strchr(current_branch, '\n');
            if (newline) *newline = '\0';
            
            if (strcmp(current_branch, name) == 0) {
                free(head_data);
                return SVCS_ERROR_INVALID; // Cannot delete current branch
            }
        }
        free(head_data);
    }
    
    char branch_path[SVCS_MAX_PATH];
    snprintf(branch_path, sizeof(branch_path), "%s/refs/heads/%s", repo->git_dir, name);
    
    if (!svcs_file_exists(branch_path)) {
        return SVCS_ERROR_NOT_FOUND;
    }
    
    if (remove(branch_path) != 0) {
        return SVCS_ERROR_IO;
    }
    
    return SVCS_OK;
}

// Get current branch name
svcs_error_t svcs_branch_current(svcs_repository_t *repo, char *name, size_t name_size) {
    if (!repo || !name || name_size == 0) {
        return SVCS_ERROR_INVALID;
    }
    
    char head_path[SVCS_MAX_PATH];
    snprintf(head_path, sizeof(head_path), "%s/HEAD", repo->git_dir);
    
    void *head_data;
    size_t head_size;
    svcs_error_t err = svcs_file_read(head_path, &head_data, &head_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    char *head_content = (char*)head_data;
    if (strncmp(head_content, "ref: refs/heads/", 16) == 0) {
        char *branch_name = head_content + 16;
        char *newline = strchr(branch_name, '\n');
        if (newline) *newline = '\0';
        
        strncpy(name, branch_name, name_size - 1);
        name[name_size - 1] = '\0';
        
        free(head_data);
        return SVCS_OK;
    }
    
    free(head_data);
    return SVCS_ERROR_NOT_FOUND;
}