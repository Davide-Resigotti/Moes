package com.moes.utils

import com.moes.data.TrainingSession
import com.moes.data.UserProfile
import java.util.Calendar
import kotlin.math.roundToInt

object CaloriesCalculator {

    fun calculate(session: TrainingSession, profile: UserProfile): String {
        // 1. Recupero dati fisiologici (con fallback se mancanti)
        val weight = if (profile.weightKg > 0) profile.weightKg else 70f
        val height = if (profile.heightCm > 0) profile.heightCm else 175f
        val age = getAge(profile.birthDate)
        val isMale = profile.gender.equals("M", ignoreCase = true)

        // 2. Calcolo BMR (Basal Metabolic Rate) - Formula Harris-Benedict rivisitata
        // Stima quanto consuma il corpo a riposo in 24 ore
        val bmrDay = if (isMale) {
            88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
        } else {
            447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
        }

        // Consumo basale per ora
        val bmrHour = bmrDay / 24.0

        // 3. Stima MET (Metabolic Equivalent of Task)
        // 1 MET = Consumo a riposo. Correre a 10km/h sono circa 10 METs.
        val speedKmh = session.avgSpeedKmh
        val met = estimateMET(speedKmh)

        // 4. Formula Finale: BMR/ora * MET * Durata(ore)
        val durationHours = session.durationMs / 3600000.0
        val totalKcal = bmrHour * met * durationHours

        return totalKcal.roundToInt().toString()
    }

    private fun getAge(birthDate: Long): Int {
        if (birthDate == 0L) return 30
        val birth = Calendar.getInstance().apply { timeInMillis = birthDate }
        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return if (age in 10..100) age else 30
    }

    private fun estimateMET(speedKmh: Double): Double {
        return when {
            speedKmh < 0.5 -> 1.0  // Riposo / Fermo
            speedKmh < 3.2 -> 2.3  // Camminata lenta
            speedKmh < 4.8 -> 3.3  // Camminata moderata
            speedKmh < 6.4 -> 5.0  // Camminata veloce / Hiking
            speedKmh < 8.0 -> 8.0  // Jogging lento
            speedKmh < 9.7 -> 10.0 // Corsa (6 min/km)
            speedKmh < 11.3 -> 11.5 // Corsa (5:20 min/km)
            speedKmh < 12.9 -> 13.5 // Corsa veloce
            speedKmh < 14.5 -> 15.0 // Corsa molto veloce
            else -> 16.0           // Sprint
        }
    }
}