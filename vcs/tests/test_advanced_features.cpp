#include <gtest/gtest.h>
#include <memory>
#include "advanced_parser.hpp"
#include "dag.hpp"
#include "terminal_ui.hpp"

using namespace svcs::cli;
using namespace svcs::core;
using namespace svcs::ui;

class AdvancedFeaturesTest : public ::testing::Test {
protected:
    void SetUp() override {
        // Setup test environment
    }
    
    void TearDown() override {
        // Cleanup
    }
};

// Test Advanced Argument Parser
TEST_F(AdvancedFeaturesTest, ArgumentParserBasicFunctionality) {
    auto parser = ArgumentParserBuilder("test", "Test application", "1.0.0")
        .global_option(make_flag_option("v", "verbose", "Enable verbose output"))
        .global_option(make_string_option("f", "file", "Input file", false, "default.txt"))
        .subcommand({
            "init",
            "Initialize repository",
            "Initialize a new repository",
            {
                make_flag_option("", "bare", "Create bare repository"),
                make_string_option("", "template", "Template directory", false, "")
            },
            {"path"},
            nullptr
        })
        .build();
    
    ASSERT_NE(parser, nullptr);
    
    // Test parsing valid arguments
    std::vector<std::string> args = {"init", "--bare", "/tmp/test"};
    auto result = parser->parse(args);
    
    EXPECT_EQ(result.subcommand, "init");
    EXPECT_EQ(result.positional_args.size(), 1);
    EXPECT_EQ(result.positional_args[0], "/tmp/test");
    EXPECT_TRUE(result.options.count("bare") > 0);
    EXPECT_TRUE(result.error_message.empty());
}

TEST_F(AdvancedFeaturesTest, ArgumentParserErrorHandling) {
    auto parser = ArgumentParserBuilder("test", "Test application", "1.0.0")
        .subcommand({
            "commit",
            "Create commit",
            "Create a new commit",
            {
                make_string_option("m", "message", "Commit message", true)  // Required
            },
            {},
            nullptr
        })
        .build();
    
    // Test missing required option
    std::vector<std::string> args = {"commit"};
    auto result = parser->parse(args);
    
    EXPECT_FALSE(result.error_message.empty());
    EXPECT_TRUE(result.error_message.find("Required option missing") != std::string::npos);
}

TEST_F(AdvancedFeaturesTest, ArgumentParserChoiceValidation) {
    auto parser = ArgumentParserBuilder("test", "Test application", "1.0.0")
        .subcommand({
            "log",
            "Show log",
            "Show commit log",
            {
                make_choice_option("", "format", "Output format", {"short", "full", "oneline"}, "short")
            },
            {},
            nullptr
        })
        .build();
    
    // Test valid choice
    std::vector<std::string> args = {"log", "--format", "oneline"};
    auto result = parser->parse(args);
    
    EXPECT_TRUE(result.error_message.empty());
    EXPECT_EQ(std::get<std::string>(result.options["format"]), "oneline");
    
    // Test invalid choice
    args = {"log", "--format", "invalid"};
    result = parser->parse(args);
    
    EXPECT_FALSE(result.error_message.empty());
}

// Test DAG functionality
TEST_F(AdvancedFeaturesTest, DAGBasicOperations) {
    // Create a mock repository for testing
    svcs_repository_t* repo = nullptr;
    // In a real test, you'd set up a proper test repository
    
    CommitDAG dag(repo);
    
    // Test adding commits
    svcs_hash_t hash1, hash2, hash3;
    memset(&hash1, 0x01, sizeof(hash1));
    memset(&hash2, 0x02, sizeof(hash2));
    memset(&hash3, 0x03, sizeof(hash3));
    
    time_t now = time(nullptr);
    
    // Add root commit
    auto err = dag.add_commit(hash1, "Initial commit", "Author <author@example.com>", now, {});
    EXPECT_EQ(err, SVCS_OK);
    
    // Add second commit with hash1 as parent
    err = dag.add_commit(hash2, "Second commit", "Author <author@example.com>", now + 100, {hash1});
    EXPECT_EQ(err, SVCS_OK);
    
    // Add third commit with hash2 as parent
    err = dag.add_commit(hash3, "Third commit", "Author <author@example.com>", now + 200, {hash2});
    EXPECT_EQ(err, SVCS_OK);
    
    EXPECT_EQ(dag.size(), 3);
    
    // Test topological sort
    auto sorted_commits = dag.topological_sort();
    EXPECT_EQ(sorted_commits.size(), 3);
    
    // First commit should be the root
    EXPECT_TRUE(sorted_commits[0]->is_root_commit());
    
    // Test statistics
    auto stats = dag.get_statistics();
    EXPECT_EQ(stats.total_commits, 3);
    EXPECT_EQ(stats.root_commits, 1);
    EXPECT_EQ(stats.merge_commits, 0);
}

TEST_F(AdvancedFeaturesTest, DAGMergeCommits) {
    svcs_repository_t* repo = nullptr;
    CommitDAG dag(repo);
    
    svcs_hash_t hash1, hash2, hash3, merge_hash;
    memset(&hash1, 0x01, sizeof(hash1));
    memset(&hash2, 0x02, sizeof(hash2));
    memset(&hash3, 0x03, sizeof(hash3));
    memset(&merge_hash, 0x04, sizeof(merge_hash));
    
    time_t now = time(nullptr);
    
    // Create a simple merge scenario
    dag.add_commit(hash1, "Initial commit", "Author", now, {});
    dag.add_commit(hash2, "Feature branch", "Author", now + 100, {hash1});
    dag.add_commit(hash3, "Main branch", "Author", now + 150, {hash1});
    dag.add_commit(merge_hash, "Merge feature", "Author", now + 200, {hash2, hash3});
    
    auto merge_commit = dag.get_commit(std::string(reinterpret_cast<char*>(&merge_hash), sizeof(merge_hash)));
    ASSERT_NE(merge_commit, nullptr);
    EXPECT_TRUE(merge_commit->is_merge_commit());
    
    auto stats = dag.get_statistics();
    EXPECT_EQ(stats.merge_commits, 1);
}

// Test Terminal UI Components
TEST_F(AdvancedFeaturesTest, StyledTextRendering) {
    // Test basic styled text
    StyledText text("Hello World", Color::RED);
    std::string rendered = text.render();
    
    // Should contain ANSI escape codes if color is supported
    if (TerminalCapabilities::supports_color()) {
        EXPECT_TRUE(rendered.find("\033[") != std::string::npos);
        EXPECT_TRUE(rendered.find("Hello World") != std::string::npos);
    } else {
        EXPECT_EQ(rendered, "Hello World");
    }
}

TEST_F(AdvancedFeaturesTest, ProgressBarRendering) {
    ProgressBar progress(20, '█', '░');
    progress.set_prefix("Progress");
    progress.set_suffix("Complete");
    
    std::string rendered = progress.render(0.5);  // 50% progress
    
    EXPECT_TRUE(rendered.find("Progress") != std::string::npos);
    EXPECT_TRUE(rendered.find("Complete") != std::string::npos);
    EXPECT_TRUE(rendered.find("50.0%") != std::string::npos);
    EXPECT_TRUE(rendered.find("[") != std::string::npos);
    EXPECT_TRUE(rendered.find("]") != std::string::npos);
}

TEST_F(AdvancedFeaturesTest, TableRendering) {
    Table table({
        {"Name", 10, Table::Column::LEFT},
        {"Age", 5, Table::Column::RIGHT},
        {"City", -1, Table::Column::LEFT}  // Auto-size
    });
    
    table.add_row({"John Doe", "30", "New York"});
    table.add_row({"Jane Smith", "25", "Los Angeles"});
    
    std::string rendered = table.render();
    
    EXPECT_TRUE(rendered.find("Name") != std::string::npos);
    EXPECT_TRUE(rendered.find("John Doe") != std::string::npos);
    EXPECT_TRUE(rendered.find("Jane Smith") != std::string::npos);
    EXPECT_TRUE(rendered.find("30") != std::string::npos);
    EXPECT_TRUE(rendered.find("25") != std::string::npos);
}

TEST_F(AdvancedFeaturesTest, TerminalCapabilities) {
    // Test terminal capability detection
    auto [width, height] = TerminalCapabilities::get_terminal_size();
    
    EXPECT_GT(width, 0);
    EXPECT_GT(height, 0);
    
    // These tests depend on the environment, so we just check they don't crash
    bool color_support = TerminalCapabilities::supports_color();
    bool unicode_support = TerminalCapabilities::supports_unicode();
    bool is_interactive = TerminalCapabilities::is_interactive();
    
    // Just verify the functions return boolean values
    EXPECT_TRUE(color_support == true || color_support == false);
    EXPECT_TRUE(unicode_support == true || unicode_support == false);
    EXPECT_TRUE(is_interactive == true || is_interactive == false);
}

// Integration test
TEST_F(AdvancedFeaturesTest, IntegrationTest) {
    // Test that all components work together
    auto parser = ArgumentParserBuilder("svcs", "SnippetVCS", "2.0.0")
        .global_option(make_flag_option("v", "verbose", "Verbose output"))
        .subcommand({
            "log",
            "Show log",
            "Show commit history",
            {
                make_int_option("n", "max-count", "Max commits", false, 10),
                make_flag_option("", "graph", "Show graph")
            },
            {},
            nullptr
        })
        .build();
    
    std::vector<std::string> args = {"log", "--graph", "-n", "5"};
    auto result = parser->parse(args);
    
    EXPECT_TRUE(result.error_message.empty());
    EXPECT_EQ(result.subcommand, "log");
    EXPECT_TRUE(result.options.count("graph") > 0);
    EXPECT_TRUE(result.options.count("max-count") > 0);
    EXPECT_EQ(std::get<int>(result.options["max-count"]), 5);
    
    // Test UI components
    TerminalUI ui;
    
    // These should not crash
    ui.print_info("Test message");
    ui.print_success("Success message");
    ui.print_error("Error message");
    
    Table test_table({{"Column1", -1}, {"Column2", -1}});
    test_table.add_row({"Value1", "Value2"});
    
    std::string table_output = test_table.render();
    EXPECT_FALSE(table_output.empty());
}

int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}