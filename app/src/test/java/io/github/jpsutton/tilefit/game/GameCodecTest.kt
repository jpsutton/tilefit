package io.github.jpsutton.tilefit.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameCodecTest {
    @Test
    fun roundTripsAFreshGame() {
        val s = Game.newGame(Random(9))
        assertEquals(s, GameCodec.decode(GameCodec.encode(s)))
    }

    @Test
    fun roundTripsAMidGame() {
        val random = Random(11)
        var s = Game.newGame(random)
        repeat(7) {
            val slot = s.hand.indexOfFirst { it != null && Game.fitsAnywhere(s.board, it) }
            if (slot < 0) return@repeat
            val p = s.hand[slot]!!
            val (r, c) = (0..SIZE - p.height).flatMap { r -> (0..SIZE - p.width).map { r to it } }
                .first { (r, c) -> Game.fits(s.board, p, r, c) }
            s = Game.place(s, slot, r, c, random)!!.state
        }
        assertEquals(s, GameCodec.decode(GameCodec.encode(s)))
    }

    @Test
    fun rejectsGarbage() {
        val good = GameCodec.encode(Game.newGame(Random(1)))
        assertNull(GameCodec.decode(""))
        assertNull(GameCodec.decode("hello"))
        assertNull(GameCodec.decode(good.replaceFirst("|0|", "|x|")))
        assertNull(GameCodec.decode(good.substringBeforeLast("|") + "|f15,f15"))
        assertNull(GameCodec.decode(good.substringBeforeLast("|") + "|-,-,-"))
        assertNull(GameCodec.decode(good.substringBeforeLast("|") + "|f15,f15,bogus"))
        assertNull(GameCodec.decode("1" + good.drop(1).replaceFirst("0", "9")))
        assertNull(GameCodec.decode(good + "|1"))
        assertNull(GameCodec.decode(good.substringBeforeLast("|") + "|-1"))
    }

    @Test
    fun readsGamesSavedBeforeTonesAndCounters() {
        val board = "1".repeat(9) + "0".repeat(72)
        val s = GameCodec.decode("$board|42|2|10|f15,-,f8")!!
        assertEquals(42, s.score)
        assertEquals(1, s.board.tone(0))
        assertEquals(0, s.groupsCleared)
        assertEquals(2, s.longestStreak)
        assertEquals(0, s.biggestClear)
    }

    @Test
    fun keepsTonesAndCounters() {
        val board = Board.EMPTY.with(listOf(0, 1), 3).with(listOf(80), 6)
        val s = GameState(board, listOf(Pieces.byId("f15"), null, null), 100, 1, 20, groupsCleared = 7, longestStreak = 3, biggestClear = 2)
        val back = GameCodec.decode(GameCodec.encode(s))!!
        assertEquals(s, back)
        assertEquals(3, back.board.tone(1))
        assertEquals(6, back.board.tone(80))
        assertEquals(7, back.groupsCleared)
    }
}
