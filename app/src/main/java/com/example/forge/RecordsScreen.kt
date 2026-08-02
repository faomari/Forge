package com.example.forge

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecordsScreen(
    records: List<PersonalRecord>,
    onSave: (PersonalRecord) -> Unit,
    onDelete: (String) -> Unit
) {
    var editing by remember { mutableStateOf<PersonalRecord?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(
                onClick = { editing = null; showEditor = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add record")
            }
        }

        if (records.isEmpty()) {
            item {
                Text(
                    "No records yet. Add your PRs — lifts, benchmark times, whatever you want to beat.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            items(records.sortedBy { it.name.lowercase() }, key = { it.id }) { rec ->
                RecordCard(
                    record = rec,
                    onEdit = { editing = rec; showEditor = true },
                    onDelete = { confirmDeleteId = rec.id }
                )
            }
        }
    }

    if (showEditor) {
        RecordEditor(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { rec -> onSave(rec); showEditor = false }
        )
    }

    confirmDeleteId?.let { id ->
        ConfirmDialog(
            title = "Delete record?",
            message = "This personal record will be removed.",
            confirmLabel = "Delete",
            onConfirm = { onDelete(id); confirmDeleteId = null },
            onDismiss = { confirmDeleteId = null }
        )
    }
}

@Composable
private fun RecordCard(record: PersonalRecord, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(record.name.ifBlank { "Record" }, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(formatShortDate(record.timestamp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            if (!record.note.isNullOrBlank()) {
                Text(record.note, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Text(record.displayValue, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Row {
            TextButton(onClick = onEdit) { Text("Edit") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordEditor(
    initial: PersonalRecord?,
    onDismiss: () -> Unit,
    onSave: (PersonalRecord) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var kind by remember { mutableStateOf(initial?.kind ?: PrKind.WEIGHT) }
    var weight by remember { mutableStateOf(if (initial?.kind == PrKind.WEIGHT && initial.weight > 0) formatWeight(initial.weight) else "") }
    var minutes by remember { mutableStateOf(if (initial?.kind == PrKind.TIME) (initial.seconds / 60).toString() else "") }
    var seconds by remember { mutableStateOf(if (initial?.kind == PrKind.TIME) (initial.seconds % 60).toString() else "") }
    var count by remember { mutableStateOf(if ((initial?.kind == PrKind.REPS || initial?.kind == PrKind.ROUNDS) && initial.count > 0) initial.count.toString() else "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(if (initial == null) "New record" else "Edit record") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (e.g. Back squat 1RM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrKind.values().forEach { k ->
                        FilterChip(
                            selected = kind == k,
                            onClick = { kind = k },
                            label = { Text(k.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                when (kind) {
                    PrKind.WEIGHT -> OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Weight (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    PrKind.TIME -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = minutes,
                            onValueChange = { minutes = it.filter { c -> c.isDigit() } },
                            label = { Text("Min") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = seconds,
                            onValueChange = { seconds = it.filter { c -> c.isDigit() } },
                            label = { Text("Sec") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    PrKind.REPS, PrKind.ROUNDS -> OutlinedTextField(
                        value = count,
                        onValueChange = { count = it.filter { c -> c.isDigit() } },
                        label = { Text(if (kind == PrKind.REPS) "Reps" else "Rounds") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val base = initial ?: PersonalRecord()
                val rec = base.copy(
                    name = name.trim(),
                    kind = kind,
                    weight = if (kind == PrKind.WEIGHT) weight.toDoubleOrNull() ?: 0.0 else 0.0,
                    seconds = if (kind == PrKind.TIME) (minutes.toIntOrNull() ?: 0) * 60 + (seconds.toIntOrNull() ?: 0) else 0,
                    count = if (kind == PrKind.REPS || kind == PrKind.ROUNDS) count.toIntOrNull() ?: 0 else 0,
                    timestamp = System.currentTimeMillis(),
                    note = note.trim().ifBlank { null }
                )
                if (rec.name.isNotBlank()) onSave(rec)
            }) {
                Text("Save", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
