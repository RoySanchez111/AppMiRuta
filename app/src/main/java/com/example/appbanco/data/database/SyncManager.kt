package com.example.appbanco.data.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ConductorUbicacion(
    val id: String = "",
    val nombre: String = "",
    val ruta: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val activo: Boolean = true,
    val updatedAt: Long = 0L
)

class SyncManager {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Sincronizar un usuario local con Firestore (Escritura en nube optimizada para Free Tier)
    suspend fun syncUserToCloud(user: UserEntity) {
        withContext(Dispatchers.IO) {
            try {
                val userMap = mapOf(
                    "id" to user.id,
                    "username" to user.username,
                    "role" to user.role,
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

    // Transmitir ubicación GPS en tiempo real del conductor hacia Firestore (Free Tier)
    suspend fun broadcastConductorLocation(
        conductorId: String,
        nombre: String,
        ruta: String,
        lat: Double,
        lng: Double,
        activo: Boolean = true
    ) {
        withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "id" to conductorId,
                    "nombre" to nombre,
                    "ruta" to ruta,
                    "lat" to lat,
                    "lng" to lng,
                    "activo" to activo,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("conductores")
                    .document(conductorId)
                    .set(data)
                    .addOnSuccessListener {
                        Log.d("SyncManager", "Ubicación del conductor $nombre transmitida en tiempo real")
                    }
                    .addOnFailureListener { e ->
                        Log.e("SyncManager", "Error al transmitir ubicación del conductor", e)
                    }
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en broadcastConductorLocation", e)
            }
        }
    }

    // Consultar conductores activos en tiempo real para dibujarlos en el mapa de los usuarios
    suspend fun fetchConductoresActivos(onResult: (List<ConductorUbicacion>) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("conductores")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val lista = snapshot.documents.mapNotNull { doc ->
                            try {
                                val activo = doc.getBoolean("activo") ?: true
                                val lat = doc.getDouble("lat") ?: 0.0
                                val lng = doc.getDouble("lng") ?: 0.0
                                if (activo && lat != 0.0 && lng != 0.0) {
                                    ConductorUbicacion(
                                        id = doc.getString("id") ?: doc.id,
                                        nombre = doc.getString("nombre") ?: "Conductor",
                                        ruta = doc.getString("ruta") ?: "L1",
                                        lat = lat,
                                        lng = lng,
                                        activo = true,
                                        updatedAt = doc.getLong("updatedAt") ?: 0L
                                    )
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                        onResult(lista)
                    }
                    .addOnFailureListener { e ->
                        Log.e("SyncManager", "Error al consultar conductores activos", e)
                        onResult(emptyList())
                    }
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en fetchConductoresActivos", e)
                onResult(emptyList())
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

    // Obtener rutas frecuentes de la nube (Lectura única Free Tier)
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
