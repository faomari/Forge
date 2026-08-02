package com.example.forge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogResultScreen(
    workout: Workout,
    elapsedSeconds: Int,
    onCancel: () -> Unit,
    onSave: (WorkoutResult, List<Movement>?) -> Unit
) {
    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDate by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = timestamp)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Log result") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val onEditDate = { showDate = true }
            when (workout.type) {
                WorkoutType.AMRAP -> AmrapEntry(workout, timestamp, onEditDate, onSave)
                WorkoutType.EMOM, WorkoutType.TABATA -> DoneEntry(workout, timestamp, onEditDate, onSave)
                WorkoutType.FOR_TIME -> ForTimeEntry(workout, elapsedSeconds, timestamp, onEditDate, onSave)
                WorkoutType.STRENGTH -> {}
            }
        }
    }

    if (showDate) {
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { timestamp = it }
                    showDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } }
        ) { DatePicker(state = dateState) }
    }
}

@Composable
private fun rememberWeights(workout: Workout): SnapshotStateMap<String, String> = remember {
    mutableStateMapOf<String, String>().apply {
        workout.movements.forEach { m -> put(m.id, if (m.weight > 0.0) formatWeight(m.weight) else "") }
    }
}

private fun updatedMovements(workout: Workout, weights: SnapshotStateMap<String, String>): List<Movement> =
    workout.movements.map { m -> m.copy(weight = weights[m.id]?.toDoubleOrNull() ?: m.weight) }

@Composable
private fun ResultHero(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF173404)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = Color(0xFFC0DD97), fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color(0xFFF5F5F2), fontSize = 40.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DateRow(timestamp: Long, onEdit: () -> Unit) {
    Spacer(Modifier.height(20.dp))
    SectionLabel("DATE")
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(formatShortDate(timestamp), color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onEdit) { Text("Change") }
    }
}

@Composable
private fun WeightSection(workout: Workout, weights: SnapshotStateMap<String, String>) {
    if (workout.movements.isEmpty()) return
    Spacer(Modifier.height(20.dp))
    SectionLabel("WEIGHT USED (optional)")
    Spacer(Modifier.height(8.dp))
    workout.movements.forEach { m ->
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${if (m.reps > 0) "${m.reps}  " else ""}${m.name}", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
            OutlinedTextField(
                value = weights[m.id] ?: "",
                onValueChange = { weights[m.id] = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("kg") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(120.dp)
            )
        }
    }
}

@Composable
private fun SaveButton(onClick: () -> Unit) {
    Spacer(Modifier.height(20.dp))
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) { Text("Save result", fontWeight = FontWeight.Medium) }
}

@Composable
private fun ColumnScope.AmrapEntry(
    workout: Workout,
    timestamp: Long,
    onEditDate: () -> Unit,
    onSave: (WorkoutResult, List<Movement>?) -> Unit
) {
    var rounds by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    val weights = rememberWeights(workout)

    ResultHero("SCORE", "${rounds.ifBlank { "0" }} + ${reps.ifBlank { "0" }}")
    Spacer(Modifier.height(20.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = rounds,
            onValueChange = { rounds = it.filter { c -> c.isDigit() } },
            label = { Text("Rounds") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it.filter { c -> c.isDigit() } },
            label = { Text("Extra reps") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
    WeightSection(workout, weights)
    DateRow(timestamp, onEditDate)
    SaveButton {
        val updated = updatedMovements(workout, weights)
        onSave(
            WorkoutResult(rounds = rounds.toIntOrNull() ?: 0, extraReps = reps.toIntOrNull() ?: 0, movements = updated, timestamp = timestamp),
            updated
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.DoneEntry(
    workout: Workout,
    timestamp: Long,
    onEditDate: () -> Unit,
    onSave: (WorkoutResult, List<Movement>?) -> Unit
) {
    var completed by remember { mutableStateOf(true) }
    var note by remember { mutableStateOf("") }
    val weights = rememberWeights(workout)

    ResultHero("SESSION", if (completed) "Completed" else "Not done")
    Spacer(Modifier.height(20.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(
            selected = completed,
            onClick = { completed = true },
            label = { Text("Completed") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        FilterChip(
            selected = !completed,
            onClick = { completed = false },
            label = { Text("Didn't finish") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.error,
                selectedLabelColor = Color(0xFF3A1414)
            )
        )
    }
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text("Note (optional)") },
        modifier = Modifier.fillMaxWidth().height(110.dp)
    )
    WeightSection(workout, weights)
    DateRow(timestamp, onEditDate)
    SaveButton {
        val updated = updatedMovements(workout, weights)
        onSave(
            WorkoutResult(completed = completed, note = note.trim().ifBlank { null }, movements = updated, timestamp = timestamp),
            updated
        )
    }
}

@Composable
private fun ColumnScope.ForTimeEntry(
    workout: Workout,
    elapsedSeconds: Int,
    timestamp: Long,
    onEditDate: () -> Unit,
    onSave: (WorkoutResult, List<Movement>?) -> Unit
) {
    val weights = rememberWeights(workout)
    var mins by remember { mutableStateOf((elapsedSeconds / 60).toString()) }
    var secs by remember { mutableStateOf((elapsedSeconds % 60).toString()) }
    val total = (mins.toIntOrNull() ?: 0) * 60 + (secs.toIntOrNull() ?: 0)

    ResultHero("YOUR TIME", formatClock(total))
    Spacer(Modifier.height(20.dp))
    SectionLabel("TIME (edit if needed)")
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = mins,
            onValueChange = { mins = it.filter { c -> c.isDigit() } },
            label = { Text("Min") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = secs,
            onValueChange = { secs = it.filter { c -> c.isDigit() } },
            label = { Text("Sec") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
    WeightSection(workout, weights)
    DateRow(timestamp, onEditDate)
    SaveButton {
        val updated = updatedMovements(workout, weights)
        onSave(WorkoutResult(elapsedSeconds = total, movements = updated, timestamp = timestamp), updated)
    }
}
