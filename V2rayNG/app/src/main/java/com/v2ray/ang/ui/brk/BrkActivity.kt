package com.v2ray.ang.ui.brk

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * UI-only phase: everything here uses fake (mock) data.
 * The real VPN core, server list from GitHub, ping test, etc. are connected later.
 */
class BrkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(0xFF040C17.toInt()))
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent { BrkApp() }
    }
}

private enum class Screen { Splash, Home, Servers }

@Composable
fun BrkApp() {
    var screen by remember { mutableStateOf(Screen.Splash) }
    var state by remember { mutableStateOf(ConnState.Disconnected) }
    var seconds by remember { mutableStateOf(0) }
    var down by remember { mutableLongStateOf(0L) }
    var up by remember { mutableLongStateOf(0L) }
    var downSpeed by remember { mutableLongStateOf(0L) }
    var upSpeed by remember { mutableLongStateOf(0L) }
    var servers by remember { mutableStateOf(mockServers) }
    var selected by remember { mutableStateOf(mockServers.first()) }
    var refreshing by remember { mutableStateOf(false) }

    // loading screen -> home after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        screen = Screen.Home
    }

    // fake connection simulation
    LaunchedEffect(state) {
        when (state) {
            ConnState.Connecting -> {
                delay(1800)
                state = ConnState.Connected
            }
            ConnState.Connected -> {
                while (true) {
                    delay(1000)
                    seconds += 1
                    downSpeed = Random.nextLong(300_000L, 2_200_000L)
                    upSpeed = Random.nextLong(80_000L, 700_000L)
                    down += downSpeed
                    up += upSpeed
                }
            }
            ConnState.Disconnected -> {}
        }
    }

    // fake "refresh servers": new random pings
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(1200)
            servers = servers.map { it.copy(ping = (it.ping + Random.nextInt(-12, 13)).coerceAtLeast(12)) }
            selected = servers.first { it.id == selected.id }
            refreshing = false
        }
    }

    BackHandler(enabled = screen == Screen.Servers) { screen = Screen.Home }

    Crossfade(targetState = screen, animationSpec = tween(450), label = "screen") { s ->
        when (s) {
            Screen.Splash -> SplashScreen()
            Screen.Home -> HomeScreen(
                state = state,
                seconds = seconds,
                downBytes = down,
                upBytes = up,
                downSpeed = downSpeed,
                upSpeed = upSpeed,
                server = selected,
                onPower = {
                    if (state == ConnState.Disconnected) {
                        state = ConnState.Connecting
                    } else {
                        state = ConnState.Disconnected
                        seconds = 0; down = 0L; up = 0L; downSpeed = 0L; upSpeed = 0L
                    }
                },
                onOpenServers = { screen = Screen.Servers },
            )
            Screen.Servers -> ServerScreen(
                servers = servers,
                selectedId = selected.id,
                refreshing = refreshing,
                onBack = { screen = Screen.Home },
                onRefresh = { if (!refreshing) refreshing = true },
                onSelect = { picked ->
                    val changed = picked.id != selected.id
                    selected = picked
                    if (changed && state == ConnState.Connected) {
                        // switching server while connected: reconnect
                        seconds = 0; down = 0L; up = 0L
                        state = ConnState.Connecting
                    }
                    screen = Screen.Home
                },
            )
        }
    }
}
