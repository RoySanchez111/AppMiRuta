package com.example.appbanco.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star

data class LineaRuta(
    val numero: String, 
    val color: Color, 
    val nombre: String, 
    val destino: String, 
    val frecuencia: String, 
    val iconoEstado: ImageVector = Icons.Default.Star
)

data class AlertaIncidencia(
    val tipo: String, 
    val tipoColor: Color, 
    val ruta: String, 
    val rutaColor: Color, 
    val titulo: String, 
    val descripcion: String, 
    val tiempo: String, 
    val icono: ImageVector, 
    val iconoContainerColor: Color
)

data class Lugar(val nombre: String, val icono: ImageVector)

open class Usuario(
    val usuario: String,
    val password: String,
    val rol: String
)

class Persona(usuario: String, password: String, rol: String): Usuario(usuario, password, rol)
class Conductor(usuario: String, password: String, rol: String): Usuario(usuario, password, rol)
