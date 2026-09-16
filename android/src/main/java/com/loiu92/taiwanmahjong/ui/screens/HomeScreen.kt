package com.loiu92.taiwanmahjong.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loiu92.taiwanmahjong.R
import com.loiu92.taiwanmahjong.ui.components.ParlorCast
import com.loiu92.taiwanmahjong.ui.i18n.AppLang
import com.loiu92.taiwanmahjong.ui.i18n.Strings
import com.loiu92.taiwanmahjong.ui.theme.MahjongColors
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    stake: Int,
    lang: AppLang,
    strings: Strings,
    onStakeChange: (Int) -> Unit,
    onToggleLang: () -> Unit,
    onStart: () -> Unit,
) {
    val zh = lang == AppLang.ZH
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.75f),
                        ),
                    ),
                ),
        )

        Text(
            text = strings.langToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clickable(onClick = onToggleLang)
                .border(1.dp, MahjongColors.roseNeon, RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 7.dp),
            color = MahjongColors.roseNeon,
            style = MaterialTheme.typography.labelLarge,
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.78f)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = strings.brandZh,
                style = MaterialTheme.typography.displayLarge,
                color = MahjongColors.gold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (zh) "茶館 · KTV 麻將廳" else "Tea Parlor · KTV Mahjong",
                color = MahjongColors.roseNeon,
                fontSize = 15.sp,
            )
            Text(
                text = strings.tagline,
                color = MahjongColors.mist,
                fontSize = 13.sp,
            )

            Spacer(Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                HostAvatar(R.drawable.char_meimei, ParlorCast.displayName(ParlorCast.MEIMEI, zh))
                HostAvatar(R.drawable.char_yaya, ParlorCast.displayName(ParlorCast.YAYA, zh))
                HostAvatar(R.drawable.char_hao, ParlorCast.displayName(ParlorCast.HAO, zh))
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                    .border(1.dp, MahjongColors.gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(18.dp),
            ) {
                Text(
                    text = "${strings.stake} $stake / ${stake * 2}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                )
                Slider(
                    value = stake.toFloat(),
                    onValueChange = { onStakeChange((it / 10).roundToInt() * 10) },
                    valueRange = 10f..200f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = MahjongColors.gold,
                        activeTrackColor = MahjongColors.roseNeon,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MahjongColors.roseNeon),
                    shape = RoundedCornerShape(26.dp),
                ) {
                    Text(
                        text = strings.soloVsAi,
                        fontSize = 17.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun HostAvatar(resId: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(resId),
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .border(2.dp, MahjongColors.gold, CircleShape),
        )
        Spacer(Modifier.height(6.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}
