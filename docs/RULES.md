# Tilefit rules

A 9x9 board, split into nine 3x3 boxes. You hold three pieces at a time.

## Turn

- Drag a piece from the tray onto empty squares of the board. Pieces cannot be rotated and cannot overlap blocks
  already placed or hang off the edge.
- When all three pieces are placed, three new random pieces are dealt. There is no timer.
- The game ends when none of the pieces still in your hand fit anywhere on the board. Pieces that no longer fit are
  shown dimmed.

## Clearing

A row, a column or a 3x3 box that is completely filled is cleared (its squares become empty). A single piece can
complete several of these at once; squares shared by them (for example where a row crosses a column) clear once.
Only the piece just placed triggers a clear, and clearing happens before the next piece is placed.

## Scoring

- Every square of a placed piece that is not cleared by that placement: 1 point.
- Clearing: each cleared row, column or box is worth 9. With `g` of them cleared by one piece the clearing points
  are `9 * g * m`, where `m = 2 * ceil(g / 2)`: x2 for one or two, x4 for three or four, x6 for five or six, ...
  A square shared by two cleared groups (where a row crosses a column, say) is removed once but counts in both
  groups.
- Streak: a piece that clears at least one group extends the streak by one; a piece that clears nothing resets it
  to zero. From the second clearing piece in a row, a flat 9 points is added. It does not grow with the streak.

Clearing points for 1 to 7 groups at once: 18, 36, 108, 144, 270, 324, 504. So a 5-square piece that completes one
row with one of its squares scores `4 + 18 = 22`, and `31` if it was the second clearing piece in a row.

## Pieces

45 pieces, each orientation its own piece: dots, bars of 2 to 5 in both directions, corners, squares of 2x2, L, T,
S and Z shapes, a plus, and a few scattered shapes (diagonals) whose squares do not touch. There is no 3x3 square.
Every square of a piece must land on an empty cell; the squares of a piece need not form a rectangle.

Each piece has a weight for three difficulty steps, picked by the score when the hand is dealt: below 180, from
180, and from 600. The easiest step leaves out the diagonals and the cup shapes; later steps deal more of the
bigger pieces. Hands are independent draws, so duplicates happen, and nothing guarantees a hand can be placed.

## Design choices

The board, the clearing of rows, columns and boxes, the three-piece hand and the end condition are the standard
rules. The point values and the piece weights are Tilefit's own numbers; the thresholds and weights live in
`game/Piece.kt` and `game/Game.kt`.
