package io.github.jpsutton.tilefit.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.jpsutton.tilefit.R

/** How a block is drawn: a darker bottom edge so it reads as a physical tile, or a soft glow. */
enum class BlockStyle { LIP, GLOW }

/**
 * Everything a theme changes: colors, fonts and how blocks are drawn. Layout and rules are the same in every
 * theme. Colors that must be told apart (empty cells of neighbouring 3x3 boxes, blocks and the clear
 * highlight) differ in lightness, not only in hue.
 */
@Immutable
class TilefitStyle(
    val key: String,
    val name: String,
    val description: String,
    val dark: Boolean,
    val background: Color,
    /** Cards, rows and the menu button. */
    val panel: Color,
    val outline: Color,
    /** Behind the whole hand, or null for no tray. */
    val tray: Color?,
    /** Behind each piece of the hand, or null for none. */
    val slot: Color?,
    val grout: Color,
    val cell: Color,
    val cellAlt: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    /** Blocks about to clear, the streak and other "hot" highlights. */
    val hot: Color,
    val onHot: Color,
    val block: Color,
    /** Block colors by [io.github.jpsutton.tilefit.game.Piece.tone], or null to draw every block in [block]. */
    val tones: List<Color>?,
    val blockStyle: BlockStyle,
    /** Corner radius of a cell, as a fraction of the cell. */
    val cellRadius: Float,
    /** Corner radius of buttons and cards. */
    val corner: Dp,
    val display: FontFamily,
    val body: FontFamily,
    /** Scores and other numbers. */
    val numbers: FontFamily,
) {
    fun blockColor(tone: Int): Color = tones?.getOrNull(tone - 1) ?: block

    /** The darker bottom edge of a [BlockStyle.LIP] block. */
    fun edgeOf(color: Color): Color = lerp(color, Color.Black, 0.28f)
}

private val Bricolage = FontFamily(
    Font(R.font.bricolage_grotesque_700, FontWeight.Bold),
    Font(R.font.bricolage_grotesque_800, FontWeight.ExtraBold),
)

private val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_500, FontWeight.Medium),
    Font(R.font.jetbrains_mono_700, FontWeight.Bold),
    Font(R.font.jetbrains_mono_800, FontWeight.ExtraBold),
)

private val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_400, FontWeight.Normal),
    Font(R.font.space_grotesk_500, FontWeight.Medium),
    Font(R.font.space_grotesk_600, FontWeight.SemiBold),
    Font(R.font.space_grotesk_700, FontWeight.Bold),
)

private val Rubik = FontFamily(
    Font(R.font.rubik_400, FontWeight.Normal),
    Font(R.font.rubik_500, FontWeight.Medium),
    Font(R.font.rubik_600, FontWeight.SemiBold),
    Font(R.font.rubik_700, FontWeight.Bold),
    Font(R.font.rubik_800, FontWeight.ExtraBold),
)

object Themes {
    val NightShift = TilefitStyle(
        key = "night", name = "Night Shift", description = "Dark. Glowing blocks, mono numbers.", dark = true,
        background = Color(0xFF0F1115), panel = Color(0xFF171A20), outline = Color(0xFF2A303C),
        tray = null, slot = null,
        grout = Color(0xFF171A20), cell = Color(0xFF1C2028), cellAlt = Color(0xFF2E3542),
        ink = Color(0xFFE8EBF0), muted = Color(0xFF9AA3B2),
        accent = Color(0xFF3EE6A8), onAccent = Color(0xFF0F1115),
        hot = Color(0xFFFF7A59), onHot = Color(0xFF0F1115),
        block = Color(0xFF3EE6A8), tones = null, blockStyle = BlockStyle.GLOW,
        cellRadius = 0.14f, corner = 14.dp,
        display = JetBrainsMono, body = SpaceGrotesk, numbers = JetBrainsMono,
    )

    val TileAndGrout = TilefitStyle(
        key = "tile", name = "Tile & Grout", description = "Light. Chunky tiles with a lip.", dark = false,
        background = Color(0xFFE9ECF1), panel = Color(0xFFFFFFFF), outline = Color(0xFFC3CBD8),
        tray = Color(0xFFDCE1E8), slot = Color(0xFFF6F7FA),
        grout = Color(0xFFC3CBD8), cell = Color(0xFFF6F7FA), cellAlt = Color(0xFFD9DFEA),
        ink = Color(0xFF15181F), muted = Color(0xFF545C6D),
        accent = Color(0xFF2F4FD6), onAccent = Color(0xFFFFFFFF),
        hot = Color(0xFFF5A524), onHot = Color(0xFF15181F),
        block = Color(0xFF2F4FD6), tones = null, blockStyle = BlockStyle.LIP,
        cellRadius = 0.19f, corner = 18.dp,
        display = Bricolage, body = FontFamily.Default, numbers = Bricolage,
    )

    val Mosaic = TilefitStyle(
        key = "mosaic", name = "Mosaic", description = "Light. Every shape keeps its color.", dark = false,
        background = Color(0xFFF4F5FA), panel = Color(0xFFFFFFFF), outline = Color(0xFFD6D9E6),
        tray = null, slot = Color(0xFFFFFFFF),
        grout = Color(0xFFD6D9E6), cell = Color(0xFFFFFFFF), cellAlt = Color(0xFFE4E6F0),
        ink = Color(0xFF1B1E3C), muted = Color(0xFF4D5170),
        accent = Color(0xFF2D68DC), onAccent = Color(0xFFFFFFFF),
        hot = Color(0xFF1B1E3C), onHot = Color(0xFFFFFFFF),
        block = Color(0xFF12A39A),
        // Bars, corners and cups, squares, T shapes, S/Z and diagonals, L shapes: see Tones.
        tones = listOf(
            Color(0xFF12A39A), Color(0xFFF27A2E), Color(0xFFF2C230),
            Color(0xFF7B5CE0), Color(0xFFE8508A), Color(0xFF3A7BEF),
        ),
        blockStyle = BlockStyle.LIP, cellRadius = 0.22f, corner = 22.dp,
        display = Rubik, body = Rubik, numbers = Rubik,
    )

    val all = listOf(NightShift, TileAndGrout, Mosaic)
}

/** What the player picked: one theme, or follow the system's dark setting. */
enum class ThemeChoice(val key: String) {
    SYSTEM("system"), NIGHT("night"), TILE("tile"), MOSAIC("mosaic");

    /** The theme to draw with. "Match system" is Tile & Grout when light and Night Shift when dark. */
    fun resolve(systemDark: Boolean): TilefitStyle = when (this) {
        SYSTEM -> if (systemDark) Themes.NightShift else Themes.TileAndGrout
        NIGHT -> Themes.NightShift
        TILE -> Themes.TileAndGrout
        MOSAIC -> Themes.Mosaic
    }

    companion object {
        /** Night Shift until the player picks something else. */
        val DEFAULT = NIGHT

        fun of(key: String?): ThemeChoice = entries.firstOrNull { it.key == key } ?: DEFAULT

        fun of(style: TilefitStyle): ThemeChoice = entries.first { it.key == style.key }
    }
}

val LocalTilefit = staticCompositionLocalOf { Themes.NightShift }

private fun typographyOf(s: TilefitStyle): Typography {
    val base = Typography()
    fun TextStyle.display() = copy(fontFamily = s.display)
    fun TextStyle.body() = copy(fontFamily = s.body)
    return Typography(
        displayLarge = base.displayLarge.display(), displayMedium = base.displayMedium.display(),
        displaySmall = base.displaySmall.display(), headlineLarge = base.headlineLarge.display(),
        headlineMedium = base.headlineMedium.display(), headlineSmall = base.headlineSmall.display(),
        titleLarge = base.titleLarge.body(), titleMedium = base.titleMedium.body(), titleSmall = base.titleSmall.body(),
        bodyLarge = base.bodyLarge.body(), bodyMedium = base.bodyMedium.body(), bodySmall = base.bodySmall.body(),
        labelLarge = base.labelLarge.body(), labelMedium = base.labelMedium.body(), labelSmall = base.labelSmall.body(),
    )
}

@Composable
fun TilefitTheme(style: TilefitStyle, content: @Composable () -> Unit) {
    val base = if (style.dark) darkColorScheme() else lightColorScheme()
    val colors = base.copy(
        primary = style.accent, onPrimary = style.onAccent,
        secondary = style.hot, onSecondary = style.onHot,
        background = style.background, onBackground = style.ink,
        surface = style.background, onSurface = style.ink,
        surfaceVariant = style.panel, onSurfaceVariant = style.muted,
        outline = style.outline,
    )
    MaterialTheme(colorScheme = colors, typography = typographyOf(style)) {
        CompositionLocalProvider(LocalTilefit provides style, content = content)
    }
}
