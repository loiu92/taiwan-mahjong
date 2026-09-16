package com.loiu92.taiwanmahjong.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.loiu92.taiwanmahjong.ui.i18n.strings
import com.loiu92.taiwanmahjong.ui.screens.HomeScreen
import com.loiu92.taiwanmahjong.ui.screens.ResultScreen
import com.loiu92.taiwanmahjong.ui.screens.TableScreen

@Composable
fun MahjongApp(vm: GameViewModel = viewModel()) {
    val state by vm.ui.collectAsState()
    val s = strings(state.lang)
    when (state.screen) {
        Screen.Home -> HomeScreen(
            stake = state.stake,
            lang = state.lang,
            strings = s,
            onStakeChange = vm::setStake,
            onToggleLang = vm::toggleLang,
            onStart = vm::startSolo,
        )
        Screen.Table -> {
            val game = state.game
            if (game == null) {
                HomeScreen(state.stake, state.lang, s, vm::setStake, vm::toggleLang, vm::startSolo)
            } else {
                TableScreen(
                    state = game,
                    selected = state.selectedTile,
                    autoPlay = state.autoPlay,
                    timer = state.timerSeconds,
                    humanIndex = state.humanIndex,
                    strings = s,
                    onSelect = vm::selectTile,
                    onDiscard = vm::discard,
                    onClaim = vm::claim,
                    onPass = vm::passClaim,
                    onHu = vm::declareHu,
                    onKong = vm::declareKong,
                    onToggleAuto = vm::toggleAuto,
                    onToggleLang = vm::toggleLang,
                    onQuit = vm::backHome,
                )
            }
        }
        Screen.Result -> {
            val game = state.game
            if (game == null) {
                HomeScreen(state.stake, state.lang, s, vm::setStake, vm::toggleLang, vm::startSolo)
            } else {
                ResultScreen(
                    state = game,
                    strings = s,
                    onNext = vm::nextHand,
                    onHome = vm::backHome,
                )
            }
        }
    }
}
