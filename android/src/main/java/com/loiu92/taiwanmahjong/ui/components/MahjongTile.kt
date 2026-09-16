package com.loiu92.taiwanmahjong.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.loiu92.taiwanmahjong.R
import com.loiu92.taiwanmahjong.engine.Dragon
import com.loiu92.taiwanmahjong.engine.FlowerKind
import com.loiu92.taiwanmahjong.engine.Suit
import com.loiu92.taiwanmahjong.engine.Tile
import com.loiu92.taiwanmahjong.engine.Wind
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors

private val WAN_NUMERALS = arrayOf("", "一", "二", "三", "四", "伍", "六", "七", "八", "九")

@DrawableRes
private fun faceDrawable(tile: Tile): Int? = when (tile) {
    is Tile.SuitTile -> when (tile.suit) {
        Suit.CHARACTERS -> when (tile.rank) {
            1 -> R.drawable.tile_c1
            2 -> R.drawable.tile_c2
            3 -> R.drawable.tile_c3
            5 -> R.drawable.tile_c5
            9 -> R.drawable.tile_c9
            else -> null
        }
        Suit.BAMBOOS -> when (tile.rank) {
            1 -> R.drawable.tile_b1
            9 -> R.drawable.tile_b9
            else -> null
        }
        Suit.DOTS -> when (tile.rank) {
            1 -> R.drawable.tile_d1
            9 -> R.drawable.tile_d9
            else -> null
        }
    }
    is Tile.Honor -> when {
        tile.wind == Wind.EAST -> R.drawable.tile_we
        tile.wind == Wind.SOUTH -> R.drawable.tile_ws
        tile.wind == Wind.WEST -> R.drawable.tile_ww
        tile.wind == Wind.NORTH -> R.drawable.tile_wn
        tile.dragon == Dragon.RED -> R.drawable.tile_dr
        tile.dragon == Dragon.GREEN -> R.drawable.tile_dg
        tile.dragon == Dragon.WHITE -> R.drawable.tile_dw
        else -> null
    }
    is Tile.Flower -> null
}

@Composable
fun MahjongTile(
    tile: Tile,
    selected: Boolean = false,
    faceUp: Boolean = true,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val w = if (compact) 30.dp else 46.dp
    val h = if (compact) 42.dp else 64.dp
    val shape = RoundedCornerShape(6.dp)
    val bitmap = if (faceUp) faceDrawable(tile) else R.drawable.tile_back

    Box(
        modifier = Modifier
            .offset(y = if (selected) (-10).dp else 0.dp)
            .shadow(
                elevation = if (selected) 8.dp else 3.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.45f),
            )
            .width(w)
            .height(h)
            .clip(shape)
            .background(
                if (faceUp) MahjongColors.tileFace else Color(0xFF1B5E20),
            )
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = when {
                    selected -> MahjongColors.gold
                    faceUp -> MahjongColors.tileFaceEdge
                    else -> Color(0xFF0D3B1E)
                },
                shape = shape,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        if (bitmap != null) {
            Image(
                painter = painterResource(bitmap),
                contentDescription = tile.id,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (faceUp) {
            // Soft top-light bevel
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                    ),
                    cornerRadius = CornerRadius(6f, 6f),
                )
                val inset = size.minDimension * 0.08f
                drawRoundRect(
                    color = Color(0xFFB0A898).copy(alpha = 0.55f),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 1.2f),
                )
                drawTaiwaneseFace(tile)
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
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
    drawNativeText(
        text = WAN_NUMERALS.getOrElse(rank) { "$rank" },
        x = cx,
        y = size.height * 0.34f,
        textSize = size.height * 0.30f,
        color = MahjongColors.tileNumBlue,
        bold = true,
    )
    drawNativeText(
        text = "萬",
        x = cx,
        y = size.height * 0.74f,
        textSize = size.height * 0.38f,
        color = MahjongColors.tileWanRed,
        bold = true,
    )
}

private fun DrawScope.drawHonorChar(ch: String, color: Color) {
    drawNativeText(
        text = ch,
        x = size.width / 2f,
        y = size.height * 0.58f,
        textSize = size.height * 0.58f,
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
        style = Stroke(width = size.minDimension * 0.07f),
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
        y = size.height * 0.58f,
        textSize = size.height * 0.45f,
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
    val stickW = size.width * 0.13f
    val stickH = size.height * 0.26f
    for ((px, py) in positions) {
        val x = size.width * px
        val y = size.height * py
        drawRoundRect(
            color = green,
            topLeft = Offset(x - stickW / 2, y - stickH / 2),
            size = Size(stickW, stickH),
            cornerRadius = CornerRadius(stickW / 2, stickW / 2),
        )
        drawCircle(
            color = Color(0xFF0D3B0D),
            radius = stickW * 0.4f,
            center = Offset(x, y),
            style = Stroke(width = 1.5f),
        )
    }
}

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
    val green = Color(0xFF5B7A4A)
    val cx = size.width * 0.5f
    val cy = size.height * 0.55f
    val body = Path().apply {
        moveTo(cx - size.width * 0.22f, cy)
        quadraticTo(cx, cy - size.height * 0.28f, cx + size.width * 0.28f, cy - size.height * 0.05f)
        quadraticTo(cx + size.width * 0.1f, cy + size.height * 0.22f, cx - size.width * 0.22f, cy)
        close()
    }
    drawPath(body, color = green)
    drawCircle(Color(0xFFC62828), radius = size.minDimension * 0.035f, center = Offset(cx + size.width * 0.28f, cy - size.height * 0.05f))
    drawCircle(MahjongColors.ink, radius = size.minDimension * 0.03f, center = Offset(cx + size.width * 0.08f, cy - size.height * 0.08f))
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
        else if (i % 2 == 0) MahjongColors.tileDotRed else MahjongColors.tileDotGreen
        val c = Offset(size.width * px, size.height * py)
        drawCircle(color, radius = r, center = c)
        drawCircle(Color.White.copy(alpha = 0.35f), radius = r * 0.35f, center = c + Offset(-r * 0.25f, -r * 0.25f))
        if (rank == 1) {
            drawCircle(color, radius = r * 0.55f, center = c, style = Stroke(width = r * 0.12f))
        }
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
