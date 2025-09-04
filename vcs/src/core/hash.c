#include "svcs.h"
#include <openssl/evp.h>
#include <openssl/sha.h>

void svcs_hash_init(svcs_hash_t *hash) {
    if (hash) {
        memset(hash->bytes, 0, SVCS_HASH_SIZE);
    }
}

void svcs_hash_update(svcs_hash_t *hash, const void *data, size_t len) {
    if (!hash || !data || len == 0) return;
    
    EVP_MD_CTX *ctx = EVP_MD_CTX_new();
    if (!ctx) return;
    
    EVP_DigestInit_ex(ctx, EVP_sha3_256(), NULL);
    EVP_DigestUpdate(ctx, data, len);
    
    unsigned int hash_len = SVCS_HASH_SIZE;
    EVP_DigestFinal_ex(ctx, hash->bytes, &hash_len);
    EVP_MD_CTX_free(ctx);
}

void svcs_hash_final(svcs_hash_t *hash) {
    // SHA1 is already finalized in svcs_hash_update
    // This function exists for API compatibility
    (void)hash;
}

void svcs_hash_to_string(const svcs_hash_t *hash, char *str) {
    if (!hash || !str) return;
    
    for (int i = 0; i < SVCS_HASH_SIZE; i++) {
        sprintf(str + (i * 2), "%02x", hash->bytes[i]);
    }
    str[SVCS_HASH_HEX_SIZE - 1] = '\0';
}

svcs_error_t svcs_hash_from_string(svcs_hash_t *hash, const char *str) {
    if (!hash || !str) {
        return SVCS_ERROR_INVALID;
    }
    
    if (strlen(str) != SVCS_HASH_HEX_SIZE - 1) {
        return SVCS_ERROR_INVALID;
    }
    
    for (int i = 0; i < SVCS_HASH_SIZE; i++) {
        char byte_str[3] = {str[i * 2], str[i * 2 + 1], '\0'};
        hash->bytes[i] = (uint8_t)strtol(byte_str, NULL, 16);
    }
    
    return SVCS_OK;
}

int svcs_hash_compare(const svcs_hash_t *a, const svcs_hash_t *b) {
    if (!a || !b) return -1;
    return memcmp(a->bytes, b->bytes, SVCS_HASH_SIZE);
}

// Compute hash of file content
svcs_error_t svcs_hash_file(const char *path, svcs_hash_t *hash) {
    if (!path || !hash) {
        return SVCS_ERROR_INVALID;
    }
    
    void *data;
    size_t size;
    svcs_error_t err = svcs_file_read(path, &data, &size);
    if (err != SVCS_OK) {
        return err;
    }
    
    // Create object header for blob
    char header[64];
    int header_len = snprintf(header, sizeof(header), "blob %zu", size);
    
    // Hash header + null byte + content using SHA-3
    EVP_MD_CTX *ctx = EVP_MD_CTX_new();
    if (!ctx) {
        free(data);
        return SVCS_ERROR_MEMORY;
    }
    
    EVP_DigestInit_ex(ctx, EVP_sha3_256(), NULL);
    EVP_DigestUpdate(ctx, header, header_len);
    EVP_DigestUpdate(ctx, "\0", 1);
    EVP_DigestUpdate(ctx, data, size);
    
    unsigned int hash_len = SVCS_HASH_SIZE;
    EVP_DigestFinal_ex(ctx, hash->bytes, &hash_len);
    EVP_MD_CTX_free(ctx);
    
    free(data);
    return SVCS_OK;
}

// Compute hash of data with object type
svcs_error_t svcs_hash_object(svcs_object_type_t type, const void *data, size_t size, svcs_hash_t *hash) {
    if (!data || !hash) {
        return SVCS_ERROR_INVALID;
    }
    
    const char *type_str;
    switch (type) {
        case SVCS_OBJ_BLOB: type_str = "blob"; break;
        case SVCS_OBJ_TREE: type_str = "tree"; break;
        case SVCS_OBJ_COMMIT: type_str = "commit"; break;
        case SVCS_OBJ_TAG: type_str = "tag"; break;
        default: return SVCS_ERROR_INVALID;
    }
    
    char header[64];
    int header_len = snprintf(header, sizeof(header), "%s %zu", type_str, size);
    
    EVP_MD_CTX *ctx = EVP_MD_CTX_new();
    if (!ctx) {
        return SVCS_ERROR_MEMORY;
    }
    
    EVP_DigestInit_ex(ctx, EVP_sha3_256(), NULL);
    EVP_DigestUpdate(ctx, header, header_len);
    EVP_DigestUpdate(ctx, "\0", 1);
    EVP_DigestUpdate(ctx, data, size);
    
    unsigned int hash_len = SVCS_HASH_SIZE;
    EVP_DigestFinal_ex(ctx, hash->bytes, &hash_len);
    EVP_MD_CTX_free(ctx);
    
    return SVCS_OK;
}