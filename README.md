# Forge

A gym app for programming and running workouts — CrossFit-style conditioning
and traditional strength — with photo backgrounds and per-type history.

Built with Kotlin + Jetpack Compose. Compiles to an installable APK via
GitHub Actions; no Android Studio needed.

## The five workout types
- **Strength** — no timer. Open it to see your dated weight x reps history.
  Start opens a logging screen with each set pre-filled from last time; your
  heaviest set is marked as the PR.
- **AMRAP** — counts down from the time cap, stops and beeps at zero, then
  asks for your score (rounds + extra reps). History tracks your scores.
- **EMOM** — beeps at the top of every interval, shows the round you're on.
  At the end you mark done / not done with an optional note.
- **Tabata** — flips between WORK and REST with a colour change and beep on
  each switch; configurable work/rest/rounds. Mark done at the end.
- **For Time** — counts up, records your finish time and the weight used,
  with the "go heavier?" progression nudge.

Each type has its own coloured badge, program screen, and history layout.
Every workout can use one of your photos as its card and detail-header
background (pick it in the edit screen); no photo falls back to a gradient.

Sample workouts of every type are seeded on first launch. Delete any with the
trash icon on the detail screen.

## Update your existing repo (fastest)
You already have the repo on GitHub with the build set up. To update it:
1. Open your repo -> Add file -> Upload files.
2. From this unzipped folder, drag in the `app` folder (that's all that
   changed — new code plus your photos). Leave `.github` alone.
3. Commit. The Build APK workflow reruns automatically; grab the new APK from
   the run's Artifacts in a few minutes.

## Your photos
Your four images are bundled in app/src/main/res/drawable/ as optimised WebP.
To add more later, drop a .webp/.jpg in that folder and add it to the list in
Theme.kt (photoCatalog).

## Play Store later
Debug build for personal use. Publishing needs only a signing key (as a GitHub
secret) and switching the workflow to bundleRelease — no code changes.

## Versions
Gradle 8.7, AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06.00,
compileSdk 34, minSdk 24, Gson 2.11.0, coroutines 1.8.1.
