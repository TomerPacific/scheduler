package com.tomerpacific.scheduler

import java.util.*
import kotlin.math.floor

object Utils {

    fun convertTimestampToDate(timestamp: Long): Date {
        return Date(timestamp)
    }

    fun truncateTimestamp(timestamp: Long): Long {
        val t = timestamp.toDouble() / 1000L
        val floored = floor(t) * 1000L
        return floored.toLong()
    }

}