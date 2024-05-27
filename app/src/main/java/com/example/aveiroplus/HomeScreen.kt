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
fun HomeScreen() {
    // Hardcoded list of activities
    val activities = listOf(
        Activity(photoResId = R.drawable.activity1, description = "Activity 1 description"),
        Activity(photoResId = R.drawable.activity2, description = "Activity 2 description"),
        Activity(photoResId = R.drawable.activity3, description = "Activity 3 description")
    )

    HomeContent(activities = activities)
}

@Composable
fun HomeContent(activities: List<Activity>) {
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

@Composable
fun ActivityItem(activity: Activity) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = activity.photoResId),
            contentDescription = "Activity Photo",
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .padding(8.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = activity.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* No implementation for now */ }) {
            Text(text = "Register")
        }
    }
}
