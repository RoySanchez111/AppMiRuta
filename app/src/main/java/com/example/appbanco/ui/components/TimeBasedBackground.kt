package com.example.appbanco.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.*

enum class PeriodoDia {
    MADRUGADA,
    MANANA,
    ATARDECER,
    OCASO,
    NOCHE
}

@Composable
fun TimeBasedBackground(content: @Composable () -> Unit) {
    val horaActual = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    
    val periodo = when (horaActual) {
        in 0..5 -> PeriodoDia.MADRUGADA
        in 6..11 -> PeriodoDia.MANANA
        in 12..17 -> PeriodoDia.ATARDECER
        in 18..19 -> PeriodoDia.OCASO
        else -> PeriodoDia.NOCHE
    }

    val coloresFondo = when (periodo) {
        PeriodoDia.MADRUGADA -> listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364)
        )
        PeriodoDia.MANANA -> listOf(
            Color(0xFF8E2DE2),
            Color(0xFF4A00E0),
            Color(0xFF00C6FF)
        )
        PeriodoDia.ATARDECER -> listOf(
            Color(0xFF2193b0),
            Color(0xFF6dd5ed)
        )
        PeriodoDia.OCASO -> listOf(
            Color(0xFFf12711),
            Color(0xFFf5af19),
            Color(0xFF654ea3)
        )
        PeriodoDia.NOCHE -> listOf(
            Color(0xFF232526),
            Color(0xFF414345)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(coloresFondo))
    ) {
        content()
    }
}
