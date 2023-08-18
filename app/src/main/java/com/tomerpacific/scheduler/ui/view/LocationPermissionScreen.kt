package com.tomerpacific.scheduler.ui.view

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.tomerpacific.scheduler.ui.model.MainViewModel

@SuppressLint("MissingPermission")
@Composable
fun LocationPermissionScreen(viewModel: MainViewModel) {


    var isLocationPermissionGranted: Boolean by remember {
        mutableStateOf(viewModel.isLocationPermissionGranted())
    }

    var isLoading: Boolean by remember {
        mutableStateOf(true)
    }

    val currentLocation = viewModel.currentLocation.observeAsState()

    val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap.isNotEmpty()) {
            val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
            isLocationPermissionGranted = areGranted
        }
    }

    if (isLocationPermissionGranted) {
        isLoading = false
        viewModel.updateLocation()
    } else {
        LaunchedEffect(isLocationPermissionGranted) {
            launcherMultiplePermissions.launch(permissions)
        }

    }

    DrawMap(isLocationPermissionGranted, isLoading, currentLocation)
}

@Composable
fun DrawMap(isLocationPermissionGranted: Boolean,
            isLoading: Boolean, currentLocation: State<LatLng?>
) {

    if (!isLocationPermissionGranted && !isLoading) {
        Text("No Location Permission",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            textAlign = TextAlign.Center)
    } else if (isLocationPermissionGranted && !isLoading && currentLocation.value != null){
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
            ) {}
        }
    } else {
        CircularProgressBarIndicator(shouldBeDisplayed = isLoading)
    }

}