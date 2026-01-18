package com.moes.data.missions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class MissionType {
    COUNT,
    DISTANCE,
    DURATION
}

data class MissionDefinition(
    val id: String,
    val title: String,
    val description: String,
    val type: MissionType,
    val threshold: Long,
    val icon: ImageVector
)

data class MissionProgress(
    val definition: MissionDefinition,
    val currentValue: Long,
    val isCompleted: Boolean,
    val progressFloat: Float
)

// LISTA STATICA DELLE MISSIONI
object MissionsData {
    val allMissions = listOf(
        // NUMERO ALLENAMENTI
        MissionDefinition(
            "c_10",
            "Principiante",
            "Completa 10 allenamenti",
            MissionType.COUNT,
            10,
            Icons.Default.DirectionsRun
        ),
        MissionDefinition(
            "c_30",
            "Costante",
            "Completa 30 allenamenti",
            MissionType.COUNT,
            30,
            Icons.Default.DirectionsRun
        ),
        MissionDefinition(
            "c_100",
            "Veterano",
            "Completa 100 allenamenti",
            MissionType.COUNT,
            100,
            Icons.Default.EmojiEvents
        ),

        // DISTANZA
        MissionDefinition(
            "d_50k",
            "Maratoneta",
            "Percorri 50 km totali",
            MissionType.DISTANCE,
            50_000,
            Icons.Default.DirectionsRun
        ),
        MissionDefinition(
            "d_300k",
            "Globetrotter",
            "Percorri 300 km totali",
            MissionType.DISTANCE,
            300_000,
            Icons.Default.DirectionsRun
        ),
        MissionDefinition(
            "d_1000k",
            "Leggenda",
            "Percorri 1.000 km totali",
            MissionType.DISTANCE,
            1_000_000,
            Icons.Default.EmojiEvents
        ),

        // TEMPO ALLENAMENTO
        MissionDefinition(
            "t_50h",
            "Dedizione",
            "Allenati per 50 ore totali",
            MissionType.DURATION,
            180_000_000,
            Icons.Default.Timer
        ),
        MissionDefinition(
            "t_200h",
            "Passione",
            "Allenati per 200 ore totali",
            MissionType.DURATION,
            720_000_000,
            Icons.Default.Timer
        ),
    )
}