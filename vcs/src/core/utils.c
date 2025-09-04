#include "svcs.h"
#include <sys/stat.h>
#include <errno.h>

svcs_error_t svcs_file_read(const char *path, void **data, size_t *size) {
    if (!path || !data || !size) {
        return SVCS_ERROR_INVALID;
    }
    
    FILE *file = fopen(path, "rb");
    if (!file) {
        return SVCS_ERROR_IO;
    }
    
    // Get file size
    fseek(file, 0, SEEK_END);
    long file_size = ftell(file);
    fseek(file, 0, SEEK_SET);
    
    if (file_size < 0) {
        fclose(file);
        return SVCS_ERROR_IO;
    }
    
    *size = (size_t)file_size;
    *data = malloc(*size);
    if (!*data) {
        fclose(file);
        return SVCS_ERROR_MEMORY;
    }
    
    size_t bytes_read = fread(*data, 1, *size, file);
    fclose(file);
    
    if (bytes_read != *size) {
        free(*data);
        *data = NULL;
        return SVCS_ERROR_IO;
    }
    
    return SVCS_OK;
}

svcs_error_t svcs_file_write(const char *path, const void *data, size_t size) {
    if (!path || !data) {
        return SVCS_ERROR_INVALID;
    }
    
    FILE *file = fopen(path, "wb");
    if (!file) {
        return SVCS_ERROR_IO;
    }
    
    size_t bytes_written = fwrite(data, 1, size, file);
    fclose(file);
    
    if (bytes_written != size) {
        return SVCS_ERROR_IO;
    }
    
    return SVCS_OK;
}

svcs_error_t svcs_mkdir_recursive(const char *path) {
    if (!path) {
        return SVCS_ERROR_INVALID;
    }
    
    char tmp[SVCS_MAX_PATH];
    char *p = NULL;
    size_t len;
    
    snprintf(tmp, sizeof(tmp), "%s", path);
    len = strlen(tmp);
    
    if (tmp[len - 1] == '/') {
        tmp[len - 1] = 0;
    }
    
    for (p = tmp + 1; *p; p++) {
        if (*p == '/') {
            *p = 0;
            
            if (mkdir(tmp, 0755) != 0 && errno != EEXIST) {
                return SVCS_ERROR_IO;
            }
            
            *p = '/';
        }
    }
    
    if (mkdir(tmp, 0755) != 0 && errno != EEXIST) {
        return SVCS_ERROR_IO;
    }
    
    return SVCS_OK;
}

int svcs_file_exists(const char *path) {
    if (!path) return 0;
    
    struct stat st;
    return stat(path, &st) == 0;
}

time_t svcs_file_mtime(const char *path) {
    if (!path) return 0;
    
    struct stat st;
    if (stat(path, &st) == 0) {
        return st.st_mtime;
    }
    
    return 0;
}

// Get relative path from base to target
char* svcs_path_relative(const char *base, const char *target) {
    if (!base || !target) return NULL;
    
    // Simple implementation - just remove base prefix if it matches
    size_t base_len = strlen(base);
    if (strncmp(base, target, base_len) == 0) {
        const char *relative = target + base_len;
        if (*relative == '/') relative++;
        
        char *result = malloc(strlen(relative) + 1);
        if (result) {
            strcpy(result, relative);
        }
        return result;
    }
    
    // If no common prefix, return copy of target
    char *result = malloc(strlen(target) + 1);
    if (result) {
        strcpy(result, target);
    }
    return result;
}

// Check if path is ignored (simplified .gitignore-like functionality)
int svcs_path_is_ignored(const char *path) {
    if (!path) return 1;
    
    // Ignore .svcs directory
    if (strstr(path, ".svcs") != NULL) {
        return 1;
    }
    
    // Ignore common temporary files
    const char *ignored_patterns[] = {
        ".tmp", ".temp", ".log", ".bak", "~", ".swp", ".swo"
    };
    
    for (size_t i = 0; i < sizeof(ignored_patterns) / sizeof(ignored_patterns[0]); i++) {
        if (strstr(path, ignored_patterns[i]) != NULL) {
            return 1;
        }
    }
    
    return 0;
}

// Simple string utilities
char* svcs_string_duplicate(const char *str) {
    if (!str) return NULL;
    
    size_t len = strlen(str);
    char *dup = malloc(len + 1);
    if (dup) {
        strcpy(dup, str);
    }
    return dup;
}

void svcs_string_trim(char *str) {
    if (!str) return;
    
    // Trim leading whitespace
    char *start = str;
    while (*start && (*start == ' ' || *start == '\t' || *start == '\n' || *start == '\r')) {
        start++;
    }
    
    // Trim trailing whitespace
    char *end = str + strlen(str) - 1;
    while (end > start && (*end == ' ' || *end == '\t' || *end == '\n' || *end == '\r')) {
        *end = '\0';
        end--;
    }
    
    // Move trimmed string to beginning
    if (start != str) {
        memmove(str, start, strlen(start) + 1);
    }
}