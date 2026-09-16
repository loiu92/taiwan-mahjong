package com.loiu92.taiwanmahjong.engine

/**
 * Heuristic bot: prefer hu, then safe/improving discards, call pong/chi when it helps.
 */
object BotPolicy {

    fun choose(state: GameState, player: Int, engine: GameEngine): PlayerIntent {
        val legal = engine.legalIntents(state, player)
        if (legal.isEmpty()) return PlayerIntent.Pass

        legal.filterIsInstance<PlayerIntent.DeclareHu>().firstOrNull()?.let { return it }
        legal.filterIsInstance<PlayerIntent.Claim>().firstOrNull { it.kind == ClaimKind.HU }?.let { return it }

        if (state.phase == Phase.AWAITING_CLAIMS) {
            val claims = legal.filterIsInstance<PlayerIntent.Claim>()
            claims.firstOrNull { it.kind == ClaimKind.KONG }?.let { return it }
            claims.firstOrNull { it.kind == ClaimKind.PONG }?.let {
                // Pong honors / terminals more eagerly
                val tile = state.lastDiscard
                if (tile != null && (tile.isHonor() || isTerminal(tile))) return it
                // otherwise sometimes pass
                if (state.player(player).hand.size <= 8) return it
            }
            claims.firstOrNull { it.kind == ClaimKind.CHI }?.let {
                if (state.player(player).hand.size <= 10) return it
            }
            return PlayerIntent.Pass
        }

        legal.filterIsInstance<PlayerIntent.DeclareKong>().firstOrNull()?.let { return it }

        val discards = legal.filterIsInstance<PlayerIntent.Discard>()
        if (discards.isEmpty()) return PlayerIntent.Pass

        val hand = state.player(player).hand
        // Prefer discarding isolated honors / singles far from sets
        val scored = discards.map { d ->
            d to discardScore(hand, d.tile)
        }
        return scored.maxBy { it.second }.first
    }

    private fun isTerminal(tile: Tile): Boolean =
        tile is Tile.SuitTile && (tile.rank == 1 || tile.rank == 9)

    private fun discardScore(hand: List<Tile>, tile: Tile): Int {
        var score = 0
        val count = hand.count { it == tile }
        if (count == 1) score += 5
        if (tile.isHonor()) score += 4
        if (tile is Tile.SuitTile) {
            val neighbors = hand.count {
                it is Tile.SuitTile && it.suit == tile.suit && kotlin.math.abs(it.rank - tile.rank) <= 2
            }
            score += (3 - neighbors).coerceAtLeast(0)
            if (tile.rank in 3..7) score -= 2
        }
        return score
    }
}
