package com.example.aveiroplus

import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.aveiroplus.components.Event
import com.example.aveiroplus.components.MapMarker
import com.example.aveiroplus.components.UserProfile
import com.example.aveiroplus.uiStates.MapUiState
import com.example.aveiroplus.viewModels.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PinConfig
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.runBlocking

val usr = UserProfile()
val mapViewModel = MapViewModel()

@Composable
fun MapScreen() {

    val mapUiState by mapViewModel.uiState.collectAsState()

    val cameraPositionState = rememberCameraPositionState()
    var showBottomSheet by remember { mutableStateOf(false) }



    LaunchedEffect(Unit) {
        runBlocking() {
            mapViewModel.getMapMarkers()
            mapViewModel.getYourMarker()
        }
        cameraPositionState.position =  CameraPosition.fromLatLngZoom(LatLng(mapUiState.yourMarker.lat, mapUiState.yourMarker.long), 10f)
    }


    Surface(

    )
    {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            onMapClick = { mapViewModel.changeUserVisibility(mapUiState.isInfoVisible)}
        ) {

            for (marker in mapUiState.markers) {
                CreateMarker(markerData = marker, uiState = mapUiState)
            }


        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (mapUiState.isInfoVisible) {
                ShowUserInfo(mapUiState.userToShow, mapUiState)
            }
        }

    }
}

@Composable
fun CreateMarker(markerData: MapMarker, uiState: MapUiState) {


    return AdvancedMarker(state = MarkerState(LatLng(markerData.lat, markerData.long)),
        onClick = {
            mapViewModel.assignUserToView(markerData.relatedUser)
            mapViewModel.changeUserVisibility(uiState.isInfoVisible)
            return@AdvancedMarker false
        })
}

@Composable
fun ShowUserInfo(usr: UserProfile, uiState: MapUiState) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = usr.profileImageUrl.takeIf { it.isNotEmpty() } ?: R.drawable.blank_profile)
            .apply {
                crossfade(true)
                placeholder(R.drawable.blank_profile)
            }.build()
    )

    val xPainter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = R.drawable.close_tab)
            .apply {
                crossfade(true)
                placeholder(R.drawable.blank_profile)
            }.build()
    )
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(400.dp)
        .background(Color.White)
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopStart,
        ){
            Image(
                painter = xPainter,
                contentDescription = "close tab",
                modifier = Modifier
                    .size(50.dp)
                    .padding(16.dp)
                    .clip(CircleShape).
                    clickable {
                      mapViewModel.changeUserVisibility(uiState.isInfoVisible)
                    },
                contentScale = ContentScale.Crop,

            )
        }
        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .padding(4.dp)
                .clip(CircleShape), // Clip to a circle shape
            contentScale = ContentScale.Crop
        )
        Text(text = usr.name + " " + usr.surname)

    }
}
