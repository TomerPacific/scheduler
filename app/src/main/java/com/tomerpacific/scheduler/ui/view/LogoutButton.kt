package com.tomerpacific.scheduler.ui.view

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun LogoutButton(shouldBeDisplayed: Boolean, onClickHandler: () -> Unit) {
    if (shouldBeDisplayed) {
        TextButton(onClick = {
            onClickHandler()
        }) {
            Text("Logout")
        }
    }

}