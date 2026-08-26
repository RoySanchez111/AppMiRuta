package com.example.appbanco

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import java.util.Calendar

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
    val lista = listOf(
        Lugar("Casa", Icons.Default.Home),
        Lugar("Trabajo", Icons.Default.Work),
        Lugar("Escuela", Icons.Default.School),
        Lugar("Gimnasio", Icons.Default.FitnessCenter)
    )
    return callback(lista)
}