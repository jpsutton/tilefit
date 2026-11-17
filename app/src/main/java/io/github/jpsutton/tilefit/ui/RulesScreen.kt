package io.github.jpsutton.tilefit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RulesScreen(onBack: () -> Unit) {
    val s = LocalTilefit.current
    Column(
        Modifier.fillMaxSize().background(s.background).safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButtonBox(Icon.BACK, "Back", onBack)
            Text("How to play", style = TextStyle(fontFamily = s.display, fontWeight = FontWeight.Bold, fontSize = 24.sp), color = s.ink)
        }
        Section("Placing", "Each turn you get three pieces. Drag one onto the board; it must fit on empty squares. Pieces cannot be rotated. Once all three are placed you get three new ones.")
        Section("Clearing", "When a row, a column or one of the nine 3x3 boxes is completely filled, it clears. One piece can clear several at once. While you drag, blocks that would clear are highlighted and the points the move would score are shown.")
        Section("Scoring", "Every square of a piece you place scores 1, unless it is cleared right away. Each row, column or 3x3 box you clear scores 9, times 2 for one or two at once, times 4 for three or four, times 6 for five or six, and so on.")
        Section("Streak", "Clear something with a piece right after the previous piece cleared something and you get a flat 9 bonus. A piece that clears nothing ends the streak.")
        Section("Tiers", "As your score passes 180 and then 600, bigger and trickier pieces are dealt more often. The tier marks at the top of the game show where you are.")
        Section("Game over", "The game ends when none of the pieces you hold fit anywhere on the board. Pieces that cannot fit are dimmed.")
    }
}

@Composable
private fun Section(title: String, body: String) {
    val s = LocalTilefit.current
    Panel {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title, Modifier.semantics { heading() },
                style = TextStyle(fontFamily = s.display, fontWeight = FontWeight.Bold, fontSize = 17.sp), color = s.ink,
            )
            Text(body, style = TextStyle(fontFamily = s.body, fontSize = 15.sp, lineHeight = 22.sp), color = s.muted)
        }
    }
}
