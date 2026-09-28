package com.example.appbanco.logic

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appbanco.data.database.UserDao
import com.example.appbanco.data.database.UserEntity
import java.util.UUID

interface Autenticable {
    suspend fun login(usuario: String, pass: String): String?
}

object AppConfig {
    const val NOMBRE_APP = "Mi Ruta"
}

class ServicioAutenticacion(private val userDao: UserDao) : Autenticable {
    
    override suspend fun login(usuario: String, pass: String): String? {
        var user = userDao.getUserByUsername(usuario)
        
        // Sincronización bidireccional: Si no está en Room local, buscamos en Cloud Firestore (ej. creado desde el Panel Web Admin)
        if (user == null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val docTask = db.collection("users").document(usuario).get()
                val doc = Tasks.await(docTask)
                if (doc.exists()) {
                    val role = doc.getString("role") ?: "pasajero"
                    val newUser = UserEntity(
                        username = usuario,
                        passwordHash = SecurityUtils.hashPassword(pass),
                        role = role
                    )
                    userDao.registerUser(newUser)
                    user = userDao.getUserByUsername(usuario)
                }
            } catch (e: Exception) {
                Log.e("AuthLogic", "Error buscando usuario en Firestore durante login", e)
            }
        }

        if (user == null) return null

        return if (SecurityUtils.checkPassword(pass, user.passwordHash)) {
            UUID.randomUUID().toString()
        } else {
            null
        }
    }
}
