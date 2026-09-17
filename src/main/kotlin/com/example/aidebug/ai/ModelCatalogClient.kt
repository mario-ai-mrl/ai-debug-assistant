package com.example.aidebug.ai

import com.google.gson.JsonParser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ModelCatalogClient(
    private val provider: AiProviderPreset,
    private val baseUrl: String,
    private val apiKey: String
) {
    private val client = HttpClient.newHttpClient()

    fun fetchModels(): List<String> {
        val endpoint = when (provider) {
            AiProviderPreset.DEEPSEEK -> "/models"
            AiProviderPreset.OLLAMA -> "/api/tags"
            else -> "/v1/models"
        }
        val requestUrl = baseUrl.trim().trimEnd('/') + endpoint
        val requestBuilder = HttpRequest.newBuilder(URI.create(requestUrl)).GET()
        if (apiKey.isNotBlank() && provider != AiProviderPreset.OLLAMA) {
            requestBuilder.header("Authorization", "Bearer ${apiKey.trim()}")
        }
        val response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            error(
                "模型查询失败：HTTP ${response.statusCode()}，" +
                    " endpoint=$requestUrl，apiKeyPresent=${apiKey.isNotBlank()}"
            )
        }
        val root = JsonParser.parseString(response.body()).asJsonObject
        val array = root[if (provider == AiProviderPreset.OLLAMA) "models" else "data"]?.asJsonArray
            ?: return emptyList()
        return array.mapNotNull { item ->
            val obj = item.asJsonObject
            obj["id"]?.asString ?: obj["name"]?.asString
        }.distinct().sorted()
    }
}
