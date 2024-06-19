package com.example.aveiroplus

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.aveiroplus.components.Event
import com.example.aveiroplus.components.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun EventDetailAdminScreen(navController: NavController, eventId: String) {
    var event by remember { mutableStateOf<Event?>(null) }
    var user by remember { mutableStateOf<UserProfile?>(null) }
    var registeredUsers by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var debugMessage by remember { mutableStateOf<String?>(null) }
    var isRegistered by remember { mutableStateOf(false) }
    var isImageDialogOpen by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(eventId) {
        try {
            debugMessage = "Fetching event with ID: $eventId"

            // Fetch event details
            val eventSnapshot = db.collection("events").document(eventId).get().await()
            if (eventSnapshot.exists()) {
                event = eventSnapshot.toObject(Event::class.java)
                debugMessage = "Event loaded: $event"
            } else {
                errorMessage = "Event not found"
                debugMessage = "Event not found with ID: $eventId"
                return@LaunchedEffect
            }

            // Fetch user details
            val userSnapshot = db.collection("users").document(currentUser?.uid ?: "").get().await()
            if (userSnapshot.exists()) {
                user = userSnapshot.toObject(UserProfile::class.java)
                debugMessage += "\nUser loaded: $user"
            } else {
                errorMessage = "User not found"
                debugMessage += "\nUser not found with UID: ${currentUser?.uid}"
                return@LaunchedEffect
            }

            // Check if user is registered for the event
            isRegistered = user?.registeredEventsIds?.contains(eventId) ?: false
            debugMessage += "\nUser is registered: $isRegistered"

            // Fetch profiles of registered users
            val registeredUsersIds = event?.registeredUsersIds ?: emptyList()
            val users = registeredUsersIds.mapNotNull { userId ->
                val registeredUserSnapshot = db.collection("users").document(userId).get().await()
                if (registeredUserSnapshot.exists()) {
                    registeredUserSnapshot.toObject(UserProfile::class.java)
                } else {
                    null
                }
            }
            registeredUsers = users
        } catch (e: Exception) {
            errorMessage = "Failed to load data: ${e.message}"
            debugMessage = "Failed to load data: ${e.message}"
        }
    }

    // Function to toggle paid status
    fun togglePaid(userId: String, isPaid: Boolean) {
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val user = snapshot.toObject(UserProfile::class.java)
            if (user != null) {
                val updatedPaidEventsIds = if (isPaid) {
                    user.paidEventsIds + eventId
                } else {
                    user.paidEventsIds - eventId
                }
                transaction.update(userRef, "paidEventsIds", updatedPaidEventsIds)
            }
        }.addOnSuccessListener {
            // Handle success (optional)
        }.addOnFailureListener { e ->
            // Handle failure (optional)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else if (event == null) {
            CircularProgressIndicator()
        } else {
            // Event details UI
            Image(
                painter = rememberAsyncImagePainter(event?.imageUrl),
                contentDescription = event?.eventName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { isImageDialogOpen = true },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = event?.eventName ?: "",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Available places: ${event?.availablePlaces}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Price: ${event?.price} EUR",  // Displaying price here
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event?.description ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Go Back")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Registered users list
            Text(
                text = "Registered Users:",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn {
                items(registeredUsers) { userProfile ->
                    UserProfileRow(userProfile, eventId, ::togglePaid)
                }
            }
        }
    }

    if (isImageDialogOpen) {
        Dialog(onDismissRequest = { isImageDialogOpen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(event?.imageUrl),
                    contentDescription = event?.eventName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { isImageDialogOpen = false },
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { isImageDialogOpen = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "X",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileRow(userProfile: UserProfile, eventId: String, onTogglePaid: (String, Boolean) -> Unit) {
    var isPaid by remember { mutableStateOf(userProfile.paidEventsIds.contains(eventId)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(userProfile.profileImageUrl),
            contentDescription = userProfile.name,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "${userProfile.name} ${userProfile.surname}", style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = {
                isPaid = !isPaid
                onTogglePaid(userProfile.uid, isPaid)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(100.dp, 36.dp)
        ) {
            Text(if (isPaid) "Paid" else "UnPaid")
        }
    }
}
