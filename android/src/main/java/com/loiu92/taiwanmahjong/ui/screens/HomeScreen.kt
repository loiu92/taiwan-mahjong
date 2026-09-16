package com.loiu92.taiwanmahjong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loiu92.taiwanmahjong.ui.i18n.AppLang
import com.loiu92.taiwanmahjong.ui.i18n.Strings
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    stake: Int,
    lang: AppLang,
    strings: Strings,
    onStakeChange: (Int) -> Unit,
    onToggleLang: () -> Unit,
    onStart: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MahjongColors.asphalt,
                        Color(0xFF0C1A2E),
                        Color(0xFF1A0A12),
                        MahjongColors.asphalt,
                    ),
                ),
            ),
    ) {
        // Neon accent bars (night-market signage vibe)
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .height(4.dp)
                .fillMaxWidth(0.35f)
                .background(MahjongColors.vermillion),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .height(4.dp)
                .fillMaxWidth(0.2f)
                .background(MahjongColors.acidYellow),
        )

        Text(
            text = strings.langToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 16.dp)
                .clickable(onClick = onToggleLang)
                .border(1.dp, MahjongColors.neonCyan, RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            color = MahjongColors.neonCyan,
            style = MaterialTheme.typography.labelLarge,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .fillMaxWidth(0.72f),
        ) {
            Text(
                text = strings.brandZh,
                style = MaterialTheme.typography.displayLarge,
                color = MahjongColors.vermillion,
                textAlign = TextAlign.Center,
            )
            Text(
                text = strings.brandEn,
                style = MaterialTheme.typography.titleLarge,
                color = MahjongColors.acidYellow,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = strings.tagline,
                color = MahjongColors.mist,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MahjongColors.asphaltLift.copy(alpha = 0.92f), RoundedCornerShape(4.dp))
                    .border(1.dp, MahjongColors.cobalt, RoundedCornerShape(4.dp))
                    .padding(20.dp),
            ) {
                Text(
                    "${strings.stake} $stake / ${stake * 2}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                )
                Slider(
                    value = stake.toFloat(),
                    onValueChange = { onStakeChange((it / 10).roundToInt() * 10) },
                    valueRange = 10f..200f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = MahjongColors.acidYellow,
                        activeTrackColor = MahjongColors.vermillion,
                        inactiveTrackColor = MahjongColors.cobaltDeep,
                    ),
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MahjongColors.vermillion),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(strings.soloVsAi, fontSize = 18.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val names = if (lang == AppLang.ZH) {
                    listOf("你", "Anada", "Lori", "Panda")
                } else {
                    listOf("You", "Anada", "Lori", "Panda")
                }
                names.forEach { name ->
                    Text(
                        text = name,
                        modifier = Modifier
                            .background(MahjongColors.cobaltDeep, RoundedCornerShape(2.dp))
                            .border(1.dp, MahjongColors.cobalt, RoundedCornerShape(2.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}
