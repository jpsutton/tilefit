package io.github.jpsutton.tilefit.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jpsutton.tilefit.game.Game
import io.github.jpsutton.tilefit.game.GameState
import io.github.jpsutton.tilefit.game.Piece
import io.github.jpsutton.tilefit.game.Pieces
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
    val s = LocalTilefit.current
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
        fade.animateTo(0f, tween(600))
        if (popupKey == e.id) popup = null
    }

    BoxWithConstraints(
        Modifier.fillMaxSize().background(s.background).safeDrawingPadding()
            .onGloballyPositioned { rootOrigin = it.positionInRoot() },
    ) {
        // Header, meters and the hand take what the board leaves; the hand is a third of the board tall.
        val boardSize: Dp = minOf(maxWidth - 32.dp, (maxHeight - 190.dp) * 3f / 4f).coerceAtLeast(120.dp)
        val boardPad = 5.dp
        val density = LocalDensity.current
        val cellPx = with(density) { (boardSize - boardPad * 2).toPx() } / SIZE
        val cellDp = with(density) { cellPx.toDp() }

        // Where the dragged piece would land, or null if it is off the board or does not fit there.
        val d = drag
        val target: Pair<Int, Int>? = d?.let {
            val topLeft = it.pointer - Offset(it.piece.width * cellPx / 2, it.piece.height * cellPx / 2 + LIFT_CELLS * cellPx)
            val col = ((topLeft.x - boardOrigin.x) / cellPx).roundToInt()
            val row = ((topLeft.y - boardOrigin.y) / cellPx).roundToInt()
            if (Game.fits(state.board, it.piece, row, col)) row to col else null
        }
        val outcome = if (d != null && target != null) Game.outcome(state, d.piece, target.first, target.second) else null
        val preview = outcome?.cleared ?: emptySet()

        Column(Modifier.fillMaxSize().padding(bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Header(state.score, vm.best, onMenu, Modifier.width(boardSize))
            Spacer(Modifier.height(14.dp))
            Row(Modifier.width(boardSize), verticalAlignment = Alignment.CenterVertically) {
                StreakMeter(state.streak, Modifier.weight(1f))
                Spacer(Modifier.width(18.dp))
                TierPips(Pieces.difficultyFor(state.score))
            }
            Box(Modifier.height(40.dp).width(boardSize), contentAlignment = Alignment.Center) {
                popup?.let { e ->
                    val parts = buildList {
                        if (e.groups > 1) add("${e.groups} at once")
                        if (e.streak > 1) add("streak ×${e.streak}")
                        add("+${e.points}")
                    }
                    Chip(parts.joinToString("  ·  "), s.hot, s.onHot, Modifier.alpha(0.35f + 0.65f * fade.value))
                }
            }
            Box(
                Modifier.size(boardSize).clip(RoundedCornerShape(boardPad * 2.6f)).background(s.grout).padding(boardPad),
            ) {
                Canvas(
                    Modifier.fillMaxSize()
                        .semantics { contentDescription = "Board, ${state.board.count} of ${SIZE * SIZE} squares filled" }
                        .onGloballyPositioned { boardOrigin = it.positionInRoot() },
                ) {
                    for (r in 0 until SIZE) for (c in 0 until SIZE) {
                        val i = cellIndex(r, c)
                        val tl = Offset(c * cellPx, r * cellPx)
                        when {
                            // Existing blocks that would clear get the highlight color.
                            state.board.isFilled(i) && i in preview -> drawBlock(s, s.hot, tl, cellPx)
                            state.board.isFilled(i) -> drawBlock(s, s.blockColor(state.board.tone(i)), tl, cellPx)
                            else -> drawEmptyCell(s, r, c, tl, cellPx)
                        }
                    }
                    if (d != null && target != null) {
                        for (c in d.piece.cells) {
                            val i = cellIndex(target.first + c.row, target.second + c.col)
                            drawGhost(s, if (i in preview) s.hot else s.pieceColor(d.piece), Offset((i % SIZE) * cellPx, (i / SIZE) * cellPx), cellPx)
                        }
                    }
                    if (fade.value > 0f) {
                        for (i in fadeCells) drawBlock(s, s.hot, Offset((i % SIZE) * cellPx, (i / SIZE) * cellPx), cellPx, alpha = fade.value)
                    }
                }
                // What the move would score, next to where the piece would land.
                if (d != null && target != null && outcome != null) {
                    val x = ((target.second + d.piece.width) * cellPx).coerceAtMost(SIZE * cellPx - cellPx * 1.8f)
                    val y = (target.first * cellPx - cellPx * 0.7f).coerceAtLeast(0f)
                    val clears = outcome.groups > 0
                    Chip(
                        "+${outcome.points}",
                        if (clears) s.hot else s.panel,
                        if (clears) s.onHot else s.ink,
                        Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) },
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Hand(state, drag?.slot, boardSize, cellDp) { slot, piece ->
                Modifier
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
                    }
            }
        }

        // The piece under the finger, drawn over everything at full board scale.
        if (d != null) {
            Canvas(Modifier.fillMaxSize()) {
                val tl = d.pointer - rootOrigin -
                    Offset(d.piece.width * cellPx / 2, d.piece.height * cellPx / 2 + LIFT_CELLS * cellPx)
                translate(tl.x, tl.y) { drawPiece(s, d.piece, cellPx * 1.04f, alpha = if (target != null) 1f else 0.75f) }
            }
        }

        if (state.isOver) {
            GameOver(state, vm.best, vm.newBest, onAgain = vm::start, onMenu = onMenu)
        }
    }
}

@Composable
private fun Header(score: Int, best: Int, onMenu: () -> Unit, modifier: Modifier) {
    val s = LocalTilefit.current
    Row(modifier.padding(top = 8.dp), verticalAlignment = Alignment.Top) {
        Stat("Score", score.grouped(), Modifier.weight(1f), size = 52.sp)
        Stat("Best", maxOf(best, score).grouped(), Modifier.padding(end = 12.dp, top = 2.dp), size = 20.sp, color = s.accent, alignEnd = true)
        IconButtonBox(Icon.MENU, "Menu", onMenu)
    }
}

/** The three slots of the hand. [slotModifier] wires the drag gesture to each slot. */
@Composable
private fun Hand(
    state: GameState,
    held: Int?,
    boardSize: Dp,
    boardCell: Dp,
    slotModifier: (Int, Piece?) -> Modifier,
) {
    val s = LocalTilefit.current
    val height = boardSize / Game.HAND
    val trayPad = if (s.tray != null) 10.dp else 0.dp
    Row(
        Modifier.width(boardSize).height(height).clip(RoundedCornerShape(s.corner + 4.dp))
            .then(if (s.tray != null) Modifier.background(s.tray) else Modifier)
            .padding(trayPad),
        horizontalArrangement = Arrangement.spacedBy(if (s.slot != null) 10.dp else 0.dp),
    ) {
        for (slot in 0 until Game.HAND) {
            val piece = state.hand[slot]
            val playable = piece != null && Game.fitsAnywhere(state.board, piece)
            val holding = held == slot
            val shape = RoundedCornerShape(s.corner)
            Box(
                Modifier.weight(1f).fillMaxSize()
                    .then(
                        when {
                            holding -> Modifier.drawBehind {
                                val w = 1.5.dp.toPx()
                                drawRoundRect(
                                    s.muted.copy(alpha = 0.6f), cornerRadius = CornerRadius(s.corner.toPx()),
                                    style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
                                )
                            }
                            s.slot != null -> Modifier.clip(shape).background(s.slot)
                            else -> Modifier
                        },
                    )
                    .then(slotModifier(slot, piece)),
                contentAlignment = Alignment.Center,
            ) {
                if (piece != null && !holding) {
                    // Tray pieces are smaller than on the board: at most half a board cell per square.
                    val cell = minOf(boardCell * 0.55f, (height - trayPad * 2) / 5.5f)
                    PieceView(
                        piece, cell,
                        Modifier.semantics { contentDescription = if (playable) "Piece of ${piece.size} squares" else "Piece of ${piece.size} squares, does not fit" },
                        alpha = if (playable) 1f else 0.3f,
                    )
                }
            }
        }
    }
}

@Composable
private fun GameOver(state: GameState, best: Int, newBest: Boolean, onAgain: () -> Unit, onMenu: () -> Unit) {
    val s = LocalTilefit.current
    Box(
        Modifier.fillMaxSize().background(s.background.copy(alpha = 0.97f))
            .pointerInput(Unit) { detectDragGestures { c, _ -> c.consume() } },
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Label("No room left", color = s.hot)
                Number(state.score.grouped(), 84.sp, color = s.accent)
                if (newBest) {
                    Chip("NEW BEST", s.accent, s.onAccent)
                } else {
                    Text("Best ${best.grouped()}", style = TextStyle(fontFamily = s.body, fontSize = 16.sp), color = s.muted)
                }
            }
            Column {
                StatLine("Pieces placed", state.moves.grouped())
                StatLine("Groups cleared", state.groupsCleared.grouped())
                StatLine("Longest streak", "${state.longestStreak}")
                StatLine("Biggest clear", if (state.biggestClear > 0) "${state.biggestClear} at once" else "none")
                StatLine("Tier reached", "${Pieces.difficultyFor(state.score) + 1} / 3")
            }
            val stuck = state.hand.filterNotNull()
            if (stuck.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Label("These did not fit")
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        for (p in stuck) PieceView(p, 16.dp)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            PrimaryButton("Play again", onAgain)
            RowButton("Menu", onMenu, icon = null)
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    val s = LocalTilefit.current
    Row(
        Modifier.fillMaxWidth()
            .drawBehind { drawLine(s.outline, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
            .padding(vertical = 13.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label(label, Modifier.weight(1f))
        Text(value, style = TextStyle(fontFamily = s.numbers, fontWeight = FontWeight.Bold, fontSize = 18.sp), color = s.ink)
    }
}
