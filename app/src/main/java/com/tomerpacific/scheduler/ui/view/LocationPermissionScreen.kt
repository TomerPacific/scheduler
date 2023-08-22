package com.tomerpacific.scheduler.ui.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.tomerpacific.scheduler.ui.model.MainViewModel

@SuppressLint("MissingPermission", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun LocationPermissionScreen(viewModel: MainViewModel) {
    val snackbarHostState = SnackbarHostState()
    val scaffoldState: ScaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState)
    val areLocationPermissionsGranted = viewModel.areLocationPermissionsGranted.observeAsState()
    val currentLocation = viewModel.currentLocation.observeAsState()
    val isWaitingForLocationPermissions = remember {
        mutableStateOf(!areLocationPermissionsGranted.value!!)
    }

    Scaffold(
        scaffoldState = scaffoldState,
    ) {
        if (!isWaitingForLocationPermissions.value) {
            DrawMap(currentLocation = currentLocation)
        } else {
            CircularProgressBarIndicator(shouldBeDisplayed = isWaitingForLocationPermissions.value)
            LocationPermissionHandler(viewModel, scaffoldState, areLocationPermissionsGranted)
        }
    }
   
}

@Composable
fun LocationPermissionHandler(viewModel: MainViewModel,
                              scaffoldState: ScaffoldState,
                              areLocationPermissionsGranted: State<Boolean?>) {

    val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap.isNotEmpty()) {
            val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
            viewModel.setAreLocationPermissionsGranted(areGranted)
        }
    }

    val activity = LocalContext.current as Activity

    val shouldShowLocationPermissionRationale =
        ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    if (shouldShowLocationPermissionRationale) {
        ShowLocationPermissionRationale(scaffoldState)
    } else {
        LaunchedEffect(true) {
            launcherMultiplePermissions.launch(permissions)
        }
    }
}

@Composable
fun ShowLocationPermissionRationale(scaffoldState: ScaffoldState) {

    LaunchedEffect(true) {
            val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                message = "In order to choose an appointment place, you need to authorize location permissions",
                actionLabel = "Grant Access",
                duration = SnackbarDuration.Long
            )
            when (snackbarResult) {
                SnackbarResult.Dismissed -> {

//                    Text("No Location Permission",
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 25.sp,
//                            textAlign = TextAlign.Center)
                }

                SnackbarResult.ActionPerformed -> {

                }
            }
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
