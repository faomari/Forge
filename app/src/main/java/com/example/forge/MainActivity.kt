package com.example.forge

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

sealed interface Screen {
    data object Root : Screen
    data class Detail(val workoutId: String) : Screen
    data class Edit(val workout: Workout, val isNew: Boolean) : Screen
    data class Run(val workout: Workout) : Screen
    data class LogResult(val workout: Workout, val elapsedSeconds: Int) : Screen
    data class StrengthLog(val workout: Workout) : Screen
    data object Settings : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ForgeApp() }
    }
}

@Composable
fun ForgeApp() {
    MaterialTheme(colorScheme = ForgeColors) {
        val context = LocalContext.current
        val repo = remember { WorkoutRepository(context) }
        var workouts by remember { mutableStateOf(repo.load()) }
        var records by remember { mutableStateOf(repo.loadRecords()) }
        var backStack by remember { mutableStateOf<List<Screen>>(listOf(Screen.Root)) }

        val current = backStack.last()
        fun push(screen: Screen) { backStack = backStack + screen }
        fun pop() { if (backStack.size > 1) backStack = backStack.dropLast(1) }

        fun persist(list: List<Workout>) { workouts = list; repo.save(list) }
        fun upsert(workout: Workout) {
            val exists = workouts.any { it.id == workout.id }
            persist(
                if (exists) workouts.map { if (it.id == workout.id) workout else it }
                else workouts + workout
            )
        }
        fun persistRecords(list: List<PersonalRecord>) { records = list; repo.saveRecords(list) }

        val gson = remember { com.google.gson.Gson() }
        fun exportJson(): String = gson.toJson(BackupData(workouts, records))
        fun importJson(text: String): Boolean = try {
            val data = gson.fromJson(text, BackupData::class.java)
            if (data != null) {
                persist(data.workouts)
                persistRecords(data.records)
                true
            } else false
        } catch (e: Exception) {
            false
        }

        BackHandler(enabled = backStack.size > 1) { pop() }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (val currentScreen = current) {
                is Screen.Root -> RootScaffold(
                    workouts = workouts,
                    records = records,
                    onOpenWorkout = { push(Screen.Detail(it.id)) },
                    onNewWorkout = { push(Screen.Edit(Workout(), isNew = true)) },
                    onAddCatalog = { template ->
                        upsert(template.copy(id = java.util.UUID.randomUUID().toString()))
                    },
                    onSaveRecord = { rec ->
                        val exists = records.any { it.id == rec.id }
                        persistRecords(
                            if (exists) records.map { if (it.id == rec.id) rec else it }
                            else records + rec
                        )
                    },
                    onDeleteRecord = { id -> persistRecords(records.filter { it.id != id }) },
                    onOpenSettings = { push(Screen.Settings) }
                )

                is Screen.Settings -> SettingsScreen(
                    exportJson = { exportJson() },
                    importJson = { importJson(it) },
                    onBack = { pop() }
                )

                is Screen.Detail -> {
                    val workout = workouts.firstOrNull { it.id == currentScreen.workoutId }
                    if (workout == null) pop()
                    else DetailScreen(
                        workout = workout,
                        onBack = { pop() },
                        onStart = {
                            push(if (workout.type == WorkoutType.STRENGTH) Screen.StrengthLog(workout) else Screen.Run(workout))
                        },
                        onEdit = { push(Screen.Edit(workout, isNew = false)) },
                        onDelete = {
                            persist(workouts.filter { it.id != workout.id })
                            pop()
                        },
                        onDeleteResult = { resultId ->
                            upsert(workout.copy(results = workout.results.filter { it.id != resultId }))
                        },
                        onAddResult = {
                            push(if (workout.type == WorkoutType.STRENGTH) Screen.StrengthLog(workout) else Screen.LogResult(workout, 0))
                        }
                    )
                }

                is Screen.Edit -> EditScreen(
                    initial = currentScreen.workout,
                    isNew = currentScreen.isNew,
                    onCancel = { pop() },
                    onSave = { updated ->
                        upsert(updated)
                        pop()
                        if (currentScreen.isNew) push(Screen.Detail(updated.id))
                    }
                )

                is Screen.Run -> RunScreen(
                    workout = currentScreen.workout,
                    onCancel = { pop() },
                    onFinish = { elapsed ->
                        pop()
                        push(Screen.LogResult(currentScreen.workout, elapsed))
                    }
                )

                is Screen.LogResult -> LogResultScreen(
                    workout = currentScreen.workout,
                    elapsedSeconds = currentScreen.elapsedSeconds,
                    onCancel = { pop() },
                    onSave = { result, updatedMovements ->
                        val updated = currentScreen.workout.copy(
                            movements = updatedMovements ?: currentScreen.workout.movements,
                            results = currentScreen.workout.results + result
                        )
                        upsert(updated)
                        pop()
                    }
                )

                is Screen.StrengthLog -> StrengthLogScreen(
                    workout = currentScreen.workout,
                    onCancel = { pop() },
                    onSave = { result ->
                        upsert(currentScreen.workout.copy(results = currentScreen.workout.results + result))
                        pop()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootScaffold(
    workouts: List<Workout>,
    records: List<PersonalRecord>,
    onOpenWorkout: (Workout) -> Unit,
    onNewWorkout: () -> Unit,
    onAddCatalog: (Workout) -> Unit,
    onSaveRecord: (PersonalRecord) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    var tab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { HeaderBanner(tab = tab, onOpenSettings = onOpenSettings) },
        floatingActionButton = {
            if (tab == 0) {
                FloatingActionButton(
                    onClick = onNewWorkout,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 10.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New workout", modifier = Modifier.size(30.dp))
                }
            }
        },
        bottomBar = {
            Column {
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x14FFFFFF)))
                NavigationBar(
                    containerColor = Color(0xFF000000),
                    modifier = Modifier.height(84.dp)
                ) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NavigationBarItem(
                        selected = tab == 0, onClick = { tab = 0 }, colors = itemColors,
                        icon = { Icon(Icons.Filled.FitnessCenter, contentDescription = null, modifier = Modifier.size(26.dp)) },
                        label = { Text("Workouts") }
                    )
                    NavigationBarItem(
                        selected = tab == 1, onClick = { tab = 1 }, colors = itemColors,
                        icon = { Icon(Icons.Filled.Explore, contentDescription = null, modifier = Modifier.size(26.dp)) },
                        label = { Text("Browse") }
                    )
                    NavigationBarItem(
                        selected = tab == 2, onClick = { tab = 2 }, colors = itemColors,
                        icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(26.dp)) },
                        label = { Text("Records") }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> WorkoutsTab(workouts = workouts, onOpen = onOpenWorkout)
                1 -> BrowseScreen(onAdd = { onAddCatalog(it); tab = 0 })
                else -> RecordsScreen(records = records, onSave = onSaveRecord, onDelete = onDeleteRecord)
            }
        }
    }
}

@Composable
private fun HeaderBanner(tab: Int, onOpenSettings: () -> Unit) {
    val res = when (tab) {
        0 -> R.drawable.header_forge
        1 -> R.drawable.header_browse
        else -> R.drawable.header_records
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(96.dp)
            .background(Color(0xFF000000))
    ) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 4.dp)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
