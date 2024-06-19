package com.example.aveiroplus

import android.content.ContentProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.aveiroplus.components.Event
import com.example.aveiroplus.components.MapMarker
import com.example.aveiroplus.components.UserProfile
import com.example.aveiroplus.uiStates.MapUiState
import com.example.aveiroplus.viewModels.MapViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PinConfig
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.runBlocking
import java.net.URL





val usr = UserProfile()
val mapViewModel = MapViewModel()

@Composable
fun MapScreen(navController: NavController) {

    val mapUiState by mapViewModel.uiState.collectAsState()

    val cameraPositionState = CameraPositionState()



    LaunchedEffect(Unit) {
        runBlocking() {
            mapViewModel.getMapMarkers()
            mapViewModel.getYourMarker()
            mapViewModel.getYourEvents()
        }
        cameraPositionState.position =  CameraPosition.fromLatLngZoom(LatLng(mapUiState.yourMarker.lat, mapUiState.yourMarker.long), 3f)
    }


    Box(

    )
    {
        if (mapUiState.isMapReady){
            GoogleMap(
                cameraPositionState = cameraPositionState,
                onMapClick = { mapViewModel.changeUserVisibility(mapUiState.isInfoVisible) }
            ) {

                for (marker in mapUiState.markers) {
                    CreateMarker(markerData = marker, uiState = mapUiState, photoString = marker.relatedUser.profileImageUrl)
                }
                
                for (eventMarker in mapUiState.yourEvents) {
                    CreateEventMarker(event = eventMarker, uiState = mapUiState, navController = navController)
                }

            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                if (mapUiState.isInfoVisible) {
                    ShowUserInfo(mapUiState.userToShow, mapUiState, navController)
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center))
        }

    }
}

@Composable
fun CreateEventMarker(event: Event, uiState: MapUiState, navController: NavController) {
    return Marker(
        state = MarkerState(LatLng(event.lat, event.long)),
        onClick = {
            navController.navigate("event_detail/${event.eventId}")
            return@Marker false
        },
        icon = BitmapDescriptorFactory.defaultMarker(
            BitmapDescriptorFactory.HUE_BLUE
        )

    )
}
@Composable
fun CreateMarker(markerData: MapMarker, uiState: MapUiState, photoString: String) {

        return Marker(
            state = MarkerState(LatLng(markerData.lat, markerData.long)),
            onClick = {
                mapViewModel.assignUserToView(markerData.relatedUser)
                mapViewModel.changeUserVisibility(uiState.isInfoVisible)
                runBlocking {
                    mapViewModel.getUserEvents(markerData.relatedUser.registeredEventsIds)
                }
                return@Marker false
            },
            icon = BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_RED
            )
        )
}

@Composable
fun ShowUserInfo(usr: UserProfile, uiState: MapUiState, navController: NavController) {
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
        Box(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            contentAlignment = Alignment.TopStart,
        ){
            Image(
                painter = xPainter,
                contentDescription = "close tab",
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .clickable {
                        mapViewModel.changeUserVisibility(uiState.isInfoVisible)
                    },
                contentScale = ContentScale.Crop,

            )
        }
        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(150.dp)
                .padding(4.dp)
                .clip(CircleShape), // Clip to a circle shape
            contentScale = ContentScale.Crop
        )
        Text(
        text = usr.name + " " + usr.surname,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(4.dp),
        color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(30.dp))
        Text(
            text = "user events:",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(4.dp),
            color = MaterialTheme.colorScheme.secondary
        )
        Surface(
           // modifier = Modifier.fillMaxSize()
        ) {
            val scrollState = rememberScrollState()
            ScrollableColumn(scrollState = scrollState, uiState, navController = navController)
        }

    }
}

@Composable
fun ScrollableColumn(scrollState: ScrollState, uiState: MapUiState, navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        for(event in uiState.eventsToShow) {
            Surface(
                modifier = Modifier.padding(16.dp)
                    .clickable { navController.navigate("event_detail/${event.eventId}") },
                shape = RoundedCornerShape(12.dp),
                color = Color.LightGray,
            ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                    Image(
                        painter = rememberAsyncImagePainter(event.imageUrl),
                        contentDescription = "Event Image",
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .size(140.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = event.eventName,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
            }
        }
    }
}
