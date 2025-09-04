# SnippetVCS Makefile
# Alternative build system for systems without CMake

CC = gcc
CXX = g++
CFLAGS = -std=c11 -Wall -Wextra -O2 -Iinclude -Isrc
CXXFLAGS = -std=c++17 -Wall -Wextra -O2 -Iinclude -Isrc
LDFLAGS = -lz -lcrypto -lssl

# Debug flags
ifdef DEBUG
    CFLAGS += -g -DDEBUG
    CXXFLAGS += -g -DDEBUG
endif

# Directories
SRCDIR = src
TESTDIR = tests
BUILDDIR = build
BINDIR = bin

# Core library sources (C)
CORE_SOURCES = $(wildcard $(SRCDIR)/core/*.c)
CORE_OBJECTS = $(CORE_SOURCES:$(SRCDIR)/%.c=$(BUILDDIR)/%.o)

# CLI sources (C++)
CLI_SOURCES = $(wildcard $(SRCDIR)/cli/*.cpp)
CLI_OBJECTS = $(CLI_SOURCES:$(SRCDIR)/%.cpp=$(BUILDDIR)/%.o)

# Test sources
TEST_SOURCES = $(wildcard $(TESTDIR)/*.c)
TEST_OBJECTS = $(TEST_SOURCES:$(TESTDIR)/%.c=$(BUILDDIR)/tests/%.o)

# Targets
TARGET = $(BINDIR)/svcs
TEST_TARGET = $(BINDIR)/test_svcs
CORE_LIB = $(BUILDDIR)/libsvcs_core.a

.PHONY: all clean test install help

all: $(TARGET)

# Create directories
$(BUILDDIR):
	mkdir -p $(BUILDDIR)/core $(BUILDDIR)/cli $(BUILDDIR)/tests

$(BINDIR):
	mkdir -p $(BINDIR)

# Core library
$(CORE_LIB): $(CORE_OBJECTS) | $(BUILDDIR)
	ar rcs $@ $^

# Core object files
$(BUILDDIR)/core/%.o: $(SRCDIR)/core/%.c | $(BUILDDIR)
	$(CC) $(CFLAGS) -c $< -o $@

# CLI object files
$(BUILDDIR)/cli/%.o: $(SRCDIR)/cli/%.cpp | $(BUILDDIR)
	$(CXX) $(CXXFLAGS) -c $< -o $@

# Main executable
$(TARGET): $(CLI_OBJECTS) $(CORE_LIB) | $(BINDIR)
	$(CXX) $(CLI_OBJECTS) $(CORE_LIB) $(LDFLAGS) -o $@

# Test object files
$(BUILDDIR)/tests/%.o: $(TESTDIR)/%.c | $(BUILDDIR)
	$(CC) $(CFLAGS) -c $< -o $@

# Test executable
$(TEST_TARGET): $(TEST_OBJECTS) $(CORE_LIB) | $(BINDIR)
	$(CC) $(TEST_OBJECTS) $(CORE_LIB) $(LDFLAGS) -o $@

# Run tests
test: $(TEST_TARGET)
	@echo "Running tests..."
	./$(TEST_TARGET)

# Install
install: $(TARGET)
	install -d $(DESTDIR)/usr/local/bin
	install -m 755 $(TARGET) $(DESTDIR)/usr/local/bin/
	install -d $(DESTDIR)/usr/local/include
	install -m 644 include/svcs.h $(DESTDIR)/usr/local/include/

# Clean
clean:
	rm -rf $(BUILDDIR) $(BINDIR)

# Help
help:
	@echo "SnippetVCS Build System"
	@echo ""
	@echo "Targets:"
	@echo "  all      - Build the main executable (default)"
	@echo "  test     - Build and run tests"
	@echo "  install  - Install to system directories"
	@echo "  clean    - Remove build artifacts"
	@echo "  help     - Show this help message"
	@echo ""
	@echo "Options:"
	@echo "  DEBUG=1  - Build with debug symbols and assertions"
	@echo ""
	@echo "Examples:"
	@echo "  make                 # Build release version"
	@echo "  make DEBUG=1         # Build debug version"
	@echo "  make test            # Run tests"
	@echo "  make install         # Install system-wide"

# Dependencies (simplified)
$(CORE_OBJECTS): include/svcs.h
$(CLI_OBJECTS): include/svcs.h $(SRCDIR)/cli/command_parser.hpp
$(TEST_OBJECTS): include/svcs.h

# Specific dependencies
$(BUILDDIR)/core/hash.o: $(SRCDIR)/core/hash.c include/svcs.h
$(BUILDDIR)/core/repository.o: $(SRCDIR)/core/repository.c include/svcs.h
$(BUILDDIR)/core/object.o: $(SRCDIR)/core/object.c include/svcs.h
$(BUILDDIR)/core/index.o: $(SRCDIR)/core/index.c include/svcs.h
$(BUILDDIR)/core/commit.o: $(SRCDIR)/core/commit.c include/svcs.h
$(BUILDDIR)/core/branch.o: $(SRCDIR)/core/branch.c include/svcs.h
$(BUILDDIR)/core/diff.o: $(SRCDIR)/core/diff.c include/svcs.h
$(BUILDDIR)/core/compress.o: $(SRCDIR)/core/compress.c include/svcs.h
$(BUILDDIR)/core/utils.o: $(SRCDIR)/core/utils.c include/svcs.h