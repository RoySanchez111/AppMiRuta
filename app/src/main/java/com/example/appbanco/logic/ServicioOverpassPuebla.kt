package com.example.appbanco.logic

import android.util.Log
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ServicioOverpassPuebla {
    companion object {
        private const val TAG = "ServicioOverpassPuebla"
        // Endpoint público oficial de Overpass API (OpenStreetMap) - 100% Gratuito y sin requerir API Key
        private const val OVERPASS_URL = "https://overpass-api.de/api/interpreter"
    }

    suspend fun obtenerCoordenadasRutasBusPuebla(): List<List<Point>> = withContext(Dispatchers.IO) {
        val rutasCompletas = mutableListOf<List<Point>>()
        try {
            // Consulta Overpass QL optimizada para rutas de autobús (route=bus) en el área metropolitana de Puebla
            val query = """
                [out:json][timeout:25];
                (
                  relation["route"="bus"](18.95,-98.28,19.10,-98.12);
                );
                out body;
                >;
                out skel qt;
            """.trimIndent()

            val url = URL(OVERPASS_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
            connection.connectTimeout = 12000
            connection.readTimeout = 12000

            connection.outputStream.use { os ->
                os.write(query.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonRoot = JSONObject(response.toString())
                val elements = jsonRoot.getJSONArray("elements")

                val nodeMap = mutableMapOf<Long, Point>()
                for (i in 0 until elements.length()) {
                    val el = elements.getJSONObject(i)
                    if (el.getString("type") == "node") {
                        val id = el.getLong("id")
                        val lat = el.getDouble("lat")
                        val lon = el.getDouble("lon")
                        nodeMap[id] = Point.fromLngLat(lon, lat)
                    }
                }

                for (i in 0 until elements.length()) {
                    val el = elements.getJSONObject(i)
                    if (el.getString("type") == "way") {
                        val nodesArray = el.getJSONArray("nodes")
                        val routePoints = mutableListOf<Point>()
                        for (j in 0 until nodesArray.length()) {
                            val nodeId = nodesArray.getLong(j)
                            nodeMap[nodeId]?.let { routePoints.add(it) }
                        }
                        if (routePoints.size > 1) {
                            rutasCompletas.add(routePoints)
                        }
                    }
                }
            } else {
                Log.e(TAG, "Error en Overpass API: Código $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción consultando Overpass API para Puebla", e)
        }
        return@withContext rutasCompletas
    }
}
