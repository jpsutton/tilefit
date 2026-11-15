package io.github.jpsutton.tilefit.data

import android.content.Context
import io.github.jpsutton.tilefit.game.GameCodec
import io.github.jpsutton.tilefit.game.GameState

/** What survives between launches: the unfinished game and the best score. */
class Store(context: Context) {
    private val prefs = context.getSharedPreferences("tilefit", Context.MODE_PRIVATE)

    fun loadGame(): GameState? = prefs.getString(GAME, null)?.let(GameCodec::decode)?.takeUnless { it.isOver }

    fun saveGame(state: GameState?) {
        prefs.edit().apply { if (state == null) remove(GAME) else putString(GAME, GameCodec.encode(state)) }.apply()
    }

    fun best(): Int = prefs.getInt(BEST, 0)

    /** Record [score] if it beats the best; returns whether it did. */
    fun submit(score: Int): Boolean {
        if (score <= best()) return false
        prefs.edit().putInt(BEST, score).apply()
        return true
    }

    private companion object {
        const val GAME = "game"
        const val BEST = "best"
    }
}
