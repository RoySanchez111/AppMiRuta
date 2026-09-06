package com.example.appbanco.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco.logic.SessionManager
import com.example.appbanco.ui.screens.Incidencia
import com.example.appbanco.data.database.UserDao
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.appbanco.logic.ServicioHorarios

data class RutaFrecuenteItem(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val color: Color,
    val icono: ImageVector
)

class MainViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _startDestination = mutableStateOf("loading")
    val startDestination: State<String> = _startDestination

    // Servicio de API de Horarios
    val servicioHorarios = ServicioHorarios()

    // Tema dinámico de la app: "Degradados", "Claro", "Oscuro"
    val modoTema = mutableStateOf("Degradados")

    // Nombre de usuario activo para saludos y perfil
    val usuarioActual = mutableStateOf("Invitado")

    // URI de la foto de perfil activa del usuario
    val fotoPerfilUri = mutableStateOf<String?>(null)

    fun cambiarTema(nuevoTema: String) {
        viewModelScope.launch {
            sessionManager.updateAppTheme(nuevoTema)
            modoTema.value = nuevoTema
        }
    }

    fun actualizarFotoPerfil(uriString: String?) {
        viewModelScope.launch {
            sessionManager.updateProfileImage(uriString)
            fotoPerfilUri.value = uriString
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
                usuarioActual.value = nuevoNombre
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
                    usuarioActual.value = name
                } else {
                    usuarioActual.value = "Invitado"
                }
            }
        }
        viewModelScope.launch {
            sessionManager.profileImageUri.collectLatest { uri ->
                fotoPerfilUri.value = uri
            }
        }
        viewModelScope.launch {
            sessionManager.appTheme.collectLatest { theme ->
                modoTema.value = theme
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