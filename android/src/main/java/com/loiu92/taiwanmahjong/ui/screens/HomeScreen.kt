package com.loiu92.taiwanmahjong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    stake: Int,
    onStakeChange: (Int) -> Unit,
    onStart: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D1B2A), Color(0xFF1B5E20), Color(0xFF0D1B2A)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.7f),
        ) {
            Text(
                text = "愛台灣 打麻將",
                style = MaterialTheme.typography.displayLarge,
                color = MahjongColors.gold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Taiwan Mahjong",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.9f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "16-tile · Full tai · Local AI",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .background(MahjongColors.hud.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .padding(20.dp)
                    .fillMaxWidth(),
            ) {
                Column {
                    Text("Stake: $stake / ${stake * 2}", color = Color.White)
                    Slider(
                        value = stake.toFloat(),
                        onValueChange = { onStakeChange((it / 10).roundToInt() * 10) },
                        valueRange = 10f..200f,
                        steps = 18,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MahjongColors.feltLight),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("Solo vs AI", fontSize = 18.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("You", "Anada", "Lori", "Panda").forEach { name ->
                    Text(
                        text = name,
                        modifier = Modifier
                            .background(Color(0xFF1565C0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}
