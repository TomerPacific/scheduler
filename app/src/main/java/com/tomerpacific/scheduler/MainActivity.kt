package com.tomerpacific.scheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.tomerpacific.scheduler.ui.model.MainViewModel
import com.tomerpacific.scheduler.ui.theme.SchedulerTheme
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tomerpacific.scheduler.ui.view.AddAppointmentScreen
import com.tomerpacific.scheduler.ui.view.AppointmentSetScreen
import com.tomerpacific.scheduler.ui.view.AppointmentsScreen
import com.tomerpacific.scheduler.ui.view.LoginScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination: String = viewModel.getStartDestination()

        setContent {
            SchedulerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_black),
                            contentDescription = "Logo",
                            contentScale = ContentScale.Inside,
                            modifier = Modifier.matchParentSize())
                        NavGraph(startDestination = startDestination)
                    }
                }
            }
        }
    }

    @Composable
    fun NavGraph(
        navController: NavHostController = rememberNavController(),
        startDestination: String = "login"
    ) {

        NavHost(
            navController = navController,
            startDestination = startDestination) {
            composable("login") {
                LoginScreen(viewModel = viewModel, onNavigateAfterLoginScreen = {
                    navController.navigate("appointments")
                })
            }
            composable("appointments") {
                AppointmentsScreen(viewModel, onUserLogout = {
                    navController.navigate("login")
                }, onAddAppointmentClicked = {
                    navController.navigate("add-appointment")
                })
            }
            composable("add-appointment") {
                AddAppointmentScreen(viewModel = viewModel, onAppointmentScheduled = { error ->
                    viewModel.updateScheduledAppointmentsForUser()
                    navController.navigate("appointment-set/${error}")
                })
            }
            composable(
                "appointment-set/{errorMsg}",
                arguments = listOf(navArgument("errorMsg") { type = NavType.StringType })) { backStackEntry ->
                AppointmentSetScreen(viewModel = viewModel,
                    backStackEntry.arguments?.getString("errorMsg"), onBackToAppointmentScreenPressed = {
                        navController.navigate("appointments")
                    })
            }
        }
    }
}
