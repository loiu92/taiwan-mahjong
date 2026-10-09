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

## Replay and player views

`ReplaySession` records initial `HandSetup` and accepted commands in a versioned
`ReplayLog`. Invalid, out-of-turn and unchanged commands are excluded. Bot steps
are normalized to the chosen player intent, so saved games do not depend on a
future bot-policy version. `ReplaySession.restore(log)` validates and replays every
command; malformed or unsupported journals fail rather than silently diverge.

The journal contains the deal seed and is **trusted server/local data**. Never
send `ReplayLog`, `HandSetup` or `GameState` to a remote player. Use
`session.viewFor(seat)` / `state.viewFor(seat)`: only that player's hand, draw and
claim choices are visible; opponents expose concealed counts and hide concealed
kong faces. Views and journal/state snapshots own immutable nested collections.
Authentication must select the viewer seat in a future remote session.

The Android table consumes `PlayerView`; the local controller keeps full state to
run bots. Signed chip balances remain supported across hands, matching settlement.

Validation (JDK17 and Android SDK):

```sh
./gradlew :engine:test :android:assembleDebug --no-daemon
```
