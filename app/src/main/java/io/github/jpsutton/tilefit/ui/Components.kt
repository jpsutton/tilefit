package io.github.jpsutton.tilefit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jpsutton.tilefit.game.BOX
import io.github.jpsutton.tilefit.game.Board
import io.github.jpsutton.tilefit.game.Piece
import io.github.jpsutton.tilefit.game.SIZE
import java.text.NumberFormat

// ---- Drawing cells and blocks ----

/** An empty board cell at [topLeft], [size] wide, shaded by which 3x3 box it is in. */
fun DrawScope.drawEmptyCell(s: TilefitStyle, row: Int, col: Int, topLeft: Offset, size: Float) {
    val alt = ((row / BOX) + (col / BOX)) % 2 == 1
    val gap = size * 0.06f
    drawRoundRect(
        if (alt) s.cellAlt else s.cell,
        topLeft = topLeft + Offset(gap, gap),
        size = Size(size - 2 * gap, size - 2 * gap),
        cornerRadius = CornerRadius(size * s.cellRadius),
    )
}

/** One block in the theme's [BlockStyle]. */
fun DrawScope.drawBlock(s: TilefitStyle, color: Color, topLeft: Offset, size: Float, alpha: Float = 1f) {
    val gap = size * 0.06f
    val inner = size - 2 * gap
    val tl = topLeft + Offset(gap, gap)
    val radius = CornerRadius(size * s.cellRadius)
    when (s.blockStyle) {
        BlockStyle.LIP -> {
            drawRoundRect(s.edgeOf(color), tl, Size(inner, inner), radius, alpha = alpha)
            drawRoundRect(color, tl, Size(inner, inner - inner * 0.11f), radius, alpha = alpha)
        }
        BlockStyle.GLOW -> {
            for ((grow, a) in listOf(gap * 2.2f to 0.07f, gap * 1.1f to 0.14f)) {
                drawRoundRect(
                    color, tl - Offset(grow, grow), Size(inner + 2 * grow, inner + 2 * grow),
                    CornerRadius(radius.x + grow), alpha = a * alpha,
                )
            }
            drawRoundRect(color, tl, Size(inner, inner), radius, alpha = alpha)
        }
    }
}

/** Where a dragged piece would land: a faint block, outlined in the glow style. */
fun DrawScope.drawGhost(s: TilefitStyle, color: Color, topLeft: Offset, size: Float) {
    val gap = size * 0.06f
    val inner = size - 2 * gap
    val tl = topLeft + Offset(gap, gap)
    val radius = CornerRadius(size * s.cellRadius)
    if (s.blockStyle == BlockStyle.GLOW) {
        drawRoundRect(color, tl, Size(inner, inner), radius, alpha = 0.16f)
        val w = size * 0.06f
        drawRoundRect(color, tl + Offset(w / 2, w / 2), Size(inner - w, inner - w), radius, style = Stroke(w))
    } else {
        drawRoundRect(color, tl, Size(inner, inner), radius, alpha = 0.42f)
    }
}

/** The color of a block of [piece] in theme [s]. */
fun TilefitStyle.pieceColor(piece: Piece): Color = blockColor(piece.tone)

/** [piece] with its top-left corner at the origin, in cells of [cell] px. */
fun DrawScope.drawPiece(s: TilefitStyle, piece: Piece, cell: Float, alpha: Float = 1f, color: Color = s.pieceColor(piece)) {
    for (c in piece.cells) drawBlock(s, color, Offset(c.col * cell, c.row * cell), cell, alpha)
}

/** The board with its blocks, filling the draw area. */
fun DrawScope.drawBoard(s: TilefitStyle, board: Board, cell: Float) {
    for (r in 0 until SIZE) for (c in 0 until SIZE) {
        val i = r * SIZE + c
        val tl = Offset(c * cell, r * cell)
        if (board.isFilled(i)) drawBlock(s, s.blockColor(board.tone(i)), tl, cell) else drawEmptyCell(s, r, c, tl, cell)
    }
}

/** A small, non-interactive picture of [board], for the menu and the theme picker. */
@Composable
fun BoardThumb(board: Board, size: Dp, modifier: Modifier = Modifier, style: TilefitStyle = LocalTilefit.current) {
    val pad = size * 0.03f
    Box(
        modifier.size(size).clip(RoundedCornerShape(size * 0.07f)).background(style.grout).padding(pad),
    ) {
        Canvas(Modifier.size(size - pad * 2)) { drawBoard(style, board, this.size.width / SIZE) }
    }
}

/** One piece, drawn at [cell] per square. */
@Composable
fun PieceView(piece: Piece, cell: Dp, modifier: Modifier = Modifier, alpha: Float = 1f, style: TilefitStyle = LocalTilefit.current) {
    Canvas(modifier.size(cell * piece.width, cell * piece.height)) {
        drawPiece(style, piece, cell.toPx(), alpha)
    }
}

// ---- Text ----

/** The small spaced capitals over a number: SCORE, BEST, STREAK. */
@Composable
fun Label(text: String, modifier: Modifier = Modifier, color: Color = LocalTilefit.current.muted) {
    val s = LocalTilefit.current
    Text(
        text.uppercase(), modifier,
        style = TextStyle(fontFamily = s.numbers, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp),
        color = color,
    )
}

/** A big number in the theme's number font. */
@Composable
fun Number(text: String, size: TextUnit, modifier: Modifier = Modifier, color: Color = LocalTilefit.current.ink) {
    val s = LocalTilefit.current
    Text(
        text, modifier,
        style = TextStyle(
            fontFamily = s.numbers, fontSize = size, fontWeight = FontWeight.ExtraBold,
            letterSpacing = (size.value * -0.03f).sp, lineHeight = size,
        ),
        color = color,
    )
}

// ---- Buttons ----

/** The main action of a screen. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, trailing: String? = null) {
    val s = LocalTilefit.current
    val shape = RoundedCornerShape(s.corner)
    Row(
        modifier.fillMaxWidth().heightIn(min = 60.dp).clip(shape).background(s.edgeOrSelf(s.accent), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(bottom = if (s.blockStyle == BlockStyle.LIP) 5.dp else 0.dp)
            .clip(shape).background(s.accent)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (trailing == null) Arrangement.Center else Arrangement.SpaceBetween,
    ) {
        Text(text, style = TextStyle(fontFamily = s.display, fontWeight = FontWeight.Bold, fontSize = 19.sp), color = s.onAccent)
        if (trailing != null) {
            Text(trailing, style = TextStyle(fontFamily = s.numbers, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp), color = s.onAccent)
        }
    }
}

private fun TilefitStyle.edgeOrSelf(c: Color) = if (blockStyle == BlockStyle.LIP) edgeOf(c) else c

/** A secondary choice: a panel row with a label and an optional trailing icon. */
@Composable
fun RowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    icon: Icon? = Icon.CHEVRON,
) {
    val s = LocalTilefit.current
    val shape = RoundedCornerShape(s.corner)
    Row(
        modifier.fillMaxWidth().heightIn(min = 56.dp).clip(shape).background(s.panel)
            .border(BorderStroke(1.dp, s.outline), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text, Modifier.weight(1f), style = TextStyle(fontFamily = s.body, fontWeight = FontWeight.SemiBold, fontSize = 17.sp), color = s.ink)
        if (detail != null) Text(detail, style = TextStyle(fontFamily = s.body, fontSize = 15.sp), color = s.muted)
        if (icon != null) IconGlyph(icon, s.muted, 18.dp)
    }
}

/** A square icon button, 44dp: menu, back. */
@Composable
fun IconButtonBox(icon: Icon, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalTilefit.current
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier.size(48.dp).clip(shape).background(s.panel).border(BorderStroke(1.dp, s.outline), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { IconGlyph(icon, s.ink, 20.dp) }
}

/** The few line icons the app uses, drawn so there is no icon library to ship. */
enum class Icon { MENU, BACK, CHEVRON, PLAY, PLUS, CHECK }

@Composable
fun IconGlyph(icon: Icon, color: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val u = this.size.width / 24f
        val stroke = Stroke(width = 2.2f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun line(vararg pts: Pair<Float, Float>) {
            val p = Path()
            pts.forEachIndexed { i, (x, y) -> if (i == 0) p.moveTo(x * u, y * u) else p.lineTo(x * u, y * u) }
            drawPath(p, color, style = stroke)
        }
        when (icon) {
            Icon.MENU -> { line(4f to 7f, 20f to 7f); line(4f to 12f, 20f to 12f); line(4f to 17f, 20f to 17f) }
            Icon.BACK -> line(15f to 6f, 9f to 12f, 15f to 18f)
            Icon.CHEVRON -> line(9f to 6f, 15f to 12f, 9f to 18f)
            Icon.PLAY -> {
                val p = Path().apply { moveTo(7f * u, 4f * u); lineTo(20f * u, 12f * u); lineTo(7f * u, 20f * u); close() }
                drawPath(p, color)
            }
            Icon.PLUS -> { line(12f to 5f, 12f to 19f); line(5f to 12f, 19f to 12f) }
            Icon.CHECK -> line(5f to 12.5f, 10f to 17.5f, 19f to 7f)
        }
    }
}

// ---- Meters ----

/** Five segments; lit up to [streak], and a count once it goes past five. */
@Composable
fun StreakMeter(streak: Int, modifier: Modifier = Modifier) {
    val s = LocalTilefit.current
    Row(
        modifier.semantics(mergeDescendants = true) { contentDescription = "Streak $streak" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Label("Streak")
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0 until 5) {
                Box(
                    Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(2.dp))
                        .background(if (i < streak) s.hot else s.outline),
                )
            }
        }
        Text(
            "×$streak", Modifier.width(34.dp),
            style = TextStyle(fontFamily = s.numbers, fontWeight = FontWeight.Bold, fontSize = 14.sp),
            color = if (streak > 0) s.hot else s.muted,
        )
    }
}

/** Which difficulty step dealing is at: three marks, [step] (0 to 2) + 1 of them lit. */
@Composable
fun TierPips(step: Int, modifier: Modifier = Modifier) {
    val s = LocalTilefit.current
    Row(
        modifier.semantics(mergeDescendants = true) { contentDescription = "Difficulty ${step + 1} of 3" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Label("Tier", Modifier.padding(end = 4.dp))
        for (i in 0 until 3) {
            Box(Modifier.size(14.dp, 6.dp).clip(RoundedCornerShape(3.dp)).background(if (i <= step) s.accent else s.outline))
        }
    }
}

/** A small rounded tag: NEW BEST, +28. */
@Composable
fun Chip(text: String, background: Color, color: Color, modifier: Modifier = Modifier) {
    val s = LocalTilefit.current
    Text(
        text,
        modifier.clip(RoundedCornerShape(8.dp)).background(background).padding(PaddingValues(horizontal = 10.dp, vertical = 4.dp)),
        style = TextStyle(fontFamily = s.numbers, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
        color = color,
    )
}

/** A label over a number, as one item for screen readers. */
@Composable
fun Stat(label: String, value: String, modifier: Modifier = Modifier, size: TextUnit = 22.sp, color: Color = LocalTilefit.current.ink, alignEnd: Boolean = false) {
    Column(
        modifier.semantics(mergeDescendants = true) { contentDescription = "$label $value" },
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Label(label)
        Number(value, size, color = color)
    }
}

/** A panel card. */
@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val s = LocalTilefit.current
    val shape = RoundedCornerShape(s.corner + 2.dp)
    Row(
        modifier.fillMaxWidth().clip(shape).background(s.panel).border(BorderStroke(1.dp, s.outline), shape).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

/** Thousands separators: 1,284. */
fun Int.grouped(): String = NumberFormat.getIntegerInstance().format(this)
