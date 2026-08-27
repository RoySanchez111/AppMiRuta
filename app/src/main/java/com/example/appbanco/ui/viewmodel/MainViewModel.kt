package com.example.appbanco.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco.logic.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _startDestination = mutableStateOf("loading")
    val startDestination: State<String> = _startDestination

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
