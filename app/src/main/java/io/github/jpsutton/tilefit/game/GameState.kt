package io.github.jpsutton.tilefit.game

/**
 * A game in progress or finished.
 *
 * @property hand the pieces on offer, [Game.HAND] slots; a slot is null once its piece is placed
 * @property streak placements in a row that cleared something, counting the last one
 * @property moves pieces placed so far
 * @property groupsCleared rows, columns and boxes cleared so far
 * @property longestStreak the longest [streak] reached in this game
 * @property biggestClear the most groups one piece has cleared at once in this game
 */
data class GameState(
    val board: Board,
    val hand: List<Piece?>,
    val score: Int,
    val streak: Int,
    val moves: Int,
    val groupsCleared: Int = 0,
    val longestStreak: Int = 0,
    val biggestClear: Int = 0,
) {
    /** True when no piece left in the hand fits anywhere on the board. */
    val isOver: Boolean = hand.filterNotNull().none { Game.fitsAnywhere(board, it) }
}
