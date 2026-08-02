package com.example.forge

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Rowing
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val ForgeColors = darkColorScheme(
    primary = Color(0xFF97C459),
    onPrimary = Color(0xFF12240A),
    secondary = Color(0xFFC0DD97),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF5F5F2),
    surface = Color(0xFF1A1D23),
    onSurface = Color(0xFFF5F5F2),
    surfaceVariant = Color(0xFF2A2E36),
    onSurfaceVariant = Color(0xFF9CA3AF),
    error = Color(0xFFF09595),
    outline = Color(0xFF3A3F47)
)

// Badge colours per workout type (background, foreground).
fun typeBadgeColors(type: WorkoutType): Pair<Color, Color> = when (type) {
    WorkoutType.STRENGTH -> Color(0xFF2E2A57) to Color(0xFFC7C2F0)
    WorkoutType.AMRAP -> Color(0xFF1B3A1E) to Color(0xFFBFE3B4)
    WorkoutType.EMOM -> Color(0xFF4A1414) to Color(0xFFF0B0B0)
    WorkoutType.TABATA -> Color(0xFF0E3A38) to Color(0xFFA8E0DC)
    WorkoutType.FOR_TIME -> Color(0xFF3E2A08) to Color(0xFFF3C77A)
}

// Bundled photos the user can pick from.
data class AppPhoto(val key: String, val resId: Int, val label: String)

val photoCatalog: List<AppPhoto> = listOf(
    AppPhoto("deadlift", R.drawable.photo_deadlift, "Deadlift"),
    AppPhoto("bench", R.drawable.photo_bench, "Bench press"),
    AppPhoto("ohpress", R.drawable.photo_ohpress, "Overhead press"),
    AppPhoto("frontsquat", R.drawable.photo_frontsquat, "Front squat"),
    AppPhoto("ohsquat", R.drawable.photo_ohsquat, "Overhead squat"),
    AppPhoto("oly_jerk", R.drawable.photo_oly_jerk, "Jerk"),
    AppPhoto("deadlift_w", R.drawable.photo_deadlift_w, "Deadlift (W)"),
    AppPhoto("frontsquat_w", R.drawable.photo_frontsquat_w, "Front squat (W)"),
    AppPhoto("clean_m", R.drawable.photo_clean_m, "Clean (M)"),
    AppPhoto("jerk_w", R.drawable.photo_jerk_w, "Jerk (W)"),
    AppPhoto("ohpress_w", R.drawable.photo_ohpress_w, "Overhead press (W)"),
    AppPhoto("ohsquat_w", R.drawable.photo_ohsquat_w, "Overhead squat (W)"),
    AppPhoto("bench_w", R.drawable.photo_bench_w, "Bench press (W)"),
    AppPhoto("rdl", R.drawable.photo_rdl, "Hinge"),
    AppPhoto("pushup", R.drawable.photo_pushup, "Push-up"),
    AppPhoto("t2b", R.drawable.photo_t2b, "Pull-ups"),
    AppPhoto("kbswing", R.drawable.photo_kbswing, "KB swing"),
    AppPhoto("boxjump", R.drawable.photo_boxjump, "Box jump"),
    AppPhoto("ropes", R.drawable.photo_ropes, "Battle ropes"),
    AppPhoto("sled", R.drawable.photo_sled, "Sled"),
    AppPhoto("row", R.drawable.photo_row, "Row"),
    AppPhoto("bike", R.drawable.photo_bike, "Bike"),
    AppPhoto("treadmill", R.drawable.photo_treadmill, "Treadmill"),
    AppPhoto("runner", R.drawable.photo_runner, "Running")
)

fun photoResFor(key: String?): Int? =
    if (key == null) null else photoCatalog.firstOrNull { it.key == key }?.resId

fun formatClock(totalSeconds: Int): String {
    val safe = if (totalSeconds < 0) 0 else totalSeconds
    return "%d:%02d".format(safe / 60, safe % 60)
}

fun formatWeight(weight: Double): String =
    if (weight % 1.0 == 0.0) weight.toInt().toString() else weight.toString()

fun formatShortDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

// One-line summary of the prescribed work, used on cards and detail.
fun movementSummary(workout: Workout): String {
    if (workout.movements.isEmpty()) return "No movements yet"
    val hasScheme = !workout.scheme.isNullOrBlank()
    return workout.movements.joinToString(" · ") { m ->
        val load = if (m.weight > 0.0) " @ ${formatWeight(m.weight)}kg" else ""
        val reps = if (!hasScheme && m.reps > 0) "${m.reps} " else ""
        "$reps${m.name}$load"
    }
}

// A short description of the format/config, e.g. "20:00 AMRAP".
fun formatConfig(workout: Workout): String = when (workout.type) {
    WorkoutType.AMRAP ->
        (workout.timeCapSeconds?.let { formatClock(it) } ?: "--:--") + " AMRAP"
    WorkoutType.EMOM -> {
        val every = workout.intervalSeconds ?: 60
        val r = workout.rounds ?: 0
        "EMOM · every ${formatClock(every)} · $r rounds"
    }
    WorkoutType.TABATA -> {
        val w = workout.workSeconds ?: 20
        val rest = workout.restSeconds ?: 10
        val r = workout.rounds ?: 8
        "Tabata · ${w}s / ${rest}s × $r"
    }
    WorkoutType.FOR_TIME ->
        "For time" + (workout.timeCapSeconds?.let { " · cap ${formatClock(it)}" } ?: "")
    WorkoutType.STRENGTH ->
        "${workout.movements.size} exercise" + if (workout.movements.size == 1) "" else "s"
}

data class Nudge(val dropPercent: Int, val sessions: Int, val suggestedWeight: Double)

fun computeNudge(workout: Workout): Nudge? {
    if (workout.type != WorkoutType.FOR_TIME) return null
    val w = workout.primaryWeight
    if (w <= 0.0) return null
    val atWeight = workout.results
        .filter { it.elapsedSeconds != null && it.primaryWeight == w }
        .sortedBy { it.timestamp }
    if (atWeight.size < 2) return null
    val oldest = atWeight.first().elapsedSeconds ?: return null
    val newest = atWeight.last().elapsedSeconds ?: return null
    if (oldest <= 0 || newest >= oldest) return null
    val dropPercent = ((oldest - newest) * 100.0 / oldest).toInt()
    if (dropPercent < 10) return null
    return Nudge(dropPercent, atWeight.size, w + 2.5)
}

// Ordered keyword -> photo key. Specific phrases first so they win over generic ones.
private val photoKeywords: List<Pair<String, String>> = listOf(
    "romanian deadlift" to "rdl", "good morning" to "rdl", "hip thrust" to "rdl",
    "deadlift" to "deadlift", "barbell row" to "deadlift", "bent" to "deadlift",
    "front squat" to "frontsquat", "overhead squat" to "ohsquat", "back squat" to "frontsquat",
    "goblet" to "frontsquat", "pistol" to "pushup",
    "snatch balance" to "ohsquat", "snatch pull" to "ohsquat", "snatch" to "ohsquat",
    "clean & jerk" to "oly_jerk", "clean and jerk" to "oly_jerk", "hang clean" to "oly_jerk",
    "power clean" to "oly_jerk", "squat clean" to "oly_jerk", "clean" to "oly_jerk",
    "jerk" to "oly_jerk", "thruster" to "oly_jerk", "overhead press" to "ohpress",
    "push press" to "ohpress", "push jerk" to "oly_jerk", "shoulder press" to "ohpress",
    "bench" to "bench", "push-up" to "pushup", "push up" to "pushup",
    "handstand" to "pushup", "hspu" to "pushup", "dip" to "pushup", "burpee" to "pushup",
    "sit-up" to "pushup", "sit up" to "pushup", "back extension" to "rdl",
    "wall ball" to "boxjump", "box jump" to "boxjump", "box" to "boxjump",
    "double-under" to "boxjump", "double under" to "boxjump", "broad jump" to "boxjump",
    "pull-up" to "t2b", "pull up" to "t2b", "chin" to "t2b", "toes" to "t2b",
    "t2b" to "t2b", "knees to" to "t2b", "k2e" to "t2b", "muscle-up" to "t2b", "muscle up" to "t2b",
    "ski" to "row", "row" to "row",
    "farmer" to "sled", "carry" to "sled", "sled" to "sled",
    "lunge" to "frontsquat", "swing" to "kbswing", "kettlebell" to "kbswing",
    "kb " to "kbswing", "rope" to "ropes",
    "bike" to "bike", "assault" to "bike", "echo" to "bike",
    "run" to "runner", "mile" to "runner", "km" to "runner", "sprint" to "runner",
    "tempo" to "runner", "distance" to "runner",
    "400m" to "runner", "800m" to "runner", "200m" to "runner", "1000m" to "row", "500m" to "row"
)

// Best photo for a workout, derived from its first recognisable movement.
fun movementPhoto(workout: Workout): String? {
    for (mv in workout.movements) {
        val n = mv.name.lowercase()
        for ((kw, key) in photoKeywords) if (n.contains(kw)) return key
    }
    return null
}

// Photo actually shown on cards: the user's explicit pick wins; otherwise match the movement.
fun displayPhoto(workout: Workout): String? = workout.photo ?: movementPhoto(workout)

// Cinematic condensed display font for workout titles.
val ForgeDisplay = FontFamily(Font(R.font.bebas_neue))

// Gold used for the workout-type badge (outlined pill).
val BadgeGold = Color(0xFFCB9A4E)

// Small icon that represents a movement, for scannable card rows.
fun movementIcon(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        n.startsWith("row") || n.contains("skierg") || n.contains("ski ") -> Icons.Filled.Rowing
        n.contains("bike") || n.contains("assault") || n.contains("echo") -> Icons.Filled.DirectionsBike
        listOf("run", "mile", " km", "sprint", "400m", "800m", "200m", "tempo", "distance").any { n.contains(it) } -> Icons.Filled.DirectionsRun
        listOf("thruster", "clean", "snatch", "deadlift", "squat", "press", "jerk",
            "bench", "curl", "barbell", "lunge", "carry", "farmer", "swing",
            "kettlebell", "kb ", "sled", "wall ball", "row").any { n.contains(it) } -> Icons.Filled.FitnessCenter
        else -> Icons.Filled.SportsGymnastics
    }
}

// A movement's one-line label. When the workout uses a rep scheme (e.g. 21-15-9),
// per-movement reps are omitted because they vary by round.
fun movementLine(workout: Workout, m: Movement): String {
    val hasScheme = !workout.scheme.isNullOrBlank()
    val reps = if (!hasScheme && m.reps > 0) "${m.reps} " else ""
    val load = if (m.weight > 0.0) " @ ${formatWeight(m.weight)}kg" else ""
    return "$reps${m.name}$load"
}
