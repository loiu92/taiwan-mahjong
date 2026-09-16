package com.loiu92.taiwanmahjong.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Taiwan 2026 — Raohe / Ximending night-market neon on wet asphalt,
 * paired with deep table felt (not generic casino green).
 */
object MahjongColors {
    val asphalt = Color(0xFF0A0C10)
    val asphaltLift = Color(0xFF141820)
    val vermillion = Color(0xFFE73121)
    val cobalt = Color(0xFF0047AB)
    val cobaltDeep = Color(0xFF003080)
    val acidYellow = Color(0xFFFFE600)
    val neonCyan = Color(0xFF00E5FF)
    val felt = Color(0xFF0F3D2E)
    val feltEdge = Color(0xFF1A5C45)
    val tileFace = Color(0xFFF7F1E3)
    val tileFaceEdge = Color(0xFFD4CBB8)
    val tileBack = Color(0xFFB71C1C)
    val tileNumBlue = Color(0xFF1A3A8F)
    val tileWanRed = Color(0xFFC62828)
    val tileBamboo = Color(0xFF1B5E20)
    val tileDotRed = Color(0xFFC62828)
    val tileDotGreen = Color(0xFF2E7D32)
    val ink = Color(0xFF1A1A1A)
    val mist = Color(0xFFB0BEC5)
}

private val colors = darkColorScheme(
    primary = MahjongColors.vermillion,
    secondary = MahjongColors.acidYellow,
    tertiary = MahjongColors.cobalt,
    background = MahjongColors.asphalt,
    surface = MahjongColors.asphaltLift,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        letterSpacing = 1.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.8.sp,
    ),
)

@Composable
fun TaiwanMahjongTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content,
    )
}
