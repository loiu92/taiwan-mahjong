package com.loiu92.taiwanmahjong.ui.i18n

enum class AppLang { ZH, EN }

data class Strings(
    val brandZh: String = "愛台灣 打麻將",
    val brandEn: String = "Taiwan Mahjong",
    val tagline: String,
    val stake: String,
    val soloVsAi: String,
    val exit: String,
    val discard: String,
    val pass: String,
    val hu: String,
    val kong: String,
    val pong: String,
    val chi: String,
    val manual: String,
    val auto: String,
    val nextHand: String,
    val home: String,
    val drawGame: String,
    val dealerStays: String,
    val wins: String,
    val scores: String,
    val totalTai: String,
    val langToggle: String,
)

fun strings(lang: AppLang): Strings = when (lang) {
    AppLang.ZH -> Strings(
        tagline = "茶館包廂 · 招待陪打 · 單機 AI",
        stake = "底",
        soloVsAi = "進入包廂",
        exit = "離開",
        discard = "打牌",
        pass = "過",
        hu = "胡",
        kong = "槓",
        pong = "碰",
        chi = "吃",
        manual = "手動",
        auto = "自動",
        nextHand = "下一局",
        home = "回首頁",
        drawGame = "流局",
        dealerStays = "莊家連莊",
        wins = "胡牌",
        scores = "分數",
        totalTai = "台",
        langToggle = "EN",
    )
    AppLang.EN -> Strings(
        tagline = "Private room · Hosts at your table · Local AI",
        stake = "Stake",
        soloVsAi = "Enter private room",
        exit = "Exit",
        discard = "Discard",
        pass = "Pass",
        hu = "Hu",
        kong = "Kong",
        pong = "Pong",
        chi = "Chi",
        manual = "Manual",
        auto = "Auto",
        nextHand = "Next hand",
        home = "Home",
        drawGame = "Draw game",
        dealerStays = "Dealer stays",
        wins = "wins",
        scores = "Scores",
        totalTai = "tai",
        langToggle = "中文",
    )
}

fun claimLabel(kind: String, s: Strings): String = when (kind) {
    "HU" -> s.hu
    "KONG" -> s.kong
    "PONG" -> s.pong
    "CHI" -> s.chi
    else -> kind
}
