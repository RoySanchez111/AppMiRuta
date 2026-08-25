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

fun obtenerColoresFondo(): List<Color> {
    val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    
    val periodo = when (horaActual) {
        in 0..5 -> PeriodoDia.MADRUGADA
        in 6..11 -> PeriodoDia.MANANA
        in 12..17 -> PeriodoDia.ATARDECER
        in 18..19 -> PeriodoDia.OCASO
        else -> PeriodoDia.NOCHE
    }

    return when (periodo) {
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
            Color(0xFF2193B0),
            Color(0xFF47B4CB),
            Color(0xFF6DD5ED)
        )
        PeriodoDia.OCASO -> listOf(
            Color(0xFFF12711),
            Color(0xFFF5AF19),
            Color(0xFF654EA3)
        )
        PeriodoDia.NOCHE -> listOf(
            Color(0xFF232526),
            Color(0xFF323436),
            Color(0xFF414345)
        )
    }
}

@Composable
fun TimeBasedBackground(modifier: Modifier = Modifier.fillMaxSize(), content: @Composable () -> Unit) {
    val coloresFondo = remember { obtenerColoresFondo() }

    Box(
        modifier = modifier
            .background(Brush.verticalGradient(coloresFondo))
    ) {
        content()
    }
}
