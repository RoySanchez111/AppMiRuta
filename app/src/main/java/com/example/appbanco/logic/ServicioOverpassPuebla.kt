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
import kotlin.math.pow

class ServicioOverpassPuebla {
    companion object {
        private const val TAG = "ServicioOverpassPuebla"
        private const val OVERPASS_URL = "https://overpass-api.de/api/interpreter"
    }

    // Retorna un mapa: "Nombre de la Ruta" -> Lista continua de Puntos
    suspend fun obtenerRutasBusMap(): Map<String, List<Point>> = withContext(Dispatchers.IO) {
        val mapaRutas = mutableMapOf<String, List<Point>>()
        try {
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
                val wayMap = mutableMapOf<Long, List<Point>>()

                // 1. Nodos
                for (i in 0 until elements.length()) {
                    val el = elements.getJSONObject(i)
                    if (el.getString("type") == "node") {
                        nodeMap[el.getLong("id")] = Point.fromLngLat(el.getDouble("lon"), el.getDouble("lat"))
                    }
                }

                // 2. Ways (Segmentos de calle)
                for (i in 0 until elements.length()) {
                    val el = elements.getJSONObject(i)
                    if (el.getString("type") == "way") {
                        val nodesArray = el.getJSONArray("nodes")
                        val wayPts = mutableListOf<Point>()
                        for (j in 0 until nodesArray.length()) {
                            nodeMap[nodesArray.getLong(j)]?.let { wayPts.add(it) }
                        }
                        if (wayPts.size > 1) {
                            wayMap[el.getLong("id")] = wayPts
                        }
                    }
                }

                // 3. Relaciones (Rutas completas)
                for (i in 0 until elements.length()) {
                    val el = elements.getJSONObject(i)
                    if (el.getString("type") == "relation") {
                        val tags = el.optJSONObject("tags")
                        val name = tags?.optString("name") ?: tags?.optString("ref") ?: "Ruta ${el.getLong("id")}"
                        val members = el.optJSONArray("members")
                        
                        if (members != null) {
                            val segments = mutableListOf<List<Point>>()
                            for (j in 0 until members.length()) {
                                val member = members.getJSONObject(j)
                                if (member.getString("type") == "way") {
                                    val role = member.optString("role")
                                    // Ignorar plataformas, quedarnos con la geometría de la ruta
                                    if (role != "platform" && role != "stop") {
                                        wayMap[member.getLong("ref")]?.let { way ->
                                            segments.add(way)
                                        }
                                    }
                                }
                            }
                            
                            if (segments.isNotEmpty()) {
                                val connected = conectarSegmentos(segments)
                                mapaRutas[name] = connected
                            }
                        }
                    }
                }
            } else {
                Log.e(TAG, "Error en Overpass API: Código $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción consultando Overpass API", e)
        }
        return@withContext mapaRutas
    }
    
    // Algoritmo para empalmar segmentos de OpenStreetMap y crear una polyline continua
    private fun conectarSegmentos(segmentos: List<List<Point>>): List<Point> {
        if (segmentos.isEmpty()) return emptyList()
        val result = mutableListOf<Point>()
        val pool = segmentos.toMutableList()
        
        var current = pool.removeAt(0).toMutableList()
        result.addAll(current)
        
        while (pool.isNotEmpty()) {
            val endPoint = result.last()
            val startPoint = result.first()
            
            var matchedIdx = -1
            var attachToEnd = true
            var reverseSegment = false
            
            for (i in pool.indices) {
                val seg = pool[i]
                if (distanciaSq(endPoint, seg.first()) < 0.00000001) {
                    matchedIdx = i; attachToEnd = true; reverseSegment = false; break
                }
                if (distanciaSq(endPoint, seg.last()) < 0.00000001) {
                    matchedIdx = i; attachToEnd = true; reverseSegment = true; break
                }
                if (distanciaSq(startPoint, seg.last()) < 0.00000001) {
                    matchedIdx = i; attachToEnd = false; reverseSegment = false; break
                }
                if (distanciaSq(startPoint, seg.first()) < 0.00000001) {
                    matchedIdx = i; attachToEnd = false; reverseSegment = true; break
                }
            }
            
            if (matchedIdx != -1) {
                val match = pool.removeAt(matchedIdx)
                val toAdd = if (reverseSegment) match.reversed() else match
                
                if (attachToEnd) {
                    result.addAll(toAdd.drop(1))
                } else {
                    result.addAll(0, toAdd.dropLast(1))
                }
            } else {
                // Si hay una brecha, agregamos el siguiente segmento de todas formas para no estancarnos
                val next = pool.removeAt(0)
                result.addAll(next)
            }
        }
        return result
    }

    private fun distanciaSq(p1: Point, p2: Point): Double {
        return (p1.longitude() - p2.longitude()).pow(2) + (p1.latitude() - p2.latitude()).pow(2)
    }

    // Mantener la firma original por retrocompatibilidad temporal si es necesario
    suspend fun obtenerCoordenadasRutasBusPuebla(): List<List<Point>> {
        return obtenerRutasBusMap().values.toList()
    }
}