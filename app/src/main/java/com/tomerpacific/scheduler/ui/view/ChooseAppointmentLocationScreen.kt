package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.tomerpacific.scheduler.ui.model.MapSearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ChooseAppointmentLocationScreen(viewModel: MainViewModel,
                                    onUserChoseCurrentLocation: () -> Unit) {
    
    val currentLocation = viewModel.currentLocation.observeAsState()
    val locationAutofill = viewModel.locationAutofill.observeAsState()
    val locationText = viewModel.locationText.observeAsState()

    if (currentLocation.value == null) {
        ShowCurrentLocationDialog(viewModel, onUserChoseCurrentLocation)
    } else {
        DrawMap(viewModel, currentLocation, locationAutofill, locationText)
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
                    onAlertDialogButtonClicked(
                        viewModel,
                        onUserChoseCurrentLocation,
                        shouldDialogBeDismissed,
                        false)
                }
                ) {
                    Text(text = "Another Location")
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAlertDialogButtonClicked(
                        viewModel,
                        onUserChoseCurrentLocation,
                        shouldDialogBeDismissed,
                        true)
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
fun DrawMap(viewModel: MainViewModel,
            currentLocation: State<LatLng?>,
            locationAutofill: State<MutableList<MapSearchResult>?>,
            locationText: State<String?>) {

    val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(
                currentLocation.value!!.latitude,
                currentLocation.value!!.longitude),
                16f)
    }

    LaunchedEffect(currentLocation.value) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(currentLocation.value!!))
    }

    Box(modifier =  Modifier.fillMaxSize()) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(compassEnabled = true),
            onMapClick = {
                viewModel.updateLocation(it)
                CoroutineScope(Dispatchers.Main).launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLng(it))
                }
            }
        )
        Surface(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(8.dp)
            .fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column {
                if (locationAutofill.value != null) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(locationAutofill.value!!) {
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable {
                                    viewModel.handleLocationResultItemClicked(it)
                                }
                            ) {
                                Text(it.address)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(IntrinsicSize.Max)) {
                    TextField(
                        value = if (locationText.value != null) locationText.value!! else "",
                        onValueChange = { input: String ->
                            viewModel.handleLocationSearchTyping(input)
                        },
                        placeholder = {
                            Text("Where should the appointment be held?")
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.Black,
                            fontSize = 15.sp
                        ),
                        singleLine = true,
                    )
                    Button(
                        modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                        onClick = {

                        }) {
                        Icon(Icons.Default.AddLocation, "Add location")
                    }
                }

            }
        }
    }
}

private fun onAlertDialogButtonClicked(viewModel: MainViewModel,
                                       onUserChoseCurrentLocation: () -> Unit,
                                       shouldDialogBeDismissed: MutableState<Boolean>,
                                       userChoseCurrentLocation:Boolean) {
    viewModel.setCurrentUserLocation(userChoseCurrentLocation)
    shouldDialogBeDismissed.value = true

    if (userChoseCurrentLocation) {
        onUserChoseCurrentLocation()
    }
}
