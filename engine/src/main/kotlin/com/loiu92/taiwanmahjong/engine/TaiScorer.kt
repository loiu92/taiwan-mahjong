package com.loiu92.taiwanmahjong.engine

object TaiScorer {

    fun score(
        state: GameState,
        winner: Int,
        source: WinSource,
        winningTile: Tile,
        alreadyInHand: Boolean,
    ): Pair<Int, List<TaiLine>> {
        val player = state.player(winner)
        val hand = if (alreadyInHand) player.hand else player.hand + winningTile
        val flowers = player.flowers
        val lines = mutableListOf<TaiLine>()

        // Limit hands
        if (flowers.size == 8) {
            lines += TaiLine("All flowers/seasons", 30)
            return 30 to lines
        }
        if (flowers.size == 7 && winningTile is Tile.Flower) {
            lines += TaiLine("Seven flowers rob eighth", 20)
            return 20 to lines
        }

        val decomp = WinDetector.decompose(hand, player.melds)
            ?: return 0 to emptyList()

        // Flowers
        if (flowers.isEmpty()) {
            lines += TaiLine("No flowers", 1)
        } else {
            lines += TaiLine("Flowers ×${flowers.size}", flowers.size)
            for (f in flowers) {
                if (f.associatedWind() == player.seat) {
                    lines += TaiLine("Seat flower ${f.id}", 1)
                }
            }
        }

        when (source) {
            WinSource.SELF_DRAW, WinSource.REPLACEMENT -> lines += TaiLine("Self-draw", 1)
            WinSource.ROB_KONG -> {
                lines += TaiLine("Rob kong", 1)
                lines += TaiLine("Self-draw", 1)
            }
            WinSource.DISCARD -> Unit
        }

        val allMelds: List<MeldInfo> = decomp.openMelds.map { MeldInfo.from(it) } +
            decomp.concealedSets.map { MeldInfo.fromConcealed(it) }

        val openCount = player.melds.size
        if (openCount == 0 && source != WinSource.DISCARD) {
            // Fully concealed with self-draw
            lines += TaiLine("Fully concealed", 1)
        } else if (openCount == 0) {
            lines += TaiLine("Concealed hand", 1)
        }

        // Honor / wind pungs
        for (m in allMelds) {
            when {
                m.isDragonPung -> lines += TaiLine("Dragon pung", 1)
                m.isWindPung -> {
                    val w = m.wind!!
                    if (w == player.seat) lines += TaiLine("Seat wind", 1)
                    if (w == state.roundWind) lines += TaiLine("Round wind", 1)
                    if (w != player.seat && w != state.roundWind) {
                        // plain wind pung: 0 in many TW tables; skip
                    }
                }
            }
        }

        val suitMelds = allMelds.filter { it.suit != null }
        val honorMelds = allMelds.filter { it.isHonor }
        val pairIsHonor = decomp.pair is Tile.Honor

        // All pungs
        if (allMelds.all { it.isPungOrKong }) {
            lines += TaiLine("All pungs", 4)
        }

        // Flush
        val suits = suitMelds.mapNotNull { it.suit }.toSet() +
            listOfNotNull((decomp.pair as? Tile.SuitTile)?.suit)
        when {
            suits.size == 1 && honorMelds.isEmpty() && !pairIsHonor ->
                lines += TaiLine("Full flush", 8)
            suits.size == 1 && (honorMelds.isNotEmpty() || pairIsHonor) ->
                lines += TaiLine("Half flush", 4)
        }

        // Dragons
        val dragonPungs = allMelds.count { it.isDragonPung }
        when (dragonPungs) {
            3 -> lines += TaiLine("Big three dragons", 8)
            2 -> {
                if (decomp.pair is Tile.Honor && decomp.pair.dragon != null) {
                    lines += TaiLine("Little three dragons", 4)
                }
            }
        }

        // Winds
        val windPungs = allMelds.count { it.isWindPung }
        when {
            windPungs == 4 -> lines += TaiLine("Big four winds", 16)
            windPungs == 3 && decomp.pair is Tile.Honor && decomp.pair.wind != null ->
                lines += TaiLine("Little four winds", 8)
        }

        if (allMelds.all { it.isHonor } && pairIsHonor) {
            lines += TaiLine("All honors", 8)
        }

        // Kongs
        for (m in player.melds.filter { it.type == MeldType.KONG }) {
            if (m.concealed) lines += TaiLine("Concealed kong", 1)
        }

        // Dealer bonus
        if (winner == state.dealer) {
            lines += TaiLine("Dealer", 1)
        }

        val total = lines.sumOf { it.tai }.coerceAtLeast(1)
        return total to lines
    }

    fun payments(
        state: GameState,
        winner: Int,
        tai: Int,
        source: WinSource,
        discarder: Int?,
    ): Map<Int, Int> {
        val amount = tai * state.stake
        val result = mutableMapOf<Int, Int>()
        when (source) {
            WinSource.DISCARD -> {
                val d = discarder ?: return emptyMap()
                result[d] = -amount
                result[winner] = amount
            }
            else -> {
                var gained = 0
                for (i in state.players.indices) {
                    if (i == winner) continue
                    var pay = amount
                    // Dealer pays/receives extra: already in tai when winner is dealer;
                    // when loser is dealer, +1 tai worth
                    if (i == state.dealer) pay += state.stake
                    result[i] = -pay
                    gained += pay
                }
                result[winner] = gained
            }
        }
        return result
    }
}

private data class MeldInfo(
    val isPungOrKong: Boolean,
    val isHonor: Boolean,
    val isDragonPung: Boolean,
    val isWindPung: Boolean,
    val wind: Wind?,
    val suit: Suit?,
) {
    companion object {
        fun from(m: Meld): MeldInfo {
            val t = m.tiles.first()
            return of(t, m.type != MeldType.CHOW)
        }

        fun fromConcealed(tiles: List<Tile>): MeldInfo {
            val t = tiles.first()
            val isPung = tiles.all { it == t }
            return of(t, isPung)
        }

        private fun of(t: Tile, isPungOrKong: Boolean): MeldInfo = MeldInfo(
            isPungOrKong = isPungOrKong,
            isHonor = t is Tile.Honor,
            isDragonPung = t is Tile.Honor && t.dragon != null && isPungOrKong,
            isWindPung = t is Tile.Honor && t.wind != null && isPungOrKong,
            wind = (t as? Tile.Honor)?.wind,
            suit = (t as? Tile.SuitTile)?.suit,
        )
    }
}
