package com.tomerpacific.scheduler.ui.view

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tomerpacific.scheduler.R

@Composable
fun SplashScreen(splashTimerEnd:() -> Unit) {

    val rawAnimationFile: Int = R.raw.schedule_animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawAnimationFile))
    val progress by animateLottieCompositionAsState(composition)

    DisposableEffect(Unit) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = {
            splashTimerEnd()
        }
        handler.postDelayed(runnable, 2500)

        onDispose {
            handler.removeCallbacks(runnable)
        }
    }

    Box(
        modifier = Modifier
            .background(if (isSystemInDarkTheme()) Color.Black else Color.White)
            .fillMaxSize()
    ) {
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