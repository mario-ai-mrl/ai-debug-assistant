package com.example.aidebug.loki

import com.example.aidebug.settings.DebugSettings
import com.google.gson.JsonParser
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class LokiClient(private val settings: DebugSettings.State) {
    private val client = HttpClient.newHttpClient()

    fun queryRange(query: String, minutes: Long = 15): String {
        if (settings.lokiBaseUrl.isBlank() || query.isBlank()) return ""
        val end = System.currentTimeMillis() * 1_000_000
        val start = end - minutes * 60 * 1_000_000_000
        val url = settings.lokiBaseUrl.trimEnd('/') + "/loki/api/v1/query_range" +
            "?query=" + encode(query) + "&start=$start&end=$end&limit=200&direction=backward"

        val builder = HttpRequest.newBuilder(URI.create(url)).GET()
        if (settings.lokiToken.isNotBlank()) {
            builder.header("Authorization", "Bearer ${settings.lokiToken}")
        }
        val response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) error("Loki HTTP ${response.statusCode()}: ${response.body()}")
        return format(response.body())
    }

    private fun format(body: String): String {
        val data = JsonParser.parseString(body).asJsonObject["data"]?.asJsonObject ?: return body
        val streams = data["result"]?.asJsonArray ?: return body
        return buildString {
            streams.forEach { item ->
                val stream = item.asJsonObject
                val values = stream["values"]?.asJsonArray ?: return@forEach
                values.forEach { value ->
                    val pair = value.asJsonArray
                    append(pair[0].asString).append(" ").append(pair[1].asString).append('\n')
                }
            }
        }.take(50_000)
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
