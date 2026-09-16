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

object MahjongColors {
    val felt = Color(0xFF1B5E20)
    val feltLight = Color(0xFF2E7D32)
    val night = Color(0xFF0D1B2A)
    val gold = Color(0xFFFFB300)
    val tileFace = Color(0xFFFFF8E7)
    val tileBack = Color(0xFFC62828)
    val hud = Color(0xFF0D47A0)
}

private val colors = darkColorScheme(
    primary = MahjongColors.feltLight,
    secondary = MahjongColors.gold,
    background = MahjongColors.night,
    surface = Color(0xFF1A237E),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
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
