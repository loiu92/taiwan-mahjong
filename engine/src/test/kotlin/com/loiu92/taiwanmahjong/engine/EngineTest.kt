package com.loiu92.taiwanmahjong.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TileTest {
    @Test
    fun fullSetHas144Tiles() {
        assertEquals(144, Tile.fullSet().size)
    }

    @Test
    fun fullSetHas8Flowers() {
        assertEquals(8, Tile.fullSet().filterIsInstance<Tile.Flower>().size)
    }
}

class WinDetectorTest {
    @Test
    fun fiveChowsAndPair() {
        // 123m 456m 789m 123p 456p + 99s  (16 tiles... wait 5*3+2=17 for dealer win shape)
        // Concealed winning hand: 5 sets + pair = 17 tiles if no melds
        val hand = listOf(
            Tile.characters(1), Tile.characters(2), Tile.characters(3),
            Tile.characters(4), Tile.characters(5), Tile.characters(6),
            Tile.characters(7), Tile.characters(8), Tile.characters(9),
            Tile.dots(1), Tile.dots(2), Tile.dots(3),
            Tile.dots(4), Tile.dots(5), Tile.dots(6),
            Tile.bamboos(9), Tile.bamboos(9),
        )
        assertEquals(17, hand.size)
        assertTrue(WinDetector.canWin(hand, emptyList()))
    }

    @Test
    fun withOpenPungNeedsFourteenConcealed() {
        // 4 concealed sets + pair = 14; + 1 open pung
        val meld = Meld(MeldType.PUNG, listOf(Tile.dragon(Dragon.RED), Tile.dragon(Dragon.RED), Tile.dragon(Dragon.RED)))
        val hand = listOf(
            Tile.characters(1), Tile.characters(2), Tile.characters(3),
            Tile.characters(4), Tile.characters(5), Tile.characters(6),
            Tile.bamboos(1), Tile.bamboos(2), Tile.bamboos(3),
            Tile.dots(1), Tile.dots(2), Tile.dots(3),
            Tile.wind(Wind.EAST), Tile.wind(Wind.EAST),
        )
        assertEquals(14, hand.size)
        assertTrue(WinDetector.canWin(hand, listOf(meld)))
    }

    @Test
    fun incompleteHandFails() {
        val hand = listOf(
            Tile.characters(1), Tile.characters(2), Tile.characters(3),
            Tile.characters(4), Tile.characters(5), Tile.characters(6),
            Tile.characters(7), Tile.characters(8), Tile.characters(9),
            Tile.dots(1), Tile.dots(2), Tile.dots(3),
            Tile.dots(4), Tile.dots(5), Tile.dots(6),
            Tile.bamboos(1), Tile.bamboos(3),
        )
        assertFalse(WinDetector.canWin(hand, emptyList()))
    }
}

class GameEngineTest {
    @Test
    fun newHandDealsCorrectSizes() {
        val engine = GameEngine()
        val state = engine.newHand(seed = 42L)
        assertEquals(4, state.players.size)
        // After flower replacement sizes vary; total tiles conserved
        val inHands = state.players.sumOf { it.hand.size + it.flowers.size }
        val inMelds = state.players.sumOf { it.melds.sumOf { m -> m.tiles.size } }
        val total = inHands + inMelds + state.liveWall.size + state.deadWall.size +
            state.players.sumOf { it.discards.size }
        assertEquals(144, total)
        assertEquals(16, state.deadWall.size)
        assertEquals(Phase.AWAITING_DISCARD, state.phase)
        assertEquals(0, state.dealer)
        assertEquals(state.dealer, state.currentPlayer)
        // Dealer starts with one extra tile; flower swaps keep relative +1
        val dealerPlayable = state.player(0).hand.size
        val others = (1..3).map { state.player(it).hand.size }
        assertTrue(others.all { dealerPlayable == it + 1 }, "dealer=$dealerPlayable others=$others")
    }

    @Test
    fun discardAdvancesOrOpensClaims() {
        val engine = GameEngine()
        var state = engine.newHand(seed = 1L)
        val tile = state.player(0).hand.first()
        state = engine.apply(state, 0, PlayerIntent.Discard(tile))
        assertTrue(
            state.phase == Phase.AWAITING_CLAIMS ||
                (state.phase == Phase.AWAITING_DISCARD && state.currentPlayer == 1),
        )
    }

    @Test
    fun botCanPlayFullHandWithoutCrash() {
        val session = LocalSession(seed = 99L)
        var guard = 0
        while (session.state.phase != Phase.HAND_OVER && guard++ < 500) {
            val s = session.state
            when (s.phase) {
                Phase.AWAITING_DISCARD -> session.botMove(s.currentPlayer)
                Phase.AWAITING_CLAIMS -> {
                    val claimants = session.state.pendingClaims.map { it.player }.distinct()
                    if (claimants.isEmpty()) break
                    val c = claimants.first()
                    session.botMove(c)
                }
                Phase.HAND_OVER -> break
            }
        }
        assertTrue(guard < 500, "hand should finish")
        assertTrue(session.state.phase == Phase.HAND_OVER)
    }
}

class TaiScorerTest {
    @Test
    fun selfDrawPaymentsSplitAmongLosers() {
        val engine = GameEngine()
        val state = engine.newHand(stake = 50, seed = 7L)
        val pays = TaiScorer.payments(state, winner = 0, tai = 3, source = WinSource.SELF_DRAW, discarder = null)
        // Winner is dealer: each of 3 losers pays 3*50; no dealer-loser surcharge
        assertEquals(450, pays[0])
        assertEquals(-150, pays[1])
        assertEquals(-150, pays[2])
        assertEquals(-150, pays[3])
    }

    @Test
    fun discardPaymentOnlyFromDiscarder() {
        val engine = GameEngine()
        val state = engine.newHand(stake = 50, seed = 7L)
        val pays = TaiScorer.payments(state, winner = 1, tai = 2, source = WinSource.DISCARD, discarder = 3)
        assertEquals(100, pays[1])
        assertEquals(-100, pays[3])
        assertFalse(pays.containsKey(0))
        assertFalse(pays.containsKey(2))
    }
}
