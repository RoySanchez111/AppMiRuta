package com.example.appbanco.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco.logic.SessionManager
import com.example.appbanco.ui.screens.Incidencia
import com.example.appbanco.data.database.UserDao
import com.example.appbanco.data.database.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.appbanco.ui.theme.EscalaAccesibilidad
import com.example.appbanco.logic.ServicioHorarios
import java.util.UUID

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

    // Tema dinámico de la app respaldado por DataStore
    val modoTema: StateFlow<String> = sessionManager.appTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Degradados")

    // Accesibilidad: Escala de fuente centralizada respaldada por DataStore
    val escalaFuente: StateFlow<EscalaAccesibilidad> = sessionManager.escalaFuente
        .map { EscalaAccesibilidad.desdeNombre(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EscalaAccesibilidad.MEDIANO)

    // Modo Offline respaldado por DataStore
    val modoOffline: StateFlow<Boolean> = sessionManager.modoOffline
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Notificaciones respaldadas por DataStore
    val notifTiempoReal: StateFlow<Boolean> = sessionManager.notifTiempoReal
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val notifRetrasos: StateFlow<Boolean> = sessionManager.notifRetrasos
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val notifSonidoVibracion: StateFlow<Boolean> = sessionManager.notifSonidoVibracion
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Nombre de usuario activo para saludos y perfil
    private val _usuarioActual = MutableStateFlow("Invitado")
    val usuarioActual: StateFlow<String> = _usuarioActual.asStateFlow()

    // URI de la foto de perfil activa del usuario
    private val _fotoPerfilUri = MutableStateFlow<String?>(null)
    val fotoPerfilUri: StateFlow<String?> = _fotoPerfilUri.asStateFlow()

    fun cambiarTema(nuevoTema: String) {
        viewModelScope.launch {
            sessionManager.updateAppTheme(nuevoTema)
        }
    }

    fun cambiarEscalaFuente(nuevaEscala: EscalaAccesibilidad) {
        viewModelScope.launch {
            sessionManager.updateAccessibilityFontScale(nuevaEscala.nombre)
        }
    }

    fun cambiarModoOffline(activado: Boolean) {
        viewModelScope.launch {
            sessionManager.updateOfflineMode(activado)
        }
    }

    fun cambiarNotifTiempoReal(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.updateNotifTiempoReal(enabled)
        }
    }

    fun cambiarNotifRetrasos(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.updateNotifRetrasos(enabled)
        }
    }

    fun cambiarNotifSonidoVibracion(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.updateNotifSonidoVibracion(enabled)
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
    val listaRutasFrecuentes = mutableStateListOf<RutaFrecuenteItem>()

    fun agregarRutaFrecuente(nueva: RutaFrecuenteItem) {
        listaRutasFrecuentes.add(nueva)
        viewModelScope.launch {
            val userId = sessionManager.currentUsername.firstOrNull() ?: "invitado"
            syncManager.syncRutaFrecuenteToCloud(userId, nueva.nombre, nueva.ubicacion)
        }
    }

    private fun cargarRutasFrecuentesRemotas() {
        viewModelScope.launch {
            val userId = sessionManager.currentUsername.firstOrNull() ?: "invitado"
            syncManager.fetchRutasFrecuentesFromCloud(userId) { listaMapas ->
                if (listaMapas.isNotEmpty()) {
                    listaMapas.forEach { map ->
                        val nombre = map["nombre"] as? String ?: "Destino"
                        val ubicacion = map["ubicacion"] as? String ?: ""
                        if (listaRutasFrecuentes.none { it.nombre == nombre }) {
                            listaRutasFrecuentes.add(
                                RutaFrecuenteItem(
                                    id = System.currentTimeMillis().toString() + nombre,
                                    nombre = nombre,
                                    ubicacion = ubicacion,
                                    color = Color(0xFF4A86F7),
                                    icono = Icons.Default.Place
                                )
                            )
                        }
                    }
                }
            }
        }
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
    val listaIncidencias = mutableStateListOf<Incidencia>()

    private val syncManager = SyncManager()

    fun agregarIncidencia(nueva: Incidencia) {
        listaIncidencias.add(0, nueva)
        viewModelScope.launch {
            syncManager.syncIncidenciaToCloud(
                id = nueva.id,
                tipo = nueva.tipo,
                ruta = nueva.ruta,
                titulo = nueva.titulo,
                descripcion = nueva.descripcion,
                tiempo = nueva.tiempo
            )
        }
    }

    private fun cargarIncidenciasRemotas() {
        viewModelScope.launch {
            syncManager.fetchIncidenciasFromCloud { listaMapas ->
                if (listaMapas.isNotEmpty()) {
                    listaMapas.forEach { map ->
                        val id = map["id"] as? String ?: UUID.randomUUID().toString()
                        val tipo = map["tipo"] as? String ?: "Reporte"
                        val ruta = map["ruta"] as? String ?: "L1"
                        val titulo = map["titulo"] as? String ?: "Incidencia"
                        val descripcion = map["descripcion"] as? String ?: ""
                        val tiempo = map["tiempo"] as? String ?: "Hace un momento"

                        if (listaIncidencias.none { it.id == id }) {
                            listaIncidencias.add(
                                Incidencia(
                                    id = id,
                                    tipo = tipo,
                                    ruta = ruta,
                                    titulo = titulo,
                                    descripcion = descripcion,
                                    tiempo = tiempo,
                                    colorEtiqueta = Color(0xFFC0392B),
                                    colorRuta = Color(0xFFF39C12)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        checkSession()
        observarUsuario()
        cargarIncidenciasRemotas()
        cargarRutasFrecuentesRemotas()
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
    }

    private fun checkSession() {
        viewModelScope.launch {
            sessionManager.isLoggedIn.collectLatest { loggedIn ->
                if (loggedIn) {
                    val role = sessionManager.userRole.firstOrNull() ?: "pasajero"
                    _startDestination.value = if (role == "conductor" || role == "admin") "inicio_conductor" else "principal"
                } else {
                    _startDestination.value = "login"
                }
            }
        }
    }
}
