package com.example.forge

import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetailScreen(
    workout: Workout,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDeleteResult: (String) -> Unit,
    onAddResult: () -> Unit
) {
    var confirmDeleteWorkout by remember { mutableStateOf(false) }
    var confirmDeleteResultId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {

        PhotoBox(
            photoKey = displayPhoto(workout),
            modifier = Modifier.fillMaxWidth().height(200.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = { confirmDeleteWorkout = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(workout.name.ifBlank { "Untitled" }.uppercase(), color = Color.White, fontFamily = ForgeDisplay, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 44.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    TypeBadge(workout.type)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(14.dp))
            Text(formatConfig(workout), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            if (!workout.notes.isNullOrBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(workout.notes, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
            }

            if (workout.movements.isNotEmpty()) {
                if (!workout.scheme.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        workout.scheme!!,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = ForgeDisplay,
                        fontSize = 30.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.dp)
                ) {
                    workout.movements.forEach { m ->
                        MovementRow(movementIcon(m.name), movementLine(workout, m))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            BestBlock(workout)

            computeNudge(workout)?.let { nudge ->
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF173404)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color(0xFFC0DD97))
                    Spacer(Modifier.width(9.dp))
                    Text(
                        "Time at ${formatWeight(workout.primaryWeight)} kg down ${nudge.dropPercent}% over ${nudge.sessions} sessions — ready to try ${formatWeight(nudge.suggestedWeight)} kg?",
                        color = Color(0xFFEAF3DE), fontSize = 12.sp
                    )
                }
            }

            val chart = remember(workout) { buildChartData(workout) }
            if (chart != null) {
                Spacer(Modifier.height(14.dp))
                ProgressChart(
                    title = chart.title,
                    values = chart.values,
                    firstLabel = chart.firstLabel,
                    lastLabel = chart.lastLabel,
                    lastDisplay = chart.lastDisplay,
                    accent = MaterialTheme.colorScheme.primary,
                    higherIsBetter = chart.higherIsBetter
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("HISTORY")
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onAddResult) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add past result", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            HistoryList(workout, onDeleteResult = { confirmDeleteResultId = it })

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (workout.type == WorkoutType.STRENGTH) "Start / log" else "Start", fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDeleteWorkout) {
        ConfirmDialog(
            title = "Delete workout?",
            message = "\"${workout.name}\" and all its history will be permanently removed.",
            confirmLabel = "Delete",
            onConfirm = { confirmDeleteWorkout = false; onDelete() },
            onDismiss = { confirmDeleteWorkout = false }
        )
    }

    confirmDeleteResultId?.let { id ->
        ConfirmDialog(
            title = "Delete this entry?",
            message = "This history entry will be removed.",
            confirmLabel = "Delete",
            onConfirm = { onDeleteResult(id); confirmDeleteResultId = null },
            onDismiss = { confirmDeleteResultId = null }
        )
    }
}

private data class ChartData(
    val title: String,
    val values: List<Float>,
    val firstLabel: String,
    val lastLabel: String,
    val lastDisplay: String,
    val higherIsBetter: Boolean
)

private fun buildChartData(workout: Workout): ChartData? {
    val asc = workout.results.sortedBy { it.timestamp }
    return when (workout.type) {
        WorkoutType.FOR_TIME -> {
            val rows = asc.filter { it.elapsedSeconds != null }
            if (rows.size < 2) null
            else ChartData(
                "FINISH TIME (lower is better)",
                rows.map { it.elapsedSeconds!!.toFloat() },
                formatShortDate(rows.first().timestamp),
                formatShortDate(rows.last().timestamp),
                formatClock(rows.last().elapsedSeconds!!),
                higherIsBetter = false
            )
        }
        WorkoutType.AMRAP -> {
            val rows = asc.filter { it.rounds != null }
            if (rows.size < 2) null
            else {
                val perRound = workout.movements.sumOf { it.reps }.coerceAtLeast(1)
                ChartData(
                    "SCORE (reps)",
                    rows.map { ((it.rounds ?: 0) * perRound + (it.extraReps ?: 0)).toFloat() },
                    formatShortDate(rows.first().timestamp),
                    formatShortDate(rows.last().timestamp),
                    "${rows.last().rounds ?: 0} + ${rows.last().extraReps ?: 0}",
                    higherIsBetter = true
                )
            }
        }
        WorkoutType.STRENGTH -> {
            val rows = asc.mapNotNull { r -> r.heaviestStrengthSet?.let { r.timestamp to it.weight } }
            if (rows.size < 2) null
            else ChartData(
                "TOP SET (kg)",
                rows.map { it.second.toFloat() },
                formatShortDate(rows.first().first),
                formatShortDate(rows.last().first),
                "${formatWeight(rows.last().second)} kg",
                higherIsBetter = true
            )
        }
        WorkoutType.EMOM, WorkoutType.TABATA -> {
            val rows = asc.filter { it.primaryWeight > 0.0 }
            if (rows.size < 2) null
            else ChartData(
                "WEIGHT (kg)",
                rows.map { it.primaryWeight.toFloat() },
                formatShortDate(rows.first().timestamp),
                formatShortDate(rows.last().timestamp),
                "${formatWeight(rows.last().primaryWeight)} kg",
                higherIsBetter = true
            )
        }
    }
}

@Composable
private fun BestBlock(workout: Workout) {
    val pair: Pair<String, String>? = when (workout.type) {
        WorkoutType.FOR_TIME -> workout.fastest?.let { r ->
            "FASTEST" to (formatClock(r.elapsedSeconds ?: 0) + if (r.primaryWeight > 0.0) "  ·  ${formatWeight(r.primaryWeight)} kg" else "")
        }
        WorkoutType.AMRAP -> workout.bestAmrap?.let { r ->
            "BEST SCORE" to "${r.rounds ?: 0} rounds + ${r.extraReps ?: 0}"
        }
        WorkoutType.STRENGTH -> workout.heaviestSet?.let { s ->
            "HEAVIEST SET" to "${formatWeight(s.weight)} kg × ${s.reps}"
        }
        WorkoutType.EMOM, WorkoutType.TABATA -> {
            val done = workout.results.count { it.completed == true }
            if (done > 0) "COMPLETED" to ("$done time" + if (done == 1) "" else "s") else null
        }
    }
    if (pair == null) return
    val (label, value) = pair

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF173404)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color(0xFFC0DD97), fontSize = 11.sp)
            Text(value, color = Color(0xFFF5F5F2), fontSize = 22.sp, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun HistoryList(workout: Workout, onDeleteResult: (String) -> Unit) {
    val history = workout.results.sortedByDescending { it.timestamp }
    if (history.isEmpty()) {
        Text("No attempts yet. Hit start and set a benchmark.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        return
    }
    when (workout.type) {
        WorkoutType.STRENGTH -> {
            val heaviest = workout.heaviestSet
            history.forEach { r -> StrengthHistoryCard(r, heaviest, onDelete = { onDeleteResult(r.id) }) }
        }
        else -> {
            val fastestId = workout.fastest?.id
            history.forEach { r -> SimpleHistoryRow(workout.type, r, r.id == fastestId, onDelete = { onDeleteResult(r.id) }) }
        }
    }
}

@Composable
private fun SimpleHistoryRow(type: WorkoutType, r: WorkoutResult, isFastest: Boolean, onDelete: () -> Unit) {
    val main: String = when (type) {
        WorkoutType.FOR_TIME -> formatClock(r.elapsedSeconds ?: 0)
        WorkoutType.AMRAP -> "${r.rounds ?: 0} rounds + ${r.extraReps ?: 0}"
        WorkoutType.EMOM, WorkoutType.TABATA -> if (r.completed == true) "Completed" else "Not completed"
        WorkoutType.STRENGTH -> ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(formatShortDate(r.timestamp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            if (!r.note.isNullOrBlank()) {
                Text(r.note, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
            }
        }
        if (r.primaryWeight > 0.0) {
            Text(
                "${formatWeight(r.primaryWeight)} kg",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 8.dp, vertical = 2.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(main, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        if (isFastest && type == WorkoutType.FOR_TIME) {
            Spacer(Modifier.width(5.dp))
            Icon(Icons.Filled.Star, contentDescription = "PB", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(14.dp))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete entry", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun StrengthHistoryCard(r: WorkoutResult, heaviest: SetEntry?, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatShortDate(r.timestamp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete entry", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.height(18.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        r.strength.forEach { entry ->
            if (entry.exerciseName.isNotBlank()) {
                Text(entry.exerciseName, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            entry.sets.forEach { s ->
                val isPr = heaviest != null && s.weight == heaviest.weight && s.reps == heaviest.reps
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
                    Text("${formatWeight(s.weight)} kg × ${s.reps}", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                    if (isPr) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Filled.Star, contentDescription = "PB", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(13.dp))
                    }
                }
            }
        }
    }
}
