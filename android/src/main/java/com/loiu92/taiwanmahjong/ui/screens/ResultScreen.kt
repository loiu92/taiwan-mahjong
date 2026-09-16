package com.loiu92.taiwanmahjong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loiu92.taiwanmahjong.engine.GameState
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors

@Composable
fun ResultScreen(
    state: GameState,
    onNext: () -> Unit,
    onHome: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val win = state.win
        if (state.isDraw || win == null) {
            Text("Draw game", style = MaterialTheme.typography.displayLarge, color = Color.White)
            Text("Dealer stays", color = Color.White.copy(alpha = 0.7f))
        } else {
            val winner = state.player(win.winner)
            Text("胡！", style = MaterialTheme.typography.displayLarge, color = MahjongColors.gold)
            Text(
                "${winner.name} wins · ${win.tai} tai · ${win.source.name}",
                color = Color.White,
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .background(MahjongColors.hud, RoundedCornerShape(12.dp))
                    .padding(16.dp),
            ) {
                win.breakdown.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(line.label, color = Color.White)
                        Text("+${line.tai}", color = MahjongColors.gold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Total ${win.tai} tai × ${state.stake} stake", color = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Scores", color = Color.White, fontSize = 16.sp)
        state.players.forEach { p ->
            val delta = state.win?.payments?.get(state.players.indexOf(p)) ?: 0
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${p.name} (${p.seat.label})", color = Color.White)
                Text(
                    "${p.chips}" + if (delta != 0) " (${if (delta > 0) "+" else ""}$delta)" else "",
                    color = if (delta > 0) MahjongColors.gold else if (delta < 0) Color(0xFFEF9A9A) else Color.White,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = MahjongColors.feltLight),
            ) { Text("Next hand") }
            Button(
                onClick = onHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF546E7A)),
            ) { Text("Home") }
        }
    }
}
