package com.tomerpacific.scheduler.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tomerpacific.scheduler.APPOINTMENT_ACTION_KET
import com.tomerpacific.scheduler.ERROR_MESSAGE_KEY
import com.tomerpacific.scheduler.NAVIGATION_DESTINATION_ADD_APPOINTMENT
import com.tomerpacific.scheduler.NAVIGATION_DESTINATION_APPOINTMENTS
import com.tomerpacific.scheduler.NAVIGATION_DESTINATION_LOGIN
import com.tomerpacific.scheduler.NAVIGATION_DESTINATION_SPLASH
import com.tomerpacific.scheduler.R
import com.tomerpacific.scheduler.ui.model.AppointmentModel
import com.tomerpacific.scheduler.ui.model.MainViewModel
import com.tomerpacific.scheduler.ui.view.AddAppointmentScreen
import com.tomerpacific.scheduler.ui.view.AppointmentScreen
import com.tomerpacific.scheduler.ui.view.AppointmentSetScreen
import com.tomerpacific.scheduler.ui.view.AppointmentsScreen
import com.tomerpacific.scheduler.ui.view.ChooseAppointmentLocationScreen
import com.tomerpacific.scheduler.ui.view.LoginScreen
import com.tomerpacific.scheduler.ui.view.SplashScreen
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


@Composable
fun NavGraph(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NAVIGATION_DESTINATION_SPLASH
) {

    val afterSplashDestination: String = viewModel.getStartDestination()

    NavHost(
        navController = navController,
        startDestination = startDestination) {
        composable(NAVIGATION_DESTINATION_SPLASH) {
            SplashScreen(navigationCallbackOnAnimationEnd = {
                navController.navigate(afterSplashDestination) {
                    popUpTo(NAVIGATION_DESTINATION_SPLASH) { inclusive = true }
                }
            })
        }
        composable(NAVIGATION_DESTINATION_LOGIN) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.logo_black),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.matchParentSize()
                )
                LoginScreen(viewModel = viewModel, onNavigateAfterLoginScreen = {
                    navController.navigate(NAVIGATION_DESTINATION_APPOINTMENTS)
                })
            }
        }
        composable(NAVIGATION_DESTINATION_APPOINTMENTS) {
            AppointmentsScreen(viewModel, onUserLogout = {
                navController.navigate(NAVIGATION_DESTINATION_LOGIN)
            }, onAddAppointmentClicked = {
                navController.navigate(NAVIGATION_DESTINATION_ADD_APPOINTMENT)
            }, onAppointmentCancelled = { appointmentAction, error ->
                viewModel.updateScheduledAppointmentsForUser()
                navController.navigate("appointment-set/${appointmentAction}/${error}")
            }, onAppointmentClicked = { appointment ->
                navController.navigate("appointment/${appointment}")
            })
        }
        composable(NAVIGATION_DESTINATION_ADD_APPOINTMENT) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.logo_black),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.matchParentSize()
                )
                AddAppointmentScreen(
                    viewModel = viewModel,
                    onAppointmentScheduled = { appointmentAction, error ->
                        viewModel.updateScheduledAppointmentsForUser()
                        navController.navigate("appointment-set/${appointmentAction}/${error}")
                    })
            }
        }
        composable(
            "appointment-set/{appointmentAction}/{errorMsg}",
            arguments = listOf(
                navArgument(APPOINTMENT_ACTION_KET) { type = NavType.StringType },
                navArgument(ERROR_MESSAGE_KEY) { type = NavType.StringType }
            )) { backStackEntry ->
            val appointmentAction = backStackEntry.arguments?.getString(APPOINTMENT_ACTION_KET)
            val errorMsg = backStackEntry.arguments?.getString(ERROR_MESSAGE_KEY)

            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.logo_black),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.matchParentSize()
                )
                AppointmentSetScreen(
                    appointmentAction,
                    errorMsg,
                    onBackToAppointmentScreenPressed = {
                        navController.navigate(NAVIGATION_DESTINATION_APPOINTMENTS)
                    })
            }
        }
        composable("appointment/{appointmentModel}",
            arguments = listOf(
                navArgument("appointmentModel") { type = NavType.StringType })) { backStackEntry ->
            val appointmentString = backStackEntry.arguments?.getString("appointmentModel")
            appointmentString?.let {
                val appointment = Json.decodeFromString<AppointmentModel>(it)
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_black),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Inside,
                        modifier = Modifier.matchParentSize()
                    )
                    AppointmentScreen(appointment = appointment, onAddLocationPressed = {
                        navController.navigate("location")
                    })
                }
            }
        }
        composable("location") {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.logo_black),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.matchParentSize()
                )
                ChooseAppointmentLocationScreen(viewModel, onUserChoseCurrentLocation = {
                    navController.popBackStack()
                })
            }
        }
    }
}