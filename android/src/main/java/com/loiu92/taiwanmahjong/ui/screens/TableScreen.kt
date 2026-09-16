package com.loiu92.taiwanmahjong.ui.screens

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
import com.loiu92.taiwanmahjong.ui.i18n.Strings
import com.loiu92.taiwanmahjong.ui.i18n.claimLabel
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors

@Composable
fun TableScreen(
    state: GameState,
    selected: Tile?,
    autoPlay: Boolean,
    timer: Int,
    humanIndex: Int,
    strings: Strings,
    onSelect: (Tile) -> Unit,
    onDiscard: (Tile) -> Unit,
    onClaim: (ClaimKind) -> Unit,
    onPass: () -> Unit,
    onHu: () -> Unit,
    onKong: () -> Unit,
    onToggleAuto: () -> Unit,
    onToggleLang: () -> Unit,
    onQuit: () -> Unit,
) {
    val human = state.player(humanIndex)
    val right = (humanIndex + 1) % 4
    val top = (humanIndex + 2) % 4
    val left = (humanIndex + 3) % 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF12261C), MahjongColors.asphalt),
                ),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MahjongColors.asphaltLift)
                .border(width = 0.dp, color = Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "${state.stake * 2}/${state.stake}",
                color = MahjongColors.acidYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            PlayerChip(state.player(top).name, state.player(top).seat.label, state.currentPlayer == top)
            Row {
                TextButton(onClick = onToggleLang) {
                    Text(strings.langToggle, color = MahjongColors.neonCyan)
                }
                TextButton(onClick = onQuit) {
                    Text(strings.exit, color = MahjongColors.mist)
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(MahjongColors.feltEdge, MahjongColors.felt),
                        ),
                        RoundedCornerShape(12.dp),
                    )
                    .border(2.dp, MahjongColors.cobaltDeep, RoundedCornerShape(12.dp)),
            ) {
                Text(
                    text = strings.brandZh,
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White.copy(alpha = 0.08f),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                )

                Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayerChip(state.player(top).name, state.player(top).seat.label, state.currentPlayer == top)
                    Spacer(Modifier.height(4.dp))
                    Row { repeat(minOf(10, state.player(top).hand.size)) { TileBack() } }
                    DiscardRow(state.player(top).discards)
                }

                Column(
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayerChip(state.player(left).name, state.player(left).seat.label, state.currentPlayer == left)
                    Spacer(Modifier.height(4.dp))
                    Column { repeat(minOf(8, state.player(left).hand.size / 2 + 1)) { TileBack() } }
                    DiscardRow(state.player(left).discards.takeLast(6))
                }

                Column(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayerChip(state.player(right).name, state.player(right).seat.label, state.currentPlayer == right)
                    Spacer(Modifier.height(4.dp))
                    Column { repeat(minOf(8, state.player(right).hand.size / 2 + 1)) { TileBack() } }
                    DiscardRow(state.player(right).discards.takeLast(6))
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    WindCompass(state)
                    TimerBadge(timer)
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DiscardRow(human.discards)
                    if (human.flowers.isNotEmpty()) {
                        Row { human.flowers.forEach { MahjongTile(it, compact = true) } }
                    }
                    if (human.melds.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            human.melds.forEach { meld ->
                                Row { meld.tiles.forEach { MahjongTile(it, compact = true) } }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MahjongColors.asphaltLift)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
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
                        ActionBtn(claimLabel(kind.name, strings)) { onClaim(kind) }
                    }
                    ActionBtn(strings.pass, muted = true, onClick = onPass)
                }
                if (canActDiscard) {
                    ActionBtn(strings.hu) { onHu() }
                    ActionBtn(strings.kong) { onKong() }
                    if (selected != null) {
                        ActionBtn(strings.discard) { onDiscard(selected) }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(human.seat.name, color = MahjongColors.mist, fontSize = 12.sp)
                Text("  ${"%,d".format(human.chips)}", color = MahjongColors.acidYellow, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                ModeToggle(autoPlay, strings, onToggleAuto)
            }

            Spacer(Modifier.height(4.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
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
                if (active) MahjongColors.acidYellow else MahjongColors.cobaltDeep,
                RoundedCornerShape(2.dp),
            )
            .border(
                1.dp,
                if (active) MahjongColors.vermillion else MahjongColors.cobalt,
                RoundedCornerShape(2.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.25f), avatarColor(name)),
                    ),
                    CircleShape,
                )
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                name.take(1),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(6.dp))
        Column {
            Text(name, color = if (active) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(wind, color = if (active) Color.Black.copy(alpha = 0.7f) else MahjongColors.mist, fontSize = 10.sp)
        }
    }
}

private fun avatarColor(name: String): Color = when (name) {
    "Anada" -> Color(0xFFE91E63)
    "Lori" -> Color(0xFF26A69A)
    "Panda" -> Color(0xFF546E7A)
    else -> MahjongColors.cobalt
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
            .background(MahjongColors.asphaltLift, RoundedCornerShape(4.dp))
            .border(1.dp, MahjongColors.cobalt, RoundedCornerShape(4.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("北", color = Color.White, fontSize = 10.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("西", color = Color.White, fontSize = 10.sp)
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(36.dp)
                    .background(MahjongColors.cobaltDeep, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("${state.wallRemaining}", color = MahjongColors.acidYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text("東", color = Color.White, fontSize = 10.sp)
        }
        Text("南 · ${state.roundWind.label}", color = MahjongColors.vermillion, fontSize = 10.sp)
    }
}

@Composable
private fun TimerBadge(seconds: Int) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                Brush.radialGradient(listOf(MahjongColors.acidYellow, MahjongColors.vermillion)),
                CircleShape,
            )
            .border(3.dp, MahjongColors.cobalt, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text("$seconds", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 22.sp)
    }
}

@Composable
private fun ActionBtn(label: String, muted: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (muted) Color(0xFF37474F) else MahjongColors.vermillion,
        ),
        shape = RoundedCornerShape(2.dp),
        modifier = Modifier.height(36.dp),
    ) {
        Text(label, fontSize = 13.sp)
    }
}

@Composable
private fun ModeToggle(auto: Boolean, strings: Strings, onToggle: () -> Unit) {
    Row {
        TextButton(onClick = { if (auto) onToggle() }) {
            Text(strings.manual, color = if (!auto) MahjongColors.acidYellow else MahjongColors.mist)
        }
        TextButton(onClick = { if (!auto) onToggle() }) {
            Text(strings.auto, color = if (auto) MahjongColors.acidYellow else MahjongColors.mist)
        }
    }
}
