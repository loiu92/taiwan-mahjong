package com.loiu92.taiwanmahjong.engine

import kotlin.random.Random

sealed class PlayerIntent {
    data class Discard(val tile: Tile) : PlayerIntent()
    data class Claim(val kind: ClaimKind) : PlayerIntent()
    data object Pass : PlayerIntent()
    data object DeclareHu : PlayerIntent()
    data object DeclareKong : PlayerIntent() // concealed or from hand after draw
}

class GameEngine(private var random: Random = Random.Default) {

    fun newHand(
        names: List<String> = listOf("You", "MeiMei", "YaYa", "Hao"),
        humanIndex: Int = 0,
        chips: List<Int> = List(4) { 72_400 },
        stake: Int = 50,
        roundWind: Wind = Wind.EAST,
        dealer: Int = 0,
        handNumber: Int = 1,
        seed: Long = Random.Default.nextLong(),
    ): GameState {
        random = Random(seed)
        val tiles = Tile.fullSet().shuffled(random).toMutableList()
        // Build walls: 144 tiles → live + dead(16)
        val dead = tiles.takeLast(16)
        val live = tiles.dropLast(16).toMutableList()

        // Seat winds: dealer is East
        val seats = Array(4) { Wind.EAST }
        for (i in 0..3) {
            seats[(dealer + i) % 4] = Wind.fromIndex(i)
        }

        val hands = List(4) { mutableListOf<Tile>() }
        // Deal 4 rounds of 4 tiles
        repeat(4) {
            for (p in 0..3) {
                val idx = (dealer + p) % 4
                repeat(4) { hands[idx] += live.removeAt(0) }
            }
        }
        // Dealer extra
        hands[dealer] += live.removeAt(0)

        var players = names.mapIndexed { i, name ->
            PlayerState(
                seat = seats[i],
                name = name,
                isHuman = i == humanIndex,
                hand = hands[i].sorted(),
                chips = chips[i],
            )
        }

        var state = GameState(
            players = players,
            liveWall = live,
            deadWall = dead,
            roundWind = roundWind,
            dealer = dealer,
            currentPlayer = dealer,
            phase = Phase.AWAITING_DISCARD,
            stake = stake,
            handNumber = handNumber,
            rngSeed = seed,
        )

        // Replace initial flowers (dealer first, then others counterclockwise)
        state = replaceInitialFlowers(state)
        // Flower replacement draws may move currentPlayer — restore dealer to discard first
        state = state.copy(currentPlayer = dealer, phase = Phase.AWAITING_DISCARD)
        val dealerHand = state.player(dealer).hand
        state = state.copy(drawnTile = dealerHand.lastOrNull())
        return state
    }

    fun apply(state: GameState, player: Int, intent: PlayerIntent): GameState {
        return when (state.phase) {
            Phase.AWAITING_DISCARD -> applyDiscardPhase(state, player, intent)
            Phase.AWAITING_CLAIMS -> applyClaimPhase(state, player, intent)
            Phase.HAND_OVER -> state
        }
    }

    /** Advance after all claims passed or timed out — draw for next player. */
    fun resolvePasses(state: GameState): GameState {
        if (state.phase != Phase.AWAITING_CLAIMS) return state
        val next = (state.lastDiscarder!! + 1) % 4
        return drawFor(state.copy(pendingClaims = emptyList(), lastDiscard = null, lastDiscarder = null), next)
    }

    fun legalIntents(state: GameState, player: Int): List<PlayerIntent> {
        return when (state.phase) {
            Phase.HAND_OVER -> emptyList()
            Phase.AWAITING_DISCARD -> {
                if (player != state.currentPlayer) return emptyList()
                val p = state.player(player)
                val intents = mutableListOf<PlayerIntent>()
                if (WinDetector.canWin(p.hand, p.melds, p.flowers)) {
                    intents += PlayerIntent.DeclareHu
                }
                if (canConcealedOrAddKong(state, player)) {
                    intents += PlayerIntent.DeclareKong
                }
                p.hand.distinct().forEach { intents += PlayerIntent.Discard(it) }
                intents
            }
            Phase.AWAITING_CLAIMS -> {
                val options = state.pendingClaims.filter { it.player == player }
                if (options.isEmpty()) return listOf(PlayerIntent.Pass)
                options.map { PlayerIntent.Claim(it.kind) } + PlayerIntent.Pass
            }
        }
    }

    // --- internals ---

    private fun applyDiscardPhase(state: GameState, player: Int, intent: PlayerIntent): GameState {
        if (player != state.currentPlayer) return state
        val p = state.player(player)
        return when (intent) {
            is PlayerIntent.DeclareHu -> {
                if (!WinDetector.canWin(p.hand, p.melds, p.flowers)) return state
                finishWin(state, player, WinSource.SELF_DRAW, null, state.drawnTile ?: p.hand.last(), true)
            }
            is PlayerIntent.DeclareKong -> tryKong(state, player)
            is PlayerIntent.Discard -> discard(state, player, intent.tile)
            else -> state
        }
    }

    private fun applyClaimPhase(state: GameState, player: Int, intent: PlayerIntent): GameState {
        val myOptions = state.pendingClaims.filter { it.player == player }
        if (myOptions.isEmpty() && intent == PlayerIntent.Pass) {
            return if (state.pendingClaims.isEmpty()) resolvePasses(state) else state
        }
        return when (intent) {
            PlayerIntent.Pass -> {
                val remaining = state.pendingClaims.filter { it.player != player }
                val next = state.copy(pendingClaims = remaining)
                if (remaining.isEmpty()) resolvePasses(next) else next
            }
            is PlayerIntent.Claim -> {
                val option = myOptions.firstOrNull { it.kind == intent.kind } ?: return state
                val priority = claimPriority(intent.kind)
                val better = state.pendingClaims.any {
                    it.player != player && claimPriority(it.kind) > priority
                }
                if (better) state else applyClaim(state, player, option)
            }
            else -> state
        }
    }

    private fun claimPriority(kind: ClaimKind): Int = when (kind) {
        ClaimKind.HU -> 4
        ClaimKind.KONG -> 3
        ClaimKind.PONG -> 2
        ClaimKind.CHI -> 1
    }

    private fun applyClaim(state: GameState, player: Int, option: ClaimOption): GameState {
        val discard = state.lastDiscard ?: return state
        val discarder = state.lastDiscarder ?: return state
        return when (option.kind) {
            ClaimKind.HU -> finishWin(state, player, WinSource.DISCARD, discarder, discard, false)
            ClaimKind.PONG -> meldFromDiscard(state, player, discarder, discard, MeldType.PUNG, 2)
            ClaimKind.KONG -> {
                val s = meldFromDiscard(state, player, discarder, discard, MeldType.KONG, 3)
                drawReplacement(s, player)
            }
            ClaimKind.CHI -> {
                val used = option.tilesUsed
                require(used.size == 2)
                var s = removeFromHand(state, player, used)
                s = s.copy(
                    players = s.players.mapIndexed { i, pl ->
                        when (i) {
                            player -> pl.copy(
                                melds = pl.melds + Meld(MeldType.CHOW, (used + discard).sorted(), discarder),
                            )
                            discarder -> pl.copy(discards = pl.discards.dropLast(1))
                            else -> pl
                        }
                    },
                    pendingClaims = emptyList(),
                    lastDiscard = null,
                    lastDiscarder = null,
                    currentPlayer = player,
                    phase = Phase.AWAITING_DISCARD,
                    drawnTile = null,
                )
                s
            }
        }
    }

    private fun meldFromDiscard(
        state: GameState,
        player: Int,
        discarder: Int,
        discard: Tile,
        type: MeldType,
        fromHand: Int,
    ): GameState {
        val hand = state.player(player).hand.toMutableList()
        repeat(fromHand) {
            val idx = hand.indexOf(discard)
            require(idx >= 0)
            hand.removeAt(idx)
        }
        val tiles = List(fromHand) { discard } + discard
        val meld = Meld(type, tiles, discarder, concealed = false)
        return state.copy(
            players = state.players.mapIndexed { i, pl ->
                when (i) {
                    player -> pl.copy(hand = hand.sorted(), melds = pl.melds + meld)
                    discarder -> pl.copy(discards = pl.discards.dropLast(1))
                    else -> pl
                }
            },
            pendingClaims = emptyList(),
            lastDiscard = null,
            lastDiscarder = null,
            currentPlayer = player,
            phase = Phase.AWAITING_DISCARD,
            drawnTile = null,
        )
    }

    private fun discard(state: GameState, player: Int, tile: Tile): GameState {
        val hand = state.player(player).hand.toMutableList()
        val idx = hand.indexOf(tile)
        if (idx < 0) return state
        hand.removeAt(idx)
        var s = state.copy(
            players = state.players.mapIndexed { i, pl ->
                if (i == player) pl.copy(hand = hand.sorted(), discards = pl.discards + tile)
                else pl
            },
            drawnTile = null,
            lastDiscard = tile,
            lastDiscarder = player,
        )
        val claims = findClaims(s, player, tile)
        return if (claims.isEmpty()) {
            val next = (player + 1) % 4
            drawFor(s.copy(lastDiscard = null, lastDiscarder = null), next)
        } else {
            s.copy(phase = Phase.AWAITING_CLAIMS, pendingClaims = claims)
        }
    }

    private fun findClaims(state: GameState, discarder: Int, tile: Tile): List<ClaimOption> {
        val options = mutableListOf<ClaimOption>()
        for (i in state.players.indices) {
            if (i == discarder) continue
            val p = state.player(i)
            // Hu
            if (WinDetector.isWinningHand(p.hand, p.melds, p.flowers, tile, alreadyInHand = false)) {
                options += ClaimOption(i, ClaimKind.HU)
            }
            // Kong / Pong
            val count = p.hand.count { it == tile }
            if (count >= 3) options += ClaimOption(i, ClaimKind.KONG)
            if (count >= 2) options += ClaimOption(i, ClaimKind.PONG)
            // Chi — only next player
            if (i == (discarder + 1) % 4 && tile is Tile.SuitTile) {
                for (combo in chiCombos(p.hand, tile)) {
                    options += ClaimOption(i, ClaimKind.CHI, combo)
                }
            }
        }
        return options
    }

    private fun chiCombos(hand: List<Tile>, tile: Tile.SuitTile): List<List<Tile>> {
        val r = tile.rank
        val suit = tile.suit
        fun has(rank: Int) = hand.any { it is Tile.SuitTile && it.suit == suit && it.rank == rank }
        fun get(rank: Int) = Tile.SuitTile(suit, rank)
        val combos = mutableListOf<List<Tile>>()
        // tile is middle
        if (r in 2..8 && has(r - 1) && has(r + 1)) combos += listOf(get(r - 1), get(r + 1))
        // tile is high
        if (r >= 3 && has(r - 2) && has(r - 1)) combos += listOf(get(r - 2), get(r - 1))
        // tile is low
        if (r <= 7 && has(r + 1) && has(r + 2)) combos += listOf(get(r + 1), get(r + 2))
        return combos
    }

    private fun drawFor(state: GameState, player: Int): GameState {
        if (state.liveWall.isEmpty()) {
            return state.copy(phase = Phase.HAND_OVER, isDraw = true, currentPlayer = player)
        }
        var live = state.liveWall.toMutableList()
        var dead = state.deadWall.toMutableList()
        val drawn = live.removeAt(0)
        var s = state.copy(
            liveWall = live,
            currentPlayer = player,
            phase = Phase.AWAITING_DISCARD,
            drawnTile = drawn,
            players = state.players.mapIndexed { i, pl ->
                if (i == player) pl.copy(hand = (pl.hand + drawn).sorted()) else pl
            },
        )
        // Flower replacement loop
        s = replaceFlowersFor(s, player)
        return s
    }

    private fun replaceInitialFlowers(state: GameState): GameState {
        var s = state
        val order = (0..3).map { (state.dealer + it) % 4 }
        for (p in order) {
            s = replaceFlowersFor(s, p)
        }
        return s
    }

    private fun replaceFlowersFor(state: GameState, player: Int): GameState {
        var s = state
        var guard = 0
        while (guard++ < 20) {
            val flowers = s.player(player).hand.filterIsInstance<Tile.Flower>()
            if (flowers.isEmpty()) break
            for (f in flowers) {
                s = exposeFlower(s, player, f)
                s = drawReplacement(s, player)
            }
        }
        return s
    }

    private fun exposeFlower(state: GameState, player: Int, flower: Tile.Flower): GameState {
        val hand = state.player(player).hand.toMutableList()
        hand.remove(flower)
        return state.copy(
            players = state.players.mapIndexed { i, pl ->
                if (i == player) pl.copy(hand = hand.sorted(), flowers = pl.flowers + flower)
                else pl
            },
        )
    }

    private fun drawReplacement(state: GameState, player: Int): GameState {
        if (state.deadWall.isEmpty() || state.liveWall.isEmpty()) {
            return state.copy(phase = Phase.HAND_OVER, isDraw = true)
        }
        var dead = state.deadWall.toMutableList()
        var live = state.liveWall.toMutableList()
        val drawn = dead.removeAt(0)
        // replenish dead wall from live tail
        if (live.isNotEmpty()) {
            dead += live.removeAt(live.lastIndex)
        }
        var s = state.copy(
            deadWall = dead,
            liveWall = live,
            drawnTile = drawn,
            players = state.players.mapIndexed { i, pl ->
                if (i == player) pl.copy(hand = (pl.hand + drawn).sorted()) else pl
            },
            currentPlayer = player,
            phase = Phase.AWAITING_DISCARD,
        )
        if (drawn is Tile.Flower) {
            s = exposeFlower(s, player, drawn)
            s = drawReplacement(s, player)
        }
        return s
    }

    private fun canConcealedOrAddKong(state: GameState, player: Int): Boolean {
        val p = state.player(player)
        val counts = p.hand.groupingBy { it }.eachCount()
        if (counts.any { it.value >= 4 }) return true
        // Add to open pung
        return p.melds.any { m ->
            m.type == MeldType.PUNG && p.hand.contains(m.tiles.first())
        }
    }

    private fun tryKong(state: GameState, player: Int): GameState {
        val p = state.player(player)
        val counts = p.hand.groupingBy { it }.eachCount()
        val quad = counts.entries.firstOrNull { it.value >= 4 }?.key
        if (quad != null) {
            var hand = p.hand.toMutableList()
            repeat(4) { hand.remove(quad) }
            val meld = Meld(MeldType.KONG, List(4) { quad }, concealed = true)
            val s = state.copy(
                players = state.players.mapIndexed { i, pl ->
                    if (i == player) pl.copy(hand = hand.sorted(), melds = pl.melds + meld) else pl
                },
            )
            return drawReplacement(s, player)
        }
        val pung = p.melds.firstOrNull { m ->
            m.type == MeldType.PUNG && p.hand.contains(m.tiles.first())
        } ?: return state
        val tile = pung.tiles.first()
        val hand = p.hand.toMutableList()
        hand.remove(tile)
        val newMelds = p.melds.map {
            if (it == pung) Meld(MeldType.KONG, List(4) { tile }, it.fromPlayer, concealed = false)
            else it
        }
        val s = state.copy(
            players = state.players.mapIndexed { i, pl ->
                if (i == player) pl.copy(hand = hand.sorted(), melds = newMelds) else pl
            },
        )
        return drawReplacement(s, player)
    }

    private fun removeFromHand(state: GameState, player: Int, tiles: List<Tile>): GameState {
        val hand = state.player(player).hand.toMutableList()
        for (t in tiles) {
            val idx = hand.indexOf(t)
            require(idx >= 0)
            hand.removeAt(idx)
        }
        return state.copy(
            players = state.players.mapIndexed { i, pl ->
                if (i == player) pl.copy(hand = hand.sorted()) else pl
            },
        )
    }

    private fun finishWin(
        state: GameState,
        winner: Int,
        source: WinSource,
        discarder: Int?,
        winningTile: Tile,
        alreadyInHand: Boolean,
    ): GameState {
        val (tai, breakdown) = TaiScorer.score(state, winner, source, winningTile, alreadyInHand)
        val pays = TaiScorer.payments(state, winner, tai, source, discarder)
        val win = WinResult(winner, source, discarder, winningTile, tai, breakdown, pays)
        val players = state.players.mapIndexed { i, pl ->
            pl.copy(chips = pl.chips + (pays[i] ?: 0))
        }
        return state.copy(
            players = players,
            phase = Phase.HAND_OVER,
            win = win,
            pendingClaims = emptyList(),
        )
    }

    fun nextHandDealerAndRound(state: GameState): Pair<Int, Wind> {
        if (state.isDraw || state.win?.winner == state.dealer) {
            return state.dealer to state.roundWind
        }
        val newDealer = (state.dealer + 1) % 4
        // Round advances when original east of the round would become dealer again —
        // simplify: advance round wind each time dealer wraps past hand start seat 0's first dealer.
        // Practical: when newDealer == 0 after leaving non-zero, bump round. Good enough for solo:
        val newRound = if (newDealer == 0) state.roundWind.next() else state.roundWind
        return newDealer to newRound
    }
}
