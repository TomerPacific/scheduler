package com.tomerpacific.scheduler.ui.view

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tomerpacific.scheduler.R

@Composable
fun SplashScreen(navigationCallbackOnAnimationEnd:() -> Unit) {

    val rawAnimationFile: Int = R.raw.schedule_animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawAnimationFile))
    val progress by animateLottieCompositionAsState(composition)

    DisposableEffect(Unit) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = {
            navigationCallbackOnAnimationEnd()
        }
        handler.postDelayed(runnable, 2000)

        onDispose {
            handler.removeCallbacks(runnable)
        }
    }

    Box(
        modifier = Modifier
            .background(if (isSystemInDarkTheme()) Color.Black else Color.White)
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Text("Scheduler", fontSize = 25.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h1)
            }
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                )
            }
        }


    }
}