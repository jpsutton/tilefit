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
        assertNull(GameCodec.decode(good.substringBeforeLast("|") + "|dot-0,dot-0"))
        assertNull(GameCodec.decode(good.substringBeforeLast("|") + "|-,-,-"))
        assertNull(GameCodec.decode(good.substringBeforeLast("|") + "|dot-0,dot-0,bogus"))
        assertNull(GameCodec.decode("1" + good.drop(1).replaceFirst("0", "2")))
    }
}
