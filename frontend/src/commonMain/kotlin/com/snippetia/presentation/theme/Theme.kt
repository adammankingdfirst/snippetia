package com.snippetia.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Color Palette
object SnippetiaColors {
    // Primary Colors
    val Primary = Color(0xFF6366F1)
    val PrimaryVariant = Color(0xFF4F46E5)
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFFE0E7FF)
    val OnPrimaryContainer = Color(0xFF1E1B4B)
    
    // Secondary Colors
    val Secondary = Color(0xFF10B981)
    val SecondaryVariant = Color(0xFF059669)
    val OnSecondary = Color.White
    val SecondaryContainer = Color(0xFFD1FAE5)
    val OnSecondaryContainer = Color(0xFF064E3B)
    
    // Tertiary Colors
    val Tertiary = Color(0xFFF59E0B)
    val TertiaryVariant = Color(0xFFD97706)
    val OnTertiary = Color.White
    val TertiaryContainer = Color(0xFFFEF3C7)
    val OnTertiaryContainer = Color(0xFF92400E)
    
    // Error Colors
    val Error = Color(0xFFEF4444)
    val OnError = Color.White
    val ErrorContainer = Color(0xFFFEE2E2)
    val OnErrorContainer = Color(0xFF991B1B)
    
    // Success Colors
    val Success = Color(0xFF22C55E)
    val OnSuccess = Color.White
    val SuccessContainer = Color(0xFFDCFCE7)
    val OnSuccessContainer = Color(0xFF166534)
    
    // Warning Colors
    val Warning = Color(0xFFF97316)
    val OnWarning = Color.White
    val WarningContainer = Color(0xFFFED7AA)
    val OnWarningContainer = Color(0xFF9A3412)
    
    // Info Colors
    val Info = Color(0xFF3B82F6)
    val OnInfo = Color.White
    val InfoContainer = Color(0xFFDBEAFE)
    val OnInfoContainer = Color(0xFF1E3A8A)
}

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = SnippetiaColors.Primary,
    onPrimary = SnippetiaColors.OnPrimary,
    primaryContainer = SnippetiaColors.PrimaryContainer,
    onPrimaryContainer = SnippetiaColors.OnPrimaryContainer,
    secondary = SnippetiaColors.Secondary,
    onSecondary = SnippetiaColors.OnSecondary,
    secondaryContainer = SnippetiaColors.SecondaryContainer,
    onSecondaryContainer = SnippetiaColors.OnSecondaryContainer,
    tertiary = SnippetiaColors.Tertiary,
    onTertiary = SnippetiaColors.OnTertiary,
    tertiaryContainer = SnippetiaColors.TertiaryContainer,
    onTertiaryContainer = SnippetiaColors.OnTertiaryContainer,
    error = SnippetiaColors.Error,
    onError = SnippetiaColors.OnError,
    errorContainer = SnippetiaColors.ErrorContainer,
    onErrorContainer = SnippetiaColors.OnErrorContainer,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1F2937),
    surface = Color.White,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
    scrim = Color(0x80000000),
    inverseSurface = Color(0xFF1F2937),
    inverseOnSurface = Color(0xFFF9FAFB),
    inversePrimary = Color(0xFF818CF8)
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF047857),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF92400E),
    tertiaryContainer = Color(0xFFD97706),
    onTertiaryContainer = Color(0xFFFEF3C7),
    error = Color(0xFFF87171),
    onError = Color(0xFF991B1B),
    errorContainer = Color(0xFFDC2626),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF64748B),
    scrim = Color(0x80000000),
    inverseSurface = Color(0xFFF1F5F9),
    inverseOnSurface = Color(0xFF1E293B),
    inversePrimary = Color(0xFF6366F1)
)

// Typography
val SnippetiaTypography = Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = androidx.compose.ui.unit.sp(57),
        lineHeight = androidx.compose.ui.unit.sp(64),
        letterSpacing = androidx.compose.ui.unit.sp(-0.25)
    ),
    displayMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = androidx.compose.ui.unit.sp(45),
        lineHeight = androidx.compose.ui.unit.sp(52),
        letterSpacing = androidx.compose.ui.unit.sp(0)
    ),
    displaySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = androidx.compose.ui.unit.sp(36),
        lineHeight = androidx.compose.ui.unit.sp(44),
        letterSpacing = androidx.compose.ui.unit.sp(0)
    ),
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = androidx.compose.ui.unit.sp(32),
        lineHeight = androidx.compose.ui.unit.sp(40),
        letterSpacing = androidx.compose.ui.unit.sp(0)
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = androidx.compose.ui.unit.sp(28),
        lineHeight = androidx.compose.ui.unit.sp(36),
        letterSpacing = androidx.compose.ui.unit.sp(0)
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = androidx.compose.ui.unit.sp(24),
        lineHeight = androidx.compose.ui.unit.sp(32),
        letterSpacing = androidx.compose.ui.unit.sp(0)
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = androidx.compose.ui.unit.sp(22),
        lineHeight = androidx.compose.ui.unit.sp(28),
        letterSpacing = androidx.compose.ui.unit.sp(0)
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = androidx.compose.ui.unit.sp(16),
        lineHeight = androidx.compose.ui.unit.sp(24),
        letterSpacing = androidx.compose.ui.unit.sp(0.15)
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = androidx.compose.ui.unit.sp(14),
        lineHeight = androidx.compose.ui.unit.sp(20),
        letterSpacing = androidx.compose.ui.unit.sp(0.1)
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = androidx.compose.ui.unit.sp(16),
        lineHeight = androidx.compose.ui.unit.sp(24),
        letterSpacing = androidx.compose.ui.unit.sp(0.5)
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = androidx.compose.ui.unit.sp(14),
        lineHeight = androidx.compose.ui.unit.sp(20),
        letterSpacing = androidx.compose.ui.unit.sp(0.25)
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = androidx.compose.ui.unit.sp(12),
        lineHeight = androidx.compose.ui.unit.sp(16),
        letterSpacing = androidx.compose.ui.unit.sp(0.4)
    ),
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = androidx.compose.ui.unit.sp(14),
        lineHeight = androidx.compose.ui.unit.sp(20),
        letterSpacing = androidx.compose.ui.unit.sp(0.1)
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = androidx.compose.ui.unit.sp(12),
        lineHeight = androidx.compose.ui.unit.sp(16),
        letterSpacing = androidx.compose.ui.unit.sp(0.5)
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = androidx.compose.ui.unit.sp(11),
        lineHeight = androidx.compose.ui.unit.sp(16),
        letterSpacing = androidx.compose.ui.unit.sp(0.5)
    )
)

@Composable
fun SnippetiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SnippetiaTypography,
        content = content
    )
}

// Language colors for syntax highlighting and badges
fun getLanguageColor(language: String): Color {
    return when (language.lowercase()) {
        "kotlin" -> Color(0xFF7F52FF)
        "java" -> Color(0xFFED8B00)
        "javascript", "js" -> Color(0xFFF7DF1E)
        "typescript", "ts" -> Color(0xFF3178C6)
        "python" -> Color(0xFF3776AB)
        "swift" -> Color(0xFFFA7343)
        "go" -> Color(0xFF00ADD8)
        "rust" -> Color(0xFFCE422B)
        "c++" -> Color(0xFF00599C)
        "c" -> Color(0xFFA8B9CC)
        "c#", "csharp" -> Color(0xFF239120)
        "php" -> Color(0xFF777BB4)
        "ruby" -> Color(0xFFCC342D)
        "dart" -> Color(0xFF0175C2)
        "flutter" -> Color(0xFF02569B)
        "react" -> Color(0xFF61DAFB)
        "vue" -> Color(0xFF4FC08D)
        "angular" -> Color(0xFFDD0031)
        "html" -> Color(0xFFE34F26)
        "css" -> Color(0xFF1572B6)
        "scss", "sass" -> Color(0xFFCF649A)
        "sql" -> Color(0xFF336791)
        "shell", "bash" -> Color(0xFF89E051)
        "powershell" -> Color(0xFF5391FE)
        "json" -> Color(0xFF000000)
        "xml" -> Color(0xFF0060AC)
        "yaml", "yml" -> Color(0xFFCB171E)
        "dockerfile" -> Color(0xFF384D54)
        "markdown", "md" -> Color(0xFF083FA1)
        "r" -> Color(0xFF276DC3)
        "matlab" -> Color(0xFFE16737)
        "scala" -> Color(0xFFDC322F)
        "haskell" -> Color(0xFF5D4F85)
        "clojure" -> Color(0xFF5881D8)
        "elixir" -> Color(0xFF6E4A7E)
        "erlang" -> Color(0xFFA90533)
        "lua" -> Color(0xFF000080)
        "perl" -> Color(0xFF39457E)
        "assembly" -> Color(0xFF6E4C13)
        "vhdl" -> Color(0xFFADB2CB)
        "verilog" -> Color(0xFFB2B7F8)
        else -> Color(0xFF6B7280)
    }
}

// Status colors
object StatusColors {
    val Success = SnippetiaColors.Success
    val Warning = SnippetiaColors.Warning
    val Error = SnippetiaColors.Error
    val Info = SnippetiaColors.Info
    
    val Online = Color(0xFF22C55E)
    val Offline = Color(0xFF6B7280)
    val Away = Color(0xFFF59E0B)
    val Busy = Color(0xFFEF4444)
}

// Gradient colors
object GradientColors {
    val PrimaryGradient = listOf(
        SnippetiaColors.Primary,
        SnippetiaColors.PrimaryVariant
    )
    
    val SecondaryGradient = listOf(
        SnippetiaColors.Secondary,
        SnippetiaColors.SecondaryVariant
    )
    
    val TertiaryGradient = listOf(
        SnippetiaColors.Tertiary,
        SnippetiaColors.TertiaryVariant
    )
    
    val SunsetGradient = listOf(
        Color(0xFFFF6B6B),
        Color(0xFFFFE66D),
        Color(0xFF4ECDC4)
    )
    
    val OceanGradient = listOf(
        Color(0xFF667EEA),
        Color(0xFF764BA2)
    )
    
    val ForestGradient = listOf(
        Color(0xFF134E5E),
        Color(0xFF71B280)
    )
}