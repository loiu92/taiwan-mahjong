package com.loiu92.taiwanmahjong.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

/**
 * Cinematic first-person parlor table — large host across, hostess at side,
 * felt + 發 emblem, player hand along the bottom (matches reference mood).
 */
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
            painter = painterResource(R.drawable.bg_table),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                        ),
                    ),
                ),
        )

        // Minimal top chrome
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${state.stake * 2}/${state.stake}",
                color = MahjongColors.gold,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (zh) "牌中見真章" else "GOOD TILES",
                    color = MahjongColors.gold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                )
                Text(
                    text = if (zh) "GOOD TILES / BETTER FRIENDS" else "BETTER FRIENDS",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                )
            }
            Row {
                TextButton(onClick = onToggleLang) {
                    Text(strings.langToggle, color = MahjongColors.gold)
                }
                TextButton(onClick = onQuit) {
                    Text(strings.exit, color = MahjongColors.mist)
                }
            }
        }

        // Left hostess (MeiMei) — large, like reference
        CinematicHost(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp, bottom = 40.dp)
                .fillMaxHeight(0.62f)
                .width(150.dp),
            player = state.player(left),
            active = state.currentPlayer == left,
            zh = zh,
            alignment = Alignment.BottomStart,
        )

        // Right hostess (smaller)
        CinematicHost(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp, bottom = 60.dp)
                .fillMaxHeight(0.48f)
                .width(120.dp),
            player = state.player(right),
            active = state.currentPlayer == right,
            zh = zh,
            alignment = Alignment.BottomEnd,
        )

        // Opposite host (Hao) — hero across table
        CinematicHost(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 36.dp)
                .fillMaxHeight(0.42f)
                .width(200.dp),
            player = state.player(top),
            active = state.currentPlayer == top,
            zh = zh,
            alignment = Alignment.BottomCenter,
            showNameAbove = false,
        )

        // Felt table plane
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 28.dp)
                .fillMaxWidth(0.58f)
                .fillMaxHeight(0.42f)
                .shadow(20.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1B5E3A), Color(0xFF0A2E1C)),
                    ),
                )
                .border(3.dp, Color(0xFF3E2723), RoundedCornerShape(14.dp)),
        ) {
            Image(
                painter = painterResource(R.drawable.emblem_fa),
                contentDescription = "發",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(72.dp),
                contentScale = ContentScale.Fit,
            )

            // Opponent wall (top of felt)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
            ) {
                repeat(minOf(12, state.player(top).hand.size)) { TileBack(compact = true) }
            }
            DiscardRow(
                tiles = state.player(top).discards,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 46.dp),
            )
            DiscardRow(
                tiles = state.player(left).discards.takeLast(6),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp),
            )
            DiscardRow(
                tiles = state.player(right).discards.takeLast(6),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp),
            )

            // Timer + wall count
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${state.wallRemaining}",
                    color = MahjongColors.gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
                TimerBadge(timer)
            }
        }

        // Bottom hand bar — first-person
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                    ),
                )
                .padding(start = 8.dp, end = 8.dp, bottom = 6.dp, top = 12.dp),
        ) {
            ActionRow(
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
            if (human.melds.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 4.dp),
                ) {
                    human.melds.forEach { meld ->
                        Row {
                            meld.tiles.forEach { MahjongTile(it, compact = true) }
                        }
                    }
                }
            }
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
                    val canAct = state.phase == Phase.AWAITING_DISCARD &&
                        state.currentPlayer == humanIndex && !autoPlay
                    MahjongTile(
                        tile = tile,
                        selected = isSelected,
                        onClick = {
                            if (!canAct) return@MahjongTile
                            if (isSelected) onDiscard(tile) else onSelect(tile)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CinematicHost(
    modifier: Modifier,
    player: PlayerState,
    active: Boolean,
    zh: Boolean,
    alignment: Alignment,
    showNameAbove: Boolean = true,
) {
    val avatar = ParlorCast.avatarRes(player.name) ?: return
    Box(modifier = modifier, contentAlignment = alignment) {
        Image(
            painter = painterResource(avatar),
            contentDescription = player.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (active) 3.dp else 0.dp,
                    color = if (active) MahjongColors.gold else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                ),
        )
        Text(
            text = "${ParlorCast.displayName(player.name, zh)} · ${player.seat.label}",
            color = if (active) MahjongColors.gold else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(if (showNameAbove) Alignment.TopCenter else Alignment.BottomCenter)
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun ActionRow(
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
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
        Image(
            painter = painterResource(R.drawable.ui_chip),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = " %,d".format(human.chips),
            color = MahjongColors.gold,
            fontWeight = FontWeight.Bold,
        )
        TextButton(onClick = { if (autoPlay) onToggleAuto() }) {
            Text(strings.manual, color = if (!autoPlay) MahjongColors.gold else MahjongColors.mist)
        }
        TextButton(onClick = { if (!autoPlay) onToggleAuto() }) {
            Text(strings.auto, color = if (autoPlay) MahjongColors.gold else MahjongColors.mist)
        }
    }
}

@Composable
private fun DiscardRow(tiles: List<Tile>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        tiles.takeLast(10).forEach { MahjongTile(it, compact = true) }
    }
}

@Composable
private fun TimerBadge(seconds: Int) {
    Box(
        modifier = Modifier.size(52.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ui_timer),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = "$seconds",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
        )
    }
}

@Composable
private fun ActionBtn(label: String, muted: Boolean = false, onClick: () -> Unit) {
    val art = when (label) {
        "胡", "Hu" -> R.drawable.ui_btn_hu
        "碰", "Pong" -> R.drawable.ui_btn_pong
        "槓", "Kong" -> R.drawable.ui_btn_kong
        "吃", "Chi" -> R.drawable.ui_btn_chi
        "過", "Pass" -> R.drawable.ui_btn_pass
        else -> null
    }
    if (art != null) {
        Image(
            painter = painterResource(art),
            contentDescription = label,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentScale = ContentScale.Crop,
        )
    } else {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (muted) Color(0xFF455A64) else MahjongColors.roseNeon,
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.height(34.dp),
        ) {
            Text(text = label, fontSize = 12.sp)
        }
    }
}
