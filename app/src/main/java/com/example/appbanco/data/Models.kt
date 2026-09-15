package com.example.appbanco.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Lugar(
    val id: String,
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val esFavorito: Boolean = false
)

data class LineaRuta(
    val numero: String,
    val nombre: String,
    val destino: String,
    val frecuencia: String,
    val color: Color,
    val iconoEstado: ImageVector
)

data class AlertaIncidencia(
    val id: String,
    val tipo: String,
    val ruta: String,
    val titulo: String,
    val descripcion: String,
    val tiempo: String,
    val tipoColor: Color,
    val rutaColor: Color,
    val iconoContainerColor: Color,
    val icono: ImageVector
)
