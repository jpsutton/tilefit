package io.github.jpsutton.tilefit.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jpsutton.tilefit.game.BOX
import io.github.jpsutton.tilefit.game.Board
import io.github.jpsutton.tilefit.game.Game
import io.github.jpsutton.tilefit.game.Piece
import io.github.jpsutton.tilefit.game.SIZE
import io.github.jpsutton.tilefit.game.cellIndex
import kotlin.math.roundToInt

/** How far above the fingertip the dragged piece floats, in board cells, so the finger does not hide it. */
private const val LIFT_CELLS = 1.6f

/** A piece being dragged: its hand slot and where the finger is, in root coordinates. */
private class Drag(val slot: Int, val piece: Piece, pointer: Offset) {
    var pointer by mutableStateOf(pointer)
}

@Composable
fun GameScreen(vm: GameViewModel, onMenu: () -> Unit) {
    val state = vm.game ?: return
    val haptics = LocalHapticFeedback.current
    var drag by remember { mutableStateOf<Drag?>(null) }
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    var boardOrigin by remember { mutableStateOf(Offset.Zero) }
    val slotOrigins = remember { Array(Game.HAND) { Offset.Zero } }
    // Cells fading out after a clear, and the points popup.
    val fade = remember { Animatable(0f) }
    var fadeCells by remember { mutableStateOf(emptySet<Int>()) }
    var popup by remember { mutableStateOf<ClearEvent?>(null) }
    var popupKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(vm.clearEvent?.id) {
        val e = vm.clearEvent ?: return@LaunchedEffect
        fadeCells = e.cells
        popup = e
        popupKey = e.id
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        fade.snapTo(1f)
        fade.animateTo(0f, tween(450))
        if (popupKey == e.id) popup = null
    }

    val primary = MaterialTheme.colorScheme.primary
    val colors = BoardColors(
        empty = MaterialTheme.colorScheme.surfaceVariant,
        emptyAlt = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        block = primary,
        clear = MaterialTheme.colorScheme.secondary,
        flash = MaterialTheme.colorScheme.tertiary,
    )

    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().onGloballyPositioned { rootOrigin = it.positionInRoot() }) {
        // Header and the piece tray take what the board leaves; the tray slots are a third of the board wide.
        val boardSize: Dp = minOf(maxWidth - 24.dp, (maxHeight - 130.dp) * 3f / 4f).coerceAtLeast(120.dp)
        val boardPx = with(LocalDensity.current) { boardSize.toPx() }
        val cellPx = boardPx / SIZE
        val slotSize = boardSize / Game.HAND

        // Where the dragged piece would land, or null if it is off the board or does not fit there.
        val d = drag
        val target: Pair<Int, Int>? = d?.let {
            val topLeft = it.pointer - Offset(it.piece.width * cellPx / 2, it.piece.height * cellPx / 2 + LIFT_CELLS * cellPx)
            val col = ((topLeft.x - boardOrigin.x) / cellPx).roundToInt()
            val row = ((topLeft.y - boardOrigin.y) / cellPx).roundToInt()
            if (Game.fits(state.board, it.piece, row, col)) row to col else null
        }
        val preview = if (d != null && target != null) Game.clearing(state.board, d.piece, target.first, target.second) else emptySet()

        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Header(state.score, vm.best, state.streak, onMenu, Modifier.width(boardSize))
            Box(Modifier.height(28.dp).width(boardSize), contentAlignment = Alignment.Center) {
                popup?.let { e ->
                    val combo = if (e.groups > 1) "${e.groups} lines! " else ""
                    val streak = if (e.streak > 1) "streak x${e.streak}  " else ""
                    Text(
                        "$streak$combo+${e.points}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.alpha(0.3f + 0.7f * fade.value),
                    )
                }
            }
            Canvas(
                Modifier.size(boardSize)
                    .semantics { contentDescription = "Board, ${state.board.count} of ${SIZE * SIZE} squares filled" }
                    .onGloballyPositioned { boardOrigin = it.positionInRoot() },
            ) {
                drawBoard(state.board, colors, cellPx)
                // Existing blocks that would clear get highlighted; the ghost of the piece goes on top.
                for (i in preview) {
                    if (state.board.isFilled(i)) drawCell(i, colors.clear, cellPx)
                }
                if (d != null && target != null) {
                    for (c in d.piece.cells) {
                        val i = cellIndex(target.first + c.row, target.second + c.col)
                        drawCell(i, if (i in preview) colors.clear else colors.block, cellPx, alpha = 0.55f)
                    }
                }
                if (fade.value > 0f) {
                    for (i in fadeCells) drawCell(i, colors.flash, cellPx, alpha = fade.value)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.width(boardSize)) {
                for (slot in 0 until Game.HAND) {
                    val piece = state.hand[slot]
                    val playable = piece != null && Game.fitsAnywhere(state.board, piece)
                    Box(
                        Modifier.size(slotSize)
                            .onGloballyPositioned { slotOrigins[slot] = it.positionInRoot() }
                            .pointerInput(piece, state.isOver, cellPx) {
                                if (piece == null || state.isOver) return@pointerInput
                                detectDragGestures(
                                    onDragStart = { local -> drag = Drag(slot, piece, slotOrigins[slot] + local) },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        drag?.let { it.pointer += amount }
                                    },
                                    onDragEnd = {
                                        val cur = drag
                                        drag = null
                                        if (cur != null) {
                                            val tl = cur.pointer - Offset(
                                                cur.piece.width * cellPx / 2,
                                                cur.piece.height * cellPx / 2 + LIFT_CELLS * cellPx,
                                            )
                                            val col = ((tl.x - boardOrigin.x) / cellPx).roundToInt()
                                            val row = ((tl.y - boardOrigin.y) / cellPx).roundToInt()
                                            if (vm.place(cur.slot, row, col) != null) {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        }
                                    },
                                    onDragCancel = { drag = null },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (piece != null) {
                            val trayCell = slotSize / 5.5f
                            val hidden = drag?.slot == slot
                            Canvas(
                                Modifier.size(trayCell * piece.width, trayCell * piece.height)
                                    .alpha(if (hidden) 0.15f else if (playable) 1f else 0.35f)
                                    .semantics { contentDescription = "Piece of ${piece.size} squares" },
                            ) {
                                drawPiece(piece, colors.block, with(this) { trayCell.toPx() })
                            }
                        }
                    }
                }
            }
        }

        // The piece under the finger, drawn over everything at full board scale.
        if (d != null) {
            Canvas(Modifier.fillMaxSize()) {
                val tl = d.pointer - rootOrigin -
                    Offset(d.piece.width * cellPx / 2, d.piece.height * cellPx / 2 + LIFT_CELLS * cellPx)
                translateTo(tl) { drawPiece(d.piece, colors.block, cellPx, alpha = if (target != null) 0.95f else 0.7f) }
            }
        }

        if (state.isOver) {
            GameOver(state.score, vm.best, vm.newBest, onAgain = vm::start, onMenu = onMenu)
        }
    }
}

private class BoardColors(val empty: Color, val emptyAlt: Color, val block: Color, val clear: Color, val flash: Color)

@Composable
private fun Header(score: Int, best: Int, streak: Int, onMenu: () -> Unit, modifier: Modifier) {
    Row(modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).semantics(mergeDescendants = true) { contentDescription = "Score $score" }) {
            Text("SCORE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "$score",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "Best $best" }) {
            Text("BEST", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${maxOf(best, score)}", style = MaterialTheme.typography.titleLarge)
        }
        TextButton(onClick = onMenu, Modifier.heightIn(min = 48.dp)) { Text("Menu") }
    }
}

@Composable
private fun GameOver(score: Int, best: Int, newBest: Boolean, onAgain: () -> Unit, onMenu: () -> Unit) {
    Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { c, _ -> c.consume() } }, contentAlignment = Alignment.Center) {
        Card(Modifier.padding(32.dp).fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Game over", style = MaterialTheme.typography.headlineSmall)
                Text("$score", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                Text(if (newBest) "New best score!" else "Best: $best", style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onAgain, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Play again") }
                OutlinedButton(onClick = onMenu, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Menu") }
            }
        }
    }
}

private fun DrawScope.drawBoard(board: Board, c: BoardColors, cell: Float) {
    for (r in 0 until SIZE) for (col in 0 until SIZE) {
        val alt = ((r / BOX) + (col / BOX)) % 2 == 1
        val i = cellIndex(r, col)
        drawCell(i, if (board.isFilled(i)) c.block else if (alt) c.emptyAlt else c.empty, cell)
    }
}

private fun DrawScope.drawCell(index: Int, color: Color, cell: Float, alpha: Float = 1f) {
    val gap = cell * 0.06f
    drawRoundRect(
        color,
        topLeft = Offset((index % SIZE) * cell + gap, (index / SIZE) * cell + gap),
        size = Size(cell - 2 * gap, cell - 2 * gap),
        cornerRadius = CornerRadius(cell * 0.18f),
        alpha = alpha,
    )
}

private fun DrawScope.drawPiece(piece: Piece, color: Color, cell: Float, alpha: Float = 1f) {
    val gap = cell * 0.06f
    for (c in piece.cells) {
        drawRoundRect(
            color,
            topLeft = Offset(c.col * cell + gap, c.row * cell + gap),
            size = Size(cell - 2 * gap, cell - 2 * gap),
            cornerRadius = CornerRadius(cell * 0.18f),
            alpha = alpha,
        )
    }
}

private fun DrawScope.translateTo(offset: Offset, block: DrawScope.() -> Unit) {
    drawContext.transform.translate(offset.x, offset.y)
    block()
    drawContext.transform.translate(-offset.x, -offset.y)
}
