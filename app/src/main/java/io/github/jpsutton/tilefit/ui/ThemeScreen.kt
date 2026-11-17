package io.github.jpsutton.tilefit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jpsutton.tilefit.game.Board

/** A board in the middle of a game, to show what a theme looks like. Digits are tones (see Board.toString). */
private val SAMPLE = Board.parse(
    "001100000" + "022200400" + "002004440" + "660030000" + "600033005" +
        "111660222" + "050000033" + "000400330" + "201100001",
)!!

@Composable
fun ThemeScreen(vm: GameViewModel, onBack: () -> Unit) {
    val s = LocalTilefit.current
    Column(
        Modifier.fillMaxSize().background(s.background).safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButtonBox(Icon.BACK, "Back", onBack)
            Text("Theme", style = TextStyle(fontFamily = s.display, fontWeight = FontWeight.Bold, fontSize = 24.sp), color = s.ink)
        }

        Panel {
            BoardThumb(SAMPLE, 168.dp)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Label("Score")
                Number("742", 36.sp)
                Chip("+28", s.hot, s.onHot, Modifier.padding(top = 6.dp))
            }
        }

        val system = vm.themeChoice == ThemeChoice.SYSTEM
        val shape = RoundedCornerShape(s.corner)
        Row(
            Modifier.fillMaxWidth().heightIn(min = 72.dp).clip(shape).background(s.panel).border(BorderStroke(1.dp, s.outline), shape)
                .toggleable(system, role = Role.Switch) { on ->
                    vm.chooseTheme(if (on) ThemeChoice.SYSTEM else ThemeChoice.of(s))
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Match system", style = TextStyle(fontFamily = s.body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp), color = s.ink)
                Text("Light: Tile & Grout · Dark: Night Shift", style = TextStyle(fontFamily = s.body, fontSize = 13.sp), color = s.muted)
            }
            Switch(
                checked = system, onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = s.accent, checkedThumbColor = s.onAccent,
                    uncheckedTrackColor = s.outline, uncheckedThumbColor = s.muted, uncheckedBorderColor = s.outline,
                ),
            )
        }

        Label(if (system) "Themes (picking one turns off Match system)" else "Themes", Modifier.padding(start = 4.dp, top = 4.dp))
        for (theme in Themes.all) {
            ThemeCard(theme, selected = theme.key == s.key, current = s) { vm.chooseTheme(ThemeChoice.of(theme)) }
        }

        Text(
            "Themes change colors, type and how blocks are drawn. Rules and scoring stay the same.",
            Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            style = TextStyle(fontFamily = s.body, fontSize = 13.sp, lineHeight = 19.sp),
            color = s.muted,
        )
    }
}

/** One theme, drawn in its own colors and fonts. [current] is the theme the screen is in, for the border. */
@Composable
private fun ThemeCard(theme: TilefitStyle, selected: Boolean, current: TilefitStyle, onClick: () -> Unit) {
    val shape = RoundedCornerShape(current.corner)
    Row(
        Modifier.fillMaxWidth().heightIn(min = 80.dp).clip(shape).background(theme.background)
            .border(BorderStroke(2.dp, if (selected) current.accent else current.outline), shape)
            .selectable(selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Canvas(Modifier.size(40.dp)) {
            val cell = size.width / 2
            drawBlock(theme, theme.blockColor(1), Offset.Zero, cell)
            drawBlock(theme, if (theme.tones != null) theme.blockColor(2) else theme.hot, Offset(cell, 0f), cell)
            drawEmptyCell(theme, 0, 3, Offset(0f, cell), cell)
            drawBlock(theme, theme.blockColor(4), Offset(cell, cell), cell)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(theme.name, style = TextStyle(fontFamily = theme.display, fontWeight = FontWeight.Bold, fontSize = 18.sp), color = theme.ink)
            Text(theme.description, style = TextStyle(fontFamily = theme.body, fontSize = 13.sp), color = theme.muted)
        }
        Box(
            Modifier.size(22.dp).border(2.dp, if (selected) theme.accent else theme.muted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(10.dp).clip(CircleShape).background(theme.accent))
        }
    }
}
