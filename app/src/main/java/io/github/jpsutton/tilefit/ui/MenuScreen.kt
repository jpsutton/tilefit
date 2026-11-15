package io.github.jpsutton.tilefit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(vm: GameViewModel, onRules: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Tilefit", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Drag pieces onto the 9x9 board. Fill a row, a column or a 3x3 box to clear it.",
            style = MaterialTheme.typography.bodyLarge,
        )

        if (vm.best > 0) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("BEST", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${vm.best}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        vm.saved?.let { saved ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Game in progress", style = MaterialTheme.typography.titleMedium)
                    Text("Score ${saved.score}, ${saved.moves} pieces placed", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = vm::resume, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Resume") }
                }
            }
        }

        val newGame: @Composable () -> Unit = { Text(if (vm.saved != null) "New game" else "Play") }
        if (vm.saved != null) {
            OutlinedButton(onClick = vm::start, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { newGame() }
        } else {
            Button(onClick = vm::start, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { newGame() }
        }

        TextButton(onClick = onRules) { Text("How to play") }
    }
}
