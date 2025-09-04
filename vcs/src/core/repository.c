#include "svcs.h"
#include <unistd.h>

svcs_error_t svcs_repository_init(const char *path) {
    char git_dir[SVCS_MAX_PATH];
    char objects_dir[SVCS_MAX_PATH];
    char refs_dir[SVCS_MAX_PATH];
    char head_file[SVCS_MAX_PATH];
    
    if (!path) {
        return SVCS_ERROR_INVALID;
    }
    
    // Create .svcs directory structure
    snprintf(git_dir, sizeof(git_dir), "%s/.svcs", path);
    snprintf(objects_dir, sizeof(objects_dir), "%s/objects", git_dir);
    snprintf(refs_dir, sizeof(refs_dir), "%s/refs", git_dir);
    
    if (svcs_mkdir_recursive(git_dir) != SVCS_OK ||
        svcs_mkdir_recursive(objects_dir) != SVCS_OK ||
        svcs_mkdir_recursive(refs_dir) != SVCS_OK) {
        return SVCS_ERROR_IO;
    }
    
    // Create HEAD file pointing to main branch
    snprintf(head_file, sizeof(head_file), "%s/HEAD", git_dir);
    const char *head_content = "ref: refs/heads/main\n";
    if (svcs_file_write(head_file, head_content, strlen(head_content)) != SVCS_OK) {
        return SVCS_ERROR_IO;
    }
    
    // Create empty index file
    char index_file[SVCS_MAX_PATH];
    snprintf(index_file, sizeof(index_file), "%s/index", git_dir);
    if (svcs_file_write(index_file, "", 0) != SVCS_OK) {
        return SVCS_ERROR_IO;
    }
    
    printf("Initialized empty SnippetVCS repository in %s\n", git_dir);
    return SVCS_OK;
}

svcs_error_t svcs_repository_open(svcs_repository_t **repo, const char *path) {
    if (!repo || !path) {
        return SVCS_ERROR_INVALID;
    }
    
    *repo = calloc(1, sizeof(svcs_repository_t));
    if (!*repo) {
        return SVCS_ERROR_MEMORY;
    }
    
    // Find .svcs directory
    char current_path[SVCS_MAX_PATH];
    strncpy(current_path, path, sizeof(current_path) - 1);
    
    while (strlen(current_path) > 1) {
        char git_dir[SVCS_MAX_PATH];
        snprintf(git_dir, sizeof(git_dir), "%s/.svcs", current_path);
        
        if (svcs_file_exists(git_dir)) {
            strncpy((*repo)->path, current_path, sizeof((*repo)->path) - 1);
            strncpy((*repo)->git_dir, git_dir, sizeof((*repo)->git_dir) - 1);
            strncpy((*repo)->work_dir, current_path, sizeof((*repo)->work_dir) - 1);
            
            // Load index
            if (svcs_index_load(*repo) != SVCS_OK) {
                free(*repo);
                *repo = NULL;
                return SVCS_ERROR_CORRUPT;
            }
            
            return SVCS_OK;
        }
        
        // Move up one directory
        char *last_slash = strrchr(current_path, '/');
        if (last_slash && last_slash != current_path) {
            *last_slash = '\0';
        } else {
            break;
        }
    }
    
    free(*repo);
    *repo = NULL;
    return SVCS_ERROR_NOT_FOUND;
}

void svcs_repository_free(svcs_repository_t *repo) {
    if (!repo) return;
    
    if (repo->index) {
        if (repo->index->entries) {
            free(repo->index->entries);
        }
        free(repo->index);
    }
    
    if (repo->current_branch) {
        free(repo->current_branch);
    }
    
    free(repo);
}

int svcs_repository_is_valid(const char *path) {
    if (!path) return 0;
    
    char git_dir[SVCS_MAX_PATH];
    snprintf(git_dir, sizeof(git_dir), "%s/.svcs", path);
    
    return svcs_file_exists(git_dir);
}