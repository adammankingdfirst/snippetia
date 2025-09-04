#pragma once

#include <string>
#include <vector>
#include <map>
#include <memory>
#include <functional>

namespace svcs {

// AI-powered merge conflict resolution
class SmartMergeEngine {
public:
    enum class ConflictType {
        CONTENT,
        RENAME,
        DELETE_MODIFY,
        BINARY,
        WHITESPACE,
        SEMANTIC
    };
    
    struct ConflictContext {
        ConflictType type;
        std::string file_path;
        std::vector<std::string> base_lines;
        std::vector<std::string> our_lines;
        std::vector<std::string> their_lines;
        std::map<std::string, std::string> metadata;
        
        // Semantic analysis
        std::string language;
        std::vector<std::string> function_signatures;
        std::vector<std::string> variable_names;
        std::map<std::string, std::string> imports;
    };
    
    struct MergeResolution {
        bool auto_resolved = false;
        std::vector<std::string> resolved_lines;
        std::string resolution_strategy;
        double confidence_score = 0.0;
        std::string explanation;
    };
    
    // Smart merge with AI assistance
    static MergeResolution smart_merge(const ConflictContext& context);
    
    // Pattern-based resolution
    static MergeResolution resolve_by_patterns(const ConflictContext& context);
    
    // Semantic analysis resolution
    static MergeResolution resolve_by_semantics(const ConflictContext& context);
    
    // Machine learning based resolution
    static MergeResolution resolve_by_ml(const ConflictContext& context);
    
    // Learn from user resolutions
    static void learn_from_resolution(const ConflictContext& context, 
                                    const MergeResolution& resolution);
    
    // Get resolution suggestions
    static std::vector<MergeResolution> get_resolution_suggestions(
        const ConflictContext& context, int max_suggestions = 3);
    
private:
    static ConflictType analyze_conflict_type(const ConflictContext& context);
    static std::string detect_language(const std::string& file_path, 
                                     const std::vector<std::string>& lines);
    static std::vector<std::string> extract_function_signatures(
        const std::vector<std::string>& lines, const std::string& language);
    static bool is_whitespace_only_conflict(const ConflictContext& context);
    static bool is_import_conflict(const ConflictContext& context);
    static bool is_comment_conflict(const ConflictContext& context);
};

// Code analysis for intelligent merging
class CodeAnalyzer {
public:
    struct CodeStructure {
        std::vector<std::string> functions;
        std::vector<std::string> classes;
        std::vector<std::string> variables;
        std::vector<std::string> imports;
        std::map<std::string, std::vector<int>> function_line_ranges;
        std::map<std::string, std::vector<int>> class_line_ranges;
    };
    
    static CodeStructure analyze_code(const std::vector<std::string>& lines, 
                                    const std::string& language);
    
    static bool are_semantically_equivalent(const std::vector<std::string>& lines1,
                                          const std::vector<std::string>& lines2,
                                          const std::string& language);
    
    static std::vector<std::string> normalize_code(const std::vector<std::string>& lines,
                                                 const std::string& language);
    
    static double calculate_similarity(const std::vector<std::string>& lines1,
                                     const std::vector<std::string>& lines2);
    
private:
    static CodeStructure analyze_cpp_code(const std::vector<std::string>& lines);
    static CodeStructure analyze_python_code(const std::vector<std::string>& lines);
    static CodeStructure analyze_javascript_code(const std::vector<std::string>& lines);
};

// Merge strategy selector
class MergeStrategySelector {
public:
    enum class Strategy {
        AUTOMATIC,
        CONSERVATIVE,
        AGGRESSIVE,
        SEMANTIC_AWARE,
        PATTERN_BASED,
        ML_ASSISTED
    };
    
    static Strategy select_best_strategy(const SmartMergeEngine::ConflictContext& context);
    
    static std::vector<Strategy> get_applicable_strategies(
        const SmartMergeEngine::ConflictContext& context);
    
    static double estimate_success_probability(Strategy strategy, 
                                             const SmartMergeEngine::ConflictContext& context);
    
private:
    static bool is_strategy_applicable(Strategy strategy, 
                                     const SmartMergeEngine::ConflictContext& context);
};

// Merge quality assessment
class MergeQualityAssessor {
public:
    struct QualityMetrics {
        double correctness_score = 0.0;
        double completeness_score = 0.0;
        double consistency_score = 0.0;
        double maintainability_score = 0.0;
        std::vector<std::string> potential_issues;
        std::vector<std::string> recommendations;
    };
    
    static QualityMetrics assess_merge_quality(
        const std::vector<std::string>& merged_lines,
        const SmartMergeEngine::ConflictContext& context);
    
    static bool validate_syntax(const std::vector<std::string>& lines, 
                              const std::string& language);
    
    static std::vector<std::string> detect_potential_issues(
        const std::vector<std::string>& lines,
        const std::string& language);
    
    static double calculate_code_quality_score(const std::vector<std::string>& lines,
                                             const std::string& language);
    
private:
    static bool check_cpp_syntax(const std::vector<std::string>& lines);
    static bool check_python_syntax(const std::vector<std::string>& lines);
    static std::vector<std::string> detect_cpp_issues(const std::vector<std::string>& lines);
    static std::vector<std::string> detect_python_issues(const std::vector<std::string>& lines);
};

// Interactive merge assistant
class InteractiveMergeAssistant {
public:
    struct MergeSession {
        std::string session_id;
        std::vector<SmartMergeEngine::ConflictContext> pending_conflicts;
        std::vector<SmartMergeEngine::MergeResolution> resolutions;
        std::map<std::string, std::string> user_preferences;
        std::chrono::system_clock::time_point start_time;
    };
    
    static std::string start_merge_session(const std::vector<std::string>& conflicted_files);
    
    static SmartMergeEngine::ConflictContext get_next_conflict(const std::string& session_id);
    
    static void apply_resolution(const std::string& session_id,
                               const SmartMergeEngine::MergeResolution& resolution);
    
    static void set_user_preference(const std::string& session_id,
                                  const std::string& key,
                                  const std::string& value);
    
    static MergeSession get_session_status(const std::string& session_id);
    
    static bool complete_merge_session(const std::string& session_id);
    
    // UI helpers
    static std::string format_conflict_for_display(
        const SmartMergeEngine::ConflictContext& context);
    
    static std::string format_resolution_options(
        const std::vector<SmartMergeEngine::MergeResolution>& resolutions);
    
private:
    static std::map<std::string, MergeSession> active_sessions_;
    static std::string generate_session_id();
};

}