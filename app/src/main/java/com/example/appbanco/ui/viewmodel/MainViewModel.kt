package com.example.appbanco.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco.logic.SessionManager
import com.example.appbanco.ui.screens.Incidencia
import com.example.appbanco.data.database.UserDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.appbanco.ui.theme.EscalaAccesibilidad
import com.example.appbanco.logic.ServicioHorarios

data class RutaFrecuenteItem(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val color: Color,
    val icono: ImageVector
)

class MainViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _startDestination = MutableStateFlow("loading")
    val startDestination: StateFlow<String> = _startDestination.asStateFlow()

    // Servicio de API de Horarios
    val servicioHorarios = ServicioHorarios()

    // Tema dinámico de la app: "Degradados", "Claro", "Oscuro"
    private val _modoTema = MutableStateFlow("Degradados")
    val modoTema: StateFlow<String> = _modoTema.asStateFlow()

    // Accesibilidad: Escala de fuente centralizada
    private val _escalaFuente = MutableStateFlow(EscalaAccesibilidad.MEDIANO)
    val escalaFuente: StateFlow<EscalaAccesibilidad> = _escalaFuente.asStateFlow()

    // Modo Offline y Ahorro de Datos
    private val _modoOffline = MutableStateFlow(false)
    val modoOffline: StateFlow<Boolean> = _modoOffline.asStateFlow()

    // Nombre de usuario activo para saludos y perfil
    private val _usuarioActual = MutableStateFlow("Invitado")
    val usuarioActual: StateFlow<String> = _usuarioActual.asStateFlow()

    // URI de la foto de perfil activa del usuario
    private val _fotoPerfilUri = MutableStateFlow<String?>(null)
    val fotoPerfilUri: StateFlow<String?> = _fotoPerfilUri.asStateFlow()

    fun cambiarTema(nuevoTema: String) {
        viewModelScope.launch {
            sessionManager.updateAppTheme(nuevoTema)
            _modoTema.value = nuevoTema
        }
    }

    fun cambiarEscalaFuente(nuevaEscala: EscalaAccesibilidad) {
        viewModelScope.launch {
            sessionManager.updateAccessibilityFontScale(nuevaEscala.nombre)
            _escalaFuente.value = nuevaEscala
        }
    }

    fun cambiarModoOffline(activado: Boolean) {
        viewModelScope.launch {
            sessionManager.updateOfflineMode(activado)
            _modoOffline.value = activado
        }
    }

    fun actualizarFotoPerfil(uriString: String?) {
        viewModelScope.launch {
            sessionManager.updateProfileImage(uriString)
            _fotoPerfilUri.value = uriString
        }
    }

    fun actualizarNombreUsuario(userDao: UserDao, nuevoNombre: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (nuevoNombre.isNotBlank()) {
                val userId = sessionManager.currentUserId.firstOrNull()
                if (userId != null) {
                    userDao.updateUsername(userId, nuevoNombre)
                }
                sessionManager.updateUsername(nuevoNombre)
                _usuarioActual.value = nuevoNombre
                onFinished(true)
            } else {
                onFinished(false)
            }
        }
    }

    // Lista global de rutas frecuentes que sobrevive al cambio de pestañas
    val listaRutasFrecuentes = mutableStateListOf(
        RutaFrecuenteItem("1", "Casa", "Registrar ubicación", Color(0xFF4A86F7), Icons.Default.Home),
        RutaFrecuenteItem("2", "Universidad", "Registrar ubicación", Color(0xFFF26E68), Icons.Default.School)
    )

    fun agregarRutaFrecuente(nueva: RutaFrecuenteItem) {
        listaRutasFrecuentes.add(nueva)
    }

    fun eliminarRutaFrecuente(id: String) {
        listaRutasFrecuentes.removeAll { it.id == id }
    }

    fun moverRutaArriba(id: String) {
        val index = listaRutasFrecuentes.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = listaRutasFrecuentes.removeAt(index)
            listaRutasFrecuentes.add(index - 1, item)
        }
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
        observarUsuario()
    }

    private fun observarUsuario() {
        viewModelScope.launch {
            sessionManager.currentUsername.collectLatest { name ->
                if (!name.isNullOrBlank()) {
                    _usuarioActual.value = name
                } else {
                    _usuarioActual.value = "Invitado"
                }
            }
        }
        viewModelScope.launch {
            sessionManager.profileImageUri.collectLatest { uri ->
                _fotoPerfilUri.value = uri
            }
        }
        viewModelScope.launch {
            sessionManager.appTheme.collectLatest { theme ->
                _modoTema.value = theme
            }
        }
        viewModelScope.launch {
            sessionManager.escalaFuente.collectLatest { escalaStr ->
                _escalaFuente.value = EscalaAccesibilidad.desdeNombre(escalaStr)
            }
        }
        viewModelScope.launch {
            sessionManager.modoOffline.collectLatest { enabled ->
                _modoOffline.value = enabled
            }
        }
    }

    private fun checkSession() {
        viewModelScope.launch {
            sessionManager.isLoggedIn.collectLatest { loggedIn ->
                _startDestination.value = if (loggedIn) "principal" else "login"
            }
        }
    }
}