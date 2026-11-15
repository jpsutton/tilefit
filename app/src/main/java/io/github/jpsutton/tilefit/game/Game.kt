package io.github.jpsutton.tilefit.game

import kotlin.random.Random

/** What a placement did: the new state, the cells it cleared and the points it earned. */
class MoveResult(val state: GameState, val cleared: Set<Int>, val groups: Int, val points: Int)

/** The rules, with no Android dependencies. See docs/RULES.md. */
object Game {
    const val HAND = 3

    fun newGame(random: Random = Random): GameState =
        GameState(Board.EMPTY, deal(random), score = 0, streak = 0, moves = 0)

    private fun deal(random: Random): List<Piece?> = List(HAND) { Pieces.random(random) }

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

    /** Points for clearing [groups] rows, columns and boxes with one piece: 10, 30, 60, 100, ... */
    fun clearPoints(groups: Int): Int = 10 * groups * (groups + 1) / 2

    /**
     * Place the piece in hand [slot] with its top-left corner at [row], [col]: score it, clear any full
     * rows, columns and boxes, and deal a new hand once all three pieces are used.
     * Returns null if there is no piece in the slot or it does not fit there.
     */
    fun place(state: GameState, slot: Int, row: Int, col: Int, random: Random = Random): MoveResult? {
        val piece = state.hand.getOrNull(slot) ?: return null
        if (!fits(state.board, piece, row, col)) return null
        val placed = state.board.with(occupied(piece, row, col), true)
        val (cleared, groups) = fullGroups(placed)
        val streak = if (groups > 0) state.streak + 1 else 0
        val points = piece.size + clearPoints(groups) * streak
        var hand = state.hand.toMutableList().also { it[slot] = null }
        if (hand.all { it == null }) hand = deal(random).toMutableList()
        val next = GameState(placed.with(cleared, false), hand, state.score + points, streak, state.moves + 1)
        return MoveResult(next, cleared, groups, points)
    }
}
