# Taiwan Mahjong

Landscape Android Taiwanese Mahjong (16-tile) with full tai scoring and local AI opponents.

## Modules

- `engine/` — pure Kotlin rules, scoring, bots (portable to Swift / Cloudflare DO later)
- `android/` — Jetpack Compose table UI

## House rules

See [docs/RULES.md](docs/RULES.md).

## Build

```bash
export JAVA_HOME=~/.local/jdk-17
export ANDROID_HOME=~/Android/Sdk
./gradlew :engine:test :android:assembleDebug
```

APK: `android/build/outputs/apk/debug/android-debug.apk`

## Play

1. Launch app (landscape)
2. Set stake → **Solo vs AI**
3. Tap a tile to select, tap again (or Discard) to discard
4. Claim Chi/Pong/Kong/Hu when prompted; toggle **Auto** for bot-assisted play

## Later

- Cloudflare Durable Objects multiplayer behind `GameSession`
- iOS Swift UI reusing / porting `engine`
