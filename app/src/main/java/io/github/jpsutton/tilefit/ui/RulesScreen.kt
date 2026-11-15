package io.github.jpsutton.tilefit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RulesScreen(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("How to play", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Section("Placing", "Each turn you get three pieces. Drag one onto the board; it must fit on empty squares. Pieces cannot be rotated. Once all three are placed you get three new ones.")
        Section("Clearing", "When a row, a column or one of the nine 3x3 boxes is completely filled, it clears. One piece can clear several at once.")
        Section("Scoring", "Every square of a piece you place scores 1, unless it is cleared right away. Each row, column or 3x3 box you clear scores 9, times 2 for one or two at once, times 4 for three or four, times 6 for five or six, and so on.")
        Section("Streak", "Clear something with a piece right after the previous piece cleared something and you get a flat 9 bonus. A piece that clears nothing ends the streak.")
        Section("Game over", "The game ends when none of the pieces you hold fit anywhere on the board. Pieces that cannot fit are dimmed.")
        TextButton(onClick = onBack) { Text("Back") }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}
