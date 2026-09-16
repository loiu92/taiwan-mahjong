package com.loiu92.taiwanmahjong.engine

/**
 * Abstraction for local vs future remote (Cloudflare DO) sessions.
 */
interface GameSession {
    val state: GameState
    fun intent(player: Int, intent: PlayerIntent): GameState
    fun legal(player: Int): List<PlayerIntent>
}

class LocalSession(
    names: List<String> = listOf("You", "MeiMei", "YaYa", "Hao"),
    stake: Int = 50,
    seed: Long = System.currentTimeMillis(),
    private val engine: GameEngine = GameEngine(),
) : GameSession {
    override var state: GameState = engine.newHand(names = names, stake = stake, seed = seed)
        private set

    override fun intent(player: Int, intent: PlayerIntent): GameState {
        state = engine.apply(state, player, intent)
        return state
    }

    override fun legal(player: Int): List<PlayerIntent> = engine.legalIntents(state, player)

    fun botMove(player: Int): GameState {
        val choice = BotPolicy.choose(state, player, engine)
        return intent(player, choice)
    }

    fun startNextHand(): GameState {
        val (dealer, round) = engine.nextHandDealerAndRound(state)
        val chips = state.players.map { it.chips }
        state = engine.newHand(
            names = state.players.map { it.name },
            humanIndex = state.players.indexOfFirst { it.isHuman }.coerceAtLeast(0),
            chips = chips,
            stake = state.stake,
            roundWind = round,
            dealer = dealer,
            handNumber = state.handNumber + 1,
        )
        return state
    }
}
