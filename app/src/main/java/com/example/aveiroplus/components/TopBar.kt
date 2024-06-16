package com.example.aveiroplus.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.aveiroplus.R

@Composable
fun TopBar() {
    val isDarkTheme = isSystemInDarkTheme()
    val logoResource = if (isDarkTheme) R.drawable.logodark else R.drawable.logo

    Surface (
        color = MaterialTheme.colorScheme.primary
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .statusBarsPadding()
        ) {
            Image(
                painter = painterResource(id = logoResource),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(48.dp)
                    .width(169.dp)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}
