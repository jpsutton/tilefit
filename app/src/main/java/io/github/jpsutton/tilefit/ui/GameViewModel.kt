package io.github.jpsutton.tilefit.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import io.github.jpsutton.tilefit.data.Store
import io.github.jpsutton.tilefit.game.Game
import io.github.jpsutton.tilefit.game.GameState
import io.github.jpsutton.tilefit.game.MoveResult

/** The last placement that cleared something, for the clear animation. [id] changes with every event. */
class ClearEvent(val id: Int, val cells: Set<Int>, val groups: Int, val points: Int, val streak: Int)

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)

    /** The game in progress (or just finished), or null on the menu. */
    var game: GameState? by mutableStateOf(null)
        private set

    /** The unfinished game saved on disk, offered as "Resume" on the menu. */
    var saved: GameState? by mutableStateOf(store.loadGame())
        private set

    var best: Int by mutableIntStateOf(store.best())
        private set

    var gamesPlayed: Int by mutableIntStateOf(store.gamesPlayed())
        private set

    var longestStreak: Int by mutableIntStateOf(store.longestStreak())
        private set

    var themeChoice: ThemeChoice by mutableStateOf(ThemeChoice.of(store.theme()))
        private set

    fun chooseTheme(choice: ThemeChoice) {
        themeChoice = choice
        store.setTheme(choice.key)
    }

    /** Set when the finished game beat the stored best. */
    var newBest: Boolean by mutableStateOf(false)
        private set

    var clearEvent: ClearEvent? by mutableStateOf(null)
        private set

    private var events = 0

    fun start() {
        newBest = false
        clearEvent = null
        game = Game.newGame().also(store::saveGame)
        saved = game
    }

    fun resume() {
        newBest = false
        clearEvent = null
        game = saved
    }

    fun toMenu() {
        game = null
        clearEvent = null
    }

    /** Place the piece of hand [slot] at [row], [col]. Returns the result, or null if it is not legal. */
    fun place(slot: Int, row: Int, col: Int): MoveResult? {
        val g = game ?: return null
        val result = Game.place(g, slot, row, col) ?: return null
        val next = result.state
        game = next
        clearEvent = if (result.groups > 0) {
            ClearEvent(++events, result.cleared, result.groups, result.points, next.streak)
        } else {
            null
        }
        if (next.isOver) {
            newBest = store.finish(next)
            best = store.best()
            gamesPlayed = store.gamesPlayed()
            longestStreak = store.longestStreak()
            store.saveGame(null)
            saved = null
        } else {
            store.saveGame(next)
            saved = next
        }
        return result
    }
}
