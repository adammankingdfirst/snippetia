# SnippetVCS - A Git-like Version Control System

A lightweight, high-performance version control system written in C/C++ designed for code snippets and small projects.

## Features

- **Repository Management**: Initialize, clone, and manage repositories
- **Commit System**: Track changes with SHA-1 hashing
- **Branching**: Create, switch, and merge branches
- **Staging Area**: Add/remove files from staging
- **Diff Engine**: Show differences between versions
- **Remote Support**: Push/pull from remote repositories
- **Compression**: Efficient storage with zlib compression
- **Cross-Platform**: Works on Linux, macOS, and Windows

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   CLI Interface │────│  Core Engine    │────│  Storage Layer  │
│   (C++ Frontend)│    │  (C Library)    │    │  (File System)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
    Command Parser          Object Database         Compressed Storage
    User Interface          Hash Management         Index Management
```

## Building

```bash
mkdir build && cd build
cmake ..
make -j$(nproc)
```

## Usage

```bash
# Initialize repository
./svcs init

# Add files
./svcs add file.txt

# Commit changes
./svcs commit -m "Initial commit"

# Create branch
./svcs branch feature-branch

# Switch branch
./svcs checkout feature-branch

# Show status
./svcs status

# Show log
./svcs log
```