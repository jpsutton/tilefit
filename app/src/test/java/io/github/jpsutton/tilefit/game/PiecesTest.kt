package io.github.jpsutton.tilefit.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PiecesTest {
    private fun dealt(difficulty: Int, draws: Int = 30_000): Set<String> {
        val random = Random(1)
        return HashSet<String>().also { seen -> repeat(draws) { seen += Pieces.random(random, difficulty).id } }
    }

    @Test
    fun idsAreUnique() {
        assertEquals(Pieces.all.size, Pieces.all.map { it.id }.toSet().size)
    }

    @Test
    fun hasTheFullPieceSet() {
        assertEquals(45, Pieces.all.size)
    }

    @Test
    fun noPieceIsA3x3Square() {
        assertTrue(Pieces.all.none { it.size == 9 })
        assertTrue(Pieces.all.none { it.width == 3 && it.height == 3 && it.size > 6 })
    }

    @Test
    fun piecesAreNormalizedAndFitTheBoard() {
        for (p in Pieces.all) {
            assertEquals(0, p.cells.minOf { it.row })
            assertEquals(0, p.cells.minOf { it.col })
            assertTrue(p.width <= SIZE && p.height <= SIZE)
            assertEquals(p.cells.size, p.cells.toSet().size)
            assertTrue(p.size in 1..5)
        }
    }

    @Test
    fun noTwoIdsAreTheSameShape() {
        assertEquals(Pieces.all.size, Pieces.all.map { it.cells.toSet() }.toSet().size)
    }

    @Test
    fun lookupById() {
        for (p in Pieces.all) assertEquals(p, Pieces.byId(p.id))
        assertNull(Pieces.byId("nope"))
        assertNull(Pieces.byId("f10")) // the 3x3 square is not in the set
        assertEquals(1, Pieces.byId("f15")!!.size)
    }

    @Test
    fun difficultyStepsFollowTheScore() {
        assertEquals(0, Pieces.difficultyFor(0))
        assertEquals(0, Pieces.difficultyFor(179))
        assertEquals(1, Pieces.difficultyFor(180))
        assertEquals(1, Pieces.difficultyFor(599))
        assertEquals(2, Pieces.difficultyFor(600))
        assertEquals(2, Pieces.difficultyFor(1_000_000))
    }

    @Test
    fun theEasiestStepSkipsTheScatteredAndCupShapes() {
        val easy = dealt(0)
        for (id in listOf("f36", "f37", "f41", "f42", "f43", "f44", "f51", "f52")) assertTrue(id, id !in easy)
        assertEquals(37, easy.size)
    }

    @Test
    fun laterStepsDealEveryPiece() {
        val all = Pieces.all.map { it.id }.toSet()
        assertEquals(all, dealt(1))
        assertEquals(all, dealt(2))
    }

    @Test
    fun anOutOfRangeDifficultyIsClamped() {
        assertNotNull(Pieces.random(Random(2), -3))
        assertNotNull(Pieces.random(Random(2), 99))
    }
}
