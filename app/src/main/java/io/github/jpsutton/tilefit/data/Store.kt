package io.github.jpsutton.tilefit.data

import android.content.Context
import io.github.jpsutton.tilefit.game.GameCodec
import io.github.jpsutton.tilefit.game.GameState

/** What survives between launches: the unfinished game, the records and the chosen theme. */
class Store(context: Context) {
    private val prefs = context.getSharedPreferences("tilefit", Context.MODE_PRIVATE)

    fun loadGame(): GameState? = prefs.getString(GAME, null)?.let(GameCodec::decode)?.takeUnless { it.isOver }

    fun saveGame(state: GameState?) {
        prefs.edit().apply { if (state == null) remove(GAME) else putString(GAME, GameCodec.encode(state)) }.apply()
    }

    fun best(): Int = prefs.getInt(BEST, 0)

    /** Finished games. Counting started with the version that added themes. */
    fun gamesPlayed(): Int = prefs.getInt(GAMES, 0)

    /** The longest streak of any finished game. */
    fun longestStreak(): Int = prefs.getInt(LONGEST_STREAK, 0)

    /** Record a finished game; returns whether its score beat the best. */
    fun finish(state: GameState): Boolean {
        val newBest = state.score > best()
        prefs.edit().apply {
            putInt(GAMES, gamesPlayed() + 1)
            if (state.longestStreak > longestStreak()) putInt(LONGEST_STREAK, state.longestStreak)
            if (newBest) putInt(BEST, state.score)
        }.apply()
        return newBest
    }

    /** The chosen theme's key (see `ThemeChoice`), or null if none was chosen yet. */
    fun theme(): String? = prefs.getString(THEME, null)

    fun setTheme(key: String) {
        prefs.edit().putString(THEME, key).apply()
    }

    private companion object {
        const val GAME = "game"
        const val BEST = "best"
        const val GAMES = "games"
        const val LONGEST_STREAK = "longest_streak"
        const val THEME = "theme"
    }
}
