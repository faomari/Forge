package com.example.forge

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

private data class SetDraft(val id: String = UUID.randomUUID().toString(), val weight: String = "", val reps: String = "")
private data class ExerciseDraft(val exerciseId: String, val name: String, val sets: List<SetDraft>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrengthLogScreen(
    workout: Workout,
    onCancel: () -> Unit,
    onSave: (WorkoutResult) -> Unit
) {
    // Pre-fill each exercise from its most recent logged session.
    val initialDrafts = remember {
        workout.movements.map { m ->
            val lastSets = workout.results
                .sortedByDescending { it.timestamp }
                .firstNotNullOfOrNull { res ->
                    res.strength.firstOrNull { it.exerciseName == m.name }?.sets
                }
            val drafts = if (lastSets != null && lastSets.isNotEmpty()) {
                lastSets.map { SetDraft(weight = formatWeight(it.weight), reps = it.reps.toString()) }
            } else {
                listOf(SetDraft())
            }
            ExerciseDraft(exerciseId = m.id, name = m.name, sets = drafts)
        }
    }
    var exercises by remember { mutableStateOf(initialDrafts) }

    fun updateExercise(exId: String, transform: (ExerciseDraft) -> ExerciseDraft) {
        exercises = exercises.map { if (it.exerciseId == exId) transform(it) else it }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Log · ${workout.name.ifBlank { "Strength" }}") },
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
            exercises.forEach { ex ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                ) {
                    Text(ex.name.ifBlank { "Exercise" }, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))

                    ex.sets.forEachIndexed { index, set ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.width(20.dp))
                            OutlinedTextField(
                                value = set.weight,
                                onValueChange = { v ->
                                    updateExercise(ex.exerciseId) { e ->
                                        e.copy(sets = e.sets.map { if (it.id == set.id) it.copy(weight = v.filter { c -> c.isDigit() || c == '.' }) else it })
                                    }
                                },
                                label = { Text("kg") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = set.reps,
                                onValueChange = { v ->
                                    updateExercise(ex.exerciseId) { e ->
                                        e.copy(sets = e.sets.map { if (it.id == set.id) it.copy(reps = v.filter { c -> c.isDigit() }) else it })
                                    }
                                },
                                label = { Text("reps") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                updateExercise(ex.exerciseId) { e -> e.copy(sets = e.sets.filter { it.id != set.id }) }
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove set", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { updateExercise(ex.exerciseId) { e -> e.copy(sets = e.sets + SetDraft()) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Add set") }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val entries = exercises.map { ex ->
                        StrengthEntry(
                            exerciseId = ex.exerciseId,
                            exerciseName = ex.name,
                            sets = ex.sets.mapNotNull { s ->
                                val reps = s.reps.toIntOrNull()
                                val weight = s.weight.toDoubleOrNull()
                                if (reps != null && reps > 0) SetEntry(weight = weight ?: 0.0, reps = reps) else null
                            }
                        )
                    }.filter { it.sets.isNotEmpty() }
                    onSave(WorkoutResult(strength = entries))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Save session", fontWeight = FontWeight.Medium) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
