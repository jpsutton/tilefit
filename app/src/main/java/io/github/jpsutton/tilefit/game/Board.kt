package io.github.jpsutton.tilefit.game

const val SIZE = 9
const val BOX = 3

/** Index of the cell at [row], [col] in the row-major list of the 81 board cells. */
fun cellIndex(row: Int, col: Int): Int = row * SIZE + col

/** The 9x9 grid: which cells hold a block. Immutable; changes return a new board. */
class Board private constructor(private val filled: BooleanArray) {
    operator fun get(row: Int, col: Int): Boolean = filled[cellIndex(row, col)]

    fun isFilled(index: Int): Boolean = filled[index]

    val count: Int get() = filled.count { it }

    /** A copy with the cells at [indices] set to [value]. */
    fun with(indices: Iterable<Int>, value: Boolean): Board {
        val copy = filled.copyOf()
        for (i in indices) copy[i] = value
        return Board(copy)
    }

    override fun equals(other: Any?): Boolean = other is Board && filled.contentEquals(other.filled)

    override fun hashCode(): Int = filled.contentHashCode()

    /** 81 characters, `1` for a block and `0` for an empty cell, row by row. */
    override fun toString(): String = String(CharArray(SIZE * SIZE) { if (filled[it]) '1' else '0' })

    companion object {
        val EMPTY = Board(BooleanArray(SIZE * SIZE))

        /** The board written by [toString], or null if [text] is not one. */
        fun parse(text: String): Board? {
            if (text.length != SIZE * SIZE || text.any { it != '0' && it != '1' }) return null
            return Board(BooleanArray(text.length) { text[it] == '1' })
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
