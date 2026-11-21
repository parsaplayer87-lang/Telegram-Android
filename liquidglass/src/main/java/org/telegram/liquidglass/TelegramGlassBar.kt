package org.telegram.liquidglass

import android.content.Context
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop

fun interface GlassOnTabSelect {
    fun select(index: Int)
}

fun interface GlassContentDrawer {
    fun draw(canvas: Canvas, width: Float, height: Float)
}

class GlassBarState {
    companion object {
        const val ICON_CHAT = 0
        const val ICON_CONTACTS = 1
        const val ICON_CALLS = 2
        const val ICON_SETTINGS = 3
        const val ICON_PROFILE = 4
    }

    var tabCount by mutableIntStateOf(4)
    var selectedIndex by mutableIntStateOf(0)
    var contentTick by mutableIntStateOf(0)
    var barLeft by mutableStateOf(0f)
    var barTop by mutableStateOf(0f)
    val labels = mutableStateListOf<String>()
    val icons = mutableStateListOf<Int>()
    val badges = mutableStateListOf<String?>()
    var onSelect: GlassOnTabSelect? = null
    var drawer: GlassContentDrawer? = null

    private fun ensure(i: Int) {
        while (labels.size <= i) {
            labels.add("")
            icons.add(0)
            badges.add(null)
        }
    }

    fun setLabel(i: Int, s: String) { ensure(i); labels[i] = s }
    fun setIcon(i: Int, id: Int) { ensure(i); icons[i] = id }
    fun setBadge(i: Int, s: String?) { ensure(i); badges[i] = s }
    fun invalidateContent() { contentTick++ }
}

object TelegramGlassBar {
    @JvmStatic
    fun createView(context: Context, state: GlassBarState): View {
        return ComposeView(context).apply {
            setContent { GlassBar(state) }
        }
    }
}

@Composable
private fun GlassBar(state: GlassBarState) {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White

    val backdrop = rememberCanvasBackdrop {
        @Suppress("UNUSED_EXPRESSION")
        state.contentTick
        drawIntoCanvas { c ->
            state.drawer?.draw(c.nativeCanvas, size.width, size.height)
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        LiquidBottomTabs(
            selectedTabIndex = { state.selectedIndex },
            onTabSelected = {
                state.selectedIndex = it
                state.onSelect?.select(it)
            },
            backdrop = backdrop,
            tabsCount = state.tabCount,
            modifier = Modifier
                .widthIn(max = 344.dp)
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    val p = coords.positionInWindow()
                    state.barLeft = p.x
                    state.barTop = p.y
                }
        ) {
            repeat(state.tabCount) { index ->
                LiquidBottomTab(onClick = {
                    state.selectedIndex = index
                    state.onSelect?.select(index)
                }) {
                    Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                        Image(
                            imageVector = GlassIcons.get(state.icons.getOrElse(index) { 0 }),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            colorFilter = ColorFilter.tint(contentColor)
                        )
                        val badge = state.badges.getOrElse(index) { null }
                        if (badge != null) {
                            GlassBadge(badge, Modifier.align(Alignment.TopEnd))
                        }
                    }
                    BasicText(
                        state.labels.getOrElse(index) { "" },
                        style = TextStyle(contentColor, 11.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassBadge(text: String, modifier: Modifier) {
    Box(
        modifier
            .offset(x = 7.dp, y = (-5).dp)
            .background(Color(0xFFE53935), RoundedCornerShape(50))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        BasicText(text, style = TextStyle(Color.White, 9.sp))
    }
}

object GlassIcons {
    const val CHAT = 0
    const val CONTACTS = 1
    const val CALLS = 2
    const val SETTINGS = 3
    const val PROFILE = 4

    fun get(id: Int): ImageVector = when (id) {
        CONTACTS -> contacts
        CALLS -> calls
        SETTINGS -> settings
        PROFILE -> profile
        else -> chat
    }

    private fun icon(name: String, data: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            addPath(
                pathData = PathParser().parsePathString(data).toNodes(),
                fill = SolidColor(Color.Black)
            )
        }.build()

    private val chat by lazy { icon("chat", "M20,2H4c-1.1,0 -2,0.9 -2,2v18l4,-4h14c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2z") }
    private val contacts by lazy { icon("contacts", "M16,11c1.66,0 2.99,-1.34 2.99,-3S17.66,5 16,5c-1.66,0 -3,1.34 -3,3s1.34,3 3,3zM8,11c1.66,0 2.99,-1.34 2.99,-3S9.66,5 8,5C6.34,5 5,6.34 5,8s1.34,3 3,3zM8,13c-2.33,0 -7,1.17 -7,3.5L1,19h14v-2.5c0,-2.33 -4.67,-3.5 -7,-3.5zM16,13c-0.29,0 -0.62,0.02 -0.97,0.05 1.16,0.84 1.97,1.97 1.97,3.45L17,19h6v-2.5c0,-2.33 -4.67,-3.5 -7,-3.5z") }
    private val calls by lazy { icon("calls", "M6.62,10.79c1.44,2.83 3.76,5.14 6.59,6.59l2.2,-2.2c0.27,-0.27 0.67,-0.36 1.02,-0.24 1.12,0.37 2.33,0.57 3.57,0.57 0.55,0 1,0.45 1,1L21,20c0,0.55 -0.45,1 -1,1 -9.39,0 -17,-7.61 -17,-17 0,-0.55 0.45,-1 1,-1h3.5c0.55,0 1,0.45 1,1 0,1.25 0.2,2.45 0.57,3.57 0.11,0.35 0.03,0.74 -0.25,1.02l-2.2,2.2z") }
    private val settings by lazy { icon("settings", "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z") }
    private val profile by lazy { icon("profile", "M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z") }
}