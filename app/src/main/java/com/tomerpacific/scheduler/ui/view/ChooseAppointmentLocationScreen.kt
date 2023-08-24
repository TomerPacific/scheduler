package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun ChooseAppointmentLocationScreen(viewModel: MainViewModel) {
    val currentLocation = viewModel.currentLocation.observeAsState()
    if (currentLocation.value == null) {
        viewModel.updateLocation()
    } else {
        DrawMap(currentLocation = currentLocation)
    }

}


@Composable
fun DrawMap(currentLocation: State<LatLng?>) {
    val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(
                currentLocation.value!!.latitude,
                currentLocation.value!!.longitude),
                16f)
        }
    Column() {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier.weight(1.0f),
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(compassEnabled = true)
        ) {

        }
    }
}
