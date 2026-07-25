package app.saadiah.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// The primary users include people in their seventies: body text never drops below 17sp
// and secondary text never below 15sp.
val BodySize = 17.sp
val SecondarySize = 15.sp
val HeroTimeSize = 44.sp
val HeroNameSize = 26.sp
val ArabicSize = 22.sp

val ScreenPadding = 20.dp
val RowGap = 14.dp
val SectionGap = 26.dp
val MinimumTapTarget = 48.dp

private val LightAccent = Color(0xFF7A5A22)
private val DarkAccent = Color(0xFFB8935A)

private val LightColours =
    lightColorScheme(
        primary = LightAccent,
        onPrimary = Color.White,
        background = Color(0xFFFCFAF6),
        onBackground = Color(0xFF1B1815),
        surface = Color(0xFFF3EDE2),
        onSurface = Color(0xFF1B1815),
        onSurfaceVariant = Color(0xFF4A4238),
    )

private val DarkColours =
    darkColorScheme(
        primary = DarkAccent,
        onPrimary = Color(0xFF1B1815),
        background = Color(0xFF14120F),
        onBackground = Color(0xFFF2EDE4),
        surface = Color(0xFF221E19),
        onSurface = Color(0xFFF2EDE4),
        onSurfaceVariant = Color(0xFFCFC5B4),
    )

private val AccessibleTypography =
    Typography(
        bodyLarge = TextStyle(fontSize = BodySize),
        bodyMedium = TextStyle(fontSize = SecondarySize),
    )

@Composable
fun SaadiahTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColours else LightColours,
        typography = AccessibleTypography,
        content = content,
    )
}
