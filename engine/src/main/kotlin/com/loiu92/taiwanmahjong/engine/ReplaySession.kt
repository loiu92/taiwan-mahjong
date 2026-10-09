package com.loiu92.taiwanmahjong.engine

import java.util.Collections

private fun <T> owned(values: List<T>): List<T> = Collections.unmodifiableList(ArrayList(values))

/** Initial conditions are part of the replay protocol; no wall/RNG is sent to a player. */
data class HandSetup(
    val seed: Long,
    val names: List<String> = listOf("You", "YaYa", "Hao", "MeiMei"),
    val humanIndex: Int = 0,
    val chips: List<Int> = List(4) { 72_400 },
    val stake: Int = 50,
    val roundWind: Wind = Wind.EAST,
    val dealer: Int = 0,
    val handNumber: Int = 1,
) {
    init {
        require(names.size == 4 && chips.size == 4)
        require(humanIndex in 0..3 && dealer in 0..3 && stake > 0 && handNumber > 0)
    }
    internal fun snapshot() = copy(names = owned(names), chips = owned(chips))
}

sealed class ReplayCommand {
    data class Player(val player: Int, val intent: PlayerIntent) : ReplayCommand()
    data class BotStep(val player: Int) : ReplayCommand()
    /** Trusted local/server timeout command; never skips unresolved claimants. */
    data object ResolvePasses : ReplayCommand()
}

data class ReplayLog(val version: Int = 1, val setup: HandSetup, val commands: List<ReplayCommand>)

data class VisibleMeld(
    val type: MeldType,
    val tiles: List<Tile>,
    val fromPlayer: Int?,
    val concealed: Boolean,
    val tileCount: Int,
)

data class VisiblePlayer(
    val seat: Wind,
    val name: String,
    val isHuman: Boolean,
    val hand: List<Tile>,
    val concealedCount: Int,
    val melds: List<VisibleMeld>,
    val flowers: List<Tile.Flower>,
    val discards: List<Tile>,
    val chips: Int,
)

/** Transport-safe table projection. Deliberately contains no walls or RNG seed. */
data class PlayerView(
    val viewer: Int,
    val players: List<VisiblePlayer>,
    val roundWind: Wind,
    val dealer: Int,
    val currentPlayer: Int,
    val phase: Phase,
    val lastDiscard: Tile?,
    val lastDiscarder: Int?,
    val pendingClaims: List<ClaimOption>,
    val drawnTile: Tile?,
    val win: WinResult?,
    val isDraw: Boolean,
    val stake: Int,
    val handNumber: Int,
    val wallRemaining: Int,
) {
    fun player(index: Int): VisiblePlayer = players[index]
}

private fun WinResult.snapshot() = copy(
    breakdown = owned(breakdown), payments = Collections.unmodifiableMap(LinkedHashMap(payments)),
)

internal fun GameState.snapshot(): GameState = copy(
    players = owned(players.map { p -> p.copy(
        hand = owned(p.hand), melds = owned(p.melds.map { it.copy(tiles = owned(it.tiles)) }),
        flowers = owned(p.flowers), discards = owned(p.discards),
    ) }),
    liveWall = owned(liveWall), deadWall = owned(deadWall),
    pendingClaims = owned(pendingClaims.map { it.copy(tilesUsed = owned(it.tilesUsed)) }),
    win = win?.snapshot(),
)

fun GameState.viewFor(viewer: Int): PlayerView {
    require(viewer in players.indices)
    return PlayerView(
        viewer = viewer,
        players = owned(players.mapIndexed { index, p -> VisiblePlayer(
            seat = p.seat, name = p.name, isHuman = p.isHuman,
            hand = if (index == viewer) owned(p.hand) else emptyList(),
            concealedCount = p.hand.size,
            melds = owned(p.melds.map { VisibleMeld(
                type = it.type,
                tiles = if (it.concealed && index != viewer) emptyList() else owned(it.tiles),
                fromPlayer = it.fromPlayer, concealed = it.concealed, tileCount = it.tiles.size,
            ) }),
            flowers = owned(p.flowers), discards = owned(p.discards), chips = p.chips,
        ) }),
        roundWind = roundWind, dealer = dealer, currentPlayer = currentPlayer, phase = phase,
        lastDiscard = lastDiscard, lastDiscarder = lastDiscarder,
        pendingClaims = owned(pendingClaims.filter { it.player == viewer }.map {
            it.copy(tilesUsed = owned(it.tilesUsed))
        }),
        drawnTile = drawnTile.takeIf { currentPlayer == viewer },
        win = win?.takeIf { phase == Phase.HAND_OVER }?.snapshot(),
        isDraw = isDraw, stake = stake, handNumber = handNumber, wallRemaining = wallRemaining,
    )
}

/** Every accepted move is journaled. Illegal/stale moves change neither state nor journal. */
class ReplaySession(setup: HandSetup, private val engine: GameEngine = GameEngine()) {
    private val initial = setup.snapshot()
    private val accepted = mutableListOf<ReplayCommand>()
    var state: GameState = engine.newHand(
        names = initial.names, humanIndex = initial.humanIndex, chips = initial.chips,
        stake = initial.stake, roundWind = initial.roundWind, dealer = initial.dealer,
        handNumber = initial.handNumber, seed = initial.seed,
    ).snapshot()
        private set

    fun legal(player: Int): List<PlayerIntent> =
        if (player in 0..3) owned(engine.legalIntents(state, player)) else emptyList()

    fun dispatch(command: ReplayCommand): Boolean {
        val canonical = when (command) {
            is ReplayCommand.BotStep -> {
                if (legal(command.player).isEmpty()) return false
                ReplayCommand.Player(command.player, BotPolicy.choose(state, command.player, engine))
            }
            else -> command
        }
        val next = when (canonical) {
            is ReplayCommand.Player -> {
                if (canonical.intent !in legal(canonical.player)) return false
                engine.apply(state, canonical.player, canonical.intent)
            }
            ReplayCommand.ResolvePasses -> {
                if (state.phase != Phase.AWAITING_CLAIMS || state.pendingClaims.isNotEmpty()) return false
                engine.resolvePasses(state)
            }
            is ReplayCommand.BotStep -> error("Bot command must be normalized")
        }
        if (next == state) return false
        state = next.snapshot()
        accepted.add(canonical)
        return true
    }

    fun viewFor(player: Int): PlayerView = state.viewFor(player)
    fun journal(): ReplayLog = ReplayLog(setup = initial.snapshot(), commands = owned(accepted))

    companion object {
        fun restore(log: ReplayLog): ReplaySession {
            require(log.version == 1) { "Unsupported replay version" }
            val commands = owned(log.commands)
            val session = ReplaySession(log.setup)
            for (command in commands) {
                require(command !is ReplayCommand.BotStep) { "Replay contains noncanonical bot command" }
                require(session.dispatch(command)) { "Replay contains an illegal/stale command" }
            }
            return session
        }
    }
}
