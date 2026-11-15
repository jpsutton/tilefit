package io.github.jpsutton.tilefit.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PiecesTest {
    @Test
    fun idsAreUnique() {
        assertEquals(Pieces.all.size, Pieces.all.map { it.id }.toSet().size)
    }

    @Test
    fun orientationCountsPerShape() {
        fun count(name: String) = Pieces.all.count { it.id.substringBeforeLast("-") == name }
        assertEquals(1, count("dot"))
        assertEquals(2, count("duo"))
        assertEquals(2, count("bar5"))
        assertEquals(4, count("corner3"))
        assertEquals(1, count("square3"))
        assertEquals(8, count("el4"))   // chiral: rotations and mirror images
        assertEquals(4, count("tee4"))
        assertEquals(4, count("skew4")) // S and Z, two orientations each
        assertEquals(1, count("plus"))
    }

    @Test
    fun piecesAreNormalizedAndFitTheBoard() {
        for (p in Pieces.all) {
            assertEquals(0, p.cells.minOf { it.row })
            assertEquals(0, p.cells.minOf { it.col })
            assertTrue(p.width <= SIZE && p.height <= SIZE)
            assertEquals(p.cells.size, p.cells.toSet().size)
        }
    }

    @Test
    fun lookupById() {
        for (p in Pieces.all) assertEquals(p, Pieces.byId(p.id))
        assertNull(Pieces.byId("nope"))
    }

    @Test
    fun randomReachesEveryPiece() {
        val random = Random(1)
        val seen = HashSet<String>()
        repeat(20_000) { seen += Pieces.random(random).id }
        assertEquals(Pieces.all.map { it.id }.toSet(), seen)
    }

    @Test
    fun dotIsASingleCell() {
        assertNotNull(Pieces.byId("dot-0"))
        assertEquals(1, Pieces.byId("dot-0")!!.size)
    }
}
