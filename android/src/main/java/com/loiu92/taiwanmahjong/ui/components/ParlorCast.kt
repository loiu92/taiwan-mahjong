package com.loiu92.taiwanmahjong.ui.components

import androidx.annotation.DrawableRes
import com.loiu92.taiwanmahjong.R

/** Tea parlor / KTV cast seated around the table. */
object ParlorCast {
    const val HUMAN = "You"
    const val MEIMEI = "MeiMei"   // left hostess
    const val HAO = "Hao"         // opposite suited host
    const val YAYA = "YaYa"       // right hostess

    /** Seat order: you, right, across, left — matches cinematic table. */
    val defaultNames = listOf(HUMAN, YAYA, HAO, MEIMEI)

    fun displayName(name: String, zh: Boolean): String = when (name) {
        HUMAN, "你" -> if (zh) "你" else "You"
        MEIMEI, "小美" -> if (zh) "小美" else "MeiMei"
        YAYA, "雅雅" -> if (zh) "雅雅" else "YaYa"
        HAO, "阿豪" -> if (zh) "阿豪" else "Hao"
        else -> name
    }

    @DrawableRes
    fun avatarRes(name: String): Int? = when (name) {
        MEIMEI, "小美" -> R.drawable.char_meimei
        YAYA, "雅雅" -> R.drawable.char_yaya
        HAO, "阿豪" -> R.drawable.char_hao
        else -> null
    }
}
