package com.example.appbanco.data.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncManager {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Sincronizar un usuario local con Firestore (Escritura en nube optimizada para Free Tier)
    suspend fun syncUserToCloud(user: UserEntity) {
        withContext(Dispatchers.IO) {
            try {
                val userMap = mapOf(
                    "id" to user.id,
                    "username" to user.username,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users")
                    .document(user.username)
                    .set(userMap)
                    .addOnSuccessListener {
                        Log.d("SyncManager", "Usuario sincronizado con la nube exitosamente")
                    }
                    .addOnFailureListener { e ->
                        Log.e("SyncManager", "Error al sincronizar usuario con la nube", e)
                    }
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en syncUserToCloud", e)
            }
        }
    }

    // Obtener datos de la nube para respaldo/sincronización (Lectura optimizada para Free Tier)
    suspend fun fetchUserFromCloud(username: String, onResult: (Map<String, Any>?) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users")
                    .document(username)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            onResult(document.data)
                        } else {
                            onResult(null)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("SyncManager", "Error al obtener usuario de la nube", e)
                        onResult(null)
                    }
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en fetchUserFromCloud", e)
                onResult(null)
            }
        }
    }

    // Sincronizar rutas frecuentes / puntos de interés con Firestore (Escritura Free Tier)
    suspend fun syncRutaFrecuenteToCloud(userId: String, rutaNombre: String, ubicacion: String) {
        withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "nombre" to rutaNombre,
                    "ubicacion" to ubicacion,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(userId)
                    .collection("rutas_frecuentes").document(rutaNombre)
                    .set(data)
                    .addOnSuccessListener {
                        Log.d("SyncManager", "Ruta frecuente sincronizada en la nube")
                    }
                    .addOnFailureListener { e ->
                        Log.e("SyncManager", "Error al sincronizar ruta frecuente", e)
                    }
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en syncRutaFrecuenteToCloud", e)
            }
        }
    }

    // Obtener rutas frecuentes de la nube (Lectura única Free Tier, sin listeners continuos)
    suspend fun fetchRutasFrecuentesFromCloud(userId: String, onResult: (List<Map<String, Any>>) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users").document(userId)
                    .collection("rutas_frecuentes")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val lista = querySnapshot.documents.mapNotNull { it.data }
                        onResult(lista)
                    }
                    .addOnFailureListener { e ->
                        Log.e("SyncManager", "Error al obtener rutas frecuentes", e)
                        onResult(emptyList())
                    }
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en fetchRutasFrecuentesFromCloud", e)
                onResult(emptyList())
            }
        }
    }
}
