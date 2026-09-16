package com.example.appbanco.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco.data.database.UserDao
import com.example.appbanco.logic.ServicioAutenticacion
import com.example.appbanco.logic.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val authService = ServicioAutenticacion(userDao)
    
    var loginSuccess by mutableStateOf(false)
        private set
        
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onLoginClick(usuario: String, pass: String) {
        val u = usuario.trim()
        val p = pass.trim()
        viewModelScope.launch {
            val token = authService.login(u, p)
            if (token != null) {
                val user = userDao.getUserByUsername(u)
                user?.let {
                    sessionManager.saveSession(it.id, it.username, token)
                    sessionManager.updateUserRole(it.role)
                    loginSuccess = true
                }
            } else {
                errorMessage = "Usuario o contraseña incorrecto"
            }
        }
    }
}
