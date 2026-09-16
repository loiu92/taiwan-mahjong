package com.loiu92.taiwanmahjong.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.loiu92.taiwanmahjong.engine.Dragon
import com.loiu92.taiwanmahjong.engine.FlowerKind
import com.loiu92.taiwanmahjong.engine.Suit
import com.loiu92.taiwanmahjong.engine.Tile
import com.loiu92.taiwanmahjong.engine.Wind
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors

/** Traditional Chinese numerals used on Taiwanese 萬 tiles (伍 for 5). */
private val WAN_NUMERALS = arrayOf("", "一", "二", "三", "四", "伍", "六", "七", "八", "九")

@Composable
fun MahjongTile(
    tile: Tile,
    selected: Boolean = false,
    faceUp: Boolean = true,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val w = if (compact) 30.dp else 44.dp
    val h = if (compact) 42.dp else 62.dp
    val shape = RoundedCornerShape(5.dp)
    Box(
        modifier = Modifier
            .offset(y = if (selected) (-10).dp else 0.dp)
            .width(w)
            .height(h)
            .background(
                if (faceUp) MahjongColors.tileFace else MahjongColors.tileBack,
                shape,
            )
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = when {
                    selected -> MahjongColors.acidYellow
                    faceUp -> MahjongColors.tileFaceEdge
                    else -> Color(0xFF7F0000)
                },
                shape = shape,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        if (faceUp) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawTaiwaneseFace(tile)
            }
        } else {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Red back with subtle diamond
                val pad = size.minDimension * 0.18f
                drawRoundRect(
                    color = Color(0xFF8B0000),
                    topLeft = Offset(pad, pad),
                    size = Size(size.width - pad * 2, size.height - pad * 2),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 2f),
                )
            }
        }
    }
}

@Composable
fun TileBack(compact: Boolean = true) {
    MahjongTile(tile = Tile.characters(1), faceUp = false, compact = compact)
}

private fun DrawScope.drawTaiwaneseFace(tile: Tile) {
    when (tile) {
        is Tile.SuitTile -> when (tile.suit) {
            Suit.CHARACTERS -> drawWan(tile.rank)
            Suit.BAMBOOS -> drawBamboo(tile.rank)
            Suit.DOTS -> drawDots(tile.rank)
        }
        is Tile.Honor -> {
            val wind = tile.wind
            val dragon = tile.dragon
            when {
                wind != null -> drawHonorChar(
                    when (wind) {
                        Wind.EAST -> "東"
                        Wind.SOUTH -> "南"
                        Wind.WEST -> "西"
                        Wind.NORTH -> "北"
                    },
                    MahjongColors.ink,
                )
                dragon == Dragon.RED -> drawHonorChar("中", MahjongColors.tileWanRed)
                dragon == Dragon.GREEN -> drawHonorChar("發", MahjongColors.tileBamboo)
                dragon == Dragon.WHITE -> drawWhiteDragon()
            }
        }
        is Tile.Flower -> drawFlower(tile)
    }
}

private fun DrawScope.drawWan(rank: Int) {
    val cx = size.width / 2f
    // Blue numeral (top)
    drawNativeText(
        text = WAN_NUMERALS.getOrElse(rank) { "$rank" },
        x = cx,
        y = size.height * 0.32f,
        textSize = size.height * 0.28f,
        color = MahjongColors.tileNumBlue,
        bold = true,
    )
    // Red 萬 (bottom)
    drawNativeText(
        text = "萬",
        x = cx,
        y = size.height * 0.72f,
        textSize = size.height * 0.36f,
        color = MahjongColors.tileWanRed,
        bold = true,
    )
}

private fun DrawScope.drawHonorChar(ch: String, color: Color) {
    drawNativeText(
        text = ch,
        x = size.width / 2f,
        y = size.height * 0.55f,
        textSize = size.height * 0.55f,
        color = color,
        bold = true,
    )
}

private fun DrawScope.drawWhiteDragon() {
    val pad = size.minDimension * 0.18f
    drawRoundRect(
        color = MahjongColors.ink,
        topLeft = Offset(pad, pad),
        size = Size(size.width - pad * 2, size.height - pad * 2),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(width = size.minDimension * 0.06f),
    )
}

private fun DrawScope.drawFlower(tile: Tile.Flower) {
    val labels = when (tile.kind) {
        FlowerKind.FLOWER -> arrayOf("", "梅", "蘭", "竹", "菊")
        FlowerKind.SEASON -> arrayOf("", "春", "夏", "秋", "冬")
    }
    drawNativeText(
        text = labels.getOrElse(tile.number) { "花" },
        x = size.width / 2f,
        y = size.height * 0.55f,
        textSize = size.height * 0.42f,
        color = Color(0xFFAD1457),
        bold = true,
    )
}

private fun DrawScope.drawBamboo(rank: Int) {
    if (rank == 1) {
        drawSparrow()
        return
    }
    val green = MahjongColors.tileBamboo
    val positions = bambooLayout(rank)
    val stickW = size.width * 0.12f
    val stickH = size.height * 0.28f
    for ((px, py) in positions) {
        val x = size.width * px
        val y = size.height * py
        drawRoundRect(
            color = green,
            topLeft = Offset(x - stickW / 2, y - stickH / 2),
            size = Size(stickW, stickH),
            cornerRadius = CornerRadius(stickW / 2, stickW / 2),
        )
        // node ring
        drawCircle(
            color = Color(0xFF0D3B0D),
            radius = stickW * 0.45f,
            center = Offset(x, y),
            style = Stroke(width = 1.5f),
        )
    }
}

/** Relative (x,y) centers for bamboo sticks 2–9. */
private fun bambooLayout(rank: Int): List<Pair<Float, Float>> = when (rank) {
    2 -> listOf(0.5f to 0.32f, 0.5f to 0.68f)
    3 -> listOf(0.5f to 0.28f, 0.32f to 0.68f, 0.68f to 0.68f)
    4 -> listOf(0.32f to 0.32f, 0.68f to 0.32f, 0.32f to 0.68f, 0.68f to 0.68f)
    5 -> listOf(0.32f to 0.28f, 0.68f to 0.28f, 0.5f to 0.5f, 0.32f to 0.72f, 0.68f to 0.72f)
    6 -> listOf(
        0.32f to 0.28f, 0.68f to 0.28f,
        0.32f to 0.5f, 0.68f to 0.5f,
        0.32f to 0.72f, 0.68f to 0.72f,
    )
    7 -> listOf(
        0.5f to 0.22f,
        0.28f to 0.42f, 0.5f to 0.42f, 0.72f to 0.42f,
        0.28f to 0.72f, 0.5f to 0.72f, 0.72f to 0.72f,
    )
    8 -> listOf(
        0.28f to 0.26f, 0.5f to 0.26f, 0.72f to 0.26f,
        0.38f to 0.5f, 0.62f to 0.5f,
        0.28f to 0.74f, 0.5f to 0.74f, 0.72f to 0.74f,
    )
    9 -> listOf(
        0.28f to 0.24f, 0.5f to 0.24f, 0.72f to 0.24f,
        0.28f to 0.5f, 0.5f to 0.5f, 0.72f to 0.5f,
        0.28f to 0.76f, 0.5f to 0.76f, 0.72f to 0.76f,
    )
    else -> listOf(0.5f to 0.5f)
}

private fun DrawScope.drawSparrow() {
    val green = MahjongColors.tileBamboo
    val cx = size.width * 0.5f
    val cy = size.height * 0.55f
    val body = Path().apply {
        moveTo(cx - size.width * 0.22f, cy)
        quadraticBezierTo(cx, cy - size.height * 0.28f, cx + size.width * 0.28f, cy - size.height * 0.05f)
        quadraticBezierTo(cx + size.width * 0.1f, cy + size.height * 0.22f, cx - size.width * 0.22f, cy)
        close()
    }
    drawPath(body, color = green)
    // beak
    drawCircle(Color(0xFFC62828), radius = size.minDimension * 0.04f, center = Offset(cx + size.width * 0.28f, cy - size.height * 0.05f))
    // eye
    drawCircle(MahjongColors.ink, radius = size.minDimension * 0.035f, center = Offset(cx + size.width * 0.08f, cy - size.height * 0.08f))
    // simple wing line (Taiwan black, not blue)
    drawLine(
        color = MahjongColors.ink,
        start = Offset(cx - size.width * 0.05f, cy),
        end = Offset(cx + size.width * 0.12f, cy + size.height * 0.08f),
        strokeWidth = 2f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawDots(rank: Int) {
    val positions = dotLayout(rank)
    val r = size.minDimension * when {
        rank == 1 -> 0.22f
        rank <= 4 -> 0.11f
        else -> 0.09f
    }
    positions.forEachIndexed { i, (px, py) ->
        val color = if (rank == 1) MahjongColors.tileDotRed
        else if ((i % 2 == 0)) MahjongColors.tileDotRed else MahjongColors.tileDotGreen
        val c = Offset(size.width * px, size.height * py)
        drawCircle(color, radius = r, center = c)
        drawCircle(Color.White.copy(alpha = 0.35f), radius = r * 0.35f, center = c + Offset(-r * 0.25f, -r * 0.25f))
    }
}

private fun dotLayout(rank: Int): List<Pair<Float, Float>> = when (rank) {
    1 -> listOf(0.5f to 0.5f)
    2 -> listOf(0.5f to 0.30f, 0.5f to 0.70f)
    3 -> listOf(0.5f to 0.28f, 0.32f to 0.70f, 0.68f to 0.70f)
    4 -> listOf(0.32f to 0.30f, 0.68f to 0.30f, 0.32f to 0.70f, 0.68f to 0.70f)
    5 -> listOf(0.30f to 0.28f, 0.70f to 0.28f, 0.5f to 0.5f, 0.30f to 0.72f, 0.70f to 0.72f)
    6 -> listOf(
        0.30f to 0.26f, 0.70f to 0.26f,
        0.30f to 0.5f, 0.70f to 0.5f,
        0.30f to 0.74f, 0.70f to 0.74f,
    )
    7 -> listOf(
        0.5f to 0.22f,
        0.28f to 0.42f, 0.5f to 0.42f, 0.72f to 0.42f,
        0.28f to 0.72f, 0.5f to 0.72f, 0.72f to 0.72f,
    )
    8 -> listOf(
        0.28f to 0.24f, 0.5f to 0.24f, 0.72f to 0.24f,
        0.38f to 0.5f, 0.62f to 0.5f,
        0.28f to 0.76f, 0.5f to 0.76f, 0.72f to 0.76f,
    )
    9 -> listOf(
        0.28f to 0.24f, 0.5f to 0.24f, 0.72f to 0.24f,
        0.28f to 0.5f, 0.5f to 0.5f, 0.72f to 0.5f,
        0.28f to 0.76f, 0.5f to 0.76f, 0.72f to 0.76f,
    )
    else -> listOf(0.5f to 0.5f)
}

private fun DrawScope.drawNativeText(
    text: String,
    x: Float,
    y: Float,
    textSize: Float,
    color: Color,
    bold: Boolean,
) {
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        this.textSize = textSize
        this.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt(),
        )
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = if (bold) {
            android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
        } else {
            android.graphics.Typeface.SERIF
        }
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}
