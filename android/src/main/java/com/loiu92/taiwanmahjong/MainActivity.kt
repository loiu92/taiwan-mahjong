package com.loiu92.taiwanmahjong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.loiu92.taiwanmahjong.ui.MahjongApp
import com.loiu92.taiwanmahjong.ui.theme.TaiwanMahjongTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaiwanMahjongTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MahjongApp()
                }
            }
        }
    }
}
