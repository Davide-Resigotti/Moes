package com.moes.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    }

    fun formatTime(millis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
    }

    fun formatTimeRange(start: Long, end: Long): String {
        return "${formatTime(start)} - ${formatTime(end)}"
    }

    @SuppressLint("DefaultLocale")
    fun formatDuration(millis: Long): String {
        val hours = millis / 3600000
        val minutes = (millis % 3600000) / 60000
        val seconds = (millis % 60000) / 1000
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatDistance(meters: Double): String {
        return String.format("%.2f km", meters / 1000)
    }

    @SuppressLint("DefaultLocale")
    fun formatSpeed(kmh: Double): String {
        return String.format("%.1f km/h", kmh)
    }

    @SuppressLint("DefaultLocale")
    fun formatPace(secondsPerKm: Double): String {
        if (secondsPerKm <= 0.0) return "-:--"
        val min = (secondsPerKm / 60).toInt()
        val sec = (secondsPerKm % 60).toInt()
        return String.format("%d:%02d /km", min, sec)
    }
}