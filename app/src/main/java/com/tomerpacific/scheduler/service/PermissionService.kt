package com.tomerpacific.scheduler.service

import android.content.Context
import android.content.pm.PackageManager

import androidx.core.content.ContextCompat

class PermissionService {

    fun areLocationPermissionsGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}