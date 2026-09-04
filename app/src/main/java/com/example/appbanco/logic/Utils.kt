package com.example.appbanco.logic

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.appbanco.R
import com.example.appbanco.data.Incidencia
import com.example.appbanco.data.Lugar
import com.example.appbanco.data.obtenerLugaresMock
import java.util.Calendar

fun obtenerEsquemaColoresDinamico(): ColorScheme {
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return if (hora in 6..18) {
        // Esquema de Día (Cream)
        lightColorScheme(
            surface = Color(0xFFF0E8E4),
            onSurface = Color(0xFF000000),
            background = Color(0xFFF0E8E4),
            onBackground = Color(0xFF000000),
            surfaceVariant = Color(0xFFFFFFFF),
            onSurfaceVariant = Color(0xFF000000),
            primary = Color(0xFFFEB30F)
        )
    } else {
        // Esquema de Noche (Dark)
        darkColorScheme(
            surface = Color(0xFF212020),
            onSurface = Color(0xFFFFFFFF),
            background = Color(0xFF212020),
            onBackground = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF333333),
            onSurfaceVariant = Color(0xFFFFFFFF),
            primary = Color(0xFFFEB30F)
        )
    }
}

fun obtenerMensajeBienvenida(usuario: String?): String {
    val nombre = usuario ?: "Invitado"
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val saludo = when (hora) {
        in 6..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    return "$saludo, $nombre"
}

fun formatearRuta(nombre: String): String {
    return "Ruta: ${nombre.uppercase()}"
}

fun obtenerLogoSegunHora(): Int {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 6..11 -> R.drawable.logo_miruta_amanecer
        in 12..18 -> R.drawable.logo_miruta_atardecer
        else -> R.drawable.logo_miruta_anochecer
    }
}

fun obtenerDatosGuardados(callback: (List<Lugar>) -> List<Lugar>): List<Lugar> {
    return callback(obtenerLugaresMock())
}

// NUEVO: Función para cargar las alertas base (simulando una base de datos)
fun obtenerAlertasIniciales(): List<Incidencia> {
    return listOf(
        Incidencia(
            tipo = "Retraso grave",
            ruta = "L5",
            titulo = "Retraso grave - Ruta Guadalupana\n(Centro Puebla - Las lomas)",
            descripcion = "Interrupción parcial del servicio por falla técnica en la Colonia Serdán.\nRetrasos estimados de entre 10 a 30 minutos.\nUse Rutas alternas",
            tiempo = "Hace 10 min",
            colorEtiqueta = Color(0xFFC63F32), // Rojo
            colorRuta = Color(0xFFF37844)      // Naranja
        ),
        Incidencia(
            tipo = "Desvío de Ruta",
            ruta = "MA",
            titulo = "Desvío de Ruta - Ruta Angelópolis\n(Angelópolis - Plaza Loreto)",
            descripcion = "Cierre de vialidad por manifestación en Av. Insurgentes Norte.\nDesvío temporal entre la calle Colorines y Tlatelolco.\nUse Rutas alternas",
            tiempo = "Hace 30 min",
            colorEtiqueta = Color(0xFFF59E0B), // Amarillo/Naranja
            colorRuta = Color(0xFF9D4EDD)      // Morado
        )
    )
}