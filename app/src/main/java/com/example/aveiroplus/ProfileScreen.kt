package com.example.aveiroplus

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.aveiroplus.components.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen() {
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    val userProfile = remember { mutableStateOf(UserProfile()) }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            try {
                val userId = it.uid
                val documentSnapshot = firestore.collection("users").document(userId).get().await()
                val name = documentSnapshot.getString("name") ?: "N/A"
                val surname = documentSnapshot.getString("surname") ?: "N/A"
                val email = documentSnapshot.getString("email") ?: "N/A"

                userProfile.value = UserProfile(
                    name = name,
                    surname = surname,
                    email = email
                )
            } catch (e: Exception) {
                // Handle the error (e.g., show a message to the user)
            }
        }
    }

    ProfileContent(userProfile = userProfile.value)
}

@Composable
fun ProfileContent(userProfile: UserProfile) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // The image handling code remains as is
        val painter = painterResource(id = R.drawable.ic_android_black_24dp) // Fallback drawable resource

        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(128.dp)
                .padding(8.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${userProfile.name} ${userProfile.surname}",
            style = MaterialTheme.typography.titleLarge // Use appropriate typography style
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = userProfile.email,
            style = MaterialTheme.typography.bodyLarge // Use appropriate typography style
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                firebaseAuth.signOut()
                val intent = Intent(context, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Logout")
        }
    }
}
