package io.github.jpsutton.tilefit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(vm: GameViewModel, onRules: () -> Unit, onTheme: () -> Unit) {
    val s = LocalTilefit.current
    BoxWithConstraints(Modifier.fillMaxSize().background(s.background).safeDrawingPadding()) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                LogoMark(56.dp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Tilefit",
                        style = TextStyle(fontFamily = s.display, fontWeight = FontWeight.ExtraBold, fontSize = 52.sp, letterSpacing = (-1.5).sp),
                        color = s.ink,
                    )
                    Text(
                        "Fill a row, a column or a 3×3 box to clear it.",
                        style = TextStyle(fontFamily = s.body, fontSize = 16.sp, lineHeight = 23.sp),
                        color = s.muted,
                    )
                }
                if (vm.best > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                        Stat("Best", vm.best.grouped(), color = s.accent)
                        if (vm.longestStreak > 0) Stat("Longest streak", "${vm.longestStreak}", color = s.hot)
                        if (vm.gamesPlayed > 0) Stat("Games", vm.gamesPlayed.grouped())
                    }
                }
                vm.saved?.let { saved ->
                    Panel {
                        BoardThumb(saved.board, 92.dp)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Label("In progress")
                            Number(saved.score.grouped(), 32.sp)
                            Text("${saved.moves} pieces placed", style = TextStyle(fontFamily = s.body, fontSize = 14.sp), color = s.muted)
                        }
                    }
                }
            }

            Column(Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val saved = vm.saved
                if (saved != null) {
                    PrimaryButton("Resume", vm::resume, trailing = saved.score.grouped())
                    RowButton("New game", vm::start, icon = Icon.PLUS)
                } else {
                    PrimaryButton("Play", vm::start)
                }
                RowButton("How to play", onRules)
                RowButton("Theme", onTheme, detail = if (vm.themeChoice == ThemeChoice.SYSTEM) "Match system" else s.name)
                Text(
                    "No ads · no account · offline",
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    style = TextStyle(fontFamily = s.numbers, fontSize = 12.sp, textAlign = TextAlign.Center),
                    color = s.muted,
                )
            }
        }
    }
}

/** A 3x3 box with its last block dropping in: the app's mark, drawn in the current theme. */
@Composable
fun LogoMark(size: Dp, modifier: Modifier = Modifier) {
    val s = LocalTilefit.current
    Canvas(modifier.size(size)) {
        val cell = this.size.width / 3f
        for (r in 0 until 3) for (c in 0 until 3) {
            if (r == 0 && c == 2) continue
            drawBlock(s, s.block, Offset(c * cell, r * cell), cell)
        }
        drawBlock(s, s.hot, Offset(2 * cell + cell * 0.08f, -cell * 0.08f), cell)
    }
}
