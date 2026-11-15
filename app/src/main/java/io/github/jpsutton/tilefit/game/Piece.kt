package io.github.jpsutton.tilefit.game

import kotlin.random.Random

/** One block of a [Piece], relative to the piece's top-left corner. */
data class Cell(val row: Int, val col: Int)

/**
 * A piece in one fixed orientation (pieces are not rotated while playing). [id] is stable
 * across versions as long as the [Pieces] catalog keeps its order; it is what a saved game stores.
 */
class Piece(val id: String, val cells: List<Cell>) {
    val height: Int = cells.maxOf { it.row } + 1
    val width: Int = cells.maxOf { it.col } + 1
    val size: Int get() = cells.size
}

object Pieces {
    private class Family(val name: String, val weight: Int, val rows: List<String>)

    // Small pieces are weighted up so a hand is rarely all large pieces.
    private val families = listOf(
        Family("dot", 1, listOf("#")),
        Family("duo", 2, listOf("##")),
        Family("bar3", 2, listOf("###")),
        Family("bar4", 2, listOf("####")),
        Family("bar5", 1, listOf("#####")),
        Family("corner3", 2, listOf("##", "#.")),
        Family("square2", 2, listOf("##", "##")),
        Family("square3", 1, listOf("###", "###", "###")),
        Family("el4", 2, listOf("#.", "#.", "##")),
        Family("tee4", 2, listOf("###", ".#.")),
        Family("skew4", 2, listOf(".##", "##.")),
        Family("corner5", 1, listOf("#..", "#..", "###")),
        Family("el5", 1, listOf("#.", "#.", "#.", "##")),
        Family("tee5", 1, listOf("###", ".#.", ".#.")),
        Family("plus", 1, listOf(".#.", "###", ".#.")),
        Family("pee", 1, listOf("##", "##", "#.")),
        Family("cup", 1, listOf("#.#", "###")),
    )

    private fun orientations(family: Family): List<Piece> {
        val base = buildList {
            family.rows.forEachIndexed { r, row -> row.forEachIndexed { c, ch -> if (ch == '#') add(Cell(r, c)) } }
        }
        val seen = LinkedHashMap<List<Cell>, Unit>()
        var shape = base
        repeat(4) {
            for (flipped in listOf(shape, shape.map { Cell(it.row, -it.col) })) {
                val minR = flipped.minOf { it.row }
                val minC = flipped.minOf { it.col }
                val normal = flipped.map { Cell(it.row - minR, it.col - minC) }
                    .sortedWith(compareBy({ it.row }, { it.col }))
                seen[normal] = Unit
            }
            shape = shape.map { Cell(it.col, -it.row) } // quarter turn
        }
        return seen.keys.mapIndexed { i, cells -> Piece("${family.name}-$i", cells) }
    }

    private val byFamily: List<Pair<Family, List<Piece>>> = families.map { it to orientations(it) }

    /** Every piece in every orientation. */
    val all: List<Piece> = byFamily.flatMap { it.second }

    private val index: Map<String, Piece> = all.associateBy { it.id }

    fun byId(id: String): Piece? = index[id]

    /** A random piece: a shape weighted by its family, then one of its orientations. */
    fun random(random: Random): Piece {
        var pick = random.nextInt(byFamily.sumOf { it.first.weight })
        for ((family, pieces) in byFamily) {
            pick -= family.weight
            if (pick < 0) return pieces[random.nextInt(pieces.size)]
        }
        error("unreachable")
    }
}
