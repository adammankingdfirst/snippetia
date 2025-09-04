#include "svcs.h"
#include <zlib.h>

svcs_error_t svcs_compress(const void *input, size_t input_size, void **output, size_t *output_size) {
    if (!input || !output || !output_size || input_size == 0) {
        return SVCS_ERROR_INVALID;
    }
    
    // Estimate compressed size (worst case: input_size + 12 bytes)
    uLongf compressed_size = compressBound((uLong)input_size);
    *output = malloc(compressed_size);
    if (!*output) {
        return SVCS_ERROR_MEMORY;
    }
    
    int result = compress((Bytef*)*output, &compressed_size, 
                         (const Bytef*)input, (uLong)input_size);
    
    if (result != Z_OK) {
        free(*output);
        *output = NULL;
        return SVCS_ERROR;
    }
    
    *output_size = (size_t)compressed_size;
    
    // Resize to actual compressed size
    void *resized = realloc(*output, *output_size);
    if (resized) {
        *output = resized;
    }
    
    return SVCS_OK;
}

svcs_error_t svcs_decompress(const void *input, size_t input_size, void **output, size_t *output_size) {
    if (!input || !output || !output_size || input_size == 0) {
        return SVCS_ERROR_INVALID;
    }
    
    // Start with a reasonable buffer size and grow if needed
    size_t buffer_size = input_size * 4; // Initial guess
    *output = malloc(buffer_size);
    if (!*output) {
        return SVCS_ERROR_MEMORY;
    }
    
    uLongf decompressed_size = (uLongf)buffer_size;
    int result = uncompress((Bytef*)*output, &decompressed_size,
                           (const Bytef*)input, (uLong)input_size);
    
    while (result == Z_BUF_ERROR) {
        // Buffer too small, double it and try again
        buffer_size *= 2;
        void *new_buffer = realloc(*output, buffer_size);
        if (!new_buffer) {
            free(*output);
            *output = NULL;
            return SVCS_ERROR_MEMORY;
        }
        
        *output = new_buffer;
        decompressed_size = (uLongf)buffer_size;
        result = uncompress((Bytef*)*output, &decompressed_size,
                           (const Bytef*)input, (uLong)input_size);
    }
    
    if (result != Z_OK) {
        free(*output);
        *output = NULL;
        return SVCS_ERROR;
    }
    
    *output_size = (size_t)decompressed_size;
    
    // Resize to actual decompressed size
    void *resized = realloc(*output, *output_size);
    if (resized) {
        *output = resized;
    }
    
    return SVCS_OK;
}

// Compress file and write to output file
svcs_error_t svcs_compress_file(const char *input_path, const char *output_path) {
    if (!input_path || !output_path) {
        return SVCS_ERROR_INVALID;
    }
    
    void *input_data;
    size_t input_size;
    svcs_error_t err = svcs_file_read(input_path, &input_data, &input_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    void *compressed_data;
    size_t compressed_size;
    err = svcs_compress(input_data, input_size, &compressed_data, &compressed_size);
    free(input_data);
    
    if (err != SVCS_OK) {
        return err;
    }
    
    err = svcs_file_write(output_path, compressed_data, compressed_size);
    free(compressed_data);
    
    return err;
}

// Decompress file and write to output file
svcs_error_t svcs_decompress_file(const char *input_path, const char *output_path) {
    if (!input_path || !output_path) {
        return SVCS_ERROR_INVALID;
    }
    
    void *compressed_data;
    size_t compressed_size;
    svcs_error_t err = svcs_file_read(input_path, &compressed_data, &compressed_size);
    if (err != SVCS_OK) {
        return err;
    }
    
    void *decompressed_data;
    size_t decompressed_size;
    err = svcs_decompress(compressed_data, compressed_size, &decompressed_data, &decompressed_size);
    free(compressed_data);
    
    if (err != SVCS_OK) {
        return err;
    }
    
    err = svcs_file_write(output_path, decompressed_data, decompressed_size);
    free(decompressed_data);
    
    return err;
}