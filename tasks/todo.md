# Taiwan Mahjong — Plan & Progress

## Goal
Landscape Android Taiwanese Mahjong (16-tile, full tai scoring), 1 human + 3 local AI, UI inspired by the reference table. Pure-Kotlin `engine` for later Swift / Cloudflare DO multiplayer.

## Locked decisions
- Rules: Taiwanese 16-tile, flowers+seasons, full tai, flat `tai × stake` payouts
- Brand: placeholder `愛台灣 打麻將` / app name **Taiwan Mahjong**
- Stack: Kotlin + Jetpack Compose, minSdk 26 / compileSdk 35
- Repo: `~/loiu92/taiwan-mahjong` (`engine` + `android`)
- Multiplayer: interface seam only in v1

## Checklist
- [x] Scaffold Gradle (`engine` + `android`), README, `docs/RULES.md`
- [x] Engine: tiles, wall, deal, turn FSM, claims, flower/kong replace + tests
- [x] Engine: win detector (5 sets + pair), tai scorer, dealer/rounds, payouts + tests
- [x] Engine: local bot policy + Auto/Manual path
- [x] Android: landscape Compose table UI + ViewModel
- [x] Android: home, stake, hand result (tai breakdown), session scoreboard
- [x] Verify: all tests green + debug APK builds
- [x] GitHub repo `loiu92/taiwan-mahjong`

## Out of scope (v1)
Cloudflare DO, accounts, iOS UI, real-money, polished commercial art.

## Review
- Engine unit tests passing (`./gradlew :engine:test`)
- Debug APK: `android/build/outputs/apk/debug/android-debug.apk`
- Playable solo vs AI with Manual/Auto, claims, tai result screen
- Next: emulator dogfood polish, richer tile art, CF DO multiplayer
