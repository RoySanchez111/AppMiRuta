package com.example.appbanco

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

interface Autenticable {
    fun validar(usuario: String, pass: String): Boolean
}

object AppConfig {
    const val NOMBRE_APP = "Mi Ruta"
    val usuariosPermitidos = mutableMapOf(
        "roy" to "123",
        "admin" to "admin"
    )
}

class ServicioAutenticacion : Autenticable {
    override fun validar(usuario: String, pass: String): Boolean {
        return AppConfig.usuariosPermitidos[usuario] == pass
    }
}

data class LineaRuta(
    val numero: String,
    val color: Color,
    val nombre: String,
    val destino: String,
    val frecuencia: String,
    val iconoEstado: ImageVector = Icons.Default.Star
)

val lineasDeRuta = listOf(
    LineaRuta("L1", Color(0xFFFF8E56), "Línea 1", "Plaza Mayor → Aeropuerto", "Cada 8 min"),
    LineaRuta("L4", Color(0xFF327CF2), "Línea 4", "Universidad → Puerto", "Cada 12 min"),
    LineaRuta("L7", Color(0xFF0DBC61), "Línea 7", "Centro → Hospital Norte", "Cada 10 min"),
    LineaRuta("MA", Color(0xFFA149A1), "Metro A", "Central → Torre Central", "Cada 4 min"),
    LineaRuta("L5", Color(0xFFFF8E56), "Línea 5", "Mercado → Estadio", "Cada 15 min")
)

data class Lugar(val nombre: String, val icono: ImageVector)

open class Usuario(
    val usuario: String,
    val password: String,
    val rol: String
)

class Persona(usuario: String, password: String, rol: String): Usuario(usuario, password, rol)
class Conductor(usuario: String, password: String, rol: String): Usuario(usuario, password, rol)

val roy = Persona("roy", "123", "Conductor")
val alex = Conductor("alex", "456", "Conductor")
val usuarios = listOf(roy, alex)