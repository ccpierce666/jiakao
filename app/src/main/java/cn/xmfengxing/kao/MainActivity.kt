package cn.xmfengxing.kao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cn.xmfengxing.kao.navigation.AppNavigation
import cn.xmfengxing.kao.ui.theme.KaoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaoTheme {
                AppNavigation()
            }
        }
    }
}
