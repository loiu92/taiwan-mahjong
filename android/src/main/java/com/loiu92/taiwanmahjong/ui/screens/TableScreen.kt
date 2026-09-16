package com.loiu92.taiwanmahjong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loiu92.taiwanmahjong.engine.ClaimKind
import com.loiu92.taiwanmahjong.engine.GameState
import com.loiu92.taiwanmahjong.engine.Phase
import com.loiu92.taiwanmahjong.engine.Tile
import com.loiu92.taiwanmahjong.ui.components.MahjongTile
import com.loiu92.taiwanmahjong.ui.components.TileBack
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors

@Composable
fun TableScreen(
    state: GameState,
    selected: Tile?,
    autoPlay: Boolean,
    timer: Int,
    humanIndex: Int,
    onSelect: (Tile) -> Unit,
    onDiscard: (Tile) -> Unit,
    onClaim: (ClaimKind) -> Unit,
    onPass: () -> Unit,
    onHu: () -> Unit,
    onKong: () -> Unit,
    onToggleAuto: () -> Unit,
    onQuit: () -> Unit,
) {
    val human = state.player(humanIndex)
    // Seat mapping around table: bottom=human, right=(h+1), top=(h+2), left=(h+3)
    val right = (humanIndex + 1) % 4
    val top = (humanIndex + 2) % 4
    val left = (humanIndex + 3) % 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF1B5E20), Color(0xFF0D1B2A)),
                ),
            ),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MahjongColors.hud)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("${state.stake * 2}/${state.stake}", color = Color.White, fontSize = 14.sp)
            PlayerChip(state.player(top).name, state.player(top).seat.label, state.currentPlayer == top)
            TextButton(onClick = onQuit) { Text("Exit", color = Color.White) }
        }

        // Table body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            // Felt
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .background(MahjongColors.felt, RoundedCornerShape(16.dp))
                    .border(3.dp, Color(0xFF4E342E), RoundedCornerShape(16.dp)),
            ) {
                Text(
                    text = "愛台灣 打麻將",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White.copy(alpha = 0.12f),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                )

                // Top opponent
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayerChip(state.player(top).name, state.player(top).seat.label, state.currentPlayer == top)
                    Spacer(Modifier.height(4.dp))
                    Row { repeat(minOf(10, state.player(top).hand.size)) { TileBack() } }
                    DiscardRow(state.player(top).discards)
                }

                // Left opponent
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayerChip(state.player(left).name, state.player(left).seat.label, state.currentPlayer == left)
                    Spacer(Modifier.height(4.dp))
                    Column { repeat(minOf(8, state.player(left).hand.size / 2 + 1)) { TileBack(compact = true) } }
                    DiscardRow(state.player(left).discards.takeLast(6))
                }

                // Right opponent
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayerChip(state.player(right).name, state.player(right).seat.label, state.currentPlayer == right)
                    Spacer(Modifier.height(4.dp))
                    Column { repeat(minOf(8, state.player(right).hand.size / 2 + 1)) { TileBack(compact = true) } }
                    DiscardRow(state.player(right).discards.takeLast(6))
                }

                // Center hub + timer
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    WindCompass(state)
                    TimerBadge(timer)
                }

                // Human discards above hand
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DiscardRow(human.discards)
                    if (human.flowers.isNotEmpty()) {
                        Row {
                            human.flowers.forEach { MahjongTile(it, compact = true) }
                        }
                    }
                    if (human.melds.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            human.melds.forEach { meld ->
                                Row {
                                    meld.tiles.forEach { MahjongTile(it, compact = true) }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Hand + controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D47A0))
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            // Claim / action buttons
            val canActDiscard = state.phase == Phase.AWAITING_DISCARD &&
                state.currentPlayer == humanIndex && !autoPlay
            val myClaims = state.pendingClaims.filter { it.player == humanIndex }.map { it.kind }.distinct()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.phase == Phase.AWAITING_CLAIMS && myClaims.isNotEmpty()) {
                    myClaims.forEach { kind ->
                        ActionBtn(kind.name) { onClaim(kind) }
                    }
                    ActionBtn("Pass", muted = true, onClick = onPass)
                }
                if (canActDiscard) {
                    ActionBtn("Hu") { onHu() }
                    ActionBtn("Kong") { onKong() }
                    if (selected != null) {
                        ActionBtn("Discard") { onDiscard(selected) }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(human.seat.name, color = Color.White, fontSize = 12.sp)
                Text("  ${human.chips}", color = MahjongColors.gold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                ModeToggle(autoPlay, onToggleAuto)
            }

            Spacer(Modifier.height(4.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(
                    count = human.hand.size,
                    key = { idx -> "${human.hand[idx].id}#$idx" },
                ) { idx ->
                    val tile = human.hand[idx]
                    val isSelected = selected == tile
                    MahjongTile(
                        tile = tile,
                        selected = isSelected,
                        onClick = {
                            if (!canActDiscard) return@MahjongTile
                            if (isSelected) onDiscard(tile) else onSelect(tile)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerChip(name: String, wind: String, active: Boolean) {
    Row(
        modifier = Modifier
            .background(
                if (active) MahjongColors.gold else Color(0xFF1565C0),
                RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(wind, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(6.dp))
        Text(name, color = if (active) Color.Black else Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun DiscardRow(tiles: List<Tile>) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        tiles.takeLast(12).forEach { MahjongTile(it, compact = true) }
    }
}

@Composable
private fun WindCompass(state: GameState) {
    Column(
        modifier = Modifier
            .background(Color(0xFF1A237E), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("N", color = Color.White, fontSize = 10.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("W", color = Color.White, fontSize = 10.sp)
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(36.dp)
                    .background(Color(0xFF0D47A0), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${state.wallRemaining}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            Text("E", color = Color.White, fontSize = 10.sp)
        }
        Text(
            "S · ${state.roundWind.label}",
            color = MahjongColors.gold,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun TimerBadge(seconds: Int) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                Brush.radialGradient(listOf(Color(0xFFFFA000), Color(0xFFE65100))),
                CircleShape,
            )
            .border(3.dp, Color(0xFF1565C0), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$seconds",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )
    }
}

@Composable
private fun ActionBtn(label: String, muted: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (muted) Color(0xFF546E7A) else Color(0xFFC62828),
        ),
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = Modifier.height(36.dp),
    ) {
        Text(label, fontSize = 13.sp)
    }
}

@Composable
private fun ModeToggle(auto: Boolean, onToggle: () -> Unit) {
    Row {
        TextButton(
            onClick = { if (auto) onToggle() },
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (!auto) Color(0xFF69F0AE) else Color.White,
            ),
        ) { Text("Manual") }
        TextButton(
            onClick = { if (!auto) onToggle() },
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (auto) Color(0xFF69F0AE) else Color.White,
            ),
        ) { Text("Auto") }
    }
}
