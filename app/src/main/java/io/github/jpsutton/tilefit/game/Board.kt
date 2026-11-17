package io.github.jpsutton.tilefit.game

const val SIZE = 9
const val BOX = 3

/** Index of the cell at [row], [col] in the row-major list of the 81 board cells. */
fun cellIndex(row: Int, col: Int): Int = row * SIZE + col

/**
 * The 9x9 grid: which cells hold a block, and for each block the [Piece.tone] of the piece that placed it
 * (0 for an empty cell). Only filled or empty matters to the rules; the tone is for drawing. Immutable;
 * changes return a new board.
 */
class Board private constructor(private val tones: ByteArray) {
    operator fun get(row: Int, col: Int): Boolean = tones[cellIndex(row, col)] != 0.toByte()

    fun isFilled(index: Int): Boolean = tones[index] != 0.toByte()

    /** The tone of the block at [index], or 0 if the cell is empty. */
    fun tone(index: Int): Int = tones[index].toInt()

    val count: Int get() = tones.count { it != 0.toByte() }

    /** A copy with the cells at [indices] set to [tone]; 0 empties them. */
    fun with(indices: Iterable<Int>, tone: Int): Board {
        require(tone in 0..Tones.COUNT)
        val copy = tones.copyOf()
        for (i in indices) copy[i] = tone.toByte()
        return Board(copy)
    }

    /** A copy with the cells at [indices] filled (with tone 1) or emptied. */
    fun with(indices: Iterable<Int>, value: Boolean): Board = with(indices, if (value) 1 else 0)

    override fun equals(other: Any?): Boolean = other is Board && tones.contentEquals(other.tones)

    override fun hashCode(): Int = tones.contentHashCode()

    /** 81 digits, row by row: `0` for an empty cell, else the block's tone (`1` to `6`). */
    override fun toString(): String = String(CharArray(SIZE * SIZE) { '0' + tones[it].toInt() })

    companion object {
        val EMPTY = Board(ByteArray(SIZE * SIZE))

        /**
         * The board written by [toString], or null if [text] is not one. Boards saved before tones existed
         * use only `0` and `1`, so they still parse.
         */
        fun parse(text: String): Board? {
            if (text.length != SIZE * SIZE || text.any { it !in '0'..('0' + Tones.COUNT) }) return null
            return Board(ByteArray(text.length) { (text[it] - '0').toByte() })
        }

        /** Every group that clears when full: 9 rows, 9 columns and 9 boxes, as cell indices. */
        val GROUPS: List<List<Int>> = buildList {
            for (r in 0 until SIZE) add(List(SIZE) { c -> cellIndex(r, c) })
            for (c in 0 until SIZE) add(List(SIZE) { r -> cellIndex(r, c) })
            for (br in 0 until SIZE step BOX) for (bc in 0 until SIZE step BOX) {
                add(List(SIZE) { k -> cellIndex(br + k / BOX, bc + k % BOX) })
            }
        }
    }
}
