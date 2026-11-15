package io.github.jpsutton.tilefit.game

/**
 * One-line text form of a [GameState], for saving an unfinished game:
 * `board|score|streak|moves|piece,piece,piece` with `-` for a used slot.
 */
object GameCodec {
    fun encode(state: GameState): String = listOf(
        state.board.toString(),
        state.score.toString(),
        state.streak.toString(),
        state.moves.toString(),
        state.hand.joinToString(",") { it?.id ?: "-" },
    ).joinToString("|")

    /** The decoded state, or null if [text] is not a valid saved game. */
    fun decode(text: String): GameState? = try {
        val parts = text.split("|")
        require(parts.size == 5)
        val board = requireNotNull(Board.parse(parts[0]))
        val score = parts[1].toInt()
        val streak = parts[2].toInt()
        val moves = parts[3].toInt()
        val hand = parts[4].split(",").map { if (it == "-") null else requireNotNull(Pieces.byId(it)) }
        require(score >= 0 && streak >= 0 && moves >= 0)
        require(hand.size == Game.HAND && hand.any { it != null })
        GameState(board, hand, score, streak, moves)
    } catch (_: IllegalArgumentException) {
        null
    }
}
