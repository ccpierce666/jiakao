package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EmptyScreen(contentPadding: PaddingValues) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {}
}
