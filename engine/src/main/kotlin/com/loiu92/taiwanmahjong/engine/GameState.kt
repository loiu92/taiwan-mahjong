package com.loiu92.taiwanmahjong.engine

enum class MeldType { CHOW, PUNG, KONG }

data class Meld(
    val type: MeldType,
    val tiles: List<Tile>,
    val fromPlayer: Int? = null,
    val concealed: Boolean = false,
) {
    init {
        when (type) {
            MeldType.CHOW -> require(tiles.size == 3)
            MeldType.PUNG -> require(tiles.size == 3)
            MeldType.KONG -> require(tiles.size == 4)
        }
    }
}

enum class Phase {
    /** Waiting for current player to discard (has drawn / dealer start). */
    AWAITING_DISCARD,
    /** Waiting for claims on the last discard. */
    AWAITING_CLAIMS,
    /** Hand finished. */
    HAND_OVER,
}

enum class WinSource {
    SELF_DRAW,
    DISCARD,
    ROB_KONG,
    REPLACEMENT,
}

data class WinResult(
    val winner: Int,
    val source: WinSource,
    val discarder: Int? = null,
    val winningTile: Tile,
    val tai: Int,
    val breakdown: List<TaiLine>,
    val payments: Map<Int, Int>,
)

data class TaiLine(val label: String, val tai: Int)

data class PlayerState(
    val seat: Wind,
    val name: String,
    val isHuman: Boolean,
    val hand: List<Tile> = emptyList(),
    val melds: List<Meld> = emptyList(),
    val flowers: List<Tile.Flower> = emptyList(),
    val discards: List<Tile> = emptyList(),
    val chips: Int = 72_400,
) {
    fun concealedCount(): Int = hand.size
}

data class ClaimOption(
    val player: Int,
    val kind: ClaimKind,
    val tilesUsed: List<Tile> = emptyList(),
)

enum class ClaimKind { HU, KONG, PONG, CHI }

/**
 * Immutable snapshot of a hand in progress.
 * Walls are lists of face-down tiles; index 0 is the draw end of the live wall.
 * Dead wall is separate (16 tiles); replacements taken from front; replenished from live tail.
 */
data class GameState(
    val players: List<PlayerState>,
    val liveWall: List<Tile>,
    val deadWall: List<Tile>,
    val roundWind: Wind,
    val dealer: Int,
    val currentPlayer: Int,
    val phase: Phase,
    val lastDiscard: Tile? = null,
    val lastDiscarder: Int? = null,
    val pendingClaims: List<ClaimOption> = emptyList(),
    val claimDeadlineMs: Long? = null,
    val drawnTile: Tile? = null,
    val win: WinResult? = null,
    val isDraw: Boolean = false,
    val stake: Int = 50,
    val handNumber: Int = 1,
    val rngSeed: Long = 0L,
) {
    val wallRemaining: Int get() = liveWall.size

    fun player(i: Int): PlayerState = players[i]

    fun seatOf(i: Int): Wind = players[i].seat
}
