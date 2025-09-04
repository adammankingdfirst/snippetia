#!/bin/bash

# SnippetVCS Build Script
# Builds the complete VCS system with Snippetia integration

set -e

echo "🔨 Building SnippetVCS with Snippetia Integration..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check dependencies
check_dependencies() {
    print_status "Checking dependencies..."
    
    local missing_deps=()
    
    # Check for required tools
    if ! command -v gcc &> /dev/null; then
        missing_deps+=("gcc")
    fi
    
    if ! command -v g++ &> /dev/null; then
        missing_deps+=("g++")
    fi
    
    # Check for required libraries
    if ! pkg-config --exists zlib; then
        missing_deps+=("zlib-dev")
    fi
    
    if ! pkg-config --exists openssl; then
        missing_deps+=("openssl-dev")
    fi
    
    if ! pkg-config --exists libcurl; then
        missing_deps+=("libcurl-dev")
    fi
    
    if ! pkg-config --exists json-c; then
        missing_deps+=("libjson-c-dev")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        print_error "Missing dependencies: ${missing_deps[*]}"
        print_status "Install them with:"
        echo "  Ubuntu/Debian: sudo apt-get install build-essential libssl-dev zlib1g-dev libcurl4-openssl-dev libjson-c-dev"
        echo "  CentOS/RHEL:   sudo yum install gcc gcc-c++ openssl-devel zlib-devel libcurl-devel json-c-devel"
        echo "  macOS:         brew install openssl zlib curl json-c"
        exit 1
    fi
    
    print_success "All dependencies found"
}

# Create build directories
setup_build_dirs() {
    print_status "Setting up build directories..."
    
    mkdir -p build/{core,cli,integration,tests}
    mkdir -p bin
    
    print_success "Build directories created"
}

# Build core library
build_core() {
    print_status "Building core library..."
    
    local core_sources=(
        "src/core/repository.c"
        "src/core/object.c"
        "src/core/index.c"
        "src/core/commit.c"
        "src/core/branch.c"
        "src/core/diff.c"
        "src/core/hash.c"
        "src/core/compress.c"
        "src/core/utils.c"
        "src/core/remote.c"
    )
    
    local core_cxx_sources=(
        "src/core/dag.cpp"
        "src/ui/terminal_ui.cpp"
    )
    
    local cflags="-std=c11 -Wall -Wextra -O2 -Iinclude -Isrc"
    cflags+=" $(pkg-config --cflags zlib openssl libcurl)"
    
    for source in "${core_sources[@]}"; do
        local obj_file="build/core/$(basename "$source" .c).o"
        print_status "Compiling $source..."
        gcc $cflags -c "$source" -o "$obj_file"
    done
    
    # Compile C++ core sources
    local cxxflags="-std=c++17 -Wall -Wextra -O2 -Iinclude -Isrc"
    cxxflags+=" $(pkg-config --cflags libcurl json-c)"
    
    for source in "${core_cxx_sources[@]}"; do
        local obj_file="build/core/$(basename "$source" .cpp).o"
        print_status "Compiling $source..."
        g++ $cxxflags -c "$source" -o "$obj_file"
    done
    
    # Create static library
    print_status "Creating core library..."
    ar rcs build/libsvcs_core.a build/core/*.o
    
    print_success "Core library built"
}

# Build integration layer
build_integration() {
    print_status "Building Snippetia integration..."
    
    local integration_sources=(
        "src/integration/snippetia_sync.c"
    )
    
    local cflags="-std=c11 -Wall -Wextra -O2 -Iinclude -Isrc"
    cflags+=" $(pkg-config --cflags zlib openssl libcurl json-c)"
    
    for source in "${integration_sources[@]}"; do
        local obj_file="build/integration/$(basename "$source" .c).o"
        print_status "Compiling $source..."
        gcc $cflags -c "$source" -o "$obj_file"
    done
    
    print_success "Integration layer built"
}

# Build CLI
build_cli() {
    print_status "Building CLI interface..."
    
    local cli_sources=(
        "src/cli/enhanced_main.cpp"
        "src/cli/advanced_parser.cpp"
        "src/cli/command_parser.cpp"
        "src/cli/commands/snippetia.cpp"
    )
    
    local cxxflags="-std=c++17 -Wall -Wextra -O2 -Iinclude -Isrc"
    
    for source in "${cli_sources[@]}"; do
        local obj_file="build/cli/$(basename "$source" .cpp).o"
        print_status "Compiling $source..."
        g++ $cxxflags -c "$source" -o "$obj_file"
    done
    
    print_success "CLI interface built"
}

# Link final executable
link_executable() {
    print_status "Linking executable..."
    
    local ldflags="$(pkg-config --libs zlib openssl libcurl json-c)"
    
    g++ build/cli/*.o build/integration/*.o build/libsvcs_core.a $ldflags -o bin/svcs
    
    print_success "Executable created: bin/svcs"
}

# Build tests
build_tests() {
    print_status "Building tests..."
    
    local test_sources=(
        "tests/test_hash.c"
        "tests/test_object.c"
        "tests/test_repository.c"
        "tests/test_commit.c"
    )
    
    local cflags="-std=c11 -Wall -Wextra -O2 -Iinclude -Isrc"
    cflags+=" $(pkg-config --cflags zlib openssl)"
    
    for source in "${test_sources[@]}"; do
        if [ -f "$source" ]; then
            local obj_file="build/tests/$(basename "$source" .c).o"
            print_status "Compiling test $source..."
            gcc $cflags -c "$source" -o "$obj_file"
        fi
    done
    
    # Link test executable
    if ls build/tests/*.o 1> /dev/null 2>&1; then
        local ldflags="$(pkg-config --libs zlib openssl)"
        gcc build/tests/*.o build/libsvcs_core.a $ldflags -o bin/test_svcs
        print_success "Test executable created: bin/test_svcs"
    else
        print_warning "No test files found, skipping test build"
    fi
}

# Run tests
run_tests() {
    if [ -f "bin/test_svcs" ]; then
        print_status "Running tests..."
        ./bin/test_svcs
        print_success "All tests passed!"
    else
        print_warning "Test executable not found, skipping tests"
    fi
}

# Install
install_system() {
    print_status "Installing SnippetVCS..."
    
    local install_dir="${1:-/usr/local}"
    
    if [ ! -w "$install_dir/bin" ]; then
        print_error "No write permission to $install_dir/bin"
        print_status "Try: sudo $0 install"
        exit 1
    fi
    
    cp bin/svcs "$install_dir/bin/"
    cp include/svcs.h "$install_dir/include/" 2>/dev/null || true
    
    print_success "SnippetVCS installed to $install_dir/bin/svcs"
}

# Show usage
show_usage() {
    echo "SnippetVCS Build Script"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  build     - Build the complete system (default)"
    echo "  test      - Build and run tests"
    echo "  install   - Install to system directories"
    echo "  clean     - Clean build artifacts"
    echo "  help      - Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  DEBUG=1   - Build with debug symbols"
    echo "  PREFIX    - Installation prefix (default: /usr/local)"
    echo ""
    echo "Examples:"
    echo "  $0                    # Build release version"
    echo "  DEBUG=1 $0            # Build debug version"
    echo "  $0 test               # Build and run tests"
    echo "  sudo $0 install       # Install system-wide"
}

# Clean build artifacts
clean_build() {
    print_status "Cleaning build artifacts..."
    
    rm -rf build bin
    
    print_success "Build artifacts cleaned"
}

# Main build process
main_build() {
    check_dependencies
    setup_build_dirs
    build_core
    build_integration
    build_cli
    link_executable
    build_tests
    
    print_success "SnippetVCS build completed successfully!"
    print_status "Executable: $(pwd)/bin/svcs"
    print_status "Usage: ./bin/svcs --help"
}

# Handle command line arguments
case "${1:-build}" in
    "build")
        main_build
        ;;
    "test")
        main_build
        run_tests
        ;;
    "install")
        if [ ! -f "bin/svcs" ]; then
            print_status "Executable not found, building first..."
            main_build
        fi
        install_system "${PREFIX:-/usr/local}"
        ;;
    "clean")
        clean_build
        ;;
    "help"|"--help"|"-h")
        show_usage
        ;;
    *)
        print_error "Unknown command: $1"
        show_usage
        exit 1
        ;;
esac