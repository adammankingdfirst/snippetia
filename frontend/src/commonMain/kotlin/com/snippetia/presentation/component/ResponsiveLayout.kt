package com.snippetia.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Cross-platform responsive layout system with adaptive UI components
 */

// Screen size breakpoints
enum class ScreenSize {
    COMPACT,    // < 600dp (phones)
    MEDIUM,     // 600-840dp (tablets, foldables)
    EXPANDED    // > 840dp (desktops, large tablets)
}

// Platform detection
enum class Platform {
    ANDROID, IOS, DESKTOP, WEB
}

@Composable
fun rememberScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth < 600.dp -> ScreenSize.COMPACT
        screenWidth < 840.dp -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }
}

@Composable
fun rememberPlatform(): Platform {
    return Platform.ANDROID // This would be determined at compile time in real implementation
}

/**
 * Adaptive layout that changes based on screen size and platform
 */
@Composable
fun AdaptiveLayout(
    modifier: Modifier = Modifier,
    compactContent: @Composable () -> Unit,
    mediumContent: @Composable () -> Unit = compactContent,
    expandedContent: @Composable () -> Unit = mediumContent
) {
    val screenSize = rememberScreenSize()
    
    AnimatedContent(
        targetState = screenSize,
        transitionSpec = {
            slideInHorizontally { width -> width } + fadeIn() with
            slideOutHorizontally { width -> -width } + fadeOut()
        },
        modifier = modifier,
        label = "adaptive_layout"
    ) { size ->
        when (size) {
            ScreenSize.COMPACT -> compactContent()
            ScreenSize.MEDIUM -> mediumContent()
            ScreenSize.EXPANDED -> expandedContent()
        }
    }
}

/**
 * Responsive grid that adapts column count based on screen size
 */
@Composable
fun <T> ResponsiveGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    compactColumns: Int = 1,
    mediumColumns: Int = 2,
    expandedColumns: Int = 3,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    itemContent: @Composable (T) -> Unit
) {
    val screenSize = rememberScreenSize()
    val columns = when (screenSize) {
        ScreenSize.COMPACT -> compactColumns
        ScreenSize.MEDIUM -> mediumColumns
        ScreenSize.EXPANDED -> expandedColumns
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement
    ) {
        items(items) { item ->
            itemContent(item)
        }
    }
}

/**
 * Adaptive navigation that switches between bottom bar, rail, and drawer
 */
@Composable
fun AdaptiveNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    items: List<NavigationItem>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val screenSize = rememberScreenSize()
    val platform = rememberPlatform()
    
    when (screenSize) {
        ScreenSize.COMPACT -> {
            // Bottom navigation for compact screens
            Scaffold(
                modifier = modifier,
                bottomBar = {
                    NavigationBar {
                        items.forEach { item ->
                            NavigationBarItem(
                                selected = selectedItem == item.id,
                                onClick = { onItemSelected(item.id) },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    content()
                }
            }
        }
        ScreenSize.MEDIUM -> {
            // Navigation rail for medium screens
            Row(modifier = modifier) {
                NavigationRail {
                    items.forEach { item ->
                        NavigationRailItem(
                            selected = selectedItem == item.id,
                            onClick = { onItemSelected(item.id) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
        }
        ScreenSize.EXPANDED -> {
            // Navigation drawer for expanded screens
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(modifier = Modifier.width(240.dp)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items.forEach { item ->
                                NavigationDrawerItem(
                                    selected = selectedItem == item.id,
                                    onClick = { onItemSelected(item.id) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) }
                                )
                            }
                        }
                    }
                },
                modifier = modifier
            ) {
                content()
            }
        }
    }
}

/**
 * Responsive card layout that adapts to screen size
 */
@Composable
fun ResponsiveCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val screenSize = rememberScreenSize()
    val cardModifier = when (screenSize) {
        ScreenSize.COMPACT -> modifier.fillMaxWidth()
        ScreenSize.MEDIUM -> modifier.fillMaxWidth(0.8f)
        ScreenSize.EXPANDED -> modifier.fillMaxWidth(0.6f)
    }
    
    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(
                when (screenSize) {
                    ScreenSize.COMPACT -> 16.dp
                    ScreenSize.MEDIUM -> 20.dp
                    ScreenSize.EXPANDED -> 24.dp
                }
            ),
            content = content
        )
    }
}

/**
 * Adaptive text that scales based on screen size
 */
@Composable
fun AdaptiveText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    val screenSize = rememberScreenSize()
    val adaptedStyle = when (screenSize) {
        ScreenSize.COMPACT -> style
        ScreenSize.MEDIUM -> style.copy(fontSize = style.fontSize * 1.1f)
        ScreenSize.EXPANDED -> style.copy(fontSize = style.fontSize * 1.2f)
    }
    
    Text(
        text = text,
        modifier = modifier,
        style = adaptedStyle
    )
}

/**
 * Platform-specific button styling
 */
@Composable
fun PlatformButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val platform = rememberPlatform()
    val screenSize = rememberScreenSize()
    
    val buttonHeight = when (screenSize) {
        ScreenSize.COMPACT -> 48.dp
        ScreenSize.MEDIUM -> 52.dp
        ScreenSize.EXPANDED -> 56.dp
    }
    
    when (platform) {
        Platform.IOS -> {
            // iOS-style button
            Button(
                onClick = onClick,
                modifier = modifier.height(buttonHeight),
                enabled = enabled,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                content = content
            )
        }
        Platform.ANDROID -> {
            // Material Design button
            Button(
                onClick = onClick,
                modifier = modifier.height(buttonHeight),
                enabled = enabled,
                content = content
            )
        }
        Platform.DESKTOP -> {
            // Desktop-optimized button
            Button(
                onClick = onClick,
                modifier = modifier.height(buttonHeight),
                enabled = enabled,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                content = content
            )
        }
        Platform.WEB -> {
            // Web-optimized button
            Button(
                onClick = onClick,
                modifier = modifier.height(buttonHeight),
                enabled = enabled,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                content = content
            )
        }
    }
}

/**
 * Responsive spacing that adapts to screen size
 */
@Composable
fun adaptiveSpacing(): Dp {
    val screenSize = rememberScreenSize()
    return when (screenSize) {
        ScreenSize.COMPACT -> 8.dp
        ScreenSize.MEDIUM -> 12.dp
        ScreenSize.EXPANDED -> 16.dp
    }
}

/**
 * Adaptive padding that scales with screen size
 */
@Composable
fun adaptivePadding(): PaddingValues {
    val screenSize = rememberScreenSize()
    val padding = when (screenSize) {
        ScreenSize.COMPACT -> 16.dp
        ScreenSize.MEDIUM -> 20.dp
        ScreenSize.EXPANDED -> 24.dp
    }
    return PaddingValues(padding)
}

/**
 * Responsive list layout that switches between single and multi-column
 */
@Composable
fun <T> ResponsiveList(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    val screenSize = rememberScreenSize()
    
    when (screenSize) {
        ScreenSize.COMPACT -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = adaptivePadding(),
                verticalArrangement = Arrangement.spacedBy(adaptiveSpacing())
            ) {
                items(items) { item ->
                    itemContent(item)
                }
            }
        }
        ScreenSize.MEDIUM, ScreenSize.EXPANDED -> {
            ResponsiveGrid(
                items = items,
                modifier = modifier,
                compactColumns = 1,
                mediumColumns = 2,
                expandedColumns = if (screenSize == ScreenSize.EXPANDED) 3 else 2,
                itemContent = itemContent
            )
        }
    }
}

/**
 * Adaptive dialog that adjusts size based on screen
 */
@Composable
fun AdaptiveDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val screenSize = rememberScreenSize()
    
    when (screenSize) {
        ScreenSize.COMPACT -> {
            // Full-screen dialog on compact screens
            androidx.compose.ui.window.Dialog(
                onDismissRequest = onDismissRequest
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    content()
                }
            }
        }
        ScreenSize.MEDIUM, ScreenSize.EXPANDED -> {
            // Regular dialog on larger screens
            androidx.compose.ui.window.Dialog(
                onDismissRequest = onDismissRequest
            ) {
                Surface(
                    modifier = modifier
                        .fillMaxWidth(
                            when (screenSize) {
                                ScreenSize.MEDIUM -> 0.8f
                                ScreenSize.EXPANDED -> 0.6f
                                else -> 1f
                            }
                        )
                        .wrapContentHeight(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * Responsive bottom sheet that adapts to screen size
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val screenSize = rememberScreenSize()
    
    when (screenSize) {
        ScreenSize.COMPACT -> {
            ModalBottomSheet(
                onDismissRequest = onDismissRequest,
                modifier = modifier
            ) {
                content()
            }
        }
        ScreenSize.MEDIUM, ScreenSize.EXPANDED -> {
            // Use dialog instead of bottom sheet on larger screens
            AdaptiveDialog(
                onDismissRequest = onDismissRequest,
                modifier = modifier
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    content = content
                )
            }
        }
    }
}

/**
 * Responsive image that adapts aspect ratio and size
 */
@Composable
fun ResponsiveImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val screenSize = rememberScreenSize()
    val imageModifier = when (screenSize) {
        ScreenSize.COMPACT -> modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
        ScreenSize.MEDIUM -> modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
        ScreenSize.EXPANDED -> modifier
            .fillMaxWidth()
            .aspectRatio(21f / 9f)
    }
    
    // AsyncImage would be used here in a real implementation
    Box(
        modifier = imageModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Image: $imageUrl",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Adaptive toolbar that changes layout based on screen size
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTopAppBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    val screenSize = rememberScreenSize()
    
    when (screenSize) {
        ScreenSize.COMPACT -> {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = navigationIcon ?: {},
                actions = actions,
                modifier = modifier
            )
        }
        ScreenSize.MEDIUM, ScreenSize.EXPANDED -> {
            LargeTopAppBar(
                title = { Text(title) },
                navigationIcon = navigationIcon ?: {},
                actions = actions,
                modifier = modifier
            )
        }
    }
}

/**
 * Responsive floating action button positioning
 */
@Composable
fun AdaptiveFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val screenSize = rememberScreenSize()
    val fabSize = when (screenSize) {
        ScreenSize.COMPACT -> FabPosition.End
        ScreenSize.MEDIUM -> FabPosition.End
        ScreenSize.EXPANDED -> FabPosition.End
    }
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        content = content
    )
}

/**
 * Platform-specific haptic feedback
 */
@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val platform = rememberPlatform()
    return remember {
        when (platform) {
            Platform.ANDROID -> AndroidHapticFeedback()
            Platform.IOS -> IOSHapticFeedback()
            Platform.DESKTOP -> NoHapticFeedback()
            Platform.WEB -> NoHapticFeedback()
        }
    }
}

// Data classes and interfaces
data class NavigationItem(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

interface HapticFeedback {
    fun performClick()
    fun performLongPress()
    fun performTextHandleMove()
}

class AndroidHapticFeedback : HapticFeedback {
    override fun performClick() {
        // Android-specific haptic feedback
    }
    
    override fun performLongPress() {
        // Android-specific haptic feedback
    }
    
    override fun performTextHandleMove() {
        // Android-specific haptic feedback
    }
}

class IOSHapticFeedback : HapticFeedback {
    override fun performClick() {
        // iOS-specific haptic feedback
    }
    
    override fun performLongPress() {
        // iOS-specific haptic feedback
    }
    
    override fun performTextHandleMove() {
        // iOS-specific haptic feedback
    }
}

class NoHapticFeedback : HapticFeedback {
    override fun performClick() {}
    override fun performLongPress() {}
    override fun performTextHandleMove() {}
}

/**
 * Responsive window size class utilities
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidth = with(density) { configuration.screenWidthDp.dp }
    val screenHeight = with(density) { configuration.screenHeightDp.dp }
    
    return WindowSizeClass(
        widthSizeClass = when {
            screenWidth < 600.dp -> WindowWidthSizeClass.Compact
            screenWidth < 840.dp -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        },
        heightSizeClass = when {
            screenHeight < 480.dp -> WindowHeightSizeClass.Compact
            screenHeight < 900.dp -> WindowHeightSizeClass.Medium
            else -> WindowHeightSizeClass.Expanded
        }
    )
}

data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass
)

enum class WindowWidthSizeClass {
    Compact, Medium, Expanded
}

enum class WindowHeightSizeClass {
    Compact, Medium, Expanded
}

/**
 * Adaptive content layout based on window size class
 */
@Composable
fun AdaptiveContentLayout(
    windowSizeClass: WindowSizeClass,
    primaryContent: @Composable () -> Unit,
    secondaryContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Single pane layout
            Column(modifier = modifier) {
                primaryContent()
                secondaryContent?.invoke()
            }
        }
        WindowWidthSizeClass.Medium -> {
            // Two pane layout (side by side)
            Row(modifier = modifier) {
                Box(modifier = Modifier.weight(1f)) {
                    primaryContent()
                }
                secondaryContent?.let { content ->
                    Box(modifier = Modifier.weight(1f)) {
                        content()
                    }
                }
            }
        }
        WindowWidthSizeClass.Expanded -> {
            // Three pane layout or expanded two pane
            Row(modifier = modifier) {
                Box(modifier = Modifier.weight(2f)) {
                    primaryContent()
                }
                secondaryContent?.let { content ->
                    Box(modifier = Modifier.weight(1f)) {
                        content()
                    }
                }
            }
        }
    }
}