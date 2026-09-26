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

    // Rutas Reales Oficiales de Puebla, Puebla (RED RUTA y Rutas Convencionales)
    val lineasPublicas = listOf(
        LineaHorario(
            codigo = "R1",
            nombre = "Línea 1 RUTA Puebla (Troncal)",
            recorrido = "Tlaxcalancingo → Chachapa",
            frecuenciaMinutos = 6,
            color = Color(0xFFE67E22), // Naranja RUTA L1
            paradas = listOf("Terminal Tlaxcalancingo", "Paseo Bravo", "China Poblana", "Hospitales", "Terminal Chachapa"),
            estadoServicio = "Operación Normal (GPS Activo)"
        ),
        LineaHorario(
            codigo = "R2",
            nombre = "Línea 2 RUTA Puebla (Diagonal)",
            recorrido = "Limones → Diagonal Defensores → 11 Sur",
            frecuenciaMinutos = 5,
            color = Color(0xFF2980B9), // Azul RUTA L2
            paradas = listOf("Terminal Margaritas / Limones", "11 Sur-Panteón", "El Gallito", "China Poblana", "Terminal Diagonal"),
            estadoServicio = "Alta Demanda"
        ),
        LineaHorario(
            codigo = "R3",
            nombre = "Línea 3 RUTA Puebla (Valsequillo - CAPU)",
            recorrido = "BUAP Valsequillo → CAPU",
            frecuenciaMinutos = 7,
            color = Color(0xFF27AE60), // Verde RUTA L3
            paradas = listOf("Terminal Valsequillo", "Facultad de Contaduría BUAP", "Plaza Cristal", "CU BUAP", "CAPU"),
            estadoServicio = "Operación Normal"
        ),
        LineaHorario(
            codigo = "R4",
            nombre = "Línea 4 RUTA (Periférico Ecológico)",
            recorrido = "Valsequillo → Amalucan → Autopista",
            frecuenciaMinutos = 10,
            color = Color(0xFF8E44AD), // Morado Línea 4
            paradas = listOf("Periférico Sur (Valsequillo)", "Ciudad Judicial", "Angelópolis", "Flor del Bosque", "Amalucan"),
            estadoServicio = "En Pruebas / Operación Regular"
        ),
        LineaHorario(
            codigo = "AZ",
            nombre = "Ruta Azteca (Convencional)",
            recorrido = "Bosques de San Sebastián → Centro → BUAP",
            frecuenciaMinutos = 8,
            color = Color(0xFFC0392B), // Rojo Ruta Azteca
            paradas = listOf("Bosques de San Sebastián", "Amalucan", "Mercado de Sabores", "Centro Histórico", "Ciudad Universitaria"),
            estadoServicio = "Retraso de 8 min (Tráfico en Centro)"
        ),
        LineaHorario(
            codigo = "R28",
            nombre = "Ruta 28 (Convencional)",
            recorrido = "Clavijero → Diagonal → CAPU",
            frecuenciaMinutos = 12,
            color = Color(0xFFF39C12), // Amarillo/Naranja Ruta 28
            paradas = listOf("Clavijero", "Xonaca", "Diagonal Defensores de la República", "CAPU"),
            estadoServicio = "Operación Normal"
        ),
        LineaHorario(
            codigo = "JBS",
            nombre = "Ruta Morados (JBS)",
            recorrido = "La Resurrección → Centro Histórico",
            frecuenciaMinutos = 9,
            color = Color(0xFF9B59B6), // Morado JBS
            paradas = listOf("La Resurrección", "Bosques", "China Poblana", "Mercado 5 de Mayo", "Centro Histórico"),
            estadoServicio = "Operación Normal"
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
