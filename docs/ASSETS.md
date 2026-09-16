# Asset inventory — one file = one art piece

The cinematic parlor screenshot is a **mood composite only**. In-game we load **separate assets** and Compose layers them.

## Scene
| File | Role |
|------|------|
| `bg_home.webp` | Home / splash room (no gameplay HUD) |
| `bg_table.webp` | In-match room behind table (no characters, no player hand) |
| `bg_parlor.webp` | Alternate parlor (optional) |
| `emblem_fa.webp` | Gold 發 medallion on felt center |

## Cast (bust / 3:4, transparent-friendly solid bokeh)
| File | Character | Seat |
|------|-----------|------|
| `char_hao.webp` | 阿豪 suited host | Across (top) |
| `char_meimei.webp` | 小美 hostess | Left |
| `char_yaya.webp` | 雅雅 hostess | Right |
| `char_you.webp` | Optional self avatar | Bottom HUD |

## UI chrome
| File | Role |
|------|------|
| `ui_nameplate.webp` | Dark lacquer name bar |
| `ui_btn_hu.webp` | Claim button — 胡 |
| `ui_btn_pong.webp` | Claim button — 碰 |
| `ui_btn_kong.webp` | Claim button — 槓 |
| `ui_btn_chi.webp` | Claim button — 吃 |
| `ui_btn_pass.webp` | Pass |
| `ui_timer.webp` | Timer ring frame |
| `ui_chip.webp` | Currency chip icon |
| `ui_compass.webp` | Wind / wall remaining frame |

## Tiles (each rank = one PNG/WebP)
See [TILE_PROMPTS.md](TILE_PROMPTS.md). Pattern: `tile_c1`…`tile_c9`, `tile_b1`…`tile_b9`, `tile_d1`…`tile_d9`, winds, dragons, flowers, `tile_back`.

**Shipped bitmaps today:** listed in TILE_PROMPTS “Status”. Missing ranks still draw procedurally until filled.

## Shipped now (`drawable-nodpi/`)

**Scene:** `bg_home`, `bg_table` (empty room — no people baked in), `bg_parlor`, `emblem_fa`  
**Cast:** `char_hao`, `char_meimei`, `char_yaya` (separate portraits)  
**UI:** `ui_chip`, `ui_timer`, `ui_btn_hu`, `ui_btn_pong`, `ui_btn_kong`, `ui_btn_chi`, `ui_btn_pass`, `ui_nameplate`  
**Tiles:** full 萬 `c1–c9`; 索 `b1,b3,b5,b9`; 筒 `d1,d5,d9`; all winds; 中發白; `tile_back`

Still procedural until generated: 索 2/4/6/7/8, 筒 2/3/4/6/7/8, flowers/seasons.

## Layer order (table screen)
1. `bg_table`
2. `char_*` portraits (left / top / right)
3. Felt plane + `emblem_fa`
4. Wall / discard tile sprites
5. HUD + hand tiles

## Art agent rule
Generate **one subject per image**. Do not bake the full table + three people + hand into a single gameplay texture.
