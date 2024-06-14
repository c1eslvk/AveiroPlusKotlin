
package com.example.aveiroplus

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.aveiroplus.components.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

private suspend fun loadUserProfile(userProfile: MutableState<UserProfile>, firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore, context: Context) {
    val currentUser = firebaseAuth.currentUser
    currentUser?.let {
        try {
            val userId = it.uid
            val documentSnapshot = firestore.collection("users").document(userId).get().await()
            val name = documentSnapshot.getString("name") ?: "N/A"
            val surname = documentSnapshot.getString("surname") ?: "N/A"
            val email = documentSnapshot.getString("email") ?: "N/A"
            val profileImageUrl = documentSnapshot.getString("profileImageUrl") ?: ""

            val userProfileData = UserProfile(
                name = name,
                surname = surname,
                email = email,
                profileImageUrl = profileImageUrl
            )
            userProfile.value = userProfileData
        } catch (e: Exception) {
            // Handle the error
        }
    }
}

@Composable
fun ProfileContent(navController: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imageSelectionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        selectedImageUri?.let { uri ->
            uploadProfileImage(uri, firebaseAuth, firestore, context)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        val userProfile = remember { mutableStateOf(UserProfile()) }
        LaunchedEffect(Unit) {
            loadUserProfile(userProfile, firebaseAuth, firestore, context)
        }

        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(data = selectedImageUri ?: userProfile.value.profileImageUrl.takeIf { it.isNotEmpty() } ?: R.drawable.blank_profile)
                .apply {
                    crossfade(true)
                    placeholder(R.drawable.blank_profile)
                }.build()
        )


        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(150.dp)
                .padding(16.dp)
                .clickable {
                    imageSelectionLauncher.launch("image/*")
                }
                .clip(CircleShape), // Clip to a circle shape
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "${userProfile.value.name} ${userProfile.value.surname}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = userProfile.value.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("your_events") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Your Events")
        }

        Button(
            onClick = {
                firebaseAuth.signOut()
                val intent = Intent(context, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Logout")
        }
    }
}


private fun uploadProfileImage(uri: Uri, firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore, context: Context) {
    val currentUser = firebaseAuth.currentUser
    currentUser?.let { user ->
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profile_images/${user.uid}")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    updateUserProfileImageUrl(user.uid, downloadUri.toString(), firestore, context)
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors
                Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

private fun updateUserProfileImageUrl(userId: String, imageUrl: String, firestore: FirebaseFirestore, context: Context) {
    val userRef = firestore.collection("users").document(userId)
    userRef.update("profileImageUrl", imageUrl)
        .addOnSuccessListener {
            Toast.makeText(context, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            // Handle any errors
            Toast.makeText(context, "Failed to update profile image URL: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}