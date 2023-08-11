package com.tomerpacific.scheduler.ui.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationPermissionScreen() {

    var isLocationPermissionGranted: Boolean by remember {
        mutableStateOf(false)
    }

    var isLoading: Boolean by remember {
        mutableStateOf(true)
    }

    val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val context = LocalContext.current

    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        isLocationPermissionGranted = areGranted
        isLoading = false
    }

    if (
        permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    ) {
        isLocationPermissionGranted = true
        isLoading = false
    } else {
        SideEffect {
            launcherMultiplePermissions.launch(permissions)
        }
    }

    DrawMap(isLocationPermissionGranted, isLoading)
}

@Composable
fun DrawMap(isLocationPermissionGranted: Boolean, isLoading: Boolean) {

    if (!isLocationPermissionGranted && !isLoading) {
        Text("No Location Permission",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            textAlign = TextAlign.Center)
    } else if (isLocationPermissionGranted && !isLoading){
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(44.810058, 20.4617586), 16f)
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