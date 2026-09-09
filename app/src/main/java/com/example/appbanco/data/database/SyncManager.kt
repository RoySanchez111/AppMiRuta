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
}
