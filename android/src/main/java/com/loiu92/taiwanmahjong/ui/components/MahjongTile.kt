package com.loiu92.taiwanmahjong.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loiu92.taiwanmahjong.engine.Dragon
import com.loiu92.taiwanmahjong.engine.Suit
import com.loiu92.taiwanmahjong.engine.Tile
import com.loiu92.taiwanmahjong.engine.displayName
import com.loiu92.taiwanmahjong.engine.shortLabel
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors

@Composable
fun MahjongTile(
    tile: Tile,
    selected: Boolean = false,
    faceUp: Boolean = true,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val w = if (compact) 28.dp else 40.dp
    val h = if (compact) 38.dp else 56.dp
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .offset(y = if (selected) (-8).dp else 0.dp)
            .width(w)
            .height(h)
            .background(if (faceUp) MahjongColors.tileFace else MahjongColors.tileBack, shape)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MahjongColors.gold else Color(0xFF9E9E9E),
                shape = shape,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (faceUp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = tile.displayName().substringBefore(" ").take(5),
                    fontSize = if (compact) 7.sp else 8.sp,
                    color = Color(0xFF455A64),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Text(
                    text = tileGlyph(tile),
                    fontSize = if (compact) 14.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = tileColor(tile),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun TileBack(compact: Boolean = true) {
    MahjongTile(
        tile = Tile.characters(1),
        faceUp = false,
        compact = compact,
    )
}

private fun tileGlyph(tile: Tile): String = when (tile) {
    is Tile.SuitTile -> when (tile.suit) {
        Suit.CHARACTERS -> "${tile.rank}万"
        Suit.BAMBOOS -> "${tile.rank}条"
        Suit.DOTS -> "${tile.rank}筒"
    }
    is Tile.Honor -> when {
        tile.wind != null -> {
            val w = tile.wind
            w!!.label
        }
        tile.dragon == Dragon.RED -> "中"
        tile.dragon == Dragon.GREEN -> "發"
        tile.dragon == Dragon.WHITE -> "白"
        else -> tile.shortLabel()
    }
    is Tile.Flower -> "花${tile.number}"
}

private fun tileColor(tile: Tile): Color = when (tile) {
    is Tile.SuitTile -> when (tile.suit) {
        Suit.CHARACTERS -> Color(0xFFB71C1C)
        Suit.BAMBOOS -> Color(0xFF1B5E20)
        Suit.DOTS -> Color(0xFF0D47A0)
    }
    is Tile.Honor -> when {
        tile.dragon == Dragon.RED -> Color(0xFFC62828)
        tile.dragon == Dragon.GREEN -> Color(0xFF2E7D32)
        tile.dragon == Dragon.WHITE -> Color(0xFF37474F)
        else -> Color(0xFF4A148C)
    }
    is Tile.Flower -> Color(0xFFAD1457)
}
