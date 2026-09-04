package com.example.appbanco.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color

val roy = Persona("roy", "123", "Conductor")
val alex = Conductor("alex", "456", "Conductor")
val usuariosList = listOf(roy, alex)

val lineasDeRuta = listOf(
    LineaRuta("L1", Color(0xFFFF8E56), "Línea 1", "Plaza Mayor → Aeropuerto", "Cada 8 min"),
    LineaRuta("L4", Color(0xFF327CF2), "Línea 4", "Universidad → Puerto", "Cada 12 min"),
    LineaRuta("L7", Color(0xFF0DBC61), "Línea 7", "Centro → Hospital Norte", "Cada 10 min"),
    LineaRuta("MA", Color(0xFFA149A1), "Metro A", "Central → Torre Central", "Cada 4 min"),
    LineaRuta("L5", Color(0xFFFF8E56), "Línea 5", "Mercado → Estadio", "Cada 15 min")
)

val alertasSimuladas = listOf(
    AlertaIncidencia(
        "Retraso grave", 
        Color(0xFFC0392B), 
        "L5", 
        Color(0xFFFF8E56), 
        "Retraso grave – Ruta Guadalupana", 
        "Interrupción parcial por falla técnica en Serdán. Retrasos de 10 a 30 min.", 
        "Hace 10 min", 
        Icons.Default.Warning, 
        Color(0xFFFADBD8)
    ),
    AlertaIncidencia(
        "Desvío de Ruta", 
        Color(0xFFF39C12), 
        "MA", 
        Color(0xFFA149A1), 
        "Desvío de Ruta – Ruta Angelópolis", 
        "Cierre por manifestación en Av. Insurgentes. Use rutas alternas.", 
        "Hace 30 min", 
        Icons.AutoMirrored.Filled.Shortcut, 
        Color(0xFFFDEBD0)
    )
)

fun obtenerLugaresMock(): List<Lugar> {
    return listOf(
        Lugar("Casa", Icons.Default.Home),
        Lugar("Trabajo", Icons.Default.Work),
        Lugar("Escuela", Icons.Default.School),
        Lugar("Gimnasio", Icons.Default.FitnessCenter)
    )
}

// NUEVO: Modelo de datos para las Alertas
data class Incidencia(
    val tipo: String,
    val ruta: String,
    val titulo: String,
    val descripcion: String,
    val tiempo: String,
    val colorEtiqueta: Color,
    val colorRuta: Color
)
