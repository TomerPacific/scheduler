package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.tomerpacific.scheduler.ui.model.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ChooseAppointmentLocationScreen(viewModel: MainViewModel,
                                    onUserChoseCurrentLocation: () -> Unit) {
    
    val currentLocation = viewModel.currentLocation.observeAsState()

    if (currentLocation.value == null) {
        ShowCurrentLocationDialog(viewModel = viewModel, onUserChoseCurrentLocation)
    } else {
        DrawMap(currentLocation = currentLocation)
    }

}


@Composable
fun ShowCurrentLocationDialog(viewModel: MainViewModel, onUserChoseCurrentLocation: () -> Unit) {

    val shouldDialogBeDismissed = remember {
        mutableStateOf(false)
    }

    if (!shouldDialogBeDismissed.value) {

        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {
                shouldDialogBeDismissed.value = true
            },
            dismissButton = {
                Button(onClick = { 
                    shouldDialogBeDismissed.value = true
                    viewModel.updateLocation()
                }
                ) {
                    Text(text = "Another Location")
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateLocation()
                    shouldDialogBeDismissed.value = true
                    onUserChoseCurrentLocation()
                }) {
                    Text("Confirm")
                }
            },
            title = {
                Text(text = "Use Current Location?")
            },
            text = {
                Text(text = "Would you like to choose your current location as the appointment place?")
            })
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

    val locationName = remember {
        mutableStateOf("")
    }

    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(all = 3.dp),
            horizontalArrangement = Arrangement.Center) {
            TextField(
                value = locationName.value,
                onValueChange = { locationName.value = it },
                placeholder = { Text(text = "Enter your location to search") },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                ),
                singleLine = true,
            )
            Button(
                onClick = {

                }) {
                Icon(Icons.Filled.Search , contentDescription = "Magnifying glass")
            }
        }
        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier.weight(1.0f),
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(compassEnabled = true),
            onMapClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLng(it))
                }
            }
        ) {

        }
    }
}
