package com.tomerpacific.scheduler.ui.view

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tomerpacific.scheduler.R

@Composable
fun SplashScreen(splashTimerEnd:() -> Unit) {

    val backgroundImage = if (isSystemInDarkTheme()) R.drawable.logo_transparent else R.drawable.logo_black

    DisposableEffect(Unit) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = {
            splashTimerEnd()
        }
        handler.postDelayed(runnable, 5000)

        onDispose {
            handler.removeCallbacks(runnable)
        }
    }

    Box(
        modifier = Modifier
            .background(if (isSystemInDarkTheme()) Color.Black else Color.White)
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = backgroundImage),
            contentDescription = "Logo",
            contentScale = ContentScale.Inside,
            modifier = Modifier.matchParentSize())
    }
}