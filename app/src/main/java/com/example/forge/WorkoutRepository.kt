package com.example.forge

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkoutRepository(context: Context) {

    private val prefs = context.getSharedPreferences("forge", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun load(): List<Workout> {
        if (!prefs.getBoolean(KEY_SEEDED, false)) {
            val seeded = sampleWorkouts()
            save(seeded)
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
            return seeded
        }
        val json = prefs.getString(KEY_WORKOUTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Workout>>() {}.type
            gson.fromJson<List<Workout>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(workouts: List<Workout>) {
        prefs.edit().putString(KEY_WORKOUTS, gson.toJson(workouts)).apply()
    }

    fun loadRecords(): List<PersonalRecord> {
        val json = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<PersonalRecord>>() {}.type
            gson.fromJson<List<PersonalRecord>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveRecords(records: List<PersonalRecord>) {
        prefs.edit().putString(KEY_RECORDS, gson.toJson(records)).apply()
    }

    private fun sampleWorkouts(): List<Workout> {
        val day = 24L * 60 * 60 * 1000
        val now = System.currentTimeMillis()

        val forTime = run {
            fun cleans(w: Double) = listOf(Movement(name = "Barbell cleans", reps = 50, weight = w))
            Workout(
                name = "50 barbell cleans",
                type = WorkoutType.FOR_TIME,
                photo = "oly_jerk",
                movements = cleans(40.0),
                results = listOf(
                    WorkoutResult(timestamp = now - 21 * day, elapsedSeconds = 270, movements = cleans(37.5)),
                    WorkoutResult(timestamp = now - 14 * day, elapsedSeconds = 252, movements = cleans(40.0)),
                    WorkoutResult(timestamp = now - 7 * day, elapsedSeconds = 232, movements = cleans(40.0)),
                    WorkoutResult(timestamp = now - 1 * day, elapsedSeconds = 220, movements = cleans(40.0))
                )
            )
        }

        val amrap = Workout(
            name = "Cindy",
            type = WorkoutType.AMRAP,
            photo = "t2b",
            timeCapSeconds = 20 * 60,
            movements = listOf(
                Movement(name = "Pull-ups", reps = 5),
                Movement(name = "Push-ups", reps = 10),
                Movement(name = "Air squats", reps = 15)
            ),
            results = listOf(
                WorkoutResult(timestamp = now - 20 * day, rounds = 19, extraReps = 3),
                WorkoutResult(timestamp = now - 6 * day, rounds = 21, extraReps = 8)
            )
        )

        val emom = Workout(
            name = "Clean & jerk EMOM",
            type = WorkoutType.EMOM,
            photo = "oly_jerk",
            intervalSeconds = 60,
            rounds = 12,
            movements = listOf(Movement(name = "Clean & jerk", reps = 2, weight = 50.0)),
            results = listOf(
                WorkoutResult(timestamp = now - 5 * day, completed = true, note = "Felt strong, unbroken")
            )
        )

        val tabata = Workout(
            name = "Tabata squats",
            type = WorkoutType.TABATA,
            photo = "ropes",
            workSeconds = 20,
            restSeconds = 10,
            rounds = 8,
            movements = listOf(Movement(name = "Air squats")),
            results = listOf(
                WorkoutResult(timestamp = now - 3 * day, completed = true, note = "132 total reps")
            )
        )

        val strength = Workout(
            name = "Deadlifts",
            type = WorkoutType.STRENGTH,
            photo = "oly_jerk",
            movements = listOf(Movement(name = "Deadlift")),
            results = listOf(
                strengthSession(now - 17 * day, "Deadlift", listOf(80.0 to 10, 100.0 to 8, 120.0 to 6, 100.0 to 8)),
                strengthSession(now - 10 * day, "Deadlift", listOf(80.0 to 10, 100.0 to 8, 130.0 to 6, 100.0 to 8)),
                strengthSession(now - 3 * day, "Deadlift", listOf(80.0 to 10, 100.0 to 8, 134.0 to 6, 100.0 to 8))
            )
        )

        return listOf(strength, forTime, amrap, emom, tabata)
    }

    private fun strengthSession(ts: Long, exercise: String, sets: List<Pair<Double, Int>>): WorkoutResult {
        return WorkoutResult(
            timestamp = ts,
            strength = listOf(
                StrengthEntry(
                    exerciseName = exercise,
                    sets = sets.map { SetEntry(weight = it.first, reps = it.second) }
                )
            )
        )
    }

    companion object {
        private const val KEY_WORKOUTS = "workouts"
        private const val KEY_RECORDS = "records"
        private const val KEY_SEEDED = "seeded_v2"
    }
}
