package com.example.appbanco.logic

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
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
            nombre = "Línea 1 - Troncal Expresa",
            recorrido = "Terminal Central → Aeropuerto Internacional",
            frecuenciaMinutos = 6,
            color = Color(0xFFFF8E56),
            paradas = listOf("Estación Central", "Plaza Comercial Sur", "Hospital General Norte", "Terminal Aeropuerto"),
            estadoServicio = "Operación Normal (GPS Activo)"
        ),
        LineaHorario(
            codigo = "L4",
            nombre = "Línea 4 - Universitaria",
            recorrido = "Campus Universitario → Puerto Intermodal",
            frecuenciaMinutos = 10,
            color = Color(0xFF327CF2),
            paradas = listOf("Facultad de Ingeniería", "Estación Central", "Centro Histórico", "Puerto Intermodal"),
            estadoServicio = "Operación Normal"
        ),
        LineaHorario(
            codigo = "L7",
            nombre = "Línea 7 - Periférico",
            recorrido = "Centro Histórico → Hospital General",
            frecuenciaMinutos = 8,
            color = Color(0xFF0DBC61),
            paradas = listOf("Centro Histórico", "Plaza Comercial Sur", "Hospital General Norte"),
            estadoServicio = "Alta Demanda"
        ),
        LineaHorario(
            codigo = "MA",
            nombre = "Metro A - Conector Metropolitano",
            recorrido = "Central Angelópolis → Torre Financiera",
            frecuenciaMinutos = 4,
            color = Color(0xFFA149A1),
            paradas = listOf("Central Metropolitana", "Angelópolis", "Torre Financiera"),
            estadoServicio = "Desvío Temporal por Obras"
        ),
        LineaHorario(
            codigo = "L5",
            nombre = "Línea 5 - Alimentadora Norte",
            recorrido = "Mercado Central → Calzada Serdán → Estadio",
            frecuenciaMinutos = 14,
            color = Color(0xFFC0392B),
            paradas = listOf("Mercado Central", "Calzada Serdán", "Estadio Deportivo"),
            estadoServicio = "Retraso de 12 min (Tráfico)"
        )
    )

    // API simulada para consultar las líneas en tiempo real desde el backend
    suspend fun obtenerLineasRemotas(): List<LineaHorario> {
        delay(350) // Simula la latencia de consulta REST API del servicio de transporte
        return lineasPublicas
    }

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

    fun buscarLineas(query: String, listaBase: List<LineaHorario> = lineasPublicas): List<LineaHorario> {
        if (query.isBlank()) return listaBase
        return listaBase.filter { linea ->
            linea.codigo.contains(query, ignoreCase = true) ||
                    linea.nombre.contains(query, ignoreCase = true) ||
                    linea.recorrido.contains(query, ignoreCase = true) ||
                    linea.paradas.any { it.contains(query, ignoreCase = true) }
        }
    }
}
