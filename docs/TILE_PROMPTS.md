# Taiwanese Mahjong Tile Art — Generation Prompts

Hand these prompts to an image-gen agent. Output **one tile per file**, PNG, transparent or neutral grey background, consistent lighting.

## Status (shipped in APK)

Bitmap faces already in `android/src/main/res/drawable-nodpi/`:

- `tile_c1` … `tile_c9` (full wan set)
- `tile_b1`, `tile_b3`, `tile_b5`, `tile_b9`
- `tile_d1`, `tile_d5`, `tile_d9`
- `tile_we`, `tile_ws`, `tile_ww`, `tile_wn`
- `tile_dr`, `tile_dg`, `tile_dw`
- `tile_back`

Compose falls back to procedural drawing for every missing id. Drop new PNGs with the filenames below and they light up automatically (extend the `faceDrawable()` map in `MahjongTile.kt`).

## Global style lock (prepend to EVERY prompt)

```
Taiwanese mahjong tile face, ivory bone acrylic tile, soft bevelled edges, traditional Taiwan carving style, angular Chinese calligraphy, cream-white face (#F7F1E3), thin grey-green edge, product photo centered, sharp high resolution, no English text, no watermark, no logo, no shadow cast on background, neutral light grey backdrop
```

**Aspect ratio:** 3:4 (portrait tile)  
**Size:** 512×682 or 768×1024  
**Filenames:** use ids below (`C1.png` … `F4.png`, `S1.png` …)

Reference mood (already generated):
- `docs/tile-refs/ref-tile-5wan.png` — 伍萬 style
- `docs/tile-refs/ref-tile-1bamboo.png` — 一索 sparrow

---

## Characters / 萬子 (blue numeral + red 萬)

Taiwan convention: **blue traditional numeral on top**, **large red 萬 below**. Use **伍** for 5 (not 五).

| File | Prompt addition |
|------|-----------------|
| `C1.png` | blue 一 on top, large red 萬 below |
| `C2.png` | blue 二 on top, large red 萬 below |
| `C3.png` | blue 三 on top, large red 萬 below |
| `C4.png` | blue 四 on top, large red 萬 below |
| `C5.png` | blue 伍 on top, large red 萬 below |
| `C6.png` | blue 六 on top, large red 萬 below |
| `C7.png` | blue 七 on top, large red 萬 below |
| `C8.png` | blue 八 on top, large red 萬 below |
| `C9.png` | blue 九 on top, large red 萬 below |

Full example for `C5.png`:

```
[GLOBAL STYLE]. Character suit wan tile: traditional Chinese blue numeral 伍 centered in the upper half, large vermillion-red 萬 character centered in the lower half, angular Taiwanese brush/carved style, crisp ink edges.
```

---

## Bamboos / 索子 (green sticks; 1 is sparrow)

Taiwan style: **black** line work on the bird (not blue). Sticks are green with dark nodes.

| File | Prompt addition |
|------|-----------------|
| `B1.png` | one bamboo: green sparrow bird facing left, simple black wing lines, red beak tip |
| `B2.png` | two green bamboo sticks vertical, one above the other |
| `B3.png` | three green bamboo sticks in triangle layout |
| `B4.png` | four green bamboo sticks in 2×2 grid |
| `B5.png` | five green bamboo sticks, plus center |
| `B6.png` | six green bamboo sticks in two columns of three |
| `B7.png` | seven green bamboo sticks, one on top row then two rows of three |
| `B8.png` | eight green bamboo sticks |
| `B9.png` | nine green bamboo sticks in 3×3 grid |

---

## Dots / 筒子 (circles)

Classic red/green coin circles; 1-dot is a large red circle with inner ring.

| File | Prompt addition |
|------|-----------------|
| `D1.png` | single large red coin circle with concentric rings |
| `D2.png` | two circles stacked vertically (red top, green bottom) |
| `D3.png` | three circles triangle |
| `D4.png` | four circles 2×2 alternating red/green |
| `D5.png` | five circles with center |
| `D6.png` | six circles two columns |
| `D7.png` | seven circles |
| `D8.png` | eight circles |
| `D9.png` | nine circles 3×3 |

---

## Winds / 風

Large single black character, Taiwan angular style.

| File | Char |
|------|------|
| `WE.png` | 東 |
| `WS.png` | 南 |
| `WW.png` | 西 |
| `WN.png` | 北 |

Prompt:

```
[GLOBAL STYLE]. Honor wind tile with a single large black Chinese character {CHAR} filling most of the face, angular Taiwanese carved calligraphy.
```

---

## Dragons / 三元

| File | Prompt addition |
|------|-----------------|
| `DR.png` | large red 中 |
| `DG.png` | large green 發 |
| `DW.png` | white dragon: empty rectangle border only (no 白 character), thick black rounded frame on cream face |

---

## Flowers & Seasons / 花草

| File | Char | Meaning |
|------|------|---------|
| `F1.png` | 梅 | plum |
| `F2.png` | 蘭 | orchid |
| `F3.png` | 竹 | bamboo |
| `F4.png` | 菊 | chrysanthemum |
| `S1.png` | 春 | spring |
| `S2.png` | 夏 | summer |
| `S3.png` | 秋 | autumn |
| `S4.png` | 冬 | winter |

Prompt:

```
[GLOBAL STYLE]. Bonus flower/season tile with large magenta-pink Chinese character {CHAR} centered, optional tiny matching botanical motif in corner, still readable as a mahjong tile.
```

---

## Tile back

| File | Prompt |
|------|--------|
| `BACK.png` | mahjong tile back solid deep vermillion red with subtle darker diamond frame, same bevelled ivory-edge silhouette as face tiles, no text |

---

## Consistency checklist for the art agent

1. Same tile silhouette, bevel, and cream face color across all 43 assets (+ back).
2. No English labels on tiles (UI handles EN/ZH separately).
3. Export cropped tight; optional 4% padding inside canvas.
4. Drop finished PNGs into `android/src/main/res/drawable-nodpi/tiles/` named exactly as above.
5. After drop-in, wire `MahjongTile` to load `painterResource` by tile id (Compose already draws procedural faces as fallback).
