#include "svcs.h"

// Simple line-based diff algorithm (Myers algorithm simplified)
static svcs_error_t compute_diff_lines(char **old_lines, size_t old_count,
                                      char **new_lines, size_t new_count,
                                      svcs_diff_hunk_t **hunk) {
    if (!hunk) {
        return SVCS_ERROR_INVALID;
    }
    
    *hunk = calloc(1, sizeof(svcs_diff_hunk_t));
    if (!*hunk) {
        return SVCS_ERROR_MEMORY;
    }
    
    // Simple implementation: treat everything as one hunk
    (*hunk)->old_start = 1;
    (*hunk)->old_count = (int)old_count;
    (*hunk)->new_start = 1;
    (*hunk)->new_count = (int)new_count;
    
    // Calculate maximum possible lines (all old + all new)
    size_t max_lines = old_count + new_count;
    (*hunk)->lines = calloc(max_lines, sizeof(svcs_diff_line_t));
    if (!(*hunk)->lines) {
        free(*hunk);
        return SVCS_ERROR_MEMORY;
    }
    
    size_t line_idx = 0;
    size_t old_idx = 0;
    size_t new_idx = 0;
    
    // Simple diff: compare line by line
    while (old_idx < old_count || new_idx < new_count) {
        if (old_idx < old_count && new_idx < new_count) {
            // Both files have lines at this position
            if (strcmp(old_lines[old_idx], new_lines[new_idx]) == 0) {
                // Lines are the same - context
                (*hunk)->lines[line_idx].type = SVCS_DIFF_CONTEXT;
                (*hunk)->lines[line_idx].old_line = (int)old_idx + 1;
                (*hunk)->lines[line_idx].new_line = (int)new_idx + 1;
                strncpy((*hunk)->lines[line_idx].content, old_lines[old_idx], 
                       sizeof((*hunk)->lines[line_idx].content) - 1);
                old_idx++;
                new_idx++;
            } else {
                // Lines are different - deletion followed by addition
                (*hunk)->lines[line_idx].type = SVCS_DIFF_DEL;
                (*hunk)->lines[line_idx].old_line = (int)old_idx + 1;
                (*hunk)->lines[line_idx].new_line = -1;
                strncpy((*hunk)->lines[line_idx].content, old_lines[old_idx], 
                       sizeof((*hunk)->lines[line_idx].content) - 1);
                line_idx++;
                old_idx++;
                
                if (line_idx < max_lines) {
                    (*hunk)->lines[line_idx].type = SVCS_DIFF_ADD;
                    (*hunk)->lines[line_idx].old_line = -1;
                    (*hunk)->lines[line_idx].new_line = (int)new_idx + 1;
                    strncpy((*hunk)->lines[line_idx].content, new_lines[new_idx], 
                           sizeof((*hunk)->lines[line_idx].content) - 1);
                    new_idx++;
                }
            }
        } else if (old_idx < old_count) {
            // Only old file has lines - deletion
            (*hunk)->lines[line_idx].type = SVCS_DIFF_DEL;
            (*hunk)->lines[line_idx].old_line = (int)old_idx + 1;
            (*hunk)->lines[line_idx].new_line = -1;
            strncpy((*hunk)->lines[line_idx].content, old_lines[old_idx], 
                   sizeof((*hunk)->lines[line_idx].content) - 1);
            old_idx++;
        } else {
            // Only new file has lines - addition
            (*hunk)->lines[line_idx].type = SVCS_DIFF_ADD;
            (*hunk)->lines[line_idx].old_line = -1;
            (*hunk)->lines[line_idx].new_line = (int)new_idx + 1;
            strncpy((*hunk)->lines[line_idx].content, new_lines[new_idx], 
                   sizeof((*hunk)->lines[line_idx].content) - 1);
            new_idx++;
        }
        
        line_idx++;
        if (line_idx >= max_lines) break;
    }
    
    (*hunk)->line_count = line_idx;
    
    return SVCS_OK;
}

static char** split_lines(const char *content, size_t content_size, size_t *line_count) {
    if (!content || content_size == 0 || !line_count) {
        *line_count = 0;
        return NULL;
    }
    
    // Count lines
    *line_count = 1;
    for (size_t i = 0; i < content_size; i++) {
        if (content[i] == '\n') {
            (*line_count)++;
        }
    }
    
    char **lines = calloc(*line_count, sizeof(char*));
    if (!lines) {
        *line_count = 0;
        return NULL;
    }
    
    size_t line_idx = 0;
    size_t line_start = 0;
    
    for (size_t i = 0; i <= content_size; i++) {
        if (i == content_size || content[i] == '\n') {
            size_t line_len = i - line_start;
            lines[line_idx] = malloc(line_len + 1);
            if (!lines[line_idx]) {
                // Cleanup on error
                for (size_t j = 0; j < line_idx; j++) {
                    free(lines[j]);
                }
                free(lines);
                *line_count = 0;
                return NULL;
            }
            
            memcpy(lines[line_idx], content + line_start, line_len);
            lines[line_idx][line_len] = '\0';
            
            line_idx++;
            line_start = i + 1;
        }
    }
    
    return lines;
}

svcs_error_t svcs_diff_files(const char *old_path, const char *new_path, svcs_diff_file_t **diff) {
    if (!diff) {
        return SVCS_ERROR_INVALID;
    }
    
    *diff = calloc(1, sizeof(svcs_diff_file_t));
    if (!*diff) {
        return SVCS_ERROR_MEMORY;
    }
    
    // Set file paths
    if (old_path) {
        strncpy((*diff)->old_path, old_path, sizeof((*diff)->old_path) - 1);
    }
    if (new_path) {
        strncpy((*diff)->new_path, new_path, sizeof((*diff)->new_path) - 1);
    }
    
    // Determine status
    if (!old_path && new_path) {
        (*diff)->status = SVCS_STATUS_ADDED;
    } else if (old_path && !new_path) {
        (*diff)->status = SVCS_STATUS_DELETED;
    } else {
        (*diff)->status = SVCS_STATUS_MODIFIED;
    }
    
    // Read file contents
    void *old_content = NULL;
    size_t old_size = 0;
    void *new_content = NULL;
    size_t new_size = 0;
    
    if (old_path && svcs_file_exists(old_path)) {
        svcs_file_read(old_path, &old_content, &old_size);
    }
    
    if (new_path && svcs_file_exists(new_path)) {
        svcs_file_read(new_path, &new_content, &new_size);
    }
    
    // Split into lines
    size_t old_line_count, new_line_count;
    char **old_lines = split_lines((char*)old_content, old_size, &old_line_count);
    char **new_lines = split_lines((char*)new_content, new_size, &new_line_count);
    
    // Compute diff
    svcs_diff_hunk_t *hunk;
    svcs_error_t err = compute_diff_lines(old_lines, old_line_count, 
                                         new_lines, new_line_count, &hunk);
    
    if (err == SVCS_OK) {
        (*diff)->hunks = hunk;
        (*diff)->hunk_count = 1;
    }
    
    // Cleanup
    if (old_lines) {
        for (size_t i = 0; i < old_line_count; i++) {
            free(old_lines[i]);
        }
        free(old_lines);
    }
    
    if (new_lines) {
        for (size_t i = 0; i < new_line_count; i++) {
            free(new_lines[i]);
        }
        free(new_lines);
    }
    
    if (old_content) free(old_content);
    if (new_content) free(new_content);
    
    return err;
}

svcs_error_t svcs_diff_commits(svcs_repository_t *repo, const svcs_hash_t *old_hash, 
                              const svcs_hash_t *new_hash, svcs_diff_file_t **diffs, size_t *count) {
    if (!repo || !diffs || !count) {
        return SVCS_ERROR_INVALID;
    }
    
    // Simplified implementation - just return empty diff for now
    // In a complete implementation, this would:
    // 1. Read both commit objects
    // 2. Compare their tree objects
    // 3. Generate diffs for changed files
    
    *diffs = NULL;
    *count = 0;
    
    return SVCS_OK;
}

void svcs_diff_free(svcs_diff_file_t *diff) {
    if (!diff) return;
    
    if (diff->hunks) {
        for (size_t i = 0; i < diff->hunk_count; i++) {
            if (diff->hunks[i].lines) {
                free(diff->hunks[i].lines);
            }
        }
        free(diff->hunks);
    }
    
    free(diff);
}

// Print diff in unified format
void svcs_diff_print(const svcs_diff_file_t *diff) {
    if (!diff) return;
    
    printf("--- %s\n", diff->old_path[0] ? diff->old_path : "/dev/null");
    printf("+++ %s\n", diff->new_path[0] ? diff->new_path : "/dev/null");
    
    for (size_t i = 0; i < diff->hunk_count; i++) {
        const svcs_diff_hunk_t *hunk = &diff->hunks[i];
        
        printf("@@ -%d,%d +%d,%d @@\n", 
               hunk->old_start, hunk->old_count,
               hunk->new_start, hunk->new_count);
        
        for (size_t j = 0; j < hunk->line_count; j++) {
            const svcs_diff_line_t *line = &hunk->lines[j];
            
            char prefix;
            switch (line->type) {
                case SVCS_DIFF_ADD: prefix = '+'; break;
                case SVCS_DIFF_DEL: prefix = '-'; break;
                case SVCS_DIFF_CONTEXT: prefix = ' '; break;
                default: prefix = '?'; break;
            }
            
            printf("%c%s\n", prefix, line->content);
        }
    }
}