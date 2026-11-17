package io.github.jpsutton.tilefit

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.jpsutton.tilefit.ui.GameScreen
import io.github.jpsutton.tilefit.ui.GameViewModel
import io.github.jpsutton.tilefit.ui.MenuScreen
import io.github.jpsutton.tilefit.ui.RulesScreen
import io.github.jpsutton.tilefit.ui.ThemeScreen
import io.github.jpsutton.tilefit.ui.TilefitTheme

private enum class Page { MENU, RULES, THEME }

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val style = vm.themeChoice.resolve(isSystemInDarkTheme())
            // The status and navigation bar icons follow the theme, not the system setting.
            DisposableEffect(style.dark) {
                val bars = if (style.dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
                onDispose {}
            }
            TilefitTheme(style) {
                Surface(color = style.background) {
                    var page by rememberSaveable { mutableStateOf(Page.MENU) }
                    when {
                        vm.game != null -> {
                            BackHandler { vm.toMenu() }
                            GameScreen(vm, onMenu = vm::toMenu)
                        }
                        page == Page.RULES -> {
                            BackHandler { page = Page.MENU }
                            RulesScreen(onBack = { page = Page.MENU })
                        }
                        page == Page.THEME -> {
                            BackHandler { page = Page.MENU }
                            ThemeScreen(vm, onBack = { page = Page.MENU })
                        }
                        else -> MenuScreen(vm, onRules = { page = Page.RULES }, onTheme = { page = Page.THEME })
                    }
                }
            }
        }
    }
}
