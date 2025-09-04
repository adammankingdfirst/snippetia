cmake_minimum_required(VERSION 3.16)
project(SnippetVCS VERSION 2.0.0 LANGUAGES C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Compiler flags for optimization and debugging
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -DNDEBUG -march=native")
set(CMAKE_CXX_FLAGS_DEBUG "-g -O0 -DDEBUG -fsanitize=address")

# Find required packages
find_package(Threads REQUIRED)
find_package(PkgConfig REQUIRED)
pkg_check_modules(ZLIB REQUIRED zlib)

# Optional dependencies
find_package(OpenSSL QUIET)
find_package(GTest QUIET)

# Find additional libraries for enhanced features
pkg_check_modules(CURL REQUIRED libcurl)
pkg_check_modules(JSON_C REQUIRED json-c)

# Include directories
include_directories(${CMAKE_SOURCE_DIR}/include)
include_directories(${CMAKE_SOURCE_DIR}/src)
include_directories(${CMAKE_SOURCE_DIR}/src/core)
include_directories(${CMAKE_SOURCE_DIR}/src/cli)
include_directories(${CMAKE_SOURCE_DIR}/src/ui)
include_directories(${CMAKE_SOURCE_DIR}/src/integration)

# Compiler flags
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra -O2")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -O2")

# Debug flags
if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -g -DDEBUG")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -DDEBUG")
endif()

# Core library (C)
set(CORE_SOURCES
    src/core/repository.c
    src/core/object.c
    src/core/index.c
    src/core/commit.c
    src/core/branch.c
    src/core/diff.c
    src/core/hash.c
    src/core/compress.c
    src/core/utils.c
    src/core/remote.c
)

# Advanced C++ components
set(CORE_CXX_SOURCES
    src/core/dag.cpp
    src/core/merge_engine.cpp
    src/core/patch_engine.cpp
    src/core/performance_monitor.cpp
    src/core/smart_merge.cpp
    src/core/repository_analytics.cpp
    src/ui/terminal_ui.cpp
    src/integration/snippetia_sync.c
    src/integration/cloud_sync_engine.cpp
)

# Create core library
add_library(svcs_core STATIC ${CORE_SOURCES} ${CORE_CXX_SOURCES})
target_link_libraries(svcs_core 
    ${ZLIB_LIBRARIES} 
    ${CURL_LIBRARIES} 
    ${JSON_C_LIBRARIES}
    Threads::Threads
)
target_include_directories(svcs_core PUBLIC 
    ${ZLIB_INCLUDE_DIRS} 
    ${CURL_INCLUDE_DIRS} 
    ${JSON_C_INCLUDE_DIRS}
)

if(OPENSSL_FOUND)
    target_link_libraries(svcs_core OpenSSL::SSL OpenSSL::Crypto)
    target_compile_definitions(svcs_core PRIVATE HAVE_OPENSSL)
endif()

# CLI application (C++)
set(CLI_SOURCES
    src/cli/enhanced_main.cpp
    src/cli/advanced_parser.cpp
    src/cli/command_parser.cpp
    src/cli/commands/snippetia.cpp
)

add_executable(svcs ${CLI_SOURCES})
target_link_libraries(svcs svcs_core)

# Basic CLI (for compatibility)
add_executable(svcs_basic src/cli/main.cpp)
target_link_libraries(svcs_basic svcs_core)

# Performance monitoring tool
add_executable(svcs_perf_monitor tools/performance_monitor.cpp)
target_link_libraries(svcs_perf_monitor svcs_core)

# Analytics tool
add_executable(svcs_analytics tools/analytics_tool.cpp)
target_link_libraries(svcs_analytics svcs_core)

# Tests
enable_testing()

# Basic C tests
set(C_TEST_SOURCES
    tests/test_hash.c
    tests/test_object.c
    tests/test_repository.c
    tests/test_commit.c
)

add_executable(test_svcs_basic ${C_TEST_SOURCES})
target_link_libraries(test_svcs_basic svcs_core)
add_test(NAME BasicCoreTests COMMAND test_svcs_basic)

# Advanced C++ tests
if(GTest_FOUND)
    set(CPP_TEST_SOURCES
        tests/test_advanced_features.cpp
        tests/test_performance.cpp
        tests/test_integration.cpp
    )
    
    add_executable(test_svcs_advanced ${CPP_TEST_SOURCES})
    target_link_libraries(test_svcs_advanced 
        svcs_core
        GTest::gtest 
        GTest::gtest_main
        Threads::Threads
    )
    add_test(NAME AdvancedTests COMMAND test_svcs_advanced)
else()
    message(WARNING "Google Test not found. Advanced C++ tests will not be built.")
endif()

# Benchmark executables
add_executable(benchmark_performance benchmarks/benchmark_performance.cpp)
target_link_libraries(benchmark_performance svcs_core)

add_executable(benchmark_merge benchmarks/benchmark_merge.cpp)
target_link_libraries(benchmark_merge svcs_core)

# Installation
install(TARGETS svcs svcs_basic DESTINATION bin)
install(TARGETS svcs_core DESTINATION lib)
install(FILES include/svcs.h DESTINATION include)
install(DIRECTORY docs/ DESTINATION share/doc/svcs)

# Documentation
find_program(DOXYGEN_EXECUTABLE doxygen)
if(DOXYGEN_EXECUTABLE)
    configure_file(${CMAKE_CURRENT_SOURCE_DIR}/docs/Doxyfile.in 
                   ${CMAKE_CURRENT_BINARY_DIR}/Doxyfile @ONLY)
    add_custom_target(docs
        ${DOXYGEN_EXECUTABLE} ${CMAKE_CURRENT_BINARY_DIR}/Doxyfile
        WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}
        COMMENT "Generating API documentation with Doxygen" VERBATIM
    )
endif()

# Packaging
set(CPACK_PACKAGE_NAME "svcs")
set(CPACK_PACKAGE_VERSION ${PROJECT_VERSION})
set(CPACK_PACKAGE_DESCRIPTION_SUMMARY "Simple Version Control System with Advanced Features")
set(CPACK_PACKAGE_VENDOR "SVCS Development Team")
set(CPACK_GENERATOR "TGZ;ZIP")
include(CPack)

# Custom targets
add_custom_target(format
    COMMAND find ${CMAKE_SOURCE_DIR}/src ${CMAKE_SOURCE_DIR}/include ${CMAKE_SOURCE_DIR}/tests 
            -name "*.cpp" -o -name "*.hpp" -o -name "*.c" -o -name "*.h" 
            | xargs clang-format -i
    COMMENT "Formatting source code"
)

add_custom_target(lint
    COMMAND find ${CMAKE_SOURCE_DIR}/src ${CMAKE_SOURCE_DIR}/include 
            -name "*.cpp" -o -name "*.hpp" 
            | xargs cppcheck --enable=all --std=c++20
    COMMENT "Running static analysis"
)

# Performance profiling target
add_custom_target(profile
    COMMAND valgrind --tool=callgrind --callgrind-out-file=callgrind.out 
            ${CMAKE_BINARY_DIR}/svcs --help
    DEPENDS svcs
    COMMENT "Running performance profiling"
)