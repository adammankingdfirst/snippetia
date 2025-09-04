package com.snippetia.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snippetia.presentation.component.CodeEditor
import com.snippetia.presentation.component.rememberCodeEditorState
import com.snippetia.presentation.util.formatTimeAgo
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryScreen(
    repositoryId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Files", "Commits", "Branches", "Settings")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Column {
                    Text("My Kotlin Utils")
                    Text(
                        text = "kotlin-utilities",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Clone repository */ }) {
                    Icon(Icons.Default.Download, contentDescription = "Clone")
                }
                IconButton(onClick = { /* Star repository */ }) {
                    Icon(Icons.Default.Star, contentDescription = "Star")
                }
                IconButton(onClick = { /* More options */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }
        )

        // Repository Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "A collection of useful Kotlin utility functions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RepositoryStatItem(
                                icon = Icons.Default.Star,
                                count = "42",
                                label = "Stars"
                            )
                            RepositoryStatItem(
                                icon = Icons.Default.CallSplit,
                                count = "12",
                                label = "Forks"
                            )
                            RepositoryStatItem(
                                icon = Icons.Default.Commit,
                                count = "156",
                                label = "Commits"
                            )
                        }
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Public") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Public,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Updated 2 hours ago",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Content
        when (selectedTab) {
            0 -> FilesTab()
            1 -> CommitsTab()
            2 -> BranchesTab()
            3 -> SettingsTab()
        }
    }
}

@Composable
private fun RepositoryStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilesTab(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Current branch info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Branch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "main",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "156 commits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Files and folders
        items(mockRepositoryFiles) { file ->
            FileItem(
                file = file,
                onClick = { /* Navigate to file */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileItem(
    file: RepositoryFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (file.isDirectory) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (file.isDirectory) FontWeight.Medium else FontWeight.Normal
                )
                if (file.lastCommitMessage.isNotEmpty()) {
                    Text(
                        text = file.lastCommitMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (file.size.isNotEmpty()) {
                    Text(
                        text = file.size,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatTimeAgo(file.lastModified),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CommitsTab(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mockCommits) { commit ->
            CommitItem(commit = commit)
        }
    }
}

@Composable
private fun CommitItem(
    commit: CommitInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = commit.authorName.first().toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = commit.message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "by ${commit.authorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = commit.hash.take(7),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatTimeAgo(commit.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (commit.filesChanged > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CommitStatChip(
                        icon = Icons.Default.Add,
                        count = commit.additions,
                        color = Color(0xFF4CAF50)
                    )
                    CommitStatChip(
                        icon = Icons.Default.Remove,
                        count = commit.deletions,
                        color = Color(0xFFF44336)
                    )
                    CommitStatChip(
                        icon = Icons.Default.Edit,
                        count = commit.filesChanged,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CommitStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BranchesTab(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mockBranches) { branch ->
            BranchItem(
                branch = branch,
                onCheckout = { /* Handle checkout */ },
                onDelete = { /* Handle delete */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BranchItem(
    branch: BranchInfo,
    onCheckout: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Branch,
                contentDescription = null,
                tint = if (branch.isCurrent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = branch.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (branch.isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (branch.isCurrent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (branch.isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Current") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
                Text(
                    text = "Last commit: ${formatTimeAgo(branch.lastCommit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!branch.isCurrent) {
                Row {
                    IconButton(onClick = onCheckout) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Checkout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RepositorySettingsCard()
        }
        
        item {
            CollaboratorsCard()
        }
        
        item {
            DangerZoneCard()
        }
    }
}

@Composable
private fun RepositorySettingsCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Repository Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingItem(
                title = "Repository Name",
                description = "kotlin-utilities",
                icon = Icons.Default.Edit,
                onClick = { /* Edit name */ }
            )
            
            SettingItem(
                title = "Description",
                description = "A collection of useful Kotlin utility functions",
                icon = Icons.Default.Description,
                onClick = { /* Edit description */ }
            )
            
            SettingItem(
                title = "Visibility",
                description = "Public",
                icon = Icons.Default.Visibility,
                onClick = { /* Change visibility */ }
            )
        }
    }
}

@Composable
private fun CollaboratorsCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collaborators",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { /* Add collaborator */ }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add collaborator")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            mockCollaborators.forEach { collaborator ->
                CollaboratorItem(collaborator = collaborator)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CollaboratorItem(
    collaborator: Collaborator,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = collaborator.name.first().toString(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = collaborator.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = collaborator.role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = { /* Remove collaborator */ }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DangerZoneCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { /* Archive repository */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Archive, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Archive Repository")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = { /* Delete repository */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Repository")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Data classes
data class RepositoryFile(
    val name: String,
    val isDirectory: Boolean,
    val size: String,
    val lastModified: LocalDateTime,
    val lastCommitMessage: String
)

data class CommitInfo(
    val hash: String,
    val message: String,
    val authorName: String,
    val timestamp: LocalDateTime,
    val additions: Int,
    val deletions: Int,
    val filesChanged: Int
)

data class BranchInfo(
    val name: String,
    val isCurrent: Boolean,
    val lastCommit: LocalDateTime
)

data class Collaborator(
    val name: String,
    val role: String
)

// Mock data
private val mockRepositoryFiles = listOf(
    RepositoryFile(
        name = "src",
        isDirectory = true,
        size = "",
        lastModified = LocalDateTime.now().minusHours(2),
        lastCommitMessage = "Add new utility functions"
    ),
    RepositoryFile(
        name = "README.md",
        isDirectory = false,
        size = "2.1 KB",
        lastModified = LocalDateTime.now().minusDays(1),
        lastCommitMessage = "Update documentation"
    ),
    RepositoryFile(
        name = "build.gradle.kts",
        isDirectory = false,
        size = "1.5 KB",
        lastModified = LocalDateTime.now().minusDays(3),
        lastCommitMessage = "Update dependencies"
    ),
    RepositoryFile(
        name = ".gitignore",
        isDirectory = false,
        size = "456 B",
        lastModified = LocalDateTime.now().minusWeeks(1),
        lastCommitMessage = "Initial commit"
    )
)

private val mockCommits = listOf(
    CommitInfo(
        hash = "a1b2c3d4e5f6",
        message = "Add new utility functions for string manipulation",
        authorName = "John Doe",
        timestamp = LocalDateTime.now().minusHours(2),
        additions = 45,
        deletions = 12,
        filesChanged = 3
    ),
    CommitInfo(
        hash = "f6e5d4c3b2a1",
        message = "Update documentation with examples",
        authorName = "Jane Smith",
        timestamp = LocalDateTime.now().minusDays(1),
        additions = 23,
        deletions = 5,
        filesChanged = 2
    ),
    CommitInfo(
        hash = "1a2b3c4d5e6f",
        message = "Fix bug in date formatting function",
        authorName = "John Doe",
        timestamp = LocalDateTime.now().minusDays(2),
        additions = 8,
        deletions = 15,
        filesChanged = 1
    )
)

private val mockBranches = listOf(
    BranchInfo(
        name = "main",
        isCurrent = true,
        lastCommit = LocalDateTime.now().minusHours(2)
    ),
    BranchInfo(
        name = "feature/new-utils",
        isCurrent = false,
        lastCommit = LocalDateTime.now().minusDays(1)
    ),
    BranchInfo(
        name = "bugfix/date-formatting",
        isCurrent = false,
        lastCommit = LocalDateTime.now().minusDays(3)
    )
)

private val mockCollaborators = listOf(
    Collaborator("John Doe", "Owner"),
    Collaborator("Jane Smith", "Maintainer"),
    Collaborator("Bob Johnson", "Contributor")
)