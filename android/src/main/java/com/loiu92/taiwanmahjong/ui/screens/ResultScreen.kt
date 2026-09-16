package com.loiu92.taiwanmahjong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.loiu92.taiwanmahjong.ui.i18n.Strings
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors

@Composable
fun ResultScreen(
    state: GameState,
    strings: Strings,
    onNext: () -> Unit,
    onHome: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MahjongColors.asphalt)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val win = state.win
        if (state.isDraw || win == null) {
            Text(strings.drawGame, style = MaterialTheme.typography.displayLarge, color = Color.White)
            Text(strings.dealerStays, color = MahjongColors.mist)
        } else {
            val winner = state.player(win.winner)
            Text("胡！", style = MaterialTheme.typography.displayLarge, color = MahjongColors.vermillion)
            Text(
                "${winner.name} ${strings.wins} · ${win.tai} ${strings.totalTai} · ${win.source.name}",
                color = Color.White,
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .background(MahjongColors.asphaltLift, RoundedCornerShape(4.dp))
                    .border(1.dp, MahjongColors.cobalt, RoundedCornerShape(4.dp))
                    .padding(16.dp),
            ) {
                win.breakdown.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(line.label, color = Color.White)
                        Text("+${line.tai}", color = MahjongColors.acidYellow)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "${strings.totalTai.uppercase()} ${win.tai} × ${state.stake}",
                    color = MahjongColors.mist,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(strings.scores, color = Color.White, fontSize = 16.sp)
        state.players.forEachIndexed { index, p ->
            val delta = state.win?.payments?.get(index) ?: 0
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${p.name} (${p.seat.label})", color = Color.White)
                Text(
                    "${"%,d".format(p.chips)}" +
                        if (delta != 0) " (${if (delta > 0) "+" else ""}$delta)" else "",
                    color = when {
                        delta > 0 -> MahjongColors.acidYellow
                        delta < 0 -> Color(0xFFFF8A80)
                        else -> Color.White
                    },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = MahjongColors.vermillion),
                shape = RoundedCornerShape(4.dp),
            ) { Text(strings.nextHand) }
            Button(
                onClick = onHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                shape = RoundedCornerShape(4.dp),
            ) { Text(strings.home) }
        }
    }
}
