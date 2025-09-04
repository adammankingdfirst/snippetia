#include "svcs.h"

static svcs_error_t create_tree_from_index(svcs_repository_t *repo, svcs_hash_t *tree_hash) {
    if (!repo || !tree_hash || !repo->index) {
        return SVCS_ERROR_INVALID;
    }
    
    if (repo->index->entry_count == 0) {
        // Empty tree
        svcs_hash_init(tree_hash);
        return SVCS_OK;
    }
    
    // Create tree object from index entries
    size_t tree_size = 0;
    for (size_t i = 0; i < repo->index->entry_count; i++) {
        svcs_index_entry_t *entry = &repo->index->entries[i];
        // Calculate size: mode + space + name + null + hash
        tree_size += snprintf(NULL, 0, "%o", entry->mode) + 1 + strlen(entry->path) + 1 + SVCS_HASH_SIZE;
    }
    
    void *tree_data = malloc(tree_size);
    if (!tree_data) {
        return SVCS_ERROR_MEMORY;
    }
    
    char *ptr = (char*)tree_data;
    for (size_t i = 0; i < repo->index->entry_count; i++) {
        svcs_index_entry_t *entry = &repo->index->entries[i];
        
        // Write mode and name
        int written = sprintf(ptr, "%o %s", entry->mode, entry->path);
        ptr += written;
        *ptr++ = '\0';
        
        // Write hash
        memcpy(ptr, entry->hash.bytes, SVCS_HASH_SIZE);
        ptr += SVCS_HASH_SIZE;
    }
    
    // Compute tree hash
    svcs_error_t err = svcs_hash_object(SVCS_OBJ_TREE, tree_data, tree_size, tree_hash);
    if (err != SVCS_OK) {
        free(tree_data);
        return err;
    }
    
    // Create and write tree object
    svcs_object_t tree_obj = {
        .type = SVCS_OBJ_TREE,
        .size = tree_size,
        .hash = *tree_hash
    };
    
    err = svcs_object_write(repo, &tree_obj);
    free(tree_data);
    
    return err;
}

svcs_error_t svcs_commit_create(svcs_repository_t *repo, const char *message, const char *author, svcs_hash_t *commit_hash) {
    if (!repo || !message || !author || !commit_hash) {
        return SVCS_ERROR_INVALID;
    }
    
    // Create tree from current index
    svcs_hash_t tree_hash;
    svcs_error_t err = create_tree_from_index(repo, &tree_hash);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Get parent commit (HEAD)
    svcs_hash_t parent_hash;
    svcs_hash_init(&parent_hash);
    
    char head_path[SVCS_MAX_PATH];
    snprintf(head_path, sizeof(head_path), "%s/HEAD", repo->git_dir);
    
    void *head_data;
    size_t head_size;
    if (svcs_file_read(head_path, &head_data, &head_size) == SVCS_OK) {
        char *head_content = (char*)head_data;
        if (strncmp(head_content, "ref: ", 5) == 0) {
            // HEAD points to a branch
            char *ref_name = head_content + 5;
            char *newline = strchr(ref_name, '\n');
            if (newline) *newline = '\0';
            
            char ref_path[SVCS_MAX_PATH];
            snprintf(ref_path, sizeof(ref_path), "%s/%s", repo->git_dir, ref_name);
            
            void *ref_data;
            size_t ref_size;
            if (svcs_file_read(ref_path, &ref_data, &ref_size) == SVCS_OK) {
                char *hash_str = (char*)ref_data;
                char *ref_newline = strchr(hash_str, '\n');
                if (ref_newline) *ref_newline = '\0';
                
                svcs_hash_from_string(&parent_hash, hash_str);
                free(ref_data);
            }
        }
        free(head_data);
    }
    
    // Create commit object
    time_t now = time(NULL);
    char timestamp_str[64];
    strftime(timestamp_str, sizeof(timestamp_str), "%s +0000", gmtime(&now));
    
    char tree_hash_str[SVCS_HASH_HEX_SIZE];
    svcs_hash_to_string(&tree_hash, tree_hash_str);
    
    char parent_hash_str[SVCS_HASH_HEX_SIZE];
    svcs_hash_to_string(&parent_hash, parent_hash_str);
    
    char commit_content[4096];
    int content_len;
    
    // Check if this is the first commit (no parent)
    int is_first_commit = 1;
    for (int i = 0; i < SVCS_HASH_SIZE; i++) {
        if (parent_hash.bytes[i] != 0) {
            is_first_commit = 0;
            break;
        }
    }
    
    if (is_first_commit) {
        content_len = snprintf(commit_content, sizeof(commit_content),
            "tree %s\n"
            "author %s %s\n"
            "committer %s %s\n"
            "\n"
            "%s\n",
            tree_hash_str, author, timestamp_str, author, timestamp_str, message);
    } else {
        content_len = snprintf(commit_content, sizeof(commit_content),
            "tree %s\n"
            "parent %s\n"
            "author %s %s\n"
            "committer %s %s\n"
            "\n"
            "%s\n",
            tree_hash_str, parent_hash_str, author, timestamp_str, author, timestamp_str, message);
    }
    
    // Compute commit hash
    err = svcs_hash_object(SVCS_OBJ_COMMIT, commit_content, content_len, commit_hash);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Create and write commit object
    svcs_object_t commit_obj = {
        .type = SVCS_OBJ_COMMIT,
        .size = content_len,
        .hash = *commit_hash
    };
    
    err = svcs_object_write(repo, &commit_obj);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Update HEAD/branch reference
    char commit_hash_str[SVCS_HASH_HEX_SIZE];
    svcs_hash_to_string(commit_hash, commit_hash_str);
    
    // Read HEAD to determine current branch
    if (svcs_file_read(head_path, &head_data, &head_size) == SVCS_OK) {
        char *head_content = (char*)head_data;
        if (strncmp(head_content, "ref: ", 5) == 0) {
            char *ref_name = head_content + 5;
            char *newline = strchr(ref_name, '\n');
            if (newline) *newline = '\0';
            
            char ref_path[SVCS_MAX_PATH];
            snprintf(ref_path, sizeof(ref_path), "%s/%s", repo->git_dir, ref_name);
            
            // Create refs directory if it doesn't exist
            char refs_dir[SVCS_MAX_PATH];
            strncpy(refs_dir, ref_path, sizeof(refs_dir) - 1);
            char *last_slash = strrchr(refs_dir, '/');
            if (last_slash) {
                *last_slash = '\0';
                svcs_mkdir_recursive(refs_dir);
            }
            
            // Write new commit hash to branch
            char commit_with_newline[SVCS_HASH_HEX_SIZE + 1];
            snprintf(commit_with_newline, sizeof(commit_with_newline), "%s\n", commit_hash_str);
            svcs_file_write(ref_path, commit_with_newline, strlen(commit_with_newline));
        }
        free(head_data);
    }
    
    return SVCS_OK;
}

svcs_error_t svcs_commit_read(svcs_repository_t *repo, const svcs_hash_t *hash, svcs_commit_t **commit) {
    if (!repo || !hash || !commit) {
        return SVCS_ERROR_INVALID;
    }
    
    svcs_object_t *obj;
    svcs_error_t err = svcs_object_read(repo, hash, &obj);
    if (err != SVCS_OK) {
        return err;
    }
    
    if (obj->type != SVCS_OBJ_COMMIT) {
        svcs_object_free(obj);
        return SVCS_ERROR_INVALID;
    }
    
    *commit = calloc(1, sizeof(svcs_commit_t));
    if (!*commit) {
        svcs_object_free(obj);
        return SVCS_ERROR_MEMORY;
    }
    
    // Parse commit content (simplified)
    // In a real implementation, you'd parse the actual commit data
    (*commit)->timestamp = time(NULL);
    strncpy((*commit)->message, "Commit message", sizeof((*commit)->message) - 1);
    strncpy((*commit)->author, "Author", sizeof((*commit)->author) - 1);
    strncpy((*commit)->committer, "Committer", sizeof((*commit)->committer) - 1);
    
    svcs_object_free(obj);
    return SVCS_OK;
}

void svcs_commit_free(svcs_commit_t *commit) {
    if (commit) {
        free(commit);
    }
}