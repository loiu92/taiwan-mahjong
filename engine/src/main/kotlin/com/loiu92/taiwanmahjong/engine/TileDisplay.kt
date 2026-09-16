package com.loiu92.taiwanmahjong.engine

fun Tile.displayName(): String = when (this) {
    is Tile.SuitTile -> when (suit) {
        Suit.CHARACTERS -> when (rank) {
            1 -> "1 Wan"
            else -> "$rank Wan"
        }
        Suit.BAMBOOS -> "$rank Bamboo"
        Suit.DOTS -> "$rank Dot"
    }
    is Tile.Honor -> when {
        wind != null -> wind.name.lowercase().replaceFirstChar { it.uppercase() }
        dragon == Dragon.RED -> "Red"
        dragon == Dragon.GREEN -> "Green"
        dragon == Dragon.WHITE -> "White"
        else -> id
    }
    is Tile.Flower -> when (kind) {
        FlowerKind.FLOWER -> "Flower $number"
        FlowerKind.SEASON -> "Season $number"
    }
}

fun Tile.shortLabel(): String = when (this) {
    is Tile.SuitTile -> when (suit) {
        Suit.CHARACTERS -> "${rank}W"
        Suit.BAMBOOS -> "${rank}B"
        Suit.DOTS -> "${rank}D"
    }
    is Tile.Honor -> when {
        wind != null -> wind.label
        dragon == Dragon.RED -> "中"
        dragon == Dragon.GREEN -> "發"
        dragon == Dragon.WHITE -> "白"
        else -> "?"
    }
    is Tile.Flower -> "花$number"
}
