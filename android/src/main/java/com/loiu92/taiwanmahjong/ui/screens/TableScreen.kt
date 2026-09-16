package com.loiu92.taiwanmahjong.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loiu92.taiwanmahjong.R
import com.loiu92.taiwanmahjong.engine.ClaimKind
import com.loiu92.taiwanmahjong.engine.GameState
import com.loiu92.taiwanmahjong.engine.Phase
import com.loiu92.taiwanmahjong.engine.PlayerState
import com.loiu92.taiwanmahjong.engine.Tile
import com.loiu92.taiwanmahjong.ui.components.MahjongTile
import com.loiu92.taiwanmahjong.ui.components.ParlorCast
import com.loiu92.taiwanmahjong.ui.components.TileBack
import com.loiu92.taiwanmahjong.ui.i18n.AppLang
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
    lang: AppLang,
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
    val zh = lang == AppLang.ZH
    val human = state.player(humanIndex)
    val right = (humanIndex + 1) % 4
    val top = (humanIndex + 2) % 4
    val left = (humanIndex + 3) % 4

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_parlor),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                    ),
                ),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TableTopBar(state, strings, onToggleLang, onQuit)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.82f)
                        .fillMaxHeight(0.78f)
                        .shadow(16.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MahjongColors.feltEdge.copy(alpha = 0.92f),
                                    MahjongColors.felt.copy(alpha = 0.95f),
                                ),
                            ),
                        )
                        .border(2.dp, MahjongColors.gold.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                ) {
                    Text(
                        text = strings.brandZh,
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White.copy(alpha = 0.07f),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    SeatPortrait(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp),
                        playerIndex = top,
                        state = state,
                        zh = zh,
                        compact = false,
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 78.dp),
                    ) {
                        repeat(minOf(8, state.player(top).hand.size)) { TileBack() }
                    }
                    DiscardRow(
                        tiles = state.player(top).discards,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 118.dp),
                    )

                    SeatPortrait(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 4.dp),
                        playerIndex = left,
                        state = state,
                        zh = zh,
                        compact = true,
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 72.dp),
                    ) {
                        repeat(minOf(6, state.player(left).hand.size / 2 + 1)) {
                            TileBack(compact = true)
                        }
                    }

                    SeatPortrait(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp),
                        playerIndex = right,
                        state = state,
                        zh = zh,
                        compact = true,
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 72.dp),
                    ) {
                        repeat(minOf(6, state.player(right).hand.size / 2 + 1)) {
                            TileBack(compact = true)
                        }
                    }

                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        WindCompass(state)
                        TimerBadge(timer)
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        DiscardRow(human.discards)
                        if (human.melds.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                human.melds.forEach { meld ->
                                    Row {
                                        meld.tiles.forEach { tile ->
                                            MahjongTile(tile, compact = true)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HandBar(
                state = state,
                human = human,
                humanIndex = humanIndex,
                selected = selected,
                autoPlay = autoPlay,
                strings = strings,
                onSelect = onSelect,
                onDiscard = onDiscard,
                onClaim = onClaim,
                onPass = onPass,
                onHu = onHu,
                onKong = onKong,
                onToggleAuto = onToggleAuto,
            )
        }
    }
}

@Composable
private fun TableTopBar(
    state: GameState,
    strings: Strings,
    onToggleLang: () -> Unit,
    onQuit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "${state.stake * 2}/${state.stake}",
            color = MahjongColors.gold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        Text(
            text = strings.brandZh,
            color = MahjongColors.roseNeon,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
        )
        Row {
            TextButton(onClick = onToggleLang) {
                Text(text = strings.langToggle, color = MahjongColors.roseNeon)
            }
            TextButton(onClick = onQuit) {
                Text(text = strings.exit, color = MahjongColors.mist)
            }
        }
    }
}

@Composable
private fun SeatPortrait(
    modifier: Modifier,
    playerIndex: Int,
    state: GameState,
    zh: Boolean,
    compact: Boolean,
) {
    val p = state.player(playerIndex)
    val active = state.currentPlayer == playerIndex
    val avatar = ParlorCast.avatarRes(p.name)
    val avatarSize = if (compact) 64.dp else 72.dp

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            if (avatar != null) {
                Image(
                    painter = painterResource(avatar),
                    contentDescription = p.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(avatarSize)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .border(
                            width = if (active) 3.dp else 2.dp,
                            color = if (active) MahjongColors.gold else Color.White.copy(alpha = 0.5f),
                            shape = CircleShape,
                        ),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(MahjongColors.rosewood),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = p.name.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (active) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(14.dp)
                        .background(MahjongColors.gold, CircleShape)
                        .border(1.dp, Color.Black, CircleShape),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${ParlorCast.displayName(p.name, zh)} · ${p.seat.label}",
            color = if (active) MahjongColors.gold else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun HandBar(
    state: GameState,
    human: PlayerState,
    humanIndex: Int,
    selected: Tile?,
    autoPlay: Boolean,
    strings: Strings,
    onSelect: (Tile) -> Unit,
    onDiscard: (Tile) -> Unit,
    onClaim: (ClaimKind) -> Unit,
    onPass: () -> Unit,
    onHu: () -> Unit,
    onKong: () -> Unit,
    onToggleAuto: () -> Unit,
) {
    val canActDiscard = state.phase == Phase.AWAITING_DISCARD &&
        state.currentPlayer == humanIndex && !autoPlay
    val myClaims = state.pendingClaims.filter { it.player == humanIndex }.map { it.kind }.distinct()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.35f),
                        Color.Black.copy(alpha = 0.82f),
                    ),
                ),
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
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
            Spacer(modifier = Modifier.weight(1f))
            Text(text = human.seat.name, color = MahjongColors.mist, fontSize = 12.sp)
            Text(
                text = "  ${"%,d".format(human.chips)}",
                color = MahjongColors.gold,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            ModeToggle(autoPlay, strings, onToggleAuto)
        }
        Spacer(modifier = Modifier.height(4.dp))
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

@Composable
private fun DiscardRow(tiles: List<Tile>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        tiles.takeLast(12).forEach { tile ->
            MahjongTile(tile, compact = true)
        }
    }
}

@Composable
private fun WindCompass(state: GameState) {
    Column(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .border(1.dp, MahjongColors.gold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "北", color = Color.White, fontSize = 10.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "西", color = Color.White, fontSize = 10.sp)
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(36.dp)
                    .background(MahjongColors.rosewood, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${state.wallRemaining}",
                    color = MahjongColors.gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            Text(text = "東", color = Color.White, fontSize = 10.sp)
        }
        Text(
            text = "南 · ${state.roundWind.label}",
            color = MahjongColors.roseNeon,
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
                Brush.radialGradient(colors = listOf(MahjongColors.gold, MahjongColors.roseNeon)),
                CircleShape,
            )
            .border(3.dp, Color.White.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$seconds",
            color = Color.Black,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
        )
    }
}

@Composable
private fun ActionBtn(label: String, muted: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (muted) Color(0xFF455A64) else MahjongColors.roseNeon,
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.height(36.dp),
    ) {
        Text(text = label, fontSize = 13.sp)
    }
}

@Composable
private fun ModeToggle(auto: Boolean, strings: Strings, onToggle: () -> Unit) {
    Row {
        TextButton(onClick = { if (auto) onToggle() }) {
            Text(
                text = strings.manual,
                color = if (!auto) MahjongColors.gold else MahjongColors.mist,
            )
        }
        TextButton(onClick = { if (!auto) onToggle() }) {
            Text(
                text = strings.auto,
                color = if (auto) MahjongColors.gold else MahjongColors.mist,
            )
        }
    }
}
