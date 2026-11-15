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

- Every square of a placed piece: 1 point.
- Clearing: with `g` rows, columns and boxes cleared by one piece, `10 * g * (g + 1) / 2` points: 10, 30, 60,
  100, 150, ...
- Streak: a piece that clears at least one group extends the streak by one; a piece that clears nothing resets it
  to zero. The clearing points are multiplied by the streak length (x1 for the first clearing piece, x2 for a
  second clearing piece in a row, ...). The piece's own squares are not multiplied.

So a piece of 4 squares that clears two groups as the third clearing piece in a row scores `4 + 30 * 3 = 94`.

## Design choices

Blockdoku-style games differ on the exact point values. These are Tilefit's own: the board, the clearing of rows,
columns and boxes, the three-piece hand and the end condition are the standard rules; the numbers above are not
taken from any particular game. The piece set is original: 17 shapes of 1 to 6 squares, in every orientation they
can take, with smaller shapes dealt somewhat more often.
