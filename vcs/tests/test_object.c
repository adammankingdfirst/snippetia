#include <stdio.h>
#include <assert.h>
#include <string.h>
#include "svcs.h"

void test_object_create_blob() {
    const char *test_path = "/tmp/svcs_object_test";
    const char *test_file = "/tmp/test_blob.txt";
    const char *test_content = "Hello, SnippetVCS!";
    
    // Clean up and setup
    system("rm -rf /tmp/svcs_object_test");
    system("rm -f /tmp/test_blob.txt");
    
    // Create test file
    FILE *f = fopen(test_file, "w");
    assert(f != NULL);
    fwrite(test_content, 1, strlen(test_content), f);
    fclose(f);
    
    // Initialize repository
    svcs_repository_init(test_path);
    
    svcs_repository_t *repo;
    svcs_error_t err = svcs_repository_open(&repo, test_path);
    assert(err == SVCS_OK);
    
    // Create blob object
    svcs_hash_t hash;
    err = svcs_object_create_blob(repo, test_file, &hash);
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
    
    // Creating the same blob again should produce the same hash
    svcs_hash_t hash2;
    err = svcs_object_create_blob(repo, test_file, &hash2);
    assert(err == SVCS_OK);
    assert(svcs_hash_compare(&hash, &hash2) == 0);
    
    svcs_repository_free(repo);
    
    // Cleanup
    system("rm -rf /tmp/svcs_object_test");
    system("rm -f /tmp/test_blob.txt");
    
    printf("✓ test_object_create_blob passed\n");
}

void test_object_write_read() {
    const char *test_path = "/tmp/svcs_object_test2";
    
    // Clean up and setup
    system("rm -rf /tmp/svcs_object_test2");
    svcs_repository_init(test_path);
    
    svcs_repository_t *repo;
    svcs_error_t err = svcs_repository_open(&repo, test_path);
    assert(err == SVCS_OK);
    
    // Create a test object
    const char *test_data = "Test object content";
    svcs_hash_t hash;
    err = svcs_hash_object(SVCS_OBJ_BLOB, test_data, strlen(test_data), &hash);
    assert(err == SVCS_OK);
    
    svcs_object_t obj = {
        .type = SVCS_OBJ_BLOB,
        .size = strlen(test_data),
        .hash = hash
    };
    
    // Write object
    err = svcs_object_write(repo, &obj);
    assert(err == SVCS_OK);
    
    // Read object back
    svcs_object_t *read_obj;
    err = svcs_object_read(repo, &hash, &read_obj);
    assert(err == SVCS_OK);
    assert(read_obj != NULL);
    
    // Verify object properties
    assert(read_obj->type == SVCS_OBJ_BLOB);
    assert(read_obj->size == strlen(test_data));
    assert(svcs_hash_compare(&read_obj->hash, &hash) == 0);
    
    svcs_object_free(read_obj);
    svcs_repository_free(repo);
    
    // Cleanup
    system("rm -rf /tmp/svcs_object_test2");
    
    printf("✓ test_object_write_read passed\n");
}

void test_object_nonexistent() {
    const char *test_path = "/tmp/svcs_object_test3";
    
    // Clean up and setup
    system("rm -rf /tmp/svcs_object_test3");
    svcs_repository_init(test_path);
    
    svcs_repository_t *repo;
    svcs_error_t err = svcs_repository_open(&repo, test_path);
    assert(err == SVCS_OK);
    
    // Try to read non-existent object
    svcs_hash_t fake_hash;
    memset(&fake_hash, 0xFF, sizeof(fake_hash)); // All 0xFF bytes
    
    svcs_object_t *obj;
    err = svcs_object_read(repo, &fake_hash, &obj);
    assert(err == SVCS_ERROR_NOT_FOUND);
    assert(obj == NULL);
    
    svcs_repository_free(repo);
    
    // Cleanup
    system("rm -rf /tmp/svcs_object_test3");
    
    printf("✓ test_object_nonexistent passed\n");
}

int main() {
    printf("Running object tests...\n");
    
    test_object_create_blob();
    test_object_write_read();
    test_object_nonexistent();
    
    printf("All object tests passed! ✓\n");
    return 0;
}