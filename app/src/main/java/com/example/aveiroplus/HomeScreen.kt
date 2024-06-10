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
import com.example.aveiroplus.components.Event

@Composable
fun HomeScreen() {
    // Hardcoded list of activities
    val events = listOf<Event>()

    HomeContent(events = events)
}

@Composable
fun HomeContent(events: List<Event>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(events.size) { index ->
            ActivityItem(event = events[index])
        }
    }
}

@Composable
fun ActivityItem(event: Event) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
//        Image(
//            painter = painterResource(id = event.photoResId),
//            contentDescription = "Activity Photo",
//            modifier = Modifier
//                .height(200.dp)
//                .fillMaxWidth()
//                .padding(8.dp),
//            contentScale = ContentScale.Crop
//        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = event.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* No implementation for now */ }) {
            Text(text = "Register")
        }
    }
}
