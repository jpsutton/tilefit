package io.github.jpsutton.tilefit.game

import kotlin.random.Random

/** One block of a [Piece], relative to the piece's top-left corner. */
data class Cell(val row: Int, val col: Int)

/**
 * A piece in one fixed orientation (pieces are not rotated while playing). Each orientation is its own
 * piece. [id] is stable and is what a saved game stores. Cells need not touch each other.
 */
class Piece(val id: String, val cells: List<Cell>) {
    val height: Int = cells.maxOf { it.row } + 1
    val width: Int = cells.maxOf { it.col } + 1
    val size: Int get() = cells.size
}

object Pieces {
    /** Score at which dealing moves up a difficulty step: below 180 is easy, from 600 it is hardest. */
    private val THRESHOLDS = intArrayOf(180, 600)

    /** The difficulty step (0, 1 or 2) for a game score: how many thresholds it has reached. */
    fun difficultyFor(score: Int): Int = THRESHOLDS.count { score >= it }

    private class Entry(val id: Int, val rows: String, val weights: IntArray)

    private fun e(id: Int, rows: String, easy: Int, medium: Int, hard: Int) =
        Entry(id, rows, intArrayOf(easy, medium, hard))

    // Every piece in the game, one entry per orientation, with its weight at each difficulty step. Weight 0
    // means it is not dealt at that step; a bigger weight is dealt more often. Rows are separated by '/'.
    // The ids are only labels and keep the numbers of the design notes; unused numbers are shapes never dealt.
    private val table = listOf(
        e(1, ".#/##", 102, 93, 85), e(2, "#./##", 102, 93, 85), e(3, "##/#.", 102, 93, 85), e(4, "##/.#", 102, 93, 85),
        e(5, "##/##", 128, 117, 105),
        e(6, "#####", 75, 90, 100), e(7, "####", 98, 105, 116), e(8, "###", 128, 117, 105),
        e(9, "##", 128, 117, 105),
        e(11, "#/#/#/#/#", 75, 90, 100), e(12, "#/#/#/#", 98, 105, 116), e(13, "#/#/#", 128, 117, 105),
        e(14, "#/#", 128, 117, 105), e(15, "#", 128, 117, 105),
        e(16, "##/#./#.", 102, 93, 85), e(17, "###/..#", 102, 93, 85), e(18, ".#/.#/##", 102, 93, 85),
        e(19, "#../###", 102, 93, 85), e(20, ".#./###", 102, 93, 85),
        e(21, "###/#../#..", 98, 105, 116), e(22, "..#/..#/###", 98, 105, 116),
        e(23, "###/..#/..#", 98, 105, 116), e(24, "#../#../###", 98, 105, 116),
        e(25, ".#/##/.#", 102, 93, 85), e(26, ".#/##/#.", 98, 105, 116), e(27, "##./.##", 98, 105, 116),
        e(28, "#./##/.#", 98, 105, 116), e(29, ".##/##.", 98, 105, 116), e(30, "#./##/#.", 102, 93, 85),
        e(35, "###/.#.", 102, 93, 85),
        e(36, "..#/.#./#..", 0, 105, 116), e(37, "#../.#./..#", 0, 105, 116),
        e(40, ".#./###/.#.", 75, 90, 100),
        e(41, "###/#.#", 0, 105, 116), e(42, "##/.#/##", 0, 105, 116),
        e(43, "#.#/###", 0, 105, 116), e(44, "##/#./##", 0, 105, 116),
        e(51, ".#/#.", 0, 93, 85), e(52, "#./.#", 0, 93, 85),
        e(59, "###/.#./.#.", 75, 90, 100), e(60, "..#/###/..#", 75, 90, 100),
        e(61, ".#./.#./###", 75, 90, 100), e(62, "#../###/#..", 75, 90, 100),
        e(67, "##/.#/.#", 102, 93, 85), e(68, "#./#./##", 102, 93, 85),
    )

    private fun pieceOf(entry: Entry): Piece {
        val cells = buildList {
            entry.rows.split('/').forEachIndexed { r, row -> row.forEachIndexed { c, ch -> if (ch == '#') add(Cell(r, c)) } }
        }
        return Piece("f${entry.id}", cells)
    }

    private val pieces: List<Pair<Piece, IntArray>> = table.map { pieceOf(it) to it.weights }

    /** Every piece that can be dealt. */
    val all: List<Piece> = pieces.map { it.first }

    private val index: Map<String, Piece> = all.associateBy { it.id }

    fun byId(id: String): Piece? = index[id]

    /** A random piece for the difficulty step [difficulty] (see [difficultyFor]), weighted. */
    fun random(random: Random, difficulty: Int = 0): Piece {
        val d = difficulty.coerceIn(0, THRESHOLDS.size)
        var pick = random.nextInt(pieces.sumOf { it.second[d] })
        for ((piece, weights) in pieces) {
            pick -= weights[d]
            if (pick < 0) return piece
        }
        error("unreachable")
    }
}
