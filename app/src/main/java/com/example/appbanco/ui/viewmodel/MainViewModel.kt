package com.example.appbanco.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco.logic.SessionManager
import com.example.appbanco.ui.screens.Incidencia
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _startDestination = mutableStateOf("loading")
    val startDestination: State<String> = _startDestination

    // Tema dinámico de la app: "Degradados", "Claro", "Oscuro"
    val modoTema = mutableStateOf("Degradados")

    fun cambiarTema(nuevoTema: String) {
        modoTema.value = nuevoTema
    }

    // Lista global de incidencias que sobrevive al cambio de pestañas
    val listaIncidencias = mutableStateListOf(
        Incidencia(
            tipo = "Retraso grave",
            ruta = "L5",
            titulo = "Retraso grave - Ruta Guadalupana",
            descripcion = "Interrupción parcial del servicio en la Colonia Serdán. Retrasos de 10 a 30 min.",
            tiempo = "Hace 10 min",
            colorEtiqueta = Color(0xFFC0392B),
            colorRuta = Color(0xFFF39C12)
        ),
        Incidencia(
            tipo = "Desvío de Ruta",
            ruta = "MA",
            titulo = "Desvío de Ruta - Ruta Angelópolis",
            descripcion = "Cierre de vialidad por manifestación en Av. Insurgentes Norte.",
            tiempo = "Hace 30 min",
            colorEtiqueta = Color(0xFFF39C12),
            colorRuta = Color(0xFF9B59B6)
        )
    )

    fun agregarIncidencia(nueva: Incidencia) {
        listaIncidencias.add(0, nueva)
    }

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            sessionManager.isLoggedIn.collectLatest { loggedIn ->
                _startDestination.value = if (loggedIn) "principal" else "login"
            }
        }
    }
}