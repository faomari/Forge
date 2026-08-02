package com.example.forge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

private data class MoveDraft(val id: String, val name: String, val reps: String, val weight: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    initial: Workout,
    isNew: Boolean,
    onCancel: () -> Unit,
    onSave: (Workout) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var type by remember { mutableStateOf(initial.type) }
    var photo by remember { mutableStateOf(initial.photo) }
    var scheme by remember { mutableStateOf(initial.scheme ?: "") }
    var notes by remember { mutableStateOf(initial.notes ?: "") }
    var showPhotoPicker by remember { mutableStateOf(false) }
    var capMinutes by remember { mutableStateOf(initial.timeCapSeconds?.let { (it / 60).toString() } ?: "") }
    var intervalSeconds by remember { mutableStateOf((initial.intervalSeconds ?: 60).toString()) }
    var rounds by remember { mutableStateOf((initial.rounds ?: if (initial.type == WorkoutType.TABATA) 8 else 10).toString()) }
    var workSeconds by remember { mutableStateOf((initial.workSeconds ?: 20).toString()) }
    var restSeconds by remember { mutableStateOf((initial.restSeconds ?: 10).toString()) }
    var moves by remember {
        mutableStateOf(
            initial.movements.map {
                MoveDraft(it.id, it.name, if (it.reps > 0) it.reps.toString() else "", if (it.weight > 0.0) formatWeight(it.weight) else "")
            }.ifEmpty { listOf(MoveDraft(UUID.randomUUID().toString(), "", "", "")) }
        )
    }

    fun save() {
        val builtMoves = moves.filter { it.name.isNotBlank() }.map {
            Movement(id = it.id, name = it.name.trim(), reps = it.reps.toIntOrNull() ?: 0, weight = it.weight.toDoubleOrNull() ?: 0.0)
        }
        val result = initial.copy(
            name = name.trim(),
            type = type,
            photo = photo,
            scheme = scheme.trim().ifBlank { null },
            notes = notes.trim().ifBlank { null },
            movements = builtMoves,
            timeCapSeconds = when (type) {
                WorkoutType.AMRAP -> (capMinutes.toIntOrNull() ?: 0).let { if (it > 0) it * 60 else 0 }
                WorkoutType.FOR_TIME -> capMinutes.toIntOrNull()?.let { if (it > 0) it * 60 else null }
                else -> null
            },
            intervalSeconds = if (type == WorkoutType.EMOM) intervalSeconds.toIntOrNull() ?: 60 else null,
            rounds = when (type) {
                WorkoutType.EMOM, WorkoutType.TABATA -> rounds.toIntOrNull() ?: 8
                else -> null
            },
            workSeconds = if (type == WorkoutType.TABATA) workSeconds.toIntOrNull() ?: 20 else null,
            restSeconds = if (type == WorkoutType.TABATA) restSeconds.toIntOrNull() ?: 10 else null
        )
        onSave(result)
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New workout" else "Edit workout") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { TextButton(onClick = { save() }) { Text("Save", color = MaterialTheme.colorScheme.primary) } },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Workout name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            SectionLabel("TYPE")
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkoutType.values().forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        label = { Text(t.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (type != WorkoutType.STRENGTH) {
                Spacer(Modifier.height(16.dp))
                SectionLabel("REP SCHEME (OPTIONAL)")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = scheme,
                    onValueChange = { scheme = it },
                    label = { Text("e.g. 21-15-9") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Set the scheme once, then list each movement a single time below — no need to repeat reps per round.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("PHOTO")
            Spacer(Modifier.height(8.dp))
            PhotoPreviewButton(photo = photo, onClick = { showPhotoPicker = true })

            Spacer(Modifier.height(16.dp))
            SectionLabel("NOTES (OPTIONAL)")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Anything to remember — strategy, scaling, target time…") },
                modifier = Modifier.fillMaxWidth().height(110.dp)
            )

            Spacer(Modifier.height(16.dp))
            TypeConfig(
                type = type,
                capMinutes = capMinutes, onCap = { capMinutes = it },
                intervalSeconds = intervalSeconds, onInterval = { intervalSeconds = it },
                rounds = rounds, onRounds = { rounds = it },
                workSeconds = workSeconds, onWork = { workSeconds = it },
                restSeconds = restSeconds, onRest = { restSeconds = it }
            )

            Spacer(Modifier.height(16.dp))
            SectionLabel(if (type == WorkoutType.STRENGTH) "EXERCISES" else "MOVEMENTS")
            Spacer(Modifier.height(8.dp))
            moves.forEachIndexed { index, draft ->
                MovementEditor(
                    draft = draft,
                    strength = type == WorkoutType.STRENGTH,
                    onChange = { updated -> moves = moves.toMutableList().also { it[index] = updated } },
                    onDelete = { moves = moves.filterIndexed { i, _ -> i != index } }
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { moves = moves + MoveDraft(UUID.randomUUID().toString(), "", "", "") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface)
            ) { Text(if (type == WorkoutType.STRENGTH) "+ Add exercise" else "+ Add movement") }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { save() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) { Text("Save workout") }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showPhotoPicker) {
        PhotoPickerOverlay(
            current = photo,
            onSelect = { photo = it; showPhotoPicker = false },
            onDismiss = { showPhotoPicker = false }
        )
    }
    }
}

@Composable
private fun PhotoPreviewButton(photo: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
    ) {
        val res = photoResFor(photo)
        if (res != null) {
            Image(
                painter = painterResource(res),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55000000))
            )
        }
        Text(
            if (res != null) "Tap to change photo" else "Tap to choose a photo",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun PhotoPickerOverlay(current: String?, onSelect: (String?) -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Choose a photo", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            PickerOption(label = "No photo", res = null, selected = current == null) { onSelect(null) }
            photoCatalog.forEach { p ->
                PickerOption(label = p.label, res = p.resId, selected = current == p.key) { onSelect(p.key) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PickerOption(label: String, res: Int?, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
    ) {
        if (res != null) {
            Image(
                painter = painterResource(res),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66000000))
        )
        Text(
            label,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        )
    }
}

@Composable
private fun TypeConfig(
    type: WorkoutType,
    capMinutes: String, onCap: (String) -> Unit,
    intervalSeconds: String, onInterval: (String) -> Unit,
    rounds: String, onRounds: (String) -> Unit,
    workSeconds: String, onWork: (String) -> Unit,
    restSeconds: String, onRest: (String) -> Unit
) {
    when (type) {
        WorkoutType.AMRAP -> NumberField(capMinutes, "Time cap (minutes)", onCap, Modifier.fillMaxWidth())
        WorkoutType.FOR_TIME -> NumberField(capMinutes, "Time cap in minutes (optional)", onCap, Modifier.fillMaxWidth())
        WorkoutType.EMOM -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField(intervalSeconds, "Interval (sec)", onInterval, Modifier.weight(1f))
            NumberField(rounds, "Rounds", onRounds, Modifier.weight(1f))
        }
        WorkoutType.TABATA -> Column {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumberField(workSeconds, "Work (sec)", onWork, Modifier.weight(1f))
                NumberField(restSeconds, "Rest (sec)", onRest, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            NumberField(rounds, "Rounds", onRounds, Modifier.fillMaxWidth())
        }
        WorkoutType.STRENGTH -> {}
    }
}

@Composable
private fun NumberField(value: String, label: String, onChange: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

@Composable
private fun MovementEditor(
    draft: MoveDraft,
    strength: Boolean,
    onChange: (MoveDraft) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft.name,
                onValueChange = { onChange(draft.copy(name = it)) },
                label = { Text(if (strength) "Exercise" else "Movement") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (!strength) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumberField(draft.reps, "Reps", { onChange(draft.copy(reps = it)) }, Modifier.weight(1f))
                OutlinedTextField(
                    value = draft.weight,
                    onValueChange = { onChange(draft.copy(weight = it.filter { c -> c.isDigit() || c == '.' })) },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
