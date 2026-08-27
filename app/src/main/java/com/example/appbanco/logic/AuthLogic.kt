package com.example.appbanco.logic

import com.example.appbanco.data.database.UserDao
import java.util.UUID

interface Autenticable {
    suspend fun login(usuario: String, pass: String): String?
}

object AppConfig {
    const val NOMBRE_APP = "Mi Ruta"
}

class ServicioAutenticacion(private val userDao: UserDao) : Autenticable {
    
    override suspend fun login(usuario: String, pass: String): String? {
        val user = userDao.getUserByUsername(usuario) ?: return null
        return if (SecurityUtils.checkPassword(pass, user.passwordHash)) {
            UUID.randomUUID().toString()
        } else {
            null
        }
    }
}
