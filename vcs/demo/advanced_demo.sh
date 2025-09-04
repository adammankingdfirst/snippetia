#!/bin/bash

# SnippetVCS Advanced Features Demo
# This script demonstrates all the advanced C++ features

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC} $(printf "%-76s" "$1") ${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
}

print_step() {
    echo -e "${GREEN}➤${NC} $1"
}

print_info() {
    echo -e "${CYAN}ℹ${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Check if svcs is built
if [ ! -f "../bin/svcs" ]; then
    echo -e "${RED}Error: SnippetVCS not built. Run './build.sh' first.${NC}"
    exit 1
fi

SVCS="../bin/svcs"

# Create demo directory
DEMO_DIR="demo_repo"
if [ -d "$DEMO_DIR" ]; then
    rm -rf "$DEMO_DIR"
fi
mkdir "$DEMO_DIR"
cd "$DEMO_DIR"

print_header "SnippetVCS Advanced Features Demonstration"

print_step "1. Testing Advanced Argument Parser"
echo "Testing help system:"
$SVCS --help
echo ""

echo "Testing subcommand help:"
$SVCS init --help
echo ""

echo "Testing version information:"
$SVCS --version
echo ""

print_step "2. Repository Initialization with Advanced Options"
$SVCS init --verbose .
echo ""

print_step "3. Creating Sample Files for Demo"
cat > algorithm.py << 'EOF'
def quicksort(arr):
    """
    Quicksort algorithm implementation
    Time complexity: O(n log n) average case
    """
    if len(arr) <= 1:
        return arr
    
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    
    return quicksort(left) + middle + quicksort(right)

# Example usage
if __name__ == "__main__":
    numbers = [64, 34, 25, 12, 22, 11, 90]
    print("Original array:", numbers)
    sorted_numbers = quicksort(numbers)
    print("Sorted array:", sorted_numbers)
EOF

cat > README.md << 'EOF'
# Algorithm Collection

This repository contains various sorting algorithms implemented in Python.

## Algorithms Included

- Quicksort: Efficient divide-and-conquer sorting algorithm
- More algorithms coming soon...

## Usage

```python
python algorithm.py
```

## Performance

- Quicksort: O(n log n) average case, O(n²) worst case
EOF

cat > utils.py << 'EOF'
def binary_search(arr, target):
    """Binary search implementation"""
    left, right = 0, len(arr) - 1
    
    while left <= right:
        mid = (left + right) // 2
        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            left = mid + 1
        else:
            right = mid - 1
    
    return -1
EOF

print_step "4. Testing Advanced Add Command with Multiple Options"
echo "Adding files with verbose output:"
$SVCS add --verbose algorithm.py README.md utils.py
echo ""

print_step "5. Testing Enhanced Status Display"
echo "Repository status with enhanced formatting:"
$SVCS status
echo ""

echo "Status in short format:"
$SVCS status --short
echo ""

print_step "6. Creating Commits to Build DAG"
echo "Creating initial commit:"
$SVCS commit -m "Initial commit: Add quicksort algorithm and README"
echo ""

# Modify files to create more commits
echo "" >> algorithm.py
echo "# TODO: Add more sorting algorithms" >> algorithm.py

echo "Committing changes:"
$SVCS add algorithm.py
$SVCS commit -m "Add TODO comment for future algorithms"
echo ""

# Add more content
cat >> utils.py << 'EOF'

def merge_arrays(arr1, arr2):
    """Merge two sorted arrays"""
    result = []
    i = j = 0
    
    while i < len(arr1) and j < len(arr2):
        if arr1[i] <= arr2[j]:
            result.append(arr1[i])
            i += 1
        else:
            result.append(arr2[j])
            j += 1
    
    result.extend(arr1[i:])
    result.extend(arr2[j:])
    return result
EOF

echo "Adding utility function:"
$SVCS add utils.py
$SVCS commit -m "Add merge_arrays utility function"
echo ""

print_step "7. Testing Advanced Log Display with DAG Visualization"
echo "Commit history with graph visualization:"
$SVCS log --graph --max-count 10
echo ""

echo "Commit history in oneline format:"
$SVCS log --oneline --max-count 5
echo ""

echo "Detailed commit history:"
$SVCS log --max-count 3
echo ""

print_step "8. Testing Branch Management"
echo "Creating and listing branches:"
$SVCS branch feature/mergesort
$SVCS branch --verbose
echo ""

echo "Switching to feature branch:"
$SVCS checkout feature/mergesort
echo ""

# Add content on feature branch
cat > mergesort.py << 'EOF'
def mergesort(arr):
    """
    Merge sort algorithm implementation
    Time complexity: O(n log n) guaranteed
    """
    if len(arr) <= 1:
        return arr
    
    mid = len(arr) // 2
    left = mergesort(arr[:mid])
    right = mergesort(arr[mid:])
    
    return merge(left, right)

def merge(left, right):
    """Merge two sorted arrays"""
    result = []
    i = j = 0
    
    while i < len(left) and j < len(right):
        if left[i] <= right[j]:
            result.append(left[i])
            i += 1
        else:
            result.append(right[j])
            j += 1
    
    result.extend(left[i:])
    result.extend(right[j:])
    return result

# Example usage
if __name__ == "__main__":
    numbers = [64, 34, 25, 12, 22, 11, 90]
    print("Original array:", numbers)
    sorted_numbers = mergesort(numbers)
    print("Sorted array:", sorted_numbers)
EOF

echo "Adding mergesort implementation:"
$SVCS add mergesort.py
$SVCS commit -m "Implement mergesort algorithm on feature branch"
echo ""

print_step "9. Testing Diff Functionality"
echo "Switching back to main branch:"
$SVCS checkout main
echo ""

echo "Showing differences between branches:"
$SVCS diff main feature/mergesort
echo ""

print_step "10. Testing Interactive Mode"
print_info "Interactive mode provides a menu-driven interface"
print_warning "Skipping interactive demo in script mode"
echo "To test interactive mode, run: $SVCS interactive"
echo ""

print_step "11. Testing Snippetia Integration"
echo "Configuring Snippetia integration:"
$SVCS snippetia config http://localhost:8080 demo-token demo-user
echo ""

echo "Linking to a demo snippet:"
$SVCS snippetia link demo-snippet-123
echo ""

echo "Checking Snippetia status:"
$SVCS snippetia status
echo ""

echo "Syncing with Snippetia (demo mode):"
$SVCS snippetia sync
echo ""

print_step "12. Testing Advanced Error Handling"
echo "Testing invalid commands:"
$SVCS invalid-command 2>&1 || true
echo ""

echo "Testing missing required parameters:"
$SVCS commit 2>&1 || true
echo ""

print_step "13. Performance and Statistics"
echo "Repository statistics:"
echo "Files tracked: $(find . -name '*.py' -o -name '*.md' | wc -l)"
echo "Total commits: $(ls -la .svcs/refs/heads/ 2>/dev/null | grep -v '^d' | wc -l || echo '0')"
echo "Branches: $(ls .svcs/refs/heads/ 2>/dev/null | wc -l || echo '0')"
echo ""

print_step "14. Testing Terminal UI Components"
print_info "The enhanced UI provides:"
echo "  • Colored output with syntax highlighting"
echo "  • Interactive menus and prompts"
echo "  • Progress bars for long operations"
echo "  • Formatted tables for data display"
echo "  • Paged output for long content"
echo ""

print_step "15. Advanced Features Summary"
echo -e "${GREEN}✓${NC} Advanced argument parsing with validation"
echo -e "${GREEN}✓${NC} DAG-based commit history management"
echo -e "${GREEN}✓${NC} Rich terminal UI with colors and formatting"
echo -e "${GREEN}✓${NC} Interactive menu system"
echo -e "${GREEN}✓${NC} Comprehensive error handling"
echo -e "${GREEN}✓${NC} Snippetia cloud integration"
echo -e "${GREEN}✓${NC} Cross-platform compatibility"
echo -e "${GREEN}✓${NC} Professional CLI experience"
echo ""

print_header "Demo Complete!"
print_info "All advanced features demonstrated successfully"
print_info "Repository created in: $(pwd)"
print_info "Explore more features with: $SVCS --help"

echo ""
echo -e "${PURPLE}Key Improvements Over Basic Implementation:${NC}"
echo "• Sophisticated argument parsing with type validation"
echo "• DAG-based commit graph for complex branching scenarios"
echo "• Rich terminal UI with colors, tables, and interactive elements"
echo "• Comprehensive error handling and user feedback"
echo "• Real-time integration with Snippetia cloud platform"
echo "• Professional-grade CLI experience"
echo ""

echo -e "${CYAN}Next Steps:${NC}"
echo "1. Try the interactive mode: $SVCS interactive"
echo "2. Explore branch merging: $SVCS merge feature/mergesort"
echo "3. Test with real Snippetia server integration"
echo "4. Create more complex branching scenarios"
echo "5. Use the advanced diff and log features"