package com.example.aveiroplus

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.aveiroplus.components.Activity

@Composable
fun AdminScreen() {
    // Hardcoded list of activities
    val activities = listOf(
        Activity(photoResId = R.drawable.ic_map, description = "Activity 1 description")
    )

    AdminContent(activities = activities)
}

@Composable
fun AdminContent(activities: List<Activity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(activities.size) { index ->
            ActivityItem(activity = activities[index])
        }
    }
}