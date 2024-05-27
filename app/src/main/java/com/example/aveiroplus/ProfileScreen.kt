package com.example.aveiroplus

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.aveiroplus.components.UserProfile
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen() {
    // Hardcoded user profile data
    val userProfile = UserProfile(
        name = "John Doe",
        email = "john.doe@example.com",
        profilePictureUrl = "" // Replace with a valid image URL
    )

    ProfileContent(userProfile = userProfile)
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
        Image(
            painter = painterResource(id = R.drawable.ic_android_black_24dp), // Use drawable resource
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(128.dp)
                .padding(8.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userProfile.name,
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
