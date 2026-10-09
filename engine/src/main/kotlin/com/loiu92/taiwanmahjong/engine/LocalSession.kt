package com.loiu92.taiwanmahjong.engine

/**
 * Trusted local controller interface. Remote delivery must expose PlayerView,
 * never this full-state interface or a replay journal.
 */
interface GameSession {
    val state: GameState
    fun intent(player: Int, intent: PlayerIntent): GameState
    fun legal(player: Int): List<PlayerIntent>
}

class LocalSession(
    names: List<String> = listOf("You", "YaYa", "Hao", "MeiMei"),
    stake: Int = 50,
    seed: Long = System.currentTimeMillis(),
    private val engine: GameEngine = GameEngine(),
) : GameSession {
    private var replay = ReplaySession(HandSetup(seed = seed, names = names, stake = stake), engine)
    override val state: GameState get() = replay.state

    override fun intent(player: Int, intent: PlayerIntent): GameState {
        replay.dispatch(ReplayCommand.Player(player, intent))
        return state
    }

    override fun legal(player: Int): List<PlayerIntent> = replay.legal(player)
    fun viewFor(player: Int): PlayerView = replay.viewFor(player)
    fun journal(): ReplayLog = replay.journal()

    fun botMove(player: Int): GameState {
        replay.dispatch(ReplayCommand.BotStep(player))
        return state
    }

    fun startNextHand(): GameState {
        val (dealer, round) = engine.nextHandDealerAndRound(state)
        val chips = state.players.map { it.chips }
        replay = ReplaySession(HandSetup(
            seed = System.currentTimeMillis(),
            names = state.players.map { it.name },
            humanIndex = state.players.indexOfFirst { it.isHuman }.coerceAtLeast(0),
            chips = chips,
            stake = state.stake,
            roundWind = round,
            dealer = dealer,
            handNumber = state.handNumber + 1,
        ), engine)
        return state
    }
}
