package com.example.appbanco.logic
//Amor prohibido murmuran por las calles

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class LineaHorario(
    val codigo: String,
    val nombre: String,
    val recorrido: String,
    val frecuenciaMinutos: Int,
    val color: Color,
    val paradas: List<String>,
    val estadoServicio: String = "Operación Normal"
)

class ServicioHorarios {

    val lineasPublicas = listOf(
        LineaHorario(
            codigo = "L1",
            nombre = "Línea 1",
            recorrido = "Plaza Mayor → Aeropuerto",
            frecuenciaMinutos = 8,
            color = Color(0xFFFF8E56),
            paradas = listOf("Tecmilenio Campus Puebla", "Plaza Mayor", "Hospital Norte", "Aeropuerto")
        ),
        LineaHorario(
            codigo = "L4",
            nombre = "Línea 4",
            recorrido = "Universidad CCU → Puerto",
            frecuenciaMinutos = 12,
            color = Color(0xFF327CF2),
            paradas = listOf("Universidad CCU", "Tecmilenio Campus Puebla", "Centro", "Puerto")
        ),
        LineaHorario(
            codigo = "L7",
            nombre = "Línea 7",
            recorrido = "Centro → Hospital Norte",
            frecuenciaMinutos = 10,
            color = Color(0xFF0DBC61),
            paradas = listOf("Centro", "Plaza Mayor", "Hospital Norte")
        ),
        LineaHorario(
            codigo = "MA",
            nombre = "Metro A",
            recorrido = "Central → Torre Central",
            frecuenciaMinutos = 4,
            color = Color(0xFFA149A1),
            paradas = listOf("Central", "Angelópolis", "Torre Central"),
            estadoServicio = "Desvío Temporal"
        ),
        LineaHorario(
            codigo = "L5",
            nombre = "Línea 5",
            recorrido = "Mercado → Calzada Serdán → Estadio",
            frecuenciaMinutos = 15,
            color = Color(0xFFC0392B),
            paradas = listOf("Mercado", "Calzada Serdán", "Estadio"),
            estadoServicio = "Retraso de 15 min"
        )
    )

    fun obtenerHoraActualFormateada(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }

    fun calcularProximasSalidas(frecuenciaMinutos: Int, cantidad: Int = 4, fechaOffsetDias: Int = 0): List<String> {
        val cal = Calendar.getInstance()
        if (fechaOffsetDias > 0) {
            cal.add(Calendar.DAY_OF_YEAR, fechaOffsetDias)
            cal.set(Calendar.HOUR_OF_DAY, 7)
            cal.set(Calendar.MINUTE, 0)
        }

        val minutoActual = cal.get(Calendar.MINUTE)
        val residuo = minutoActual % frecuenciaMinutos
        val minutosParaSiguiente = frecuenciaMinutos - residuo

        cal.add(Calendar.MINUTE, minutosParaSiguiente)

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val salidas = mutableListOf<String>()

        for (i in 0 until cantidad) {
            salidas.add(sdf.format(cal.time))
            cal.add(Calendar.MINUTE, frecuenciaMinutos)
        }

        return salidas
    }

    fun buscarLineas(query: String): List<LineaHorario> {
        if (query.isBlank()) return lineasPublicas
        return lineasPublicas.filter { linea ->
            linea.codigo.contains(query, ignoreCase = true) ||
                    linea.nombre.contains(query, ignoreCase = true) ||
                    linea.recorrido.contains(query, ignoreCase = true) ||
                    linea.paradas.any { it.contains(query, ignoreCase = true) }
        }
    }
}
