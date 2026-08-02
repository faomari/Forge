package com.example.forge

import java.util.UUID

enum class WorkoutType(val label: String, val badge: String) {
    STRENGTH("Strength", "STRENGTH"),
    AMRAP("AMRAP", "AMRAP"),
    EMOM("EMOM", "EMOM"),
    TABATA("Tabata", "TABATA"),
    FOR_TIME("For time", "FOR TIME")
}

// A prescribed movement. For WOD types this is "reps x name @ weight".
// For strength this is just an exercise name (reps/weight are logged per set).
data class Movement(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val reps: Int = 0,
    val weight: Double = 0.0
)

// One logged set for strength.
data class SetEntry(
    val weight: Double = 0.0,
    val reps: Int = 0
)

// A strength exercise with its logged sets for one session.
data class StrengthEntry(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val sets: List<SetEntry> = emptyList()
)

// A single attempt. Only the fields relevant to the workout's type are used.
data class WorkoutResult(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    // FOR_TIME
    val elapsedSeconds: Int? = null,
    val movements: List<Movement> = emptyList(),
    // AMRAP
    val rounds: Int? = null,
    val extraReps: Int? = null,
    // EMOM / TABATA
    val completed: Boolean? = null,
    val note: String? = null,
    // STRENGTH
    val strength: List<StrengthEntry> = emptyList()
) {
    val primaryWeight: Double
        get() = movements.filter { it.weight > 0.0 }.maxByOrNull { it.weight }?.weight ?: 0.0

    val heaviestStrengthSet: SetEntry?
        get() = strength.flatMap { it.sets }.maxByOrNull { it.weight }
}

data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: WorkoutType = WorkoutType.FOR_TIME,
    val photo: String? = null,
    val notes: String? = null,
    val scheme: String? = null,
    val movements: List<Movement> = emptyList(),
    // Timer config (only some apply per type)
    val timeCapSeconds: Int? = null,   // AMRAP cap, FOR_TIME optional cap
    val intervalSeconds: Int? = null,  // EMOM interval length
    val rounds: Int? = null,           // EMOM / TABATA number of rounds
    val workSeconds: Int? = null,      // TABATA work
    val restSeconds: Int? = null,      // TABATA rest
    val results: List<WorkoutResult> = emptyList()
) {
    val primaryWeight: Double
        get() = movements.filter { it.weight > 0.0 }.maxByOrNull { it.weight }?.weight ?: 0.0

    val fastest: WorkoutResult?
        get() = results.filter { it.elapsedSeconds != null }.minByOrNull { it.elapsedSeconds!! }

    val bestAmrap: WorkoutResult?
        get() = results.filter { it.rounds != null }
            .maxByOrNull { (it.rounds ?: 0) * 100000 + (it.extraReps ?: 0) }

    val heaviestSet: SetEntry?
        get() = results.mapNotNull { it.heaviestStrengthSet }.maxByOrNull { it.weight }
}

enum class PrKind(val label: String) {
    WEIGHT("Weight (kg)"),
    TIME("Time"),
    REPS("Reps"),
    ROUNDS("Rounds")
}

// A personal record the user records and maintains themselves.
data class PersonalRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val kind: PrKind = PrKind.WEIGHT,
    val weight: Double = 0.0,   // WEIGHT
    val seconds: Int = 0,       // TIME
    val count: Int = 0,         // REPS / ROUNDS
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
) {
    val displayValue: String
        get() = when (kind) {
            PrKind.WEIGHT -> "${formatWeight(weight)} kg"
            PrKind.TIME -> formatClock(seconds)
            PrKind.REPS -> "$count reps"
            PrKind.ROUNDS -> "$count rounds"
        }
}

// Wrapper for export/import of all app data.
data class BackupData(
    val workouts: List<Workout> = emptyList(),
    val records: List<PersonalRecord> = emptyList()
)
