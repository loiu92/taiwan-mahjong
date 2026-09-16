package com.loiu92.taiwanmahjong.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loiu92.taiwanmahjong.engine.ClaimKind
import com.loiu92.taiwanmahjong.engine.GameState
import com.loiu92.taiwanmahjong.engine.LocalSession
import com.loiu92.taiwanmahjong.engine.Phase
import com.loiu92.taiwanmahjong.engine.PlayerIntent
import com.loiu92.taiwanmahjong.engine.Tile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class UiState(
    val screen: Screen = Screen.Home,
    val game: GameState? = null,
    val selectedTile: Tile? = null,
    val autoPlay: Boolean = false,
    val stake: Int = 50,
    val timerSeconds: Int = 30,
    val humanIndex: Int = 0,
)

enum class Screen { Home, Table, Result }

class GameViewModel : ViewModel() {
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var session: LocalSession? = null
    private var loopJob: Job? = null
    private var timerJob: Job? = null

    fun setStake(stake: Int) {
        _ui.update { it.copy(stake = stake) }
    }

    fun startSolo() {
        val stake = _ui.value.stake
        session = LocalSession(stake = stake)
        _ui.update {
            it.copy(
                screen = Screen.Table,
                game = session!!.state,
                selectedTile = null,
                timerSeconds = 30,
            )
        }
        restartTimer()
        kickBots()
    }

    fun selectTile(tile: Tile) {
        val state = _ui.value.game ?: return
        if (state.phase != Phase.AWAITING_DISCARD) return
        if (state.currentPlayer != _ui.value.humanIndex) return
        _ui.update { it.copy(selectedTile = tile) }
    }

    fun discardSelected() {
        val tile = _ui.value.selectedTile ?: return
        humanIntent(PlayerIntent.Discard(tile))
    }

    fun discard(tile: Tile) {
        humanIntent(PlayerIntent.Discard(tile))
    }

    fun claim(kind: ClaimKind) {
        humanIntent(PlayerIntent.Claim(kind))
    }

    fun passClaim() {
        humanIntent(PlayerIntent.Pass)
    }

    fun declareHu() {
        humanIntent(PlayerIntent.DeclareHu)
    }

    fun declareKong() {
        humanIntent(PlayerIntent.DeclareKong)
    }

    fun toggleAuto() {
        _ui.update { it.copy(autoPlay = !it.autoPlay) }
        kickBots()
    }

    fun nextHand() {
        val s = session ?: return
        s.startNextHand()
        _ui.update {
            it.copy(screen = Screen.Table, game = s.state, selectedTile = null, timerSeconds = 30)
        }
        restartTimer()
        kickBots()
    }

    fun backHome() {
        loopJob?.cancel()
        timerJob?.cancel()
        session = null
        _ui.update { UiState(stake = it.stake) }
    }

    private fun humanIntent(intent: PlayerIntent) {
        val s = session ?: return
        val human = _ui.value.humanIndex
        if (_ui.value.autoPlay && intent !is PlayerIntent.Pass && intent !is PlayerIntent.Claim) {
            // auto mode lets bots drive human too
        }
        s.intent(human, intent)
        publish(s)
        if (s.state.phase == Phase.HAND_OVER) {
            _ui.update { it.copy(screen = Screen.Result) }
            return
        }
        restartTimer()
        kickBots()
    }

    private fun publish(s: LocalSession) {
        _ui.update { it.copy(game = s.state, selectedTile = null) }
    }

    private fun kickBots() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            val s = session ?: return@launch
            while (isActive) {
                val state = s.state
                if (state.phase == Phase.HAND_OVER) {
                    _ui.update { it.copy(screen = Screen.Result, game = state) }
                    return@launch
                }
                val human = _ui.value.humanIndex
                val auto = _ui.value.autoPlay

                when (state.phase) {
                    Phase.AWAITING_DISCARD -> {
                        val p = state.currentPlayer
                        if (p == human && !auto) return@launch
                        delay(if (p == human) 400 else 700)
                        s.botMove(p)
                        publish(s)
                        restartTimer()
                    }
                    Phase.AWAITING_CLAIMS -> {
                        val humanBest = state.pendingClaims
                            .filter { it.player == human }
                            .maxOfOrNull { claimPriority(it.kind) } ?: -1
                        val ordered = state.pendingClaims
                            .groupBy { it.player }
                            .mapValues { (_, opts) -> opts.maxOf { claimPriority(it.kind) } }
                            .entries
                            .sortedByDescending { it.value }
                        val next = ordered.firstOrNull { (pid, best) ->
                            (pid != human || auto) && best >= humanBest
                        }?.key
                        if (next == null) return@launch
                        delay(500)
                        s.botMove(next)
                        publish(s)
                        restartTimer()
                    }
                    Phase.HAND_OVER -> return@launch
                }
            }
        }
    }

    private fun claimPriority(kind: ClaimKind): Int = when (kind) {
        ClaimKind.HU -> 4
        ClaimKind.KONG -> 3
        ClaimKind.PONG -> 2
        ClaimKind.CHI -> 1
    }

    private fun restartTimer() {
        timerJob?.cancel()
        _ui.update { it.copy(timerSeconds = 30) }
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val left = _ui.value.timerSeconds - 1
                if (left <= 0) {
                    autoTimeout()
                    return@launch
                }
                _ui.update { it.copy(timerSeconds = left) }
            }
        }
    }

    private fun autoTimeout() {
        val s = session ?: return
        val state = s.state
        val human = _ui.value.humanIndex
        when (state.phase) {
            Phase.AWAITING_DISCARD -> {
                if (state.currentPlayer == human) s.botMove(human)
            }
            Phase.AWAITING_CLAIMS -> {
                if (state.pendingClaims.any { it.player == human }) {
                    s.intent(human, PlayerIntent.Pass)
                }
            }
            else -> return
        }
        publish(s)
        if (s.state.phase == Phase.HAND_OVER) {
            _ui.update { it.copy(screen = Screen.Result) }
        } else {
            restartTimer()
            kickBots()
        }
    }
}
