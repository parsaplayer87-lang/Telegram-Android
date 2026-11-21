package org.telegram.liquidglass

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop

object TelegramGlassBar {
    @JvmStatic
    fun createView(context: Context): View {
        return ComposeView(context).apply {
            setContent { GlassBarDemo() }
        }
    }
}

@Composable
private fun GlassBarDemo() {
    val backdrop = rememberCanvasBackdrop {
        drawRect(
            Brush.linearGradient(
                listOf(Color(0xFF1E3A8A), Color(0xFF7C3AED), Color(0xFFDB2777))
            )
        )
    }
    var selected by remember { mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize()) {
        LiquidBottomTabs(
            selectedTabIndex = { selected },
            onTabSelected = { selected = it },
            backdrop = backdrop,
            tabsCount = 3,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 36.dp)
                .padding(bottom = 24.dp)
        ) {
            repeat(3) { index ->
                LiquidBottomTab(onClick = { selected = index }) {
                    Box(Modifier.size(28.dp))
                    BasicText("Tab ${index + 1}", style = TextStyle(Color.White, 12.sp))
                }
            }
        }
    }
}