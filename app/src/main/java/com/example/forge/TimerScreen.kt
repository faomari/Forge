package com.example.forge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun RunScreen(
    workout: Workout,
    onCancel: () -> Unit,
    onFinish: (Int) -> Unit
) {
    when (workout.type) {
        WorkoutType.FOR_TIME -> ForTimeTimer(workout, onCancel, onFinish)
        WorkoutType.AMRAP -> AmrapTimer(workout, onCancel, onFinish)
        WorkoutType.EMOM -> EmomTimer(workout, onCancel, onFinish)
        WorkoutType.TABATA -> TabataTimer(workout, onCancel, onFinish)
        WorkoutType.STRENGTH -> onFinish(0) // strength never routes here
    }
}

@Composable
private fun rememberTicker(running: Boolean): Long {
    var elapsedMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(running) {
        if (running) {
            val base = elapsedMs
            val start = System.currentTimeMillis()
            while (running) {
                elapsedMs = base + (System.currentTimeMillis() - start)
                delay(50)
            }
        }
    }
    return elapsedMs
}

@Composable
private fun TimerFrame(
    background: Color,
    pill: String,
    pillBg: Color,
    pillFg: Color,
    running: Boolean,
    onToggle: () -> Unit,
    onFinishLabel: String,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    center: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                pill,
                color = pillFg,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(pillBg)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) { center() }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onToggle,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2127), contentColor = Color.White)
            ) {
                Icon(if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (running) "Pause" else "Resume")
            }
            Button(
                onClick = onFinish,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text(onFinishLabel, fontWeight = FontWeight.Medium) }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Cancel",
            color = Color(0xFF9CA3AF),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onCancel() }.padding(10.dp)
        )
    }
}

@Composable
private fun MovementsRef(workout: Workout) {
    Spacer(Modifier.height(24.dp))
    if (!workout.scheme.isNullOrBlank()) {
        Text(
            workout.scheme!!,
            color = Color(0xFF97C459),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
    workout.movements.forEach { m ->
        Text(movementLine(workout, m), color = Color(0xFFF5F5F2), fontSize = 16.sp, modifier = Modifier.padding(vertical = 3.dp))
    }
}

@Composable
private fun ForTimeTimer(workout: Workout, onCancel: () -> Unit, onFinish: (Int) -> Unit) {
    val context = LocalContext.current
    var running by remember { mutableStateOf(true) }
    val elapsedMs = rememberTicker(running)
    val elapsed = (elapsedMs / 1000).toInt()
    val cap = workout.timeCapSeconds
    var capSignaled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { Feedback.signal(context) }
    LaunchedEffect(elapsed) {
        if (cap != null && !capSignaled && elapsed >= cap) { capSignaled = true; Feedback.signal(context) }
    }
    val over = cap != null && elapsed >= cap

    TimerFrame(
        background = Color(0xFF0C0F0A),
        pill = "FOR TIME" + if (cap != null) "  ·  cap ${formatClock(cap)}" else "",
        pillBg = Color(0xFF173404), pillFg = Color(0xFFC0DD97),
        running = running, onToggle = { running = !running },
        onFinishLabel = "Finish",
        onFinish = { running = false; Feedback.signal(context); onFinish(elapsed) },
        onCancel = onCancel
    ) {
        Text("ELAPSED", color = Color(0xFF9CA3AF), fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Text(formatClock(elapsed), color = if (over) Color(0xFFF09595) else MaterialTheme.colorScheme.primary, fontSize = 72.sp, fontWeight = FontWeight.Medium)
        MovementsRef(workout)
    }
}

@Composable
private fun AmrapTimer(workout: Workout, onCancel: () -> Unit, onFinish: (Int) -> Unit) {
    val context = LocalContext.current
    val total = workout.timeCapSeconds ?: 0
    var running by remember { mutableStateOf(true) }
    val elapsedMs = rememberTicker(running)
    val elapsed = (elapsedMs / 1000).toInt()
    val remaining = (total - elapsed).coerceAtLeast(0)
    val done = total > 0 && elapsed >= total

    LaunchedEffect(Unit) { Feedback.signal(context) }
    LaunchedEffect(done) {
        if (done) { running = false; Feedback.signal(context); onFinish(elapsed) }
    }

    TimerFrame(
        background = Color(0xFF0C0F0A),
        pill = "AMRAP  ·  ${formatClock(total)}",
        pillBg = Color(0xFF173404), pillFg = Color(0xFFC0DD97),
        running = running, onToggle = { running = !running },
        onFinishLabel = "End early",
        onFinish = { running = false; Feedback.signal(context); onFinish(elapsed) },
        onCancel = onCancel
    ) {
        Text("TIME REMAINING", color = Color(0xFF9CA3AF), fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Text(formatClock(remaining), color = MaterialTheme.colorScheme.primary, fontSize = 72.sp, fontWeight = FontWeight.Medium)
        MovementsRef(workout)
    }
}

@Composable
private fun EmomTimer(workout: Workout, onCancel: () -> Unit, onFinish: (Int) -> Unit) {
    val context = LocalContext.current
    val interval = workout.intervalSeconds ?: 60
    val rounds = workout.rounds ?: 1
    val total = interval * rounds
    var running by remember { mutableStateOf(true) }
    val elapsedMs = rememberTicker(running)
    val elapsed = (elapsedMs / 1000).toInt()
    val currentRound = ((elapsed / interval) + 1).coerceAtMost(rounds)
    val remainingInInterval = interval - (elapsed % interval)
    val done = elapsed >= total

    LaunchedEffect(currentRound) { if (!done) Feedback.signal(context) }
    LaunchedEffect(done) {
        if (done) { running = false; Feedback.signal(context); onFinish(elapsed) }
    }

    TimerFrame(
        background = Color(0xFF060E17),
        pill = "EMOM  ·  every ${formatClock(interval)}",
        pillBg = Color(0xFF042C53), pillFg = Color(0xFFB5D4F4),
        running = running, onToggle = { running = !running },
        onFinishLabel = "End",
        onFinish = { running = false; Feedback.signal(context); onFinish(elapsed) },
        onCancel = onCancel
    ) {
        Text("ROUND $currentRound / $rounds", color = Color(0xFF9CA3AF), fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Text(formatClock(remainingInInterval), color = Color(0xFFB5D4F4), fontSize = 72.sp, fontWeight = FontWeight.Medium)
        MovementsRef(workout)
    }
}

@Composable
private fun TabataTimer(workout: Workout, onCancel: () -> Unit, onFinish: (Int) -> Unit) {
    val context = LocalContext.current
    val work = workout.workSeconds ?: 20
    val rest = workout.restSeconds ?: 10
    val rounds = workout.rounds ?: 8
    val cycle = (work + rest).coerceAtLeast(1)
    val total = cycle * rounds
    var running by remember { mutableStateOf(true) }
    val elapsedMs = rememberTicker(running)
    val elapsed = (elapsedMs / 1000).toInt()
    val round = ((elapsed / cycle) + 1).coerceAtMost(rounds)
    val within = elapsed % cycle
    val isWork = within < work
    val remainingInPhase = if (isWork) work - within else cycle - within
    val done = elapsed >= total
    val phaseKey = "$round-${if (isWork) "W" else "R"}"

    LaunchedEffect(phaseKey) { if (!done) Feedback.signal(context) }
    LaunchedEffect(done) {
        if (done) { running = false; Feedback.signal(context); onFinish(elapsed) }
    }

    val phaseColor = if (isWork) MaterialTheme.colorScheme.primary else Color(0xFFF0A9BC)
    val bg = if (isWork) Color(0xFF0C0F0A) else Color(0xFF1A0E13)

    TimerFrame(
        background = bg,
        pill = "TABATA  ·  ${work}s / ${rest}s × $rounds",
        pillBg = Color(0xFF4A1D2A), pillFg = Color(0xFFF0A9BC),
        running = running, onToggle = { running = !running },
        onFinishLabel = "End",
        onFinish = { running = false; Feedback.signal(context); onFinish(elapsed) },
        onCancel = onCancel
    ) {
        Text("ROUND $round / $rounds", color = Color(0xFF9CA3AF), fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Text(if (isWork) "WORK" else "REST", color = phaseColor, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(formatClock(remainingInPhase), color = phaseColor, fontSize = 68.sp, fontWeight = FontWeight.Medium)
        MovementsRef(workout)
    }
}
