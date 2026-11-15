package io.github.jpsutton.tilefit.game

import kotlin.random.Random

/** What a placement did: the new state, the cells it cleared and the points it earned. */
class MoveResult(val state: GameState, val cleared: Set<Int>, val groups: Int, val points: Int)

/** The rules, with no Android dependencies. See docs/RULES.md. */
object Game {
    const val HAND = 3

    /** Flat bonus for a clearing piece that follows another clearing piece. */
    const val STREAK_BONUS = 9

    /** Points per cleared row, column or box, before the multiplier. */
    const val ZONE_POINTS = 9

    fun newGame(random: Random = Random): GameState =
        GameState(Board.EMPTY, deal(random, 0), score = 0, streak = 0, moves = 0)

    /** Three random pieces; which ones are likely depends on [score] (see [Pieces.difficultyFor]). */
    private fun deal(random: Random, score: Int): List<Piece?> {
        val difficulty = Pieces.difficultyFor(score)
        return List(HAND) { Pieces.random(random, difficulty) }
    }

    /** Whether [piece] with its top-left corner at [row], [col] is on the board and on empty cells only. */
    fun fits(board: Board, piece: Piece, row: Int, col: Int): Boolean {
        if (row < 0 || col < 0 || row + piece.height > SIZE || col + piece.width > SIZE) return false
        return piece.cells.none { board[row + it.row, col + it.col] }
    }

    fun fitsAnywhere(board: Board, piece: Piece): Boolean {
        for (r in 0..SIZE - piece.height) for (c in 0..SIZE - piece.width) {
            if (fits(board, piece, r, c)) return true
        }
        return false
    }

    private fun occupied(piece: Piece, row: Int, col: Int): List<Int> =
        piece.cells.map { cellIndex(row + it.row, col + it.col) }

    /** The cells of the full rows, columns and boxes on [board] and how many such groups there are. */
    private fun fullGroups(board: Board): Pair<Set<Int>, Int> {
        val cells = HashSet<Int>()
        var groups = 0
        for (group in Board.GROUPS) {
            if (group.all(board::isFilled)) {
                groups++
                cells.addAll(group)
            }
        }
        return cells to groups
    }

    /** The cells that clear if [piece] is placed at [row], [col], for the drag preview. Assumes it fits. */
    fun clearing(board: Board, piece: Piece, row: Int, col: Int): Set<Int> =
        fullGroups(board.with(occupied(piece, row, col), true)).first

    /**
     * Points for clearing [groups] rows, columns and boxes with one piece: 9 per group, times a multiplier
     * of 2 for one or two groups, 4 for three or four, 6 for five or six, and so on.
     */
    fun clearPoints(groups: Int): Int {
        if (groups <= 0) return 0
        val multiplier = 2 * ((groups + 1) / 2)
        return ZONE_POINTS * groups * multiplier
    }

    /**
     * Place the piece in hand [slot] with its top-left corner at [row], [col]: score it, clear any full
     * rows, columns and boxes, and deal a new hand once all three pieces are used. A piece that clears
     * something right after one that did also earns [STREAK_BONUS].
     * Returns null if there is no piece in the slot or it does not fit there.
     */
    fun place(state: GameState, slot: Int, row: Int, col: Int, random: Random = Random): MoveResult? {
        val piece = state.hand.getOrNull(slot) ?: return null
        if (!fits(state.board, piece, row, col)) return null
        val placed = state.board.with(occupied(piece, row, col), true)
        val (cleared, groups) = fullGroups(placed)
        val streak = if (groups > 0) state.streak + 1 else 0
        // A square of the piece that is cleared right away does not score as a square.
        val surviving = occupied(piece, row, col).count { it !in cleared }
        val points = surviving + clearPoints(groups) + if (streak > 1) STREAK_BONUS else 0
        val score = state.score + points
        var hand = state.hand.toMutableList().also { it[slot] = null }
        if (hand.all { it == null }) hand = deal(random, score).toMutableList()
        val next = GameState(placed.with(cleared, false), hand, score, streak, state.moves + 1)
        return MoveResult(next, cleared, groups, points)
    }
}
