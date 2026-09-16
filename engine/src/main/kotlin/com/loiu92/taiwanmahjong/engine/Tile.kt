package com.loiu92.taiwanmahjong.engine

/** Seat order counterclockwise from dealer perspective of play: East → South → West → North. */
enum class Wind(val label: String, val index: Int) {
    EAST("E", 0),
    SOUTH("S", 1),
    WEST("W", 2),
    NORTH("N", 3);

    fun next(): Wind = entries[(index + 1) % 4]

    companion object {
        fun fromIndex(i: Int): Wind = entries[i % 4]
    }
}

enum class Suit { CHARACTERS, BAMBOOS, DOTS }

sealed class Tile : Comparable<Tile> {
    abstract val id: String
    abstract val sortKey: Int

    data class SuitTile(val suit: Suit, val rank: Int) : Tile() {
        init {
            require(rank in 1..9)
        }

        override val id: String = when (suit) {
            Suit.CHARACTERS -> "C$rank"
            Suit.BAMBOOS -> "B$rank"
            Suit.DOTS -> "D$rank"
        }

        override val sortKey: Int = when (suit) {
            Suit.CHARACTERS -> rank
            Suit.BAMBOOS -> 10 + rank
            Suit.DOTS -> 20 + rank
        }

        override fun toString(): String = id
    }

    data class Honor(val wind: Wind? = null, val dragon: Dragon? = null) : Tile() {
        init {
            require((wind != null) xor (dragon != null))
        }

        override val id: String = when {
            wind != null -> wind.name
            else -> dragon!!.name
        }

        override val sortKey: Int = when {
            wind != null -> 30 + wind.index
            else -> 40 + dragon!!.ordinal
        }

        override fun toString(): String = id
    }

    data class Flower(val kind: FlowerKind, val number: Int) : Tile() {
        init {
            require(number in 1..4)
        }

        override val id: String = "${kind.name}$number"
        override val sortKey: Int = 50 + kind.ordinal * 4 + number
        override fun toString(): String = id

        fun associatedWind(): Wind = Wind.fromIndex(number - 1)
    }

    override fun compareTo(other: Tile): Int = sortKey.compareTo(other.sortKey)

    companion object {
        fun characters(rank: Int) = SuitTile(Suit.CHARACTERS, rank)
        fun bamboos(rank: Int) = SuitTile(Suit.BAMBOOS, rank)
        fun dots(rank: Int) = SuitTile(Suit.DOTS, rank)
        fun wind(w: Wind) = Honor(wind = w)
        fun dragon(d: Dragon) = Honor(dragon = d)
        fun flower(kind: FlowerKind, n: Int) = Flower(kind, n)

        /** Full 144-tile Taiwanese set. */
        fun fullSet(): List<Tile> {
            val tiles = mutableListOf<Tile>()
            for (suit in Suit.entries) {
                for (rank in 1..9) {
                    repeat(4) { tiles += SuitTile(suit, rank) }
                }
            }
            for (w in Wind.entries) repeat(4) { tiles += Honor(wind = w) }
            for (d in Dragon.entries) repeat(4) { tiles += Honor(dragon = d) }
            for (n in 1..4) {
                tiles += Flower(FlowerKind.FLOWER, n)
                tiles += Flower(FlowerKind.SEASON, n)
            }
            return tiles
        }
    }
}

enum class Dragon { RED, GREEN, WHITE }

enum class FlowerKind { FLOWER, SEASON }

fun Tile.isFlower(): Boolean = this is Tile.Flower
fun Tile.isHonor(): Boolean = this is Tile.Honor
fun Tile.isSuit(): Boolean = this is Tile.SuitTile
