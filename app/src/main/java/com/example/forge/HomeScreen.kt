package com.example.forge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkoutsTab(
    workouts: List<Workout>,
    onOpen: (Workout) -> Unit
) {
    if (workouts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No workouts yet.\nTap the + button, or add one from Browse.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(workouts, key = { it.id }) { workout ->
                WorkoutCard(workout = workout, onClick = { onOpen(workout) })
            }
        }
    }
}

@Composable
private fun WorkoutCard(workout: Workout, onClick: () -> Unit) {
    PhotoBox(
        photoKey = displayPhoto(workout),
        corner = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp)
            .clickable { onClick() }
    ) {
        // Type badge, top-right
        Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                TypeBadge(workout.type)
            }
        }
        // Title + movements, left, vertically centred
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.68f)
                .padding(start = 20.dp, end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = workout.name.ifBlank { "Untitled" }.uppercase(),
                color = Color.White,
                fontFamily = ForgeDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 46.sp,
                lineHeight = 46.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 12.dp)
                    .width(44.dp)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            workout.movements.take(3).forEach { mv ->
                MovementRow(movementIcon(mv.name), movementLine(workout, mv))
            }
        }
    }
}
