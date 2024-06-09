package com.example.aveiroplus

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.aveiroplus.components.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ProfileActivity : ComponentActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val imageSelectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                handleSelectedImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()

            val userProfile = remember { mutableStateOf(UserProfile()) }
            LaunchedEffect(Unit) {
                loadUserProfile(userProfile)
            }
            ProfileContent(userProfile = userProfile.value, launchImageSelection = { launchImageSelection() }, firebaseAuth = firebaseAuth)
        }
    }

    private suspend fun loadUserProfile(userProfile: MutableState<UserProfile>) {
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

    private fun handleSelectedImage(selectedImageUri: Uri) {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("profile_images/${user.uid}")

            imageRef.putFile(selectedImageUri)
                .addOnSuccessListener { _ ->
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateUserProfileImageUrl(user.uid, downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                    Toast.makeText(applicationContext, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserProfileImageUrl(userId: String, imageUrl: String) {
        val userRef = firestore.collection("users").document(userId)
        userRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle any errors
                Toast.makeText(applicationContext, "Failed to update profile image URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun launchImageSelection() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imageSelectionLauncher.launch(intent)
    }
}

@Composable
fun ProfileContent(userProfile: UserProfile, launchImageSelection: () -> Unit, firebaseAuth: FirebaseAuth) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val painter = if (userProfile.profileImageUrl.isNotEmpty()) {
            rememberImagePainter(
                data = userProfile.profileImageUrl,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.blank_profile)
                }
            )
        } else {
            painterResource(id = R.drawable.blank_profile)
        }

        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(128.dp)
                .padding(8.dp)
                .clickable {
                    launchImageSelection()
                },
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${userProfile.name} ${userProfile.surname}",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = userProfile.email,
            style = MaterialTheme.typography.bodyLarge
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