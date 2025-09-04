#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include "svcs.h"

void test_repository_init() {
    const char *test_path = "/tmp/svcs_test_repo";
    
    // Clean up any existing test directory
    system("rm -rf /tmp/svcs_test_repo");
    
    // Test repository initialization
    svcs_error_t err = svcs_repository_init(test_path);
    assert(err == SVCS_OK);
    
    // Check that .svcs directory was created
    char svcs_dir[1024];
    snprintf(svcs_dir, sizeof(svcs_dir), "%s/.svcs", test_path);
    assert(svcs_file_exists(svcs_dir));
    
    // Check that objects directory was created
    char objects_dir[1024];
    snprintf(objects_dir, sizeof(objects_dir), "%s/.svcs/objects", test_path);
    assert(svcs_file_exists(objects_dir));
    
    // Check that refs directory was created
    char refs_dir[1024];
    snprintf(refs_dir, sizeof(refs_dir), "%s/.svcs/refs", test_path);
    assert(svcs_file_exists(refs_dir));
    
    // Check that HEAD file was created
    char head_file[1024];
    snprintf(head_file, sizeof(head_file), "%s/.svcs/HEAD", test_path);
    assert(svcs_file_exists(head_file));
    
    // Cleanup
    system("rm -rf /tmp/svcs_test_repo");
    
    printf("✓ test_repository_init passed\n");
}

void test_repository_open() {
    const char *test_path = "/tmp/svcs_test_repo2";
    
    // Clean up and initialize
    system("rm -rf /tmp/svcs_test_repo2");
    svcs_repository_init(test_path);
    
    // Test opening repository
    svcs_repository_t *repo;
    svcs_error_t err = svcs_repository_open(&repo, test_path);
    assert(err == SVCS_OK);
    assert(repo != NULL);
    
    // Check repository paths
    assert(strlen(repo->path) > 0);
    assert(strlen(repo->git_dir) > 0);
    assert(strstr(repo->git_dir, ".svcs") != NULL);
    
    // Check that index was loaded
    assert(repo->index != NULL);
    
    svcs_repository_free(repo);
    
    // Test opening non-existent repository
    svcs_repository_t *repo2;
    err = svcs_repository_open(&repo2, "/tmp/nonexistent");
    assert(err == SVCS_ERROR_NOT_FOUND);
    assert(repo2 == NULL);
    
    // Cleanup
    system("rm -rf /tmp/svcs_test_repo2");
    
    printf("✓ test_repository_open passed\n");
}

void test_repository_is_valid() {
    const char *test_path = "/tmp/svcs_test_repo3";
    
    // Clean up
    system("rm -rf /tmp/svcs_test_repo3");
    
    // Should not be valid before initialization
    assert(!svcs_repository_is_valid(test_path));
    
    // Initialize repository
    svcs_repository_init(test_path);
    
    // Should be valid after initialization
    assert(svcs_repository_is_valid(test_path));
    
    // Test with NULL path
    assert(!svcs_repository_is_valid(NULL));
    
    // Cleanup
    system("rm -rf /tmp/svcs_test_repo3");
    
    printf("✓ test_repository_is_valid passed\n");
}

void test_repository_nested_discovery() {
    const char *base_path = "/tmp/svcs_nested_test";
    const char *nested_path = "/tmp/svcs_nested_test/subdir/deep";
    
    // Clean up and create directory structure
    system("rm -rf /tmp/svcs_nested_test");
    system("mkdir -p /tmp/svcs_nested_test/subdir/deep");
    
    // Initialize repository in base directory
    svcs_repository_init(base_path);
    
    // Should be able to open repository from nested directory
    svcs_repository_t *repo;
    svcs_error_t err = svcs_repository_open(&repo, nested_path);
    assert(err == SVCS_OK);
    assert(repo != NULL);
    
    // Repository path should point to base directory
    assert(strstr(repo->path, "svcs_nested_test") != NULL);
    assert(strstr(repo->path, "subdir") == NULL);
    
    svcs_repository_free(repo);
    
    // Cleanup
    system("rm -rf /tmp/svcs_nested_test");
    
    printf("✓ test_repository_nested_discovery passed\n");
}

int main() {
    printf("Running repository tests...\n");
    
    test_repository_init();
    test_repository_open();
    test_repository_is_valid();
    test_repository_nested_discovery();
    
    printf("All repository tests passed! ✓\n");
    return 0;
}