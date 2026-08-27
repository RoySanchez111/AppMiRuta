package com.example.appbanco.logic

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.appbanco.R
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
