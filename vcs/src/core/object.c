#include "svcs.h"

static char* get_object_path(svcs_repository_t *repo, const svcs_hash_t *hash) {
    char hash_str[SVCS_HASH_HEX_SIZE];
    svcs_hash_to_string(hash, hash_str);
    
    char *path = malloc(SVCS_MAX_PATH);
    if (!path) return NULL;
    
    snprintf(path, SVCS_MAX_PATH, "%s/objects/%.2s/%s", 
             repo->git_dir, hash_str, hash_str + 2);
    
    return path;
}

svcs_error_t svcs_object_read(svcs_repository_t *repo, const svcs_hash_t *hash, svcs_object_t **obj) {
    if (!repo || !hash || !obj) {
        return SVCS_ERROR_INVALID;
    }
    
    char *path = get_object_path(repo, hash);
    if (!path) {
        return SVCS_ERROR_MEMORY;
    }
    
    if (!svcs_file_exists(path)) {
        free(path);
        return SVCS_ERROR_NOT_FOUND;
    }
    
    void *compressed_data;
    size_t compressed_size;
    svcs_error_t err = svcs_file_read(path, &compressed_data, &compressed_size);
    free(path);
    
    if (err != SVCS_OK) {
        return err;
    }
    
    // Decompress object data
    void *data;
    size_t size;
    err = svcs_decompress(compressed_data, compressed_size, &data, &size);
    free(compressed_data);
    
    if (err != SVCS_OK) {
        return err;
    }
    
    // Parse object header
    char *header_end = memchr(data, '\0', size);
    if (!header_end) {
        free(data);
        return SVCS_ERROR_CORRUPT;
    }
    
    char *header = (char*)data;
    char *space = strchr(header, ' ');
    if (!space) {
        free(data);
        return SVCS_ERROR_CORRUPT;
    }
    
    *space = '\0';
    size_t object_size = strtoul(space + 1, NULL, 10);
    
    *obj = malloc(sizeof(svcs_object_t));
    if (!*obj) {
        free(data);
        return SVCS_ERROR_MEMORY;
    }
    
    // Determine object type
    if (strcmp(header, "blob") == 0) {
        (*obj)->type = SVCS_OBJ_BLOB;
    } else if (strcmp(header, "tree") == 0) {
        (*obj)->type = SVCS_OBJ_TREE;
    } else if (strcmp(header, "commit") == 0) {
        (*obj)->type = SVCS_OBJ_COMMIT;
    } else if (strcmp(header, "tag") == 0) {
        (*obj)->type = SVCS_OBJ_TAG;
    } else {
        free(data);
        free(*obj);
        return SVCS_ERROR_CORRUPT;
    }
    
    (*obj)->size = object_size;
    (*obj)->hash = *hash;
    
    // Store object content (after header + null byte)
    size_t content_size = size - (header_end - (char*)data + 1);
    if (content_size != object_size) {
        free(data);
        free(*obj);
        return SVCS_ERROR_CORRUPT;
    }
    
    free(data);
    return SVCS_OK;
}

svcs_error_t svcs_object_write(svcs_repository_t *repo, svcs_object_t *obj) {
    if (!repo || !obj) {
        return SVCS_ERROR_INVALID;
    }
    
    char *path = get_object_path(repo, &obj->hash);
    if (!path) {
        return SVCS_ERROR_MEMORY;
    }
    
    // Create directory if it doesn't exist
    char dir_path[SVCS_MAX_PATH];
    strncpy(dir_path, path, sizeof(dir_path) - 1);
    char *last_slash = strrchr(dir_path, '/');
    if (last_slash) {
        *last_slash = '\0';
        svcs_mkdir_recursive(dir_path);
    }
    
    // Object already exists
    if (svcs_file_exists(path)) {
        free(path);
        return SVCS_OK;
    }
    
    // Create object data with header
    const char *type_str;
    switch (obj->type) {
        case SVCS_OBJ_BLOB: type_str = "blob"; break;
        case SVCS_OBJ_TREE: type_str = "tree"; break;
        case SVCS_OBJ_COMMIT: type_str = "commit"; break;
        case SVCS_OBJ_TAG: type_str = "tag"; break;
        default:
            free(path);
            return SVCS_ERROR_INVALID;
    }
    
    char header[64];
    int header_len = snprintf(header, sizeof(header), "%s %zu", type_str, obj->size);
    
    // For now, we'll write uncompressed data
    // In a real implementation, you'd compress with zlib
    FILE *file = fopen(path, "wb");
    if (!file) {
        free(path);
        return SVCS_ERROR_IO;
    }
    
    fwrite(header, 1, header_len, file);
    fwrite("\0", 1, 1, file);
    // Note: In a complete implementation, you'd write the actual object content here
    
    fclose(file);
    free(path);
    
    return SVCS_OK;
}

void svcs_object_free(svcs_object_t *obj) {
    if (obj) {
        free(obj);
    }
}

// Create blob object from file
svcs_error_t svcs_object_create_blob(svcs_repository_t *repo, const char *file_path, svcs_hash_t *hash) {
    if (!repo || !file_path || !hash) {
        return SVCS_ERROR_INVALID;
    }
    
    void *data;
    size_t size;
    svcs_error_t err = svcs_file_read(file_path, &data, &size);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Compute hash
    err = svcs_hash_object(SVCS_OBJ_BLOB, data, size, hash);
    if (err != SVCS_OK) {
        free(data);
        return err;
    }
    
    // Create object
    svcs_object_t obj = {
        .type = SVCS_OBJ_BLOB,
        .size = size,
        .hash = *hash
    };
    
    err = svcs_object_write(repo, &obj);
    free(data);
    
    return err;
}