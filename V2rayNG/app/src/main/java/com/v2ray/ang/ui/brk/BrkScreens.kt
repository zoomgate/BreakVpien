package com.v2ray.ang.ui.brk

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// ═════════════════════════ Shared background ═════════════════════════

@Composable
fun BrkBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Brk.BgTop, Brk.BgBottom))),
    ) { content() }
}

// ═════════════════════════ 1) Loading screen ═════════════════════════

@Composable
fun SplashScreen() {
    BrkBackground {
        // faint map + green wave at the bottom
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(300.dp),
        ) {
            WorldDots(alpha = 0.20f)
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val wave = Path().apply {
                    moveTo(0f, h * 0.62f)
                    cubicTo(w * 0.25f, h * 0.50f, w * 0.45f, h * 0.78f, w * 0.7f, h * 0.60f)
                    cubicTo(w * 0.85f, h * 0.49f, w * 0.95f, h * 0.42f, w, h * 0.38f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    wave,
                    Brush.verticalGradient(
                        listOf(Brk.Green.copy(alpha = 0.55f), Brk.GreenDeep.copy(alpha = 0.25f), Color.Transparent),
                        startY = h * 0.35f, endY = h,
                    ),
                )
                val crest = Path().apply {
                    moveTo(0f, h * 0.62f)
                    cubicTo(w * 0.25f, h * 0.50f, w * 0.45f, h * 0.78f, w * 0.7f, h * 0.60f)
                    cubicTo(w * 0.85f, h * 0.49f, w * 0.95f, h * 0.42f, w, h * 0.38f)
                }
                drawPath(crest, Brk.Green.copy(alpha = 0.8f), style = Stroke(width = 2.5f))
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ShieldBolt(size = 130.dp)
            Spacer(Modifier.height(22.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) { append("Break ") }
                    withStyle(SpanStyle(color = Brk.Green)) { append("VPN") }
                },
                style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(8.dp))
            Text("Fast  •  Secure  •  Global", color = Brk.TextSecondary, fontSize = 17.sp)
            Spacer(Modifier.height(56.dp))
            Spinner()
            Spacer(Modifier.height(18.dp))
            Text("Connecting to the internet...", color = Brk.TextSecondary, fontSize = 15.sp)
        }
    }
}

@Composable
private fun Spinner() {
    val t = rememberInfiniteTransition(label = "spin")
    val angle by t.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "angle",
    )
    Canvas(Modifier.size(58.dp)) {
        val stroke = 5.dp.toPx()
        val inset = stroke / 2
        val sz = Size(size.width - stroke, size.height - stroke)
        drawArc(
            Color.White.copy(alpha = 0.08f), 0f, 360f, false,
            topLeft = Offset(inset, inset), size = sz, style = Stroke(stroke),
        )
        rotate(angle) {
            drawArc(
                Brk.Green, -90f, 95f, false,
                topLeft = Offset(inset, inset), size = sz,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
    }
}

// ═════════════════════════ 2) Home screen ═════════════════════════

@Composable
fun HomeScreen(
    state: ConnState,
    seconds: Int,
    downBytes: Long,
    upBytes: Long,
    downSpeed: Long,
    upSpeed: Long,
    server: BrkServer,
    onPower: () -> Unit,
    onOpenServers: () -> Unit,
) {
    BrkBackground {
        WorldDots(Modifier.align(Alignment.TopCenter).padding(top = 90.dp), alpha = 0.10f)
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HomeTopBar()
            Spacer(Modifier.height(26.dp))
            StatusPill(state)
            Spacer(Modifier.height(20.dp))
            Text(
                formatTime(seconds),
                color = Color.White,
                style = TextStyle(fontSize = 54.sp, fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
            )
            Spacer(Modifier.height(22.dp))
            TrafficCard(state, downBytes, upBytes, downSpeed, upSpeed)

            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                PowerButton(state, onPower)
            }

            ServerCard(server, onOpenServers)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeTopBar() {
    Box(Modifier.fillMaxWidth().height(60.dp)) {
        Ico(IconKind.Menu, 28.dp, Color.White, Modifier.align(Alignment.CenterStart))
        Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
            ShieldBolt(size = 30.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) { append("Break ") }
                    withStyle(SpanStyle(color = Brk.Green)) { append("VPN") }
                },
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            )
        }
        Ico(IconKind.Bell, 26.dp, Color.White, Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun StatusPill(state: ConnState) {
    val bg by animateColorAsState(
        when (state) {
            ConnState.Connected -> Brk.GreenDeep
            ConnState.Connecting -> Brk.OrangeDeep
            ConnState.Disconnected -> Brk.RedDeep
        },
        tween(450), label = "pillBg",
    )
    val label = when (state) {
        ConnState.Connected -> "Connected"
        ConnState.Connecting -> "Connecting..."
        ConnState.Disconnected -> "Disconnected"
    }
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ico(IconKind.Link, 20.dp, Color.White)
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TrafficCard(state: ConnState, down: Long, up: Long, downSpeed: Long, upSpeed: Long) {
    val live = state == ConnState.Connected
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brk.Card)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TrafficCell(
            Modifier.weight(1f), IconKind.ArrowDown, Brk.Green,
            formatBytes(down), if (live) "Download  ${formatSpeed(downSpeed)}" else "Download",
        )
        Box(Modifier.width(1.dp).height(40.dp).background(Brk.CardBorder))
        TrafficCell(
            Modifier.weight(1f), IconKind.ArrowUp, Brk.Blue,
            formatBytes(up), if (live) "Upload  ${formatSpeed(upSpeed)}" else "Upload",
        )
    }
}

@Composable
private fun TrafficCell(modifier: Modifier, icon: IconKind, tint: Color, value: String, label: String) {
    Row(modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Ico(icon, 26.dp, tint)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(value, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Brk.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PowerButton(state: ConnState, onClick: () -> Unit) {
    val accent by animateColorAsState(
        when (state) {
            ConnState.Connected -> Brk.Green
            ConnState.Connecting -> Brk.Orange
            ConnState.Disconnected -> Brk.Red
        },
        tween(500), label = "accent",
    )
    val t = rememberInfiniteTransition(label = "power")
    val pulse by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse",
    )
    val spin by t.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "spin",
    )
    val breathe by t.animateFloat(
        0.97f, 1.03f,
        infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "breathe",
    )

    Box(
        Modifier
            .size(300.dp)
            .tap(onClick),
        contentAlignment = Alignment.Center,
    ) {
        // expanding rings + glow
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val base = 100.dp.toPx()
            drawCircle(
                Brush.radialGradient(
                    listOf(accent.copy(alpha = 0.30f), accent.copy(alpha = 0.08f), Color.Transparent),
                    center = c, radius = 150.dp.toPx(),
                ),
                radius = 150.dp.toPx(), center = c,
            )
            if (state != ConnState.Disconnected) {
                for (i in 0..2) {
                    val p = (pulse + i / 3f) % 1f
                    drawCircle(
                        accent.copy(alpha = (1f - p) * 0.35f),
                        radius = base + p * 48.dp.toPx(), center = c,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }
        }
        // the button itself
        Box(
            Modifier
                .size(190.dp)
                .scale(if (state == ConnState.Disconnected) 1f else breathe)
                .drawBehind {
                    val stroke = 11.dp.toPx()
                    val inset = stroke / 2
                    val sz = Size(size.width - stroke, size.height - stroke)
                    drawCircle(
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.30f), Brk.GreenDark.copy(alpha = 0.9f)),
                            center = center, radius = size.minDimension / 2,
                        ),
                    )
                    drawArc(
                        accent, 0f, 360f, false,
                        topLeft = Offset(inset, inset), size = sz, style = Stroke(stroke),
                    )
                    if (state == ConnState.Connecting) {
                        rotate(spin) {
                            drawArc(
                                Color.White, -90f, 70f, false,
                                topLeft = Offset(inset, inset), size = sz,
                                style = Stroke(stroke, cap = StrokeCap.Round),
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Ico(IconKind.Power, 78.dp, Color.White)
        }
    }
}

@Composable
private fun ServerCard(server: BrkServer, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brk.Card)
            .tap(onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagCircle(server.code, 46.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(server.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("Ping: ${server.ping} ms", color = Brk.Aqua, fontSize = 14.sp)
        }
        Ico(IconKind.Chevron, 22.dp, Brk.TextSecondary)
    }
}

@Composable
fun FlagCircle(code: String, size: Dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0x22FFFFFF)),
        contentAlignment = Alignment.Center,
    ) {
        Text(flagEmoji(code), fontSize = (size.value * 0.6f).sp)
    }
}

// ═════════════════════════ 3) Server list screen ═════════════════════════

@Composable
fun ServerScreen(
    servers: List<BrkServer>,
    selectedId: String,
    refreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSelect: (BrkServer) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var region by remember { mutableStateOf<Region?>(null) }

    val shown = servers.filter {
        (region == null || it.region == region) &&
            (query.isBlank() || it.name.contains(query.trim(), ignoreCase = true))
    }

    BrkBackground {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 18.dp),
        ) {
            // top bar
            Box(Modifier.fillMaxWidth().height(60.dp)) {
                Box(Modifier.align(Alignment.CenterStart).tap(onBack).padding(6.dp)) {
                    Ico(IconKind.Back, 28.dp, Color.White)
                }
                Text(
                    "Change Server", color = Color.White, fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center),
                )
                val t = rememberInfiniteTransition(label = "refreshIcon")
                val rot by t.animateFloat(
                    0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "rot",
                )
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .tap(onRefresh)
                        .padding(6.dp),
                ) {
                    Ico(
                        IconKind.Refresh, 26.dp, Color.White,
                        Modifier.graphicsLayer { rotationZ = if (refreshing) rot else 0f },
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // search
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brk.Card)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Ico(IconKind.Search, 22.dp, Brk.TextSecondary)
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("Search server...", color = Brk.TextSecondary, fontSize = 16.sp)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(Brk.Green),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // filter chips
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Chip("All", region == null, Modifier.weight(1f)) { region = null }
                Region.values().forEach { r ->
                    Chip(r.label, region == r, Modifier.weight(1f)) { region = r }
                }
            }

            Spacer(Modifier.height(14.dp))

            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(shown, key = { it.id }) { s ->
                    ServerRow(s, s.id == selectedId) { onSelect(s) }
                }
            }

            Spacer(Modifier.height(12.dp))

            // refresh button
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Brk.Blue.copy(alpha = if (refreshing) 0.6f else 1f))
                    .tap(onRefresh),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Ico(IconKind.Refresh, 22.dp, Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (refreshing) "Refreshing..." else "Refresh Server",
                        color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Brk.Green else Brk.Chip)
            .tap(onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (selected) Color(0xFF04210F) else Color.White,
            fontSize = 15.sp, fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ServerRow(server: BrkServer, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Color(0xFF0A2834) else Brk.Card)
            .then(if (selected) Modifier.border(1.5.dp, Brk.Green, shape) else Modifier)
            .tap(onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagCircle(server.code, 46.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(server.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("${server.ping} ms", color = pingColor(server.ping), fontSize = 14.sp)
        }
        SignalBars(signalBars(server.ping), pingColor(server.ping))
        Spacer(Modifier.width(14.dp))
        Ico(IconKind.Chevron, 20.dp, Brk.TextSecondary)
    }
}

@Composable
private fun SignalBars(level: Int, color: Color) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (i in 1..4) {
            Box(
                Modifier
                    .width(4.dp)
                    .height((6 + i * 4).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i <= level) color else Color.White.copy(alpha = 0.15f)),
            )
        }
    }
}

// ═════════════════════════ Formatting helpers ═════════════════════════

fun formatTime(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}

fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1024 -> String.format(Locale.US, "%.2f GB", mb / 1024)
        mb >= 1 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb)
    }
}

fun formatSpeed(bytesPerSec: Long): String {
    val mb = bytesPerSec / 1024.0 / 1024.0
    return if (mb >= 1) String.format(Locale.US, "%.1f MB/s", mb)
    else String.format(Locale.US, "%.0f KB/s", bytesPerSec / 1024.0)
}
