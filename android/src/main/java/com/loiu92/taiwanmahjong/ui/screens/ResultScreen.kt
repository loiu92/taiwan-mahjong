package com.loiu92.taiwanmahjong.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loiu92.taiwanmahjong.R
import com.loiu92.taiwanmahjong.engine.GameState
import com.loiu92.taiwanmahjong.ui.components.ParlorCast
import com.loiu92.taiwanmahjong.ui.i18n.AppLang
import com.loiu92.taiwanmahjong.ui.i18n.Strings
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors

@Composable
fun ResultScreen(
    state: GameState,
    lang: AppLang,
    strings: Strings,
    onNext: () -> Unit,
    onHome: () -> Unit,
) {
    val zh = lang == AppLang.ZH
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_parlor),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f)),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                Text("胡！", style = MaterialTheme.typography.displayLarge, color = MahjongColors.gold)
                Text(
                    "${ParlorCast.displayName(winner.name, zh)} ${strings.wins} · ${win.tai} ${strings.totalTai}",
                    color = Color.White,
                    fontSize = 18.sp,
                )
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                        .border(1.dp, MahjongColors.gold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
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
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(strings.scores, color = Color.White, fontSize = 16.sp)
            state.players.forEachIndexed { index, p ->
                val delta = state.win?.payments?.get(index) ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${ParlorCast.displayName(p.name, zh)} (${p.seat.label})", color = Color.White)
                    Text(
                        "${"%,d".format(p.chips)}" +
                            if (delta != 0) " (${if (delta > 0) "+" else ""}$delta)" else "",
                        color = when {
                            delta > 0 -> MahjongColors.gold
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
                    colors = ButtonDefaults.buttonColors(containerColor = MahjongColors.roseNeon),
                    shape = RoundedCornerShape(24.dp),
                ) { Text(strings.nextHand) }
                Button(
                    onClick = onHome,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                    shape = RoundedCornerShape(24.dp),
                ) { Text(strings.home) }
            }
        }
    }
}
