package io.github.jpsutton.tilefit.game

/**
 * One-line text form of a [GameState], for saving an unfinished game:
 * `board|score|streak|moves|piece,piece,piece|groupsCleared|longestStreak|biggestClear` with `-` for a used
 * slot. The board is 81 digits (see [Board.toString]). Games saved by older versions have only the first five
 * fields and a board of `0` and `1`; they load with the counters at 0.
 */
object GameCodec {
    fun encode(state: GameState): String = listOf(
        state.board.toString(),
        state.score.toString(),
        state.streak.toString(),
        state.moves.toString(),
        state.hand.joinToString(",") { it?.id ?: "-" },
        state.groupsCleared.toString(),
        state.longestStreak.toString(),
        state.biggestClear.toString(),
    ).joinToString("|")

    /** The decoded state, or null if [text] is not a valid saved game. */
    fun decode(text: String): GameState? = try {
        val parts = text.split("|")
        require(parts.size == 5 || parts.size == 8)
        val board = requireNotNull(Board.parse(parts[0]))
        val score = parts[1].toInt()
        val streak = parts[2].toInt()
        val moves = parts[3].toInt()
        val hand = parts[4].split(",").map { if (it == "-") null else requireNotNull(Pieces.byId(it)) }
        val counters = parts.drop(5).map { it.toInt() }
        require(score >= 0 && streak >= 0 && moves >= 0 && counters.all { it >= 0 })
        require(hand.size == Game.HAND && hand.any { it != null })
        GameState(
            board, hand, score, streak, moves,
            groupsCleared = counters.getOrElse(0) { 0 },
            longestStreak = counters.getOrElse(1) { streak },
            biggestClear = counters.getOrElse(2) { 0 },
        )
    } catch (_: IllegalArgumentException) {
        null
    }
}
