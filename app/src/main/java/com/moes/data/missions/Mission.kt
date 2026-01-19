package com.moes.data.missions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class MissionType {
    COUNT,      // Numero totale allenamenti
    DISTANCE,   // Distanza totale (metri)
    DURATION,   // Tempo totale (millisecondi)
    STREAK,     // Giorni consecutivi
    OVER_5K,    // Numero sessioni > 5km
    OVER_10K    // Numero sessioni > 10km
}

data class MissionLevel(
    val title: String,
    val threshold: Long
)

data class MissionDefinition(
    val id: String,
    val baseTitle: String,
    val descriptionTemplate: String,
    val type: MissionType,
    val icon: ImageVector,
    val levels: List<MissionLevel>
)

data class MissionProgress(
    val definition: MissionDefinition,
    val currentLevelIndex: Int,
    val currentLevelTarget: Long,
    val currentValue: Long,
    val progressFloat: Float,
    val isFullyCompleted: Boolean
)

object MissionsData {
    val allMissions = listOf(
        // 1. NUMERO TOTALE ALLENAMENTI
        MissionDefinition(
            id = "total_count",
            baseTitle = "Costanza",
            descriptionTemplate = "Completa %s allenamenti totali",
            type = MissionType.COUNT,
            icon = Icons.Default.DirectionsRun,
            levels = listOf(
                MissionLevel("Principiante", 10),
                MissionLevel("Intermedio", 50),
                MissionLevel("Veterano", 100)
            )
        ),

        // 2. DISTANZA TOTALE
        MissionDefinition(
            id = "total_distance",
            baseTitle = "Macinatore di Km",
            descriptionTemplate = "Percorri un totale di %s",
            type = MissionType.DISTANCE,
            icon = Icons.Default.Speed,
            levels = listOf(
                MissionLevel("Maratoneta", 50_000),
                MissionLevel("Globetrotter", 500_000),
                MissionLevel("Leggenda", 2_000_000)
            )
        ),

        // 3. TEMPO TOTALE
        MissionDefinition(
            id = "total_duration",
            baseTitle = "Dedizione",
            descriptionTemplate = "Allenati per %s totali",
            type = MissionType.DURATION,
            icon = Icons.Default.Timer,
            levels = listOf(
                MissionLevel("Appassionato", 36_000_000),
                MissionLevel("Atleta", 180_000_000),
                MissionLevel("Iron Man", 720_000_000)
            )
        ),

        // 4. STREAK (Giorni Consecutivi)
        MissionDefinition(
            id = "streak",
            baseTitle = "Non ti ferma nessuno",
            descriptionTemplate = "Allenati per %s giorni di fila",
            type = MissionType.STREAK,
            icon = Icons.Default.LocalFireDepartment,
            levels = listOf(
                MissionLevel("Riscaldamento", 3),
                MissionLevel("Inarrestabile", 7),
                MissionLevel("On Fire", 30)
            )
        ),

        // 5. SESSIONI > 5KM
        MissionDefinition(
            id = "over_5k",
            baseTitle = "Mezzofondista",
            descriptionTemplate = "Completa %s allenamenti da almeno 5 km",
            type = MissionType.OVER_5K,
            icon = Icons.Default.EmojiEvents,
            levels = listOf(
                MissionLevel("Primi Passi", 5),
                MissionLevel("Esperto", 20),
                MissionLevel("Maestro", 50)
            )
        ),

        // 6. SESSIONI > 10KM
        MissionDefinition(
            id = "over_10k",
            baseTitle = "Fondista",
            descriptionTemplate = "Completa %s allenamenti da almeno 10 km",
            type = MissionType.OVER_10K,
            icon = Icons.Default.EmojiEvents,
            levels = listOf(
                MissionLevel("La Sfida", 1),
                MissionLevel("Resistente", 10),
                MissionLevel("Instancabile", 25)
            )
        )
    )
}