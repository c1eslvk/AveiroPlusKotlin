package com.example.aveiroplus

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.aveiroplus.components.UserProfile

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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_person_2_24), // Use drawable resource
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
    }
}