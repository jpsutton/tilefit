package io.github.jpsutton.tilefit.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {
    private val dot = Pieces.byId("f15")!!
    private val bar3h = Pieces.byId("f8")!!
    private val square2 = Pieces.byId("f5")!!

    private fun board(vararg rows: String): Board {
        require(rows.size == SIZE)
        return Board.parse(rows.joinToString("") { it.replace('.', '0').replace('#', '1') })!!
    }

    private fun state(board: Board, hand: List<Piece?>, streak: Int = 0, score: Int = 0) =
        GameState(board, hand, score, streak, 0)

    private val empty = board(
        ".........", ".........", ".........", ".........", ".........", ".........", ".........", ".........", ".........",
    )

    @Test
    fun newGameHasEmptyBoardAndFullHand() {
        val s = Game.newGame(Random(7))
        assertEquals(Board.EMPTY, s.board)
        assertEquals(Game.HAND, s.hand.size)
        assertTrue(s.hand.all { it != null })
        assertEquals(0, s.score)
        assertFalse(s.isOver)
    }

    @Test
    fun fitsChecksBoundsAndOverlap() {
        assertTrue(Game.fits(empty, bar3h, 0, 0))
        assertTrue(Game.fits(empty, bar3h, 8, 6))
        assertFalse(Game.fits(empty, bar3h, 8, 7))
        assertFalse(Game.fits(empty, bar3h, -1, 0))
        val withDot = empty.with(listOf(cellIndex(4, 4)), true)
        assertFalse(Game.fits(withDot, bar3h, 4, 3))
        assertTrue(Game.fits(withDot, bar3h, 3, 3))
    }

    @Test
    fun placingScoresOnePointPerCell() {
        val s = state(empty, listOf(bar3h, dot, dot))
        val r = Game.place(s, 0, 0, 0)!!
        assertEquals(3, r.state.score)
        assertEquals(0, r.groups)
        assertNull(r.state.hand[0])
        assertEquals(1, r.state.moves)
    }

    @Test
    fun illegalPlacementIsRejected() {
        val s = state(empty, listOf(bar3h, null, dot))
        assertNull(Game.place(s, 0, 8, 8))
        assertNull(Game.place(s, 1, 0, 0)) // used slot
        assertNull(Game.place(s, 5, 0, 0)) // no such slot
    }

    @Test
    fun completingARowClearsIt() {
        val b = board(
            "######...", ".........", ".........", ".........", ".........", ".........", ".........", ".........", ".........",
        )
        val r = Game.place(state(b, listOf(bar3h, dot, dot)), 0, 0, 6)!!
        assertEquals(1, r.groups)
        assertEquals(9, r.cleared.size)
        // the piece's 3 cells are all cleared, so only the 18 for one group counts
        assertEquals(18, r.points)
        assertEquals(1, r.state.streak)
        // the row is gone; nothing else was on the board
        assertEquals(0, r.state.board.count)
    }

    @Test
    fun completingAColumnClearsIt() {
        val bar3v = Pieces.byId("f13")!!
        val rows = Array(SIZE) { r -> if (r < 6) "........#" else "........." }
        val r = Game.place(state(board(*rows), listOf(bar3v, dot, dot)), 0, 6, 8)!!
        assertEquals(1, r.groups)
        assertEquals(0, r.state.board.count)
    }

    @Test
    fun completingABoxClears() {
        val b = board(
            ".........", ".........", ".........", ".........", ".........", ".........", "......###", "......###", ".........",
        )
        val r = Game.place(state(b, listOf(bar3h, dot, dot)), 0, 8, 6)!!
        assertEquals(1, r.groups)
        assertEquals(9, r.cleared.size)
        assertEquals(0, r.state.board.count)
    }

    @Test
    fun rowAndBoxClearTogetherCountTwoGroupsAndLeaveTheRest() {
        // Top row filled except cols 0-2 (box 0 top row); box 0 rows 1-2 filled. Dropping a bar in the
        // top-left completes row 0 and box 0 at once.
        val b = board(
            "...######", "###......", "###......", ".........", ".........", ".........", ".........", ".........", "........#",
        )
        val r = Game.place(state(b, listOf(bar3h, dot, dot)), 0, 0, 0)!!
        assertEquals(2, r.groups)
        assertEquals(15, r.cleared.size) // 9 + 9 - 3 shared
        assertEquals(Game.clearPoints(2), r.points)
        assertEquals(36, r.points)
        // the lone block outside both groups survives
        assertEquals(1, r.state.board.count)
        assertTrue(r.state.board[8, 8])
    }

    @Test
    fun intersectionOfRowAndColumnClearsOnce() {
        // Row 0 and column 0 both complete with the dot at (0,0).
        val rows = Array(SIZE) { r -> if (r == 0) ".########" else "#........" }
        val r = Game.place(state(board(*rows), listOf(dot, dot, dot)), 0, 0, 0)!!
        assertEquals(2, r.groups)
        assertEquals(17, r.cleared.size)
        assertEquals(0, r.state.board.count)
    }

    @Test
    fun clearPointsGrowWithGroups() {
        assertEquals(0, Game.clearPoints(0))
        assertEquals(18, Game.clearPoints(1))
        assertEquals(36, Game.clearPoints(2))
        assertEquals(108, Game.clearPoints(3))
        assertEquals(144, Game.clearPoints(4))
        assertEquals(270, Game.clearPoints(5))
        assertEquals(324, Game.clearPoints(6))
        assertEquals(504, Game.clearPoints(7))
    }

    @Test
    fun streakAddsAFlatBonusFromTheSecondClearingPiece() {
        val b = board(
            "########.", ".........", ".........", ".........", ".........", ".........", ".........", ".........", ".........",
        )
        val first = Game.place(state(b, listOf(dot, dot, dot), streak = 0), 0, 0, 8)!!
        assertEquals(1, first.state.streak)
        assertEquals(18, first.points) // no bonus yet
        val second = Game.place(state(b, listOf(dot, dot, dot), streak = 1), 0, 0, 8)!!
        assertEquals(18 + 9, second.points)
        val clearing = Game.place(state(b, listOf(dot, dot, dot), streak = 2), 0, 0, 8)!!
        assertEquals(3, clearing.state.streak)
        assertEquals(18 + 9, clearing.points) // flat, does not grow with the streak
        val plain = Game.place(state(empty, listOf(dot, dot, dot), streak = 4), 0, 0, 0)!!
        assertEquals(0, plain.state.streak)
        assertEquals(1, plain.points)
    }

    @Test
    fun onlyClearedSquaresOfThePieceLoseTheirPoint() {
        // A vertical bar of 3 completes row 0 with only its top cell; the other 2 survive and score 1 each.
        val bar3v = Pieces.byId("f13")!!
        val rows = Array(SIZE) { r -> if (r == 0) "########." else "........." }
        val r = Game.place(state(board(*rows), listOf(bar3v, dot, dot)), 0, 0, 8)!!
        assertEquals(1, r.groups)
        assertEquals(2 + 18, r.points)
        assertEquals(2, r.state.board.count)
    }

    @Test
    fun theNewHandIsDealtForTheScoreAfterThePlacement() {
        // At 179 the dot takes the score to 180, the first medium step: its hand can hold the scattered pieces
        // that easy hands never do. Over many seeds some hand must contain one.
        val scattered = setOf("f36", "f37", "f41", "f42", "f43", "f44", "f51", "f52")
        val hands = (0 until 200).map { Game.place(state(empty, listOf(null, null, dot), score = 179), 2, 0, 0, Random(it))!! }
        assertTrue(hands.all { it.state.score == 180 })
        assertTrue(hands.any { r -> r.state.hand.any { it!!.id in scattered } })
        val easy = (0 until 200).map { Game.place(state(empty, listOf(null, null, dot), score = 100), 2, 0, 0, Random(it))!! }
        assertTrue(easy.none { r -> r.state.hand.any { it!!.id in scattered } })
    }

    @Test
    fun newHandIsDealtOnlyWhenAllPiecesAreUsed() {
        var s = Game.newGame(Random(3))
        val first = Game.place(s, 0, 0, 0, Random(3))
        // slot 0 may not fit at 0,0 for every piece, so find a legal anchor
        s = first?.state ?: run {
            val p = s.hand[0]!!
            val (r, c) = (0 until SIZE).flatMap { r -> (0 until SIZE).map { r to it } }.first { (r, c) -> Game.fits(s.board, p, r, c) }
            Game.place(s, 0, r, c, Random(3))!!.state
        }
        assertNull(s.hand[0])
        assertNotNull(s.hand[1])
    }

    @Test
    fun lastPieceTriggersDeal() {
        val s = state(empty, listOf(null, null, dot))
        val r = Game.place(s, 2, 0, 0, Random(5))!!
        assertTrue(r.state.hand.all { it != null })
    }

    @Test
    fun clearingPreviewMatchesPlacement() {
        val b = board(
            "######...", ".........", ".........", ".........", ".........", ".........", ".........", ".........", ".........",
        )
        assertEquals(Game.place(state(b, listOf(bar3h, dot, dot)), 0, 0, 6)!!.cleared, Game.clearing(b, bar3h, 0, 6))
        assertTrue(Game.clearing(b, bar3h, 3, 0).isEmpty())
    }

    @Test
    fun gameIsOverWhenNothingFits() {
        // Checkerboard: no cell has an empty neighbour, so only the dot would fit... and it is not in hand.
        val rows = Array(SIZE) { r -> String(CharArray(SIZE) { c -> if ((r + c) % 2 == 0) '#' else '.' }) }
        val cb = board(*rows)
        assertTrue(state(cb, listOf(bar3h, null, square2)).isOver)
        assertFalse(state(cb, listOf(bar3h, null, dot)).isOver)
    }

    @Test
    fun gameEndsAfterTheLastFittingPieceIsPlaced() {
        val rows = Array(SIZE) { r -> String(CharArray(SIZE) { c -> if ((r + c) % 2 == 0) '#' else '.' }) }
        val s = state(board(*rows), listOf(dot, square2, null))
        val r = Game.place(s, 0, 0, 1, Random(1))!!
        assertTrue(r.state.isOver)
    }

    @Test
    fun placedPiecesNeverOverlapOverARandomGame() {
        val random = Random(42)
        var s = Game.newGame(random)
        var moves = 0
        while (!s.isOver && moves < 500) {
            val slot = s.hand.indexOfFirst { it != null && Game.fitsAnywhere(s.board, it) }
            val p = s.hand[slot]!!
            val anchors = (0..SIZE - p.height).flatMap { r -> (0..SIZE - p.width).map { r to it } }
            val (r, c) = anchors.filter { (r, c) -> Game.fits(s.board, p, r, c) }.random(random)
            val before = s.board.count
            val res = Game.place(s, slot, r, c, random)!!
            assertEquals(before + p.size - res.cleared.size, res.state.board.count)
            s = res.state
            moves++
        }
        assertTrue(s.isOver)
    }

    @Test
    fun placedBlocksKeepThePieceTone() {
        val r = Game.place(state(empty, listOf(square2, dot, dot)), 0, 0, 0)!!
        assertEquals(Tones.SQUARE, r.state.board.tone(cellIndex(1, 1)))
        assertEquals(0, r.state.board.tone(cellIndex(2, 2)))
    }

    @Test
    fun countersTrackClearsAndStreaks() {
        val nearlyFullRow = board(
            "########.", ".........", ".........", ".........", ".........", ".........", ".........", ".........", "########.",
        )
        var s = state(nearlyFullRow, listOf(dot, dot, bar3h))
        s = Game.place(s, 0, 0, 8)!!.state
        assertEquals(1, s.groupsCleared)
        assertEquals(1, s.longestStreak)
        assertEquals(1, s.biggestClear)
        s = Game.place(s, 1, 8, 8)!!.state
        assertEquals(2, s.groupsCleared)
        assertEquals(2, s.longestStreak)
        s = Game.place(s, 2, 4, 0)!!.state
        assertEquals(0, s.streak)
        assertEquals(2, s.longestStreak)
    }

    @Test
    fun outcomeMatchesPlace() {
        val s = state(empty, listOf(bar3h, dot, dot), streak = 1)
        val o = Game.outcome(s, bar3h, 2, 3)!!
        val r = Game.place(s, 0, 2, 3)!!
        assertEquals(r.points, o.points)
        assertEquals(r.cleared, o.cleared)
        assertNull(Game.outcome(s, bar3h, 8, 7))
    }

    @Test
    fun everyPieceHasATone() {
        assertTrue(Pieces.all.all { it.tone in 1..Tones.COUNT })
    }
}
