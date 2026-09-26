package com.example.appbanco.data.database

import android.util.Log
import com.google.android.gms.tasks.Tasks
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
                Tasks.await(
                    firestore.collection("users")
                        .document(user.username)
                        .set(userMap)
                )
                Log.d("SyncManager", "Usuario sincronizado con la nube exitosamente")
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en syncUserToCloud", e)
            }
        }
    }

    // Obtener datos de la nube para respaldo/sincronización (Lectura optimizada para Free Tier)
    suspend fun fetchUserFromCloud(username: String, onResult: (Map<String, Any>?) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val document = Tasks.await(
                    firestore.collection("users")
                        .document(username)
                        .get()
                )
                val data = if (document != null && document.exists()) document.data else null
                withContext(Dispatchers.Main) {
                    onResult(data)
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en fetchUserFromCloud", e)
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    // Transmitir ubicación GPS en tiempo real del conductor hacia Firestore (Solo Cuentas Autorizadas con rol Conductor)
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
                // Validación de cuenta autorizada
                val userDoc = try {
                    Tasks.await(firestore.collection("users").document(conductorId).get())
                } catch (e: Exception) { null }

                val rol = userDoc?.getString("role") ?: "conductor"
                if (rol != "conductor" && rol != "admin") {
                    Log.w("SyncManager", "Acceso denegado: El usuario $conductorId no está autorizado como conductor")
                    return@withContext
                }

                val data = mapOf(
                    "id" to conductorId,
                    "nombre" to nombre,
                    "ruta" to ruta,
                    "lat" to lat,
                    "lng" to lng,
                    "activo" to activo,
                    "updatedAt" to System.currentTimeMillis()
                )
                Tasks.await(
                    firestore.collection("conductores")
                        .document(conductorId)
                        .set(data)
                )
                Log.d("SyncManager", "Ubicación del conductor autorizado $nombre transmitida en tiempo real")
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en broadcastConductorLocation", e)
            }
        }
    }

    // Consultar conductores activos en tiempo real para dibujarlos en el mapa de los usuarios
    suspend fun fetchConductoresActivos(onResult: (List<ConductorUbicacion>) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = Tasks.await(firestore.collection("conductores").get())
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
                withContext(Dispatchers.Main) {
                    onResult(lista)
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en fetchConductoresActivos", e)
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
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
                Tasks.await(
                    firestore.collection("users").document(userId)
                        .collection("rutas_frecuentes").document(rutaNombre)
                        .set(data)
                )
                Log.d("SyncManager", "Ruta frecuente sincronizada en la nube")
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en syncRutaFrecuenteToCloud", e)
            }
        }
    }

    // Obtener rutas frecuentes de la nube (Lectura única Free Tier)
    suspend fun fetchRutasFrecuentesFromCloud(userId: String, onResult: (List<Map<String, Any>>) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val querySnapshot = Tasks.await(
                    firestore.collection("users").document(userId)
                        .collection("rutas_frecuentes")
                        .get()
                )
                val lista = querySnapshot.documents.mapNotNull { it.data }
                withContext(Dispatchers.Main) {
                    onResult(lista)
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en fetchRutasFrecuentesFromCloud", e)
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    // Sincronizar incidencia / alerta a la nube
    suspend fun syncIncidenciaToCloud(id: String, tipo: String, ruta: String, titulo: String, descripcion: String, tiempo: String) {
        withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "id" to id,
                    "tipo" to tipo,
                    "ruta" to ruta,
                    "titulo" to titulo,
                    "descripcion" to descripcion,
                    "tiempo" to tiempo,
                    "updatedAt" to System.currentTimeMillis()
                )
                Tasks.await(
                    firestore.collection("incidencias")
                        .document(id)
                        .set(data)
                )
                Log.d("SyncManager", "Incidencia sincronizada en la nube")
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en syncIncidenciaToCloud", e)
            }
        }
    }

    // Obtener incidencias de la nube
    suspend fun fetchIncidenciasFromCloud(onResult: (List<Map<String, Any>>) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val querySnapshot = Tasks.await(
                    firestore.collection("incidencias")
                        .get()
                )
                val lista = querySnapshot.documents.mapNotNull { it.data }
                withContext(Dispatchers.Main) {
                    onResult(lista)
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Excepción en fetchIncidenciasFromCloud", e)
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }
}
