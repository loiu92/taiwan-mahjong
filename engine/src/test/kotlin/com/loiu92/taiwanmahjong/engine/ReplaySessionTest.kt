package com.loiu92.taiwanmahjong.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReplaySessionTest {
    @Test
    fun equalSeedsAndSetupProduceEqualInitialState() {
        val setup = HandSetup(seed = 42L)
        assertEquals(ReplaySession(setup).state, ReplaySession(setup).state)
        assertFalse(ReplaySession(setup).state == ReplaySession(setup.copy(seed = 43L)).state)
        assertTrue(ReplaySession(setup).journal().commands.isEmpty())
    }

    @Test
    fun nonDefaultSetupSurvivesReplay() {
        val setup = HandSetup(
            seed = 81L,
            names = listOf("A", "B", "C", "D"),
            humanIndex = 2,
            chips = listOf(100, 200, 300, 400),
            stake = 5,
            roundWind = Wind.WEST,
            dealer = 3,
            handNumber = 7,
        )
        val session = ReplaySession(setup)
        assertEquals(setup.names, session.state.players.map { it.name })
        assertEquals(setup.chips, session.state.players.map { it.chips })
        assertEquals(listOf(false, false, true, false), session.state.players.map { it.isHuman })
        assertEquals(3, session.state.currentPlayer)
        assertEquals(Wind.WEST, session.state.roundWind)
        assertEquals(5, session.state.stake)
        assertEquals(7, session.state.handNumber)
        assertEquals(session.state, ReplaySession.restore(session.journal()).state)
    }

    @Test
    fun fullHandsReplayEveryAcceptedBotTransition() {
        for (seed in listOf(1L, 42L, 99L)) {
            val session = ReplaySession(HandSetup(seed = seed))
            var moves = 0
            while (session.state.phase != Phase.HAND_OVER && moves < 500) {
                val command = botCommand(session.state)
                val chosen = BotPolicy.choose(session.state, command.player, GameEngine())
                assertTrue(session.dispatch(command), "seed=$seed move=$moves")
                moves++
                assertEquals(moves, session.journal().commands.size)
                assertEquals(ReplayCommand.Player(command.player, chosen), session.journal().commands.last())
                if (moves % 10 == 0) {
                    assertEquals(session.state, ReplaySession.restore(session.journal()).state)
                }
            }
            assertTrue(moves < 500, "seed=$seed should terminate")
            assertEquals(Phase.HAND_OVER, session.state.phase)
            val restored = ReplaySession.restore(session.journal())
            assertEquals(session.state, restored.state)
            assertEquals(session.journal(), restored.journal())
            for (seat in 0..3) assertEquals(session.viewFor(seat), restored.viewFor(seat))
        }
    }

    @Test
    fun explicitPlayerCommandsReplayTogetherWithBotSteps() {
        val session = ReplaySession(HandSetup(seed = 42L))
        val original = session.state
        val discard = PlayerIntent.Discard(original.player(original.currentPlayer).hand.first())
        val first = ReplayCommand.Player(original.currentPlayer, discard)
        assertTrue(session.dispatch(first))
        repeat(12) {
            if (session.state.phase != Phase.HAND_OVER) {
                assertTrue(session.dispatch(botCommand(session.state)))
            }
        }
        assertEquals(first, session.journal().commands.first())
        assertTrue(session.journal().commands.all { it is ReplayCommand.Player })
        assertEquals(session.state, ReplaySession.restore(session.journal()).state)
    }

    @Test
    fun invalidPlayerCommandsNeitherChangeStateNorEnterJournal() {
        val session = ReplaySession(HandSetup(seed = 42L))
        val before = session.state
        val current = before.currentPlayer
        val absentTile = Tile.fullSet().first { it !in before.player(current).hand }
        val invalid = listOf(
            ReplayCommand.Player((current + 1) % 4, PlayerIntent.Discard(before.player(current).hand.first())),
            ReplayCommand.Player(current, PlayerIntent.Discard(absentTile)),
            ReplayCommand.Player(current, PlayerIntent.Pass),
            ReplayCommand.Player(current, PlayerIntent.Claim(ClaimKind.HU)),
            ReplayCommand.Player(-1, PlayerIntent.Pass),
            ReplayCommand.Player(4, PlayerIntent.Pass),
            ReplayCommand.BotStep(-1),
            ReplayCommand.BotStep(4),
            ReplayCommand.BotStep((current + 1) % 4),
            ReplayCommand.ResolvePasses,
        )
        for (command in invalid) {
            assertFalse(session.dispatch(command), "$command must be rejected")
            assertEquals(before, session.state)
            assertTrue(session.journal().commands.isEmpty())
        }
        assertTrue(session.legal(-1).isEmpty())
        assertTrue(session.legal(4).isEmpty())
    }

    @Test
    fun rejectedClaimAndPrematureResolutionPreserveClaimWindow() {
        val session = claimWindow()
        val before = session.state
        val log = session.journal()
        val discarder = assertNotNull(before.lastDiscarder)
        assertFalse(session.dispatch(ReplayCommand.Player(discarder, PlayerIntent.Claim(ClaimKind.HU))))
        assertFalse(session.dispatch(ReplayCommand.ResolvePasses))
        assertEquals(before, session.state)
        assertEquals(log, session.journal())
    }

    @Test
    fun finishedHandRejectsFurtherCommandsWithoutGrowingJournal() {
        val session = ReplaySession(HandSetup(seed = 99L))
        finishWithBots(session)
        val finalState = session.state
        val log = session.journal()
        for (seat in 0..3) {
            assertFalse(session.dispatch(ReplayCommand.BotStep(seat)))
            assertFalse(session.dispatch(ReplayCommand.Player(seat, PlayerIntent.Pass)))
        }
        assertFalse(session.dispatch(ReplayCommand.ResolvePasses))
        assertEquals(finalState, session.state)
        assertEquals(log, session.journal())
    }

    @Test
    fun callerOwnedSetupListsCannotAlterSessionOrSavedReplay() {
        val names = mutableListOf("A", "B", "C", "D")
        val chips = mutableListOf(100, 200, 300, 400)
        val session = ReplaySession(HandSetup(seed = 7L, names = names, chips = chips))
        val before = session.state
        names[0] = "tampered"
        chips[0] = -1000
        assertEquals(listOf("A", "B", "C", "D"), session.journal().setup.names)
        assertEquals(listOf(100, 200, 300, 400), session.journal().setup.chips)
        assertEquals(before, ReplaySession.restore(session.journal()).state)
    }

    @Test
    fun exportedJournalCannotMutateOwnedCommandsOrSetup() {
        val session = ReplaySession(HandSetup(seed = 7L))
        assertTrue(session.dispatch(botCommand(session.state)))
        val expectedState = session.state
        val expectedCommands = session.journal().commands.toList()
        val expectedNames = session.journal().setup.names.toList()
        val expectedChips = session.journal().setup.chips.toList()
        val exported = session.journal()
        tryClear(exported.commands)
        tryClear(exported.setup.names)
        tryClear(exported.setup.chips)
        assertEquals(expectedCommands, session.journal().commands)
        assertEquals(expectedNames, session.journal().setup.names)
        assertEquals(expectedChips, session.journal().setup.chips)
        assertEquals(expectedState, ReplaySession.restore(session.journal()).state)
    }

    @Test
    fun restoreDoesNotRetainCallerOwnedLogLists() {
        val original = ReplaySession(HandSetup(seed = 7L))
        assertTrue(original.dispatch(botCommand(original.state)))
        val commands = original.journal().commands.toMutableList()
        val names = original.journal().setup.names.toMutableList()
        val chips = original.journal().setup.chips.toMutableList()
        val input = original.journal().copy(
            setup = original.journal().setup.copy(names = names, chips = chips),
            commands = commands,
        )
        val restored = ReplaySession.restore(input)
        commands.clear()
        names.clear()
        chips.clear()
        assertEquals(original.state, restored.state)
        assertEquals(original.journal(), restored.journal())
    }

    @Test
    fun unsupportedVersionsAndRejectedJournalCommandsFailRestore() {
        val valid = ReplaySession(HandSetup(seed = 42L)).journal()
        for (version in listOf(-1, 0, 2, Int.MAX_VALUE)) {
            assertFailsWith<IllegalArgumentException> { ReplaySession.restore(valid.copy(version = version)) }
        }
        for (command in listOf(
            ReplayCommand.Player(-1, PlayerIntent.Pass),
            ReplayCommand.Player(4, PlayerIntent.Pass),
            ReplayCommand.Player(0, PlayerIntent.Pass),
            ReplayCommand.BotStep(0),
            ReplayCommand.ResolvePasses,
        )) {
            assertFailsWith<IllegalArgumentException> {
                ReplaySession.restore(valid.copy(commands = listOf(command)))
            }
        }
    }

    @Test
    fun malformedSetupFailsBeforeReplay() {
        val valid = ReplaySession(HandSetup(seed = 42L)).journal()
        val malformed = listOf(
            { valid.setup.copy(names = listOf("only one")) },
            { valid.setup.copy(chips = listOf(10)) },
            { valid.setup.copy(humanIndex = -1) },
            { valid.setup.copy(humanIndex = 4) },
            { valid.setup.copy(dealer = -1) },
            { valid.setup.copy(dealer = 4) },
            { valid.setup.copy(stake = 0) },
            { valid.setup.copy(handNumber = 0) },
        )
        for (makeSetup in malformed) {
            assertFailsWith<IllegalArgumentException> { ReplaySession.restore(valid.copy(setup = makeSetup())) }
        }
    }

    @Test
    fun everySeatSeesOnlyItsOwnHandAndDrawnTile() {
        val session = ReplaySession(HandSetup(seed = 42L))
        for (viewer in 0..3) {
            val view = session.viewFor(viewer)
            assertEquals(viewer, view.viewer)
            assertEquals(session.state.wallRemaining, view.wallRemaining)
            assertEquals(session.state.roundWind, view.roundWind)
            assertEquals(session.state.phase, view.phase)
            assertEquals(4, view.players.size)
            for (seat in 0..3) {
                val trusted = session.state.player(seat)
                val visible = view.player(seat)
                assertEquals(trusted.name, visible.name)
                assertEquals(trusted.chips, visible.chips)
                assertEquals(trusted.hand.size, visible.concealedCount)
                assertEquals(if (seat == viewer) trusted.hand else emptyList(), visible.hand)
                assertEquals(trusted.flowers, visible.flowers)
                assertEquals(trusted.discards, visible.discards)
            }
            assertEquals(if (viewer == session.state.currentPlayer) session.state.drawnTile else null, view.drawnTile)
            assertEquals(null, view.win)
        }
        assertFailsWith<IllegalArgumentException> { session.viewFor(-1) }
        assertFailsWith<IllegalArgumentException> { session.viewFor(4) }
    }

    @Test
    fun claimProjectionDoesNotExposeOtherPlayersChoices() {
        val session = claimWindow()
        assertTrue(session.state.pendingClaims.isNotEmpty())
        for (viewer in 0..3) {
            val view = session.viewFor(viewer)
            assertEquals(session.state.pendingClaims.filter { it.player == viewer }, view.pendingClaims)
            assertTrue(view.pendingClaims.all { it.player == viewer })
            assertEquals(session.state.lastDiscard, view.lastDiscard)
            assertEquals(session.state.lastDiscarder, view.lastDiscarder)
            assertEquals(null, view.drawnTile)
        }
    }

    @Test
    fun concealedKongFacesRemainPrivateWhileCountsArePublic() {
        val session = concealedKongSession()
        val owner = session.state.currentPlayer
        assertTrue(session.dispatch(ReplayCommand.Player(owner, PlayerIntent.DeclareKong)))
        val index = session.state.player(owner).melds.indexOfFirst { it.concealed }
        assertTrue(index >= 0)
        val trusted = session.state.player(owner).melds[index]
        for (viewer in 0..3) {
            val meld = session.viewFor(viewer).player(owner).melds[index]
            assertEquals(MeldType.KONG, meld.type)
            assertTrue(meld.concealed)
            assertEquals(4, meld.tileCount)
            assertEquals(if (viewer == owner) trusted.tiles else emptyList(), meld.tiles)
        }
        assertEquals(session.state, ReplaySession.restore(session.journal()).state)
    }

    @Test
    fun constructedProjectionPreservesOpenMeldsAndHidesOnlyOtherConcealedFaces() {
        val initial = GameEngine().newHand(seed = 42L)
        val concealed = Meld(MeldType.KONG, List(4) { Tile.characters(7) }, concealed = true)
        val open = Meld(MeldType.PUNG, List(3) { Tile.dragon(Dragon.RED) }, fromPlayer = 2)
        val state = initial.copy(players = initial.players.mapIndexed { seat, player ->
            if (seat == 1) player.copy(melds = listOf(concealed, open)) else player
        })
        for (viewer in 0..3) {
            val melds = state.viewFor(viewer).player(1).melds
            assertEquals(2, melds.size)
            assertEquals(if (viewer == 1) concealed.tiles else emptyList(), melds[0].tiles)
            assertEquals(4, melds[0].tileCount)
            assertEquals(open.tiles, melds[1].tiles)
            assertEquals(open.fromPlayer, melds[1].fromPlayer)
            assertEquals(3, melds[1].tileCount)
        }
    }

    @Test
    fun projectionHasNoTrustedStateWallOrRandomSeedFields() {
        val view = ReplaySession(HandSetup(seed = 42L)).viewFor(0)
        val fields = view.javaClass.declaredFields.map { it.name }.toSet()
        assertTrue(fields.intersect(setOf("state", "liveWall", "deadWall", "rngSeed", "setup", "claimDeadlineMs")).isEmpty())
        assertTrue(view.javaClass.declaredFields.none {
            it.type == GameState::class.java || it.type == HandSetup::class.java
        })
        assertTrue(view.players.all { player ->
            player.javaClass.declaredFields.none { it.type == PlayerState::class.java }
        })
    }

    @Test
    fun playerProjectionCannotAlterTrustedLists() {
        val session = claimWindow()
        val before = ReplaySession.restore(session.journal()).state
        val viewer = session.state.pendingClaims.first().player
        val view = session.viewFor(viewer)
        tryClear(view.player(viewer).hand)
        tryClear(view.player(viewer).flowers)
        tryClear(view.player(viewer).discards)
        tryClear(view.pendingClaims)
        assertEquals(before, session.state)
        assertEquals(before, ReplaySession.restore(session.journal()).state)
    }

    @Test
    fun signedSettlementBalancesSurviveNextHandAndReplay() {
        val session = LocalSession(stake = 40_000, seed = 3L)
        var steps = 0
        while (session.state.phase != Phase.HAND_OVER && steps++ < 500) {
            session.botMove(botCommand(session.state).player)
        }
        assertEquals(Phase.HAND_OVER, session.state.phase)
        val balances = session.state.players.map { it.chips }
        assertTrue(balances.any { it < 0 }, "Fixture must exercise a negative settlement balance")
        assertEquals(session.state, ReplaySession.restore(session.journal()).state)
        val next = session.startNextHand()
        assertEquals(Phase.AWAITING_DISCARD, next.phase)
        assertEquals(balances, next.players.map { it.chips })
        assertEquals(balances, session.journal().setup.chips)
        assertEquals(next, ReplaySession.restore(session.journal()).state)
    }

    private fun botCommand(state: GameState): ReplayCommand.BotStep {
        val actor = if (state.phase == Phase.AWAITING_CLAIMS) {
            state.pendingClaims.maxBy { it.kind.priority() }.player
        } else state.currentPlayer
        return ReplayCommand.BotStep(actor)
    }

    private fun ClaimKind.priority(): Int = when (this) {
        ClaimKind.HU -> 4
        ClaimKind.KONG -> 3
        ClaimKind.PONG -> 2
        ClaimKind.CHI -> 1
    }

    private fun finishWithBots(session: ReplaySession) {
        var steps = 0
        while (session.state.phase != Phase.HAND_OVER && steps++ < 500) {
            assertTrue(session.dispatch(botCommand(session.state)))
        }
        assertEquals(Phase.HAND_OVER, session.state.phase)
    }

    private fun claimWindow(): ReplaySession {
        for (seed in 1L..100L) {
            val session = ReplaySession(HandSetup(seed = seed))
            repeat(20) {
                if (session.state.phase == Phase.AWAITING_CLAIMS) return session
                if (session.state.phase != Phase.HAND_OVER) {
                    assertTrue(session.dispatch(botCommand(session.state)))
                }
            }
        }
        error("Deterministic fixture search must find a claim window")
    }

    private fun concealedKongSession(): ReplaySession {
        val engine = GameEngine()
        for (seed in 1L..10_000L) {
            val state = engine.newHand(seed = seed)
            if (state.player(state.currentPlayer).hand.groupingBy { it }.eachCount().any { it.value == 4 }) {
                return ReplaySession(HandSetup(seed = seed))
            }
        }
        error("Deterministic fixture search must find a concealed kong")
    }

    private fun <T> tryClear(list: List<T>) {
        try {
            (list as? MutableList<T>)?.clear()
        } catch (_: UnsupportedOperationException) {
            // Both immutable exports and mutable defensive copies honor ownership.
        }
    }
}
