package io.github.jpsutton.tilefit.game

/**
 * A game in progress or finished.
 *
 * @property hand the pieces on offer, [Game.HAND] slots; a slot is null once its piece is placed
 * @property streak placements in a row that cleared something, counting the last one
 * @property moves pieces placed so far
 */
data class GameState(
    val board: Board,
    val hand: List<Piece?>,
    val score: Int,
    val streak: Int,
    val moves: Int,
) {
    /** True when no piece left in the hand fits anywhere on the board. */
    val isOver: Boolean = hand.filterNotNull().none { Game.fitsAnywhere(board, it) }
}
