package com.example.forge

// Built-in workouts the user can browse by category and add to their own list.
// CrossFit benchmark weights are men's RX in kg; Strength/Olympic default to 0 so
// the user sets their own loads. Adjust after adding.

// Category keys used for the Browse picker.
object Category {
    const val CROSSFIT = "CrossFit"
    const val RUNNING = "Running"
    const val HYROX = "Hyrox"
    const val STRENGTH = "Strength"
    const val OLYMPIC = "Olympic Lifting"
    val ordered = listOf(CROSSFIT, RUNNING, HYROX, STRENGTH, OLYMPIC)
}

data class CatalogEntry(val category: String, val workout: Workout)

private fun m(name: String, reps: Int = 0, weight: Double = 0.0) =
    Movement(name = name, reps = reps, weight = weight)

private fun crossfit(): List<CatalogEntry> = listOf(
    Workout(name = "Fran", type = WorkoutType.FOR_TIME, photo = "oly_jerk",
        notes = "21-15-9 reps for time", scheme = "21-15-9",
        movements = listOf(m("Thrusters", 21, 43.0), m("Pull-ups", 21))),
    Workout(name = "Cindy", type = WorkoutType.AMRAP, photo = "pushup",
        timeCapSeconds = 20 * 60, notes = "AMRAP in 20 min",
        movements = listOf(m("Pull-ups", 5), m("Push-ups", 10), m("Air squats", 15))),
    Workout(name = "Grace", type = WorkoutType.FOR_TIME, photo = "oly_jerk",
        notes = "30 reps for time",
        movements = listOf(m("Clean & jerk", 30, 61.0))),
    Workout(name = "Isabel", type = WorkoutType.FOR_TIME, photo = "oly_jerk",
        notes = "30 reps for time",
        movements = listOf(m("Snatch", 30, 61.0))),
    Workout(name = "Helen", type = WorkoutType.FOR_TIME, photo = "ropes",
        notes = "3 rounds for time",
        movements = listOf(m("Run 400m"), m("KB swings", 21, 24.0), m("Pull-ups", 12))),
    Workout(name = "Diane", type = WorkoutType.FOR_TIME, photo = "deadlift",
        notes = "21-15-9 reps for time", scheme = "21-15-9",
        movements = listOf(m("Deadlifts", 21, 102.0), m("Handstand push-ups", 21))),
    Workout(name = "Elizabeth", type = WorkoutType.FOR_TIME, photo = "oly_jerk",
        notes = "21-15-9 reps for time", scheme = "21-15-9",
        movements = listOf(m("Squat cleans", 21, 61.0), m("Ring dips", 21))),
    Workout(name = "Annie", type = WorkoutType.FOR_TIME, photo = "boxjump",
        notes = "50-40-30-20-10 reps for time", scheme = "50-40-30-20-10",
        movements = listOf(m("Double-unders", 50), m("Sit-ups", 50))),
    Workout(name = "Barbara", type = WorkoutType.FOR_TIME, photo = "pushup",
        notes = "5 rounds for time · 3 min rest between rounds",
        movements = listOf(m("Pull-ups", 20), m("Push-ups", 30), m("Sit-ups", 40), m("Air squats", 50))),
    Workout(name = "Chelsea", type = WorkoutType.EMOM, photo = "t2b",
        intervalSeconds = 60, rounds = 30, notes = "EMOM for 30 min",
        movements = listOf(m("Pull-ups", 5), m("Push-ups", 10), m("Air squats", 15))),
    Workout(name = "Karen", type = WorkoutType.FOR_TIME, photo = "boxjump",
        notes = "150 reps for time",
        movements = listOf(m("Wall balls", 150, 9.0))),
    Workout(name = "Angie", type = WorkoutType.FOR_TIME, photo = "t2b",
        notes = "For time · finish each before moving on",
        movements = listOf(m("Pull-ups", 100), m("Push-ups", 100), m("Sit-ups", 100), m("Air squats", 100))),
    Workout(name = "Nancy", type = WorkoutType.FOR_TIME, photo = "ohsquat",
        notes = "5 rounds for time",
        movements = listOf(m("Run 400m"), m("Overhead squats", 15, 43.0))),
    Workout(name = "Jackie", type = WorkoutType.FOR_TIME, photo = "row",
        notes = "For time",
        movements = listOf(m("Row 1000m"), m("Thrusters", 50, 20.0), m("Pull-ups", 30))),
    Workout(name = "Mary", type = WorkoutType.AMRAP, photo = "ohsquat",
        timeCapSeconds = 20 * 60, notes = "AMRAP in 20 min",
        movements = listOf(m("Handstand push-ups", 5), m("Pistols", 10), m("Pull-ups", 15))),
    Workout(name = "Kelly", type = WorkoutType.FOR_TIME, photo = "boxjump",
        notes = "5 rounds for time",
        movements = listOf(m("Run 400m"), m("Box jumps", 30, 0.0), m("Wall balls", 30, 9.0))),
    Workout(name = "Eva", type = WorkoutType.FOR_TIME, photo = "ropes",
        notes = "5 rounds for time",
        movements = listOf(m("Run 800m"), m("KB swings", 30, 32.0), m("Pull-ups", 30))),
    Workout(name = "The Chief", type = WorkoutType.AMRAP, photo = "oly_jerk",
        intervalSeconds = 180, rounds = 5, notes = "5 x 3-min AMRAP, 1 min rest between",
        movements = listOf(m("Power cleans", 3, 61.0), m("Push-ups", 6), m("Air squats", 9))),
    Workout(name = "Fight Gone Bad", type = WorkoutType.FOR_TIME, photo = "ropes",
        notes = "3 rounds · 1 min per station, 1 min rest",
        movements = listOf(m("Wall balls", 0, 9.0), m("SDHP", 0, 34.0), m("Box jumps", 0), m("Push press", 0, 34.0), m("Row (cal)", 0))),
    Workout(name = "Filthy Fifty", type = WorkoutType.FOR_TIME, photo = "boxjump",
        notes = "50 reps of each, for time",
        movements = listOf(m("Box jumps", 50), m("Jumping pull-ups", 50), m("KB swings", 50, 16.0),
            m("Walking lunges", 50), m("K2E", 50), m("Push press", 50, 20.0), m("Back extensions", 50),
            m("Wall balls", 50, 9.0), m("Burpees", 50), m("Double-unders", 50))),
    Workout(name = "Murph", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "For time · 9/6kg vest · partition the middle",
        movements = listOf(m("Run 1 mile"), m("Pull-ups", 100), m("Push-ups", 200), m("Air squats", 300), m("Run 1 mile"))),
    Workout(name = "DT", type = WorkoutType.FOR_TIME, photo = "deadlift",
        notes = "5 rounds for time",
        movements = listOf(m("Deadlifts", 12, 70.0), m("Hang power cleans", 9, 70.0), m("Push jerks", 6, 70.0))),
    Workout(name = "JT", type = WorkoutType.FOR_TIME, photo = "pushup",
        notes = "21-15-9 reps for time",
        movements = listOf(m("Handstand push-ups", 21), m("Ring dips", 21), m("Push-ups", 21))),
    Workout(name = "Michael", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "3 rounds for time",
        movements = listOf(m("Run 800m"), m("Back extensions", 50), m("Sit-ups", 50)))
).map { CatalogEntry(Category.CROSSFIT, it) }

private fun running(): List<CatalogEntry> = listOf(
    Workout(name = "8 x 400m", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "8 x 400m hard · 90s rest between. Log total working time.",
        movements = listOf(m("400m repeat", 8))),
    Workout(name = "6 x 800m", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "6 x 800m · 2 min rest between. Log total working time.",
        movements = listOf(m("800m repeat", 6))),
    Workout(name = "5 x 1km", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "5 x 1km at threshold · 2 min rest. Log total working time.",
        movements = listOf(m("1km repeat", 5))),
    Workout(name = "4 x 1 mile", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "4 x 1 mile · 3 min rest. Log total working time.",
        movements = listOf(m("1 mile repeat", 4))),
    Workout(name = "5K time trial", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "5km all out for time",
        movements = listOf(m("Run 5km"))),
    Workout(name = "10K time trial", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "10km all out for time",
        movements = listOf(m("Run 10km"))),
    Workout(name = "Cooper test", type = WorkoutType.AMRAP, photo = "runner",
        timeCapSeconds = 12 * 60, notes = "Run as far as possible in 12 min · log distance in reps (m)",
        movements = listOf(m("Max distance (m)"))),
    Workout(name = "Tempo 30 min", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "30 min sustained tempo · comfortably hard",
        movements = listOf(m("Tempo run 30 min"))),
    Workout(name = "Long run", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "Easy long run · log time, note distance",
        movements = listOf(m("Long run"))),
    Workout(name = "Pyramid intervals", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "200-400-800-400-200m hard · equal rest. Log total working time.",
        movements = listOf(m("200m"), m("400m"), m("800m"), m("400m"), m("200m")))
).map { CatalogEntry(Category.RUNNING, it) }

private fun hyrox(): List<CatalogEntry> = listOf(
    Workout(name = "Hyrox (full race)", type = WorkoutType.FOR_TIME, photo = "sled",
        notes = "For time · 8 runs + 8 stations, in order · set loads for your division",
        movements = listOf(
            m("Run 1km"), m("SkiErg 1000m"), m("Run 1km"), m("Sled push 50m"),
            m("Run 1km"), m("Sled pull 50m"), m("Run 1km"), m("Burpee broad jump 80m"),
            m("Run 1km"), m("Row 1000m"), m("Run 1km"), m("Farmers carry 200m"),
            m("Run 1km"), m("Sandbag lunges 100m"), m("Run 1km"), m("Wall balls", 100, 9.0))),
    Workout(name = "Half Hyrox", type = WorkoutType.FOR_TIME, photo = "sled",
        notes = "For time · 4 runs + 4 stations",
        movements = listOf(
            m("Run 1km"), m("SkiErg 500m"), m("Run 1km"), m("Sled push 25m"),
            m("Run 1km"), m("Row 500m"), m("Run 1km"), m("Wall balls", 50, 9.0))),
    Workout(name = "Hyrox stations (no run)", type = WorkoutType.FOR_TIME, photo = "row",
        notes = "For time · the 8 stations back to back",
        movements = listOf(
            m("SkiErg 1000m"), m("Sled push 50m"), m("Sled pull 50m"), m("Burpee broad jump 80m"),
            m("Row 1000m"), m("Farmers carry 200m"), m("Sandbag lunges 100m"), m("Wall balls", 100, 9.0))),
    Workout(name = "Hyrox running (8x1km)", type = WorkoutType.FOR_TIME, photo = "runner",
        notes = "For time · 8 x 1km, short rest between",
        movements = List(8) { m("Run 1km") }),
    Workout(name = "SkiErg 1000m", type = WorkoutType.FOR_TIME, photo = "row",
        notes = "1000m on the SkiErg for time",
        movements = listOf(m("SkiErg 1000m"))),
    Workout(name = "Row 1000m", type = WorkoutType.FOR_TIME, photo = "row",
        notes = "1000m row for time",
        movements = listOf(m("Row 1000m"))),
    Workout(name = "Sled push/pull", type = WorkoutType.FOR_TIME, photo = "sled",
        notes = "Sled push 50m + sled pull 50m for time · set your loads",
        movements = listOf(m("Sled push 50m"), m("Sled pull 50m"))),
    Workout(name = "Wall balls x100", type = WorkoutType.FOR_TIME, photo = "boxjump",
        notes = "100 wall balls for time",
        movements = listOf(m("Wall balls", 100, 9.0)))
).map { CatalogEntry(Category.HYROX, it) }

private fun strength(): List<CatalogEntry> = listOf(
    Workout(name = "Upper Body Strength", type = WorkoutType.STRENGTH, photo = "pushup",
        notes = "4 sets each · 6-8 reps",
        movements = listOf(m("Bench press"), m("Barbell row"), m("Overhead press"), m("Pull-ups"), m("Barbell curl"))),
    Workout(name = "Lower Body Strength", type = WorkoutType.STRENGTH, photo = "frontsquat",
        notes = "4 sets each · 5-8 reps",
        movements = listOf(m("Back squat"), m("Romanian deadlift"), m("Walking lunges"), m("Leg curl"), m("Calf raise"))),
    Workout(name = "Push Day", type = WorkoutType.STRENGTH, photo = "pushup",
        notes = "4 sets each · 6-10 reps",
        movements = listOf(m("Bench press"), m("Overhead press"), m("Incline dumbbell press"), m("Dips"), m("Triceps pushdown"))),
    Workout(name = "Pull Day", type = WorkoutType.STRENGTH, photo = "deadlift",
        notes = "4 sets each · 6-10 reps",
        movements = listOf(m("Deadlift"), m("Barbell row"), m("Pull-ups"), m("Face pull"), m("Barbell curl"))),
    Workout(name = "Full Body 5x5", type = WorkoutType.STRENGTH, photo = "frontsquat",
        notes = "5 sets x 5 reps",
        movements = listOf(m("Back squat"), m("Bench press"), m("Barbell row"))),
    Workout(name = "Back Squat 5x5", type = WorkoutType.STRENGTH, photo = "frontsquat",
        notes = "5 sets x 5 reps · add weight when all reps hit",
        movements = listOf(m("Back squat"))),
    Workout(name = "Bench Press 5x5", type = WorkoutType.STRENGTH, photo = "pushup",
        notes = "5 sets x 5 reps",
        movements = listOf(m("Bench press"))),
    Workout(name = "Deadlift 5x3", type = WorkoutType.STRENGTH, photo = "deadlift",
        notes = "5 sets x 3 reps · heavy",
        movements = listOf(m("Deadlift"))),
    Workout(name = "Overhead Press 5x5", type = WorkoutType.STRENGTH, photo = "oly_jerk",
        notes = "5 sets x 5 reps",
        movements = listOf(m("Overhead press"))),
    Workout(name = "Starting Strength A", type = WorkoutType.STRENGTH, photo = "frontsquat",
        notes = "Squat 3x5 · Bench 3x5 · Deadlift 1x5",
        movements = listOf(m("Back squat"), m("Bench press"), m("Deadlift"))),
    Workout(name = "Starting Strength B", type = WorkoutType.STRENGTH, photo = "oly_jerk",
        notes = "Squat 3x5 · Overhead press 3x5 · Power clean 5x3",
        movements = listOf(m("Back squat"), m("Overhead press"), m("Power clean"))),
    Workout(name = "Posterior Chain", type = WorkoutType.STRENGTH, photo = "rdl",
        notes = "4 sets each · 6-8 reps",
        movements = listOf(m("Deadlift"), m("Romanian deadlift"), m("Good morning"), m("Hip thrust"))),
    Workout(name = "1RM Test Day", type = WorkoutType.STRENGTH, photo = "deadlift",
        notes = "Work up to a heavy single on each",
        movements = listOf(m("Back squat"), m("Bench press"), m("Deadlift")))
).map { CatalogEntry(Category.STRENGTH, it) }

private fun olympic(): List<CatalogEntry> = listOf(
    Workout(name = "Snatch (heavy single)", type = WorkoutType.STRENGTH, photo = "ohsquat",
        notes = "Work up to a heavy single · then a few back-off sets",
        movements = listOf(m("Snatch"))),
    Workout(name = "Clean & Jerk (heavy single)", type = WorkoutType.STRENGTH, photo = "oly_jerk",
        notes = "Work up to a heavy single · then back-off sets",
        movements = listOf(m("Clean & jerk"))),
    Workout(name = "Power Snatch", type = WorkoutType.STRENGTH, photo = "ohsquat",
        notes = "6 sets x 2 reps · technical",
        movements = listOf(m("Power snatch"))),
    Workout(name = "Power Clean", type = WorkoutType.STRENGTH, photo = "oly_jerk",
        notes = "6 sets x 2 reps",
        movements = listOf(m("Power clean"))),
    Workout(name = "Hang Snatch", type = WorkoutType.STRENGTH, photo = "ohsquat",
        notes = "5 sets x 3 reps · from the hang",
        movements = listOf(m("Hang snatch"))),
    Workout(name = "Hang Clean", type = WorkoutType.STRENGTH, photo = "oly_jerk",
        notes = "5 sets x 3 reps",
        movements = listOf(m("Hang clean"))),
    Workout(name = "Snatch Balance", type = WorkoutType.STRENGTH, photo = "ohsquat",
        notes = "5 sets x 3 reps · speed under the bar",
        movements = listOf(m("Snatch balance"))),
    Workout(name = "Overhead Squat", type = WorkoutType.STRENGTH, photo = "ohsquat",
        notes = "5 sets x 3 reps",
        movements = listOf(m("Overhead squat"))),
    Workout(name = "Front Squat", type = WorkoutType.STRENGTH, photo = "frontsquat",
        notes = "5 sets x 3 reps",
        movements = listOf(m("Front squat"))),
    Workout(name = "Snatch Complex", type = WorkoutType.STRENGTH, photo = "ohsquat",
        notes = "Snatch pull + hang snatch + OHS · 5 rounds",
        movements = listOf(m("Snatch pull"), m("Hang snatch"), m("Overhead squat"))),
    Workout(name = "C&J Complex", type = WorkoutType.STRENGTH, photo = "oly_jerk",
        notes = "Clean + front squat + jerk · 5 rounds",
        movements = listOf(m("Clean"), m("Front squat"), m("Jerk"))),
    Workout(name = "Snatch EMOM", type = WorkoutType.EMOM, photo = "ohsquat",
        intervalSeconds = 60, rounds = 10, notes = "EMOM 10 · 1 snatch at ~75%",
        movements = listOf(m("Snatch", 1))),
    Workout(name = "Snatch 1RM", type = WorkoutType.STRENGTH, photo = "ohsquat",
        notes = "Work up to a 1-rep max",
        movements = listOf(m("Snatch"))),
    Workout(name = "Clean & Jerk 1RM", type = WorkoutType.STRENGTH, photo = "oly_jerk",
        notes = "Work up to a 1-rep max",
        movements = listOf(m("Clean & jerk")))
).map { CatalogEntry(Category.OLYMPIC, it) }

fun benchmarkCatalog(): List<CatalogEntry> =
    crossfit() + running() + hyrox() + strength() + olympic()

// Representative photo for each category tile in Browse.
fun categoryPhoto(category: String): String = when (category) {
    Category.CROSSFIT -> "boxjump"
    Category.RUNNING -> "runner"
    Category.HYROX -> "sled"
    Category.STRENGTH -> "deadlift"
    Category.OLYMPIC -> "oly_jerk"
    else -> "boxjump"
}
