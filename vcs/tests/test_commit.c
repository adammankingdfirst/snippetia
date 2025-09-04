#include <stdio.h>
#include <assert.h>
#include <string.h>
#include "svcs.h"

void test_commit_create() {
    const char *test_path = "/tmp/svcs_commit_test";
    const char *test_file = "/tmp/commit_test.txt";
    const char *test_content = "Test file for commit";
    const char *commit_message = "Initial commit";
    const char *author = "Test Author <test@example.com>";
    
    // Clean up and setup
    system("rm -rf /tmp/svcs_commit_test");
    system("rm -f /tmp/commit_test.txt");
    
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
    
    // Add file to index
    err = svcs_index_add(repo, test_file);
    assert(err == SVCS_OK);
    
    // Create commit
    svcs_hash_t commit_hash;
    err = svcs_commit_create(repo, commit_message, author, &commit_hash);
    assert(err == SVCS_OK);
    
    // Commit hash should not be all zeros
    int all_zeros = 1;
    for (int i = 0; i < SVCS_HASH_SIZE; i++) {
        if (commit_hash.bytes[i] != 0) {
            all_zeros = 0;
            break;
        }
    }
    assert(!all_zeros);
    
    svcs_repository_free(repo);
    
    // Cleanup
    system("rm -rf /tmp/svcs_commit_test");
    system("rm -f /tmp/commit_test.txt");
    
    printf("✓ test_commit_create passed\n");
}

void test_commit_read() {
    const char *test_path = "/tmp/svcs_commit_test2";
    const char *test_file = "/tmp/commit_test2.txt";
    const char *test_content = "Test file for commit read";
    const char *commit_message = "Test commit for reading";
    const char *author = "Test Author <test@example.com>";
    
    // Clean up and setup
    system("rm -rf /tmp/svcs_commit_test2");
    system("rm -f /tmp/commit_test2.txt");
    
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
    
    // Add file and create commit
    err = svcs_index_add(repo, test_file);
    assert(err == SVCS_OK);
    
    svcs_hash_t commit_hash;
    err = svcs_commit_create(repo, commit_message, author, &commit_hash);
    assert(err == SVCS_OK);
    
    // Read commit back
    svcs_commit_t *commit;
    err = svcs_commit_read(repo, &commit_hash, &commit);
    assert(err == SVCS_OK);
    assert(commit != NULL);
    
    // Verify commit properties (simplified check)
    assert(strlen(commit->message) > 0);
    assert(strlen(commit->author) > 0);
    assert(commit->timestamp > 0);
    
    svcs_commit_free(commit);
    svcs_repository_free(repo);
    
    // Cleanup
    system("rm -rf /tmp/svcs_commit_test2");
    system("rm -f /tmp/commit_test2.txt");
    
    printf("✓ test_commit_read passed\n");
}

void test_commit_empty_index() {
    const char *test_path = "/tmp/svcs_commit_test3";
    const char *commit_message = "Empty commit";
    const char *author = "Test Author <test@example.com>";
    
    // Clean up and setup
    system("rm -rf /tmp/svcs_commit_test3");
    svcs_repository_init(test_path);
    
    svcs_repository_t *repo;
    svcs_error_t err = svcs_repository_open(&repo, test_path);
    assert(err == SVCS_OK);
    
    // Try to create commit with empty index
    svcs_hash_t commit_hash;
    err = svcs_commit_create(repo, commit_message, author, &commit_hash);
    assert(err == SVCS_OK); // Should succeed with empty tree
    
    svcs_repository_free(repo);
    
    // Cleanup
    system("rm -rf /tmp/svcs_commit_test3");
    
    printf("✓ test_commit_empty_index passed\n");
}

void test_commit_multiple() {
    const char *test_path = "/tmp/svcs_commit_test4";
    const char *test_file1 = "/tmp/commit_test4_1.txt";
    const char *test_file2 = "/tmp/commit_test4_2.txt";
    const char *author = "Test Author <test@example.com>";
    
    // Clean up and setup
    system("rm -rf /tmp/svcs_commit_test4");
    system("rm -f /tmp/commit_test4_*.txt");
    
    // Create test files
    FILE *f1 = fopen(test_file1, "w");
    assert(f1 != NULL);
    fwrite("File 1 content", 1, 14, f1);
    fclose(f1);
    
    FILE *f2 = fopen(test_file2, "w");
    assert(f2 != NULL);
    fwrite("File 2 content", 1, 14, f2);
    fclose(f2);
    
    // Initialize repository
    svcs_repository_init(test_path);
    
    svcs_repository_t *repo;
    svcs_error_t err = svcs_repository_open(&repo, test_path);
    assert(err == SVCS_OK);
    
    // First commit
    err = svcs_index_add(repo, test_file1);
    assert(err == SVCS_OK);
    
    svcs_hash_t commit1_hash;
    err = svcs_commit_create(repo, "First commit", author, &commit1_hash);
    assert(err == SVCS_OK);
    
    // Second commit
    err = svcs_index_add(repo, test_file2);
    assert(err == SVCS_OK);
    
    svcs_hash_t commit2_hash;
    err = svcs_commit_create(repo, "Second commit", author, &commit2_hash);
    assert(err == SVCS_OK);
    
    // Commits should have different hashes
    assert(svcs_hash_compare(&commit1_hash, &commit2_hash) != 0);
    
    svcs_repository_free(repo);
    
    // Cleanup
    system("rm -rf /tmp/svcs_commit_test4");
    system("rm -f /tmp/commit_test4_*.txt");
    
    printf("✓ test_commit_multiple passed\n");
}

int main() {
    printf("Running commit tests...\n");
    
    test_commit_create();
    test_commit_read();
    test_commit_empty_index();
    test_commit_multiple();
    
    printf("All commit tests passed! ✓\n");
    return 0;
}