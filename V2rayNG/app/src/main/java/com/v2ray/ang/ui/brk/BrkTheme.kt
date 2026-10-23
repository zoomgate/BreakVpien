package com.v2ray.ang.ui.brk

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

// ───────────────────────── Colors ─────────────────────────

object Brk {
    val BgTop = Color(0xFF0A1728)
    val BgBottom = Color(0xFF040C17)
    val Card = Color(0xFF0D2036)
    val CardBorder = Color(0xFF173350)
    val Chip = Color(0xFF0F2742)
    val Green = Color(0xFF1DE98B)
    val GreenDeep = Color(0xFF0B7F57)
    val GreenDark = Color(0xFF03251F)
    val Red = Color(0xFFFF4D5E)
    val RedDeep = Color(0xFF7A1F2B)
    val Orange = Color(0xFFFFA43A)
    val OrangeDeep = Color(0xFF8A5214)
    val Blue = Color(0xFF0A84FF)
    val Aqua = Color(0xFF2FD3E6)
    val TextSecondary = Color(0xFF9FB0C7)
}

// ───────────────────────── Models (mock data for the UI phase) ─────────────────────────

enum class ConnState { Disconnected, Connecting, Connected }

enum class Region(val label: String) {
    EUROPE("Europe"), ASIA("Asia"), AMERICA("America")
}

data class BrkServer(
    val id: String,
    val name: String,
    val code: String,   // ISO country code, used for the flag
    val ping: Int,      // ms
    val region: Region,
)

val mockServers = listOf(
    BrkServer("nl4", "Netherlands 4", "NL", 32, Region.EUROPE),
    BrkServer("de1", "Germany 1", "DE", 45, Region.EUROPE),
    BrkServer("us3", "United States 3", "US", 67, Region.AMERICA),
    BrkServer("gb2", "United Kingdom 2", "GB", 72, Region.EUROPE),
    BrkServer("ca1", "Canada 1", "CA", 84, Region.AMERICA),
    BrkServer("fr1", "France 1", "FR", 91, Region.EUROPE),
    BrkServer("sg2", "Singapore 2", "SG", 108, Region.ASIA),
    BrkServer("jp1", "Japan 1", "JP", 134, Region.ASIA),
)

/** "NL" -> flag emoji. No image files needed. */
fun flagEmoji(code: String): String {
    if (code.length != 2) return "🌐"
    val up = code.uppercase()
    val first = 0x1F1E6 + (up[0] - 'A')
    val second = 0x1F1E6 + (up[1] - 'A')
    return String(Character.toChars(first)) + String(Character.toChars(second))
}

fun pingColor(ping: Int): Color = when {
    ping < 60 -> Brk.Green
    ping < 120 -> Brk.Aqua
    else -> Brk.Orange
}

fun signalBars(ping: Int): Int = when {
    ping < 60 -> 4
    ping < 100 -> 3
    ping < 150 -> 2
    else -> 1
}

// ───────────────────────── Helpers ─────────────────────────

/** Click without ripple (keeps the look clean on the dark design). */
@Composable
fun Modifier.tap(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    return this.clickable(interactionSource = source, indication = null, onClick = onClick)
}

// ───────────────────────── Logo: shield + lightning bolt ─────────────────────────

@Composable
fun ShieldBolt(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val u = this.size.minDimension / 100f
        fun p(x: Float, y: Float) = Offset(x * u, y * u)

        val shield = Path().apply {
            moveTo(50 * u, 6 * u)
            lineTo(88 * u, 20 * u)
            lineTo(88 * u, 46 * u)
            cubicTo(88 * u, 72 * u, 72 * u, 88 * u, 50 * u, 96 * u)
            cubicTo(28 * u, 88 * u, 12 * u, 72 * u, 12 * u, 46 * u)
            lineTo(12 * u, 20 * u)
            close()
        }
        val brush = Brush.linearGradient(
            listOf(Color(0xFF2CF59A), Color(0xFF12C7A6)),
            start = p(10f, 0f), end = p(90f, 100f),
        )
        drawPath(shield, Color(0x3319E3C2), style = Stroke(width = 14 * u, join = StrokeJoin.Round))
        drawPath(shield, brush, style = Stroke(width = 7 * u, join = StrokeJoin.Round))

        val bolt = Path().apply {
            moveTo(58 * u, 20 * u)
            lineTo(36 * u, 55 * u)
            lineTo(49 * u, 55 * u)
            lineTo(43 * u, 80 * u)
            lineTo(66 * u, 43 * u)
            lineTo(53 * u, 43 * u)
            close()
        }
        drawPath(bolt, brush)
    }
}

// ───────────────────────── Simple icons (drawn in code, no extra libraries) ─────────────────────────

enum class IconKind { Menu, Bell, Back, Chevron, Search, Refresh, ArrowDown, ArrowUp, Link, Power }

@Composable
fun Ico(kind: IconKind, size: Dp, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val u = this.size.minDimension / 24f
        drawIcon(kind, u, color)
    }
}

private fun DrawScope.line(a: Offset, b: Offset, color: Color, w: Float) =
    drawLine(color, a, b, strokeWidth = w, cap = StrokeCap.Round)

private fun DrawScope.drawIcon(kind: IconKind, u: Float, color: Color) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val w = 2f * u
    when (kind) {
        IconKind.Menu -> {
            line(o(4f, 7f), o(20f, 7f), color, w)
            line(o(4f, 12f), o(20f, 12f), color, w)
            line(o(4f, 17f), o(20f, 17f), color, w)
        }
        IconKind.Bell -> {
            val bell = Path().apply {
                moveTo(12 * u, 2.5f * u)
                cubicTo(8.5f * u, 2.5f * u, 6.5f * u, 5.2f * u, 6.5f * u, 8.5f * u)
                lineTo(6.5f * u, 12 * u)
                cubicTo(6.5f * u, 14 * u, 5.5f * u, 15 * u, 4.5f * u, 16.5f * u)
                lineTo(19.5f * u, 16.5f * u)
                cubicTo(18.5f * u, 15 * u, 17.5f * u, 14 * u, 17.5f * u, 12 * u)
                lineTo(17.5f * u, 8.5f * u)
                cubicTo(17.5f * u, 5.2f * u, 15.5f * u, 2.5f * u, 12 * u, 2.5f * u)
                close()
            }
            drawPath(bell, color)
            drawCircle(color, radius = 2.2f * u, center = o(12f, 19.3f))
        }
        IconKind.Back -> {
            line(o(14.5f, 5f), o(7.5f, 12f), color, w * 1.1f)
            line(o(7.5f, 12f), o(14.5f, 19f), color, w * 1.1f)
        }
        IconKind.Chevron -> {
            line(o(9f, 5f), o(16f, 12f), color, w * 1.1f)
            line(o(16f, 12f), o(9f, 19f), color, w * 1.1f)
        }
        IconKind.Search -> {
            drawCircle(color, radius = 6.2f * u, center = o(10.5f, 10.5f), style = Stroke(width = w))
            line(o(15.2f, 15.2f), o(20f, 20f), color, w)
        }
        IconKind.Refresh -> {
            val r = 7.5f * u
            val c = o(12f, 12f)
            val start = -40f
            val sweep = 280f
            drawArc(
                color, startAngle = start, sweepAngle = sweep, useCenter = false,
                topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2),
                style = Stroke(width = w, cap = StrokeCap.Round),
            )
            val end = Math.toRadians((start + sweep).toDouble())
            val ex = c.x + r * cos(end).toFloat()
            val ey = c.y + r * sin(end).toFloat()
            val rx = cos(end).toFloat(); val ry = sin(end).toFloat()
            val tx = -ry; val ty = rx // tangent (clockwise)
            val s = 3.2f * u
            val head = Path().apply {
                moveTo(ex + rx * s, ey + ry * s)
                lineTo(ex - rx * s, ey - ry * s)
                lineTo(ex + tx * s * 1.6f, ey + ty * s * 1.6f)
                close()
            }
            drawPath(head, color)
        }
        IconKind.ArrowDown -> {
            line(o(12f, 4f), o(12f, 16f), color, w)
            line(o(6.5f, 11f), o(12f, 16.5f), color, w)
            line(o(17.5f, 11f), o(12f, 16.5f), color, w)
            line(o(5f, 20.5f), o(19f, 20.5f), color, w)
        }
        IconKind.ArrowUp -> {
            line(o(12f, 20f), o(12f, 8f), color, w)
            line(o(6.5f, 13f), o(12f, 7.5f), color, w)
            line(o(17.5f, 13f), o(12f, 7.5f), color, w)
            line(o(5f, 3.5f), o(19f, 3.5f), color, w)
        }
        IconKind.Link -> {
            rotate(-45f, pivot = o(12f, 12f)) {
                drawRoundRect(
                    color, topLeft = o(2.5f, 8f), size = Size(11f * u, 8f * u),
                    cornerRadius = CornerRadius(4f * u), style = Stroke(width = 1.8f * u),
                )
                drawRoundRect(
                    color, topLeft = o(10.5f, 8f), size = Size(11f * u, 8f * u),
                    cornerRadius = CornerRadius(4f * u), style = Stroke(width = 1.8f * u),
                )
            }
        }
        IconKind.Power -> {
            val r = 8.2f * u
            val c = o(12f, 12.8f)
            drawArc(
                color, startAngle = -58f, sweepAngle = 296f, useCenter = false,
                topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2),
                style = Stroke(width = 2.6f * u, cap = StrokeCap.Round),
            )
            line(o(12f, 3f), o(12f, 12f), color, 2.6f * u)
        }
    }
}

// ───────────────────────── Dotted world map (faint background) ─────────────────────────

private val worldMask = listOf(
    "........................########........................................",
    "......................##########........................................",
    ".......................#########...................#############........",
    "...##################....######........####..##########################.",
    "...####################..####.........#################################.",
    ".........###############..............###############################...",
    "..........###############..........################################.....",
    "...........#############...........##############################.......",
    "...........###########............###.##########################........",
    "............#########.............##......######################........",
    ".............########.............######...##################...........",
    ".............#####.#..............############.#############............",
    "..............###................#############....##########............",
    "................###.............###############...###..###..............",
    "..................#..............#############.....#....#...............",
    "....................####..........############..........................",
    "....................#####............########...........#...............",
    "....................#######...........######............#######.........",
    "....................#########.........######...............#####........",
    ".....................########.........######..................###.......",
    "......................######..........######................#####.......",
    "......................######...........####................#######......",
    "......................#####............####................########.....",
    "......................####..............##.................#######......",
    ".....................####.......................................##......",
    ".....................##.................................................",
    ".....................##.................................................",
    ".....................#..................................................",
    "........................................................................",
)

@Composable
fun WorldDots(modifier: Modifier = Modifier, dotColor: Color = Color(0xFF3FB6B0), alpha: Float = 0.16f) {
    Canvas(modifier.fillMaxSize()) {
        val cols = worldMask[0].length
        val rows = worldMask.size
        val cell = this.size.width / cols
        val radius = cell * 0.26f
        for (y in 0 until rows) {
            val row = worldMask[y]
            for (x in 0 until cols) {
                if (row[x] == '#') {
                    drawCircle(dotColor.copy(alpha = alpha), radius, Offset(x * cell + cell / 2, y * cell + cell / 2))
                }
            }
        }
    }
}
