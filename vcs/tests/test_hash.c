#include <stdio.h>
#include <assert.h>
#include <string.h>
#include "svcs.h"

void test_hash_init() {
    svcs_hash_t hash;
    svcs_hash_init(&hash);
    
    // Check that hash is initialized to zeros
    for (int i = 0; i < SVCS_HASH_SIZE; i++) {
        assert(hash.bytes[i] == 0);
    }
    
    printf("✓ test_hash_init passed\n");
}

void test_hash_string_conversion() {
    svcs_hash_t hash;
    
    // Set a known pattern
    for (int i = 0; i < SVCS_HASH_SIZE; i++) {
        hash.bytes[i] = (uint8_t)i;
    }
    
    char hash_str[SVCS_HASH_HEX_SIZE];
    svcs_hash_to_string(&hash, hash_str);
    
    // Convert back
    svcs_hash_t hash2;
    svcs_error_t err = svcs_hash_from_string(&hash2, hash_str);
    assert(err == SVCS_OK);
    
    // Should be identical
    assert(svcs_hash_compare(&hash, &hash2) == 0);
    
    printf("✓ test_hash_string_conversion passed\n");
}

void test_hash_compare() {
    svcs_hash_t hash1, hash2, hash3;
    
    svcs_hash_init(&hash1);
    svcs_hash_init(&hash2);
    svcs_hash_init(&hash3);
    
    // Same hashes should compare equal
    assert(svcs_hash_compare(&hash1, &hash2) == 0);
    
    // Different hashes should not compare equal
    hash3.bytes[0] = 1;
    assert(svcs_hash_compare(&hash1, &hash3) != 0);
    
    printf("✓ test_hash_compare passed\n");
}

void test_hash_object() {
    const char *test_data = "Hello, World!";
    size_t data_size = strlen(test_data);
    
    svcs_hash_t hash;
    svcs_error_t err = svcs_hash_object(SVCS_OBJ_BLOB, test_data, data_size, &hash);
    assert(err == SVCS_OK);
    
    // Hash should not be all zeros
    int all_zeros = 1;
    for (int i = 0; i < SVCS_HASH_SIZE; i++) {
        if (hash.bytes[i] != 0) {
            all_zeros = 0;
            break;
        }
    }
    assert(!all_zeros);
    
    // Same data should produce same hash
    svcs_hash_t hash2;
    err = svcs_hash_object(SVCS_OBJ_BLOB, test_data, data_size, &hash2);
    assert(err == SVCS_OK);
    assert(svcs_hash_compare(&hash, &hash2) == 0);
    
    printf("✓ test_hash_object passed\n");
}

void test_hash_invalid_input() {
    svcs_hash_t hash;
    
    // Test invalid string conversion
    svcs_error_t err = svcs_hash_from_string(&hash, "invalid_hash");
    assert(err == SVCS_ERROR_INVALID);
    
    err = svcs_hash_from_string(&hash, "too_short");
    assert(err == SVCS_ERROR_INVALID);
    
    // Test null inputs
    assert(svcs_hash_compare(NULL, &hash) == -1);
    assert(svcs_hash_compare(&hash, NULL) == -1);
    
    printf("✓ test_hash_invalid_input passed\n");
}

int main() {
    printf("Running hash tests...\n");
    
    test_hash_init();
    test_hash_string_conversion();
    test_hash_compare();
    test_hash_object();
    test_hash_invalid_input();
    
    printf("All hash tests passed! ✓\n");
    return 0;
}