#include "svcs.h"

svcs_error_t svcs_index_load(svcs_repository_t *repo) {
    if (!repo) {
        return SVCS_ERROR_INVALID;
    }
    
    char index_path[SVCS_MAX_PATH];
    snprintf(index_path, sizeof(index_path), "%s/index", repo->git_dir);
    
    if (!svcs_file_exists(index_path)) {
        // Create empty index
        repo->index = calloc(1, sizeof(svcs_index_t));
        if (!repo->index) {
            return SVCS_ERROR_MEMORY;
        }
        repo->index->timestamp = time(NULL);
        return SVCS_OK;
    }
    
    void *data;
    size_t size;
    svcs_error_t err = svcs_file_read(index_path, &data, &size);
    if (err != SVCS_OK) {
        return err;
    }
    
    if (size == 0) {
        // Empty index file
        repo->index = calloc(1, sizeof(svcs_index_t));
        if (!repo->index) {
            free(data);
            return SVCS_ERROR_MEMORY;
        }
        repo->index->timestamp = time(NULL);
        free(data);
        return SVCS_OK;
    }
    
    // Parse index file (simplified binary format)
    char *ptr = (char*)data;
    
    // Read header
    if (size < sizeof(uint32_t) * 2) {
        free(data);
        return SVCS_ERROR_CORRUPT;
    }
    
    uint32_t version = *(uint32_t*)ptr;
    ptr += sizeof(uint32_t);
    
    uint32_t entry_count = *(uint32_t*)ptr;
    ptr += sizeof(uint32_t);
    
    if (version != 1) {
        free(data);
        return SVCS_ERROR_CORRUPT;
    }
    
    repo->index = calloc(1, sizeof(svcs_index_t));
    if (!repo->index) {
        free(data);
        return SVCS_ERROR_MEMORY;
    }
    
    repo->index->entry_count = entry_count;
    repo->index->timestamp = time(NULL);
    
    if (entry_count > 0) {
        repo->index->entries = calloc(entry_count, sizeof(svcs_index_entry_t));
        if (!repo->index->entries) {
            free(repo->index);
            free(data);
            return SVCS_ERROR_MEMORY;
        }
        
        // Read entries (simplified format)
        for (size_t i = 0; i < entry_count; i++) {
            if (ptr + sizeof(svcs_index_entry_t) > (char*)data + size) {
                free(repo->index->entries);
                free(repo->index);
                free(data);
                return SVCS_ERROR_CORRUPT;
            }
            
            memcpy(&repo->index->entries[i], ptr, sizeof(svcs_index_entry_t));
            ptr += sizeof(svcs_index_entry_t);
        }
    }
    
    free(data);
    return SVCS_OK;
}

svcs_error_t svcs_index_save(svcs_repository_t *repo) {
    if (!repo || !repo->index) {
        return SVCS_ERROR_INVALID;
    }
    
    char index_path[SVCS_MAX_PATH];
    snprintf(index_path, sizeof(index_path), "%s/index", repo->git_dir);
    
    // Calculate total size
    size_t total_size = sizeof(uint32_t) * 2; // version + entry_count
    total_size += repo->index->entry_count * sizeof(svcs_index_entry_t);
    
    void *data = malloc(total_size);
    if (!data) {
        return SVCS_ERROR_MEMORY;
    }
    
    char *ptr = (char*)data;
    
    // Write header
    uint32_t version = 1;
    memcpy(ptr, &version, sizeof(uint32_t));
    ptr += sizeof(uint32_t);
    
    uint32_t entry_count = (uint32_t)repo->index->entry_count;
    memcpy(ptr, &entry_count, sizeof(uint32_t));
    ptr += sizeof(uint32_t);
    
    // Write entries
    for (size_t i = 0; i < repo->index->entry_count; i++) {
        memcpy(ptr, &repo->index->entries[i], sizeof(svcs_index_entry_t));
        ptr += sizeof(svcs_index_entry_t);
    }
    
    svcs_error_t err = svcs_file_write(index_path, data, total_size);
    free(data);
    
    return err;
}

svcs_error_t svcs_index_add(svcs_repository_t *repo, const char *path) {
    if (!repo || !path) {
        return SVCS_ERROR_INVALID;
    }
    
    // Check if file exists
    if (!svcs_file_exists(path)) {
        return SVCS_ERROR_NOT_FOUND;
    }
    
    // Get file stats
    struct stat st;
    if (stat(path, &st) != 0) {
        return SVCS_ERROR_IO;
    }
    
    // Compute file hash
    svcs_hash_t hash;
    svcs_error_t err = svcs_hash_file(path, &hash);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Create blob object
    err = svcs_object_create_blob(repo, path, &hash);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Find existing entry or create new one
    svcs_index_entry_t *entry = NULL;
    for (size_t i = 0; i < repo->index->entry_count; i++) {
        if (strcmp(repo->index->entries[i].path, path) == 0) {
            entry = &repo->index->entries[i];
            break;
        }
    }
    
    if (!entry) {
        // Add new entry
        repo->index->entries = realloc(repo->index->entries, 
                                     (repo->index->entry_count + 1) * sizeof(svcs_index_entry_t));
        if (!repo->index->entries) {
            return SVCS_ERROR_MEMORY;
        }
        
        entry = &repo->index->entries[repo->index->entry_count];
        repo->index->entry_count++;
        
        strncpy(entry->path, path, sizeof(entry->path) - 1);
        entry->path[sizeof(entry->path) - 1] = '\0';
    }
    
    // Update entry
    entry->hash = hash;
    entry->mode = st.st_mode;
    entry->mtime = st.st_mtime;
    entry->size = st.st_size;
    entry->status = SVCS_STATUS_ADDED;
    
    return svcs_index_save(repo);
}

svcs_error_t svcs_index_remove(svcs_repository_t *repo, const char *path) {
    if (!repo || !path) {
        return SVCS_ERROR_INVALID;
    }
    
    // Find entry
    for (size_t i = 0; i < repo->index->entry_count; i++) {
        if (strcmp(repo->index->entries[i].path, path) == 0) {
            // Remove entry by shifting remaining entries
            memmove(&repo->index->entries[i], &repo->index->entries[i + 1],
                   (repo->index->entry_count - i - 1) * sizeof(svcs_index_entry_t));
            repo->index->entry_count--;
            
            return svcs_index_save(repo);
        }
    }
    
    return SVCS_ERROR_NOT_FOUND;
}

svcs_error_t svcs_index_status(svcs_repository_t *repo, svcs_index_entry_t **entries, size_t *count) {
    if (!repo || !entries || !count) {
        return SVCS_ERROR_INVALID;
    }
    
    *entries = NULL;
    *count = 0;
    
    if (repo->index->entry_count == 0) {
        return SVCS_OK;
    }
    
    *entries = malloc(repo->index->entry_count * sizeof(svcs_index_entry_t));
    if (!*entries) {
        return SVCS_ERROR_MEMORY;
    }
    
    *count = repo->index->entry_count;
    
    // Check status of each file
    for (size_t i = 0; i < repo->index->entry_count; i++) {
        svcs_index_entry_t *entry = &(*entries)[i];
        *entry = repo->index->entries[i];
        
        if (!svcs_file_exists(entry->path)) {
            entry->status = SVCS_STATUS_DELETED;
        } else {
            // Check if file has been modified
            time_t current_mtime = svcs_file_mtime(entry->path);
            if (current_mtime != entry->mtime) {
                svcs_hash_t current_hash;
                if (svcs_hash_file(entry->path, &current_hash) == SVCS_OK) {
                    if (svcs_hash_compare(&current_hash, &entry->hash) != 0) {
                        entry->status = SVCS_STATUS_MODIFIED;
                    }
                }
            }
        }
    }
    
    return SVCS_OK;
}