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

/** Taiwanese luxury tea parlor / KTV — rosewood, amber lantern, rose neon. */
object MahjongColors {
    val asphalt = Color(0xFF120A0C)
    val asphaltLift = Color(0xFF1C1014)
    val rosewood = Color(0xFF4A1C1C)
    val vermillion = Color(0xFFE23B3B)
    val roseNeon = Color(0xFFFF4D8D)
    val amber = Color(0xFFFFB347)
    val gold = Color(0xFFE8C547)
    val cobalt = Color(0xFF3D5A80)
    val cobaltDeep = Color(0xFF1B2838)
    val acidYellow = Color(0xFFFFD166)
    val neonCyan = Color(0xFFFF8FAB)
    val felt = Color(0xFF0E3A2C)
    val feltEdge = Color(0xFF1F5C45)
    val tileFace = Color(0xFFF7F1E3)
    val tileFaceEdge = Color(0xFFD4CBB8)
    val tileBack = Color(0xFFB71C1C)
    val tileNumBlue = Color(0xFF1A3A8F)
    val tileWanRed = Color(0xFFC62828)
    val tileBamboo = Color(0xFF1B5E20)
    val tileDotRed = Color(0xFFC62828)
    val tileDotGreen = Color(0xFF2E7D32)
    val ink = Color(0xFF1A1A1A)
    val mist = Color(0xFFE0C8C8)
}

private val colors = darkColorScheme(
    primary = MahjongColors.roseNeon,
    secondary = MahjongColors.amber,
    tertiary = MahjongColors.gold,
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
