package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun LoginTabView(viewModel: MainViewModel) {
    var tabIndex by remember { mutableStateOf(0) }

    val tabs = listOf("Login", "Sign up")

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        when (tabIndex) {
            0 -> LoginScreen(viewModel)
            1 -> SignupScreen(viewModel)
        }
    }

}


