package com.snippetia.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFD0BCFF)
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF6750A4)
)

@Composable
fun SnippetiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

// Syntax highlighting color scheme
object SyntaxColors {
    val keyword = Color(0xFF9C27B0)      // Purple
    val string = Color(0xFF4CAF50)       // Green
    val comment = Color(0xFF757575)      // Gray
    val number = Color(0xFF2196F3)       // Blue
    val function = Color(0xFFFF9800)     // Orange
    val type = Color(0xFF00BCD4)         // Cyan
    val variable = Color(0xFFE91E63)     // Pink
    val operator = Color(0xFF795548)     // Brown
}

// Language indicator colors (for badges, not syntax highlighting)
fun getLanguageIndicatorColor(language: String): Color {
    return when (language.lowercase()) {
        "javascript", "js" -> Color(0xFFF7DF1E)
        "typescript", "ts" -> Color(0xFF3178C6)
        "python", "py" -> Color(0xFF3776AB)
        "java" -> Color(0xFFED8B00)
        "kotlin", "kt" -> Color(0xFF7F52FF)
        "swift" -> Color(0xFFFA7343)
        "go" -> Color(0xFF00ADD8)
        "rust", "rs" -> Color(0xFFCE422B)
        "cpp", "c++" -> Color(0xFF00599C)
        "c" -> Color(0xFFA8B9CC)
        "csharp", "c#" -> Color(0xFF239120)
        "php" -> Color(0xFF777BB4)
        "ruby", "rb" -> Color(0xFFCC342D)
        "scala" -> Color(0xFFDC322F)
        "dart" -> Color(0xFF0175C2)
        "html" -> Color(0xFFE34F26)
        "css" -> Color(0xFF1572B6)
        "scss", "sass" -> Color(0xFFCF649A)
        "shell", "bash", "sh" -> Color(0xFF89E051)
        "sql" -> Color(0xFF336791)
        "json" -> Color(0xFF000000)
        "xml" -> Color(0xFF0060AC)
        "yaml", "yml" -> Color(0xFFCB171E)
        "markdown", "md" -> Color(0xFF083FA1)
        else -> Color(0xFF6B7280) // Default gray color
    }
}