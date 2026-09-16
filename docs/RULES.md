# House rules — Taiwan Mahjong (this app)

Canonical reference for the engine. Casual Taiwanese 16-tile with flowers.

## Basics
- 144 tiles: 136 suits/honors + 8 flowers/seasons.
- Hand size 16; dealer starts with 17. Win = **5 sets + 1 pair**.
- Sets: chow (chi), pung, kong. Flowers are not sets — expose + replace from dead wall.
- Dead wall: 16 tiles, replenishing. Kong/flower replacements from dead wall.

## Claims (on discard)
Priority: **Hu > Kong > Pong > Chi**. Chi only from the previous player (left of discarder in counterclockwise play).

## Dealer & rounds
- Four rounds: East → South → West → North.
- Dealer stays after dealer win or draw; rotates after non-dealer win.
- Seat winds rotate with the dealer.

## Scoring (tai, additive)
- Unit: **tai**. Payment = `tai × stake` (stake from lobby, e.g. 50).
- Self-draw: each loser pays winner.
- Discard win: discarder only pays.
- Dealer win or dealer loss: **+1 tai** to the hand value for payment purposes (common casual house rule).
- Minimum to hu: **1 tai** (no-flower counts as 1 tai when applicable).

### Flower tai
- Each flower/season: 1 tai
- No flowers: 1 tai
- Seat flower / season matching seat wind: extra 1 tai (in addition to the base 1)
- All 8 flowers/seasons: limit hand (30 tai, ignore other patterns)
- Seven flowers + rob 8th: limit (20 tai)

### Pattern tai (v1 set)
Implemented in engine; expand as needed:
- Self-draw: 1
- Fully concealed hand: 1
- Pung/kong of dragons: 1 each
- Seat wind pung/kong: 1
- Round wind pung/kong: 1
- All pungs: 4
- Half flush: 4
- Full flush: 8
- Little three dragons: 4
- Big three dragons: 8
- Little four winds: 8
- Big four winds: 16
- All honors: 8
- Concealed kong: 1 each (open kong: 0 extra beyond honor/wind value)
- Rob kong / win on replacement: treat as self-draw for payment + 1 tai self-draw

Exact pattern list and edge cases live in `TaiScorer` tests — those tests are the source of truth if this doc drifts.
