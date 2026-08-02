package com.example.forge

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Minimal line chart. `values` are in chronological order (oldest first).
// When higherIsBetter is false (e.g. finish times), smaller values are drawn higher.
@Composable
fun ProgressChart(
    title: String,
    values: List<Float>,
    firstLabel: String,
    lastLabel: String,
    lastDisplay: String,
    accent: Color,
    higherIsBetter: Boolean
) {
    if (values.size < 2) return
    val min = values.minOrNull() ?: 0f
    val max = values.maxOrNull() ?: 0f
    val range = (max - min).let { if (it == 0f) 1f else it }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            Text(lastDisplay, color = accent, fontSize = 13.sp)
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(top = 10.dp, bottom = 6.dp)
        ) {
            val pad = 10f
            val w = size.width
            val h = size.height
            val usableW = (w - 2 * pad).coerceAtLeast(1f)
            val usableH = (h - 2 * pad).coerceAtLeast(1f)
            val n = values.size

            val points = values.mapIndexed { i, v ->
                val x = pad + usableW * i / (n - 1)
                val norm = (v - min) / range
                val y = if (higherIsBetter) pad + (1f - norm) * usableH else pad + norm * usableH
                Offset(x, y)
            }
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = accent,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 5f
                )
            }
            points.forEach { p ->
                drawCircle(color = accent, radius = 7f, center = p)
                drawCircle(color = Color(0xFF1A1D23), radius = 3f, center = p)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(firstLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            Text(lastLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
    }
}
