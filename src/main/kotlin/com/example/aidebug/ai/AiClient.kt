package com.example.aidebug.ai

import com.example.aidebug.model.DebugContext
import com.example.aidebug.settings.DebugSettings
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class AiClient(private val settings: DebugSettings.State) {
    private val client = HttpClient.newHttpClient()

    fun analyze(context: DebugContext): String {
        require(settings.aiApiKey.isNotBlank()) { "请先配置 AI API Key" }
        val prompt = """
            你是一名资深软件工程师，请分析下面的 Bug。
            输出：最可能根因、代码/日志证据、涉及位置、修复建议、验证步骤。
            如果证据不足，请明确说明缺少什么信息。不要编造不存在的代码。

            问题描述：${context.problem}
            文件：${context.fileName}
            代码：
            ${context.code.take(80_000)}
            异常堆栈：
            ${context.stackTrace.take(30_000)}
            Loki 日志：
            ${context.logs.take(50_000)}
            Trace/Request IDs：${context.traceIds.joinToString(", ")}
            Git diff：
            ${context.gitDiff.take(60_000)}
        """.trimIndent()

        return request(prompt)
    }

    fun generatePatch(context: DebugContext): String {
        require(settings.aiApiKey.isNotBlank()) { "请先配置 AI API Key" }
        val prompt = """
            你是一名资深软件工程师。请根据下面的 Bug 上下文生成最小可行修复补丁。
            只输出标准 unified diff，不要 Markdown 代码围栏，不要修改无关代码。
            如果证据不足或无法安全生成补丁，只输出：NO_SAFE_PATCH

            问题描述：${context.problem}
            代码：
            ${context.code.take(80_000)}
            异常堆栈：
            ${context.stackTrace.take(30_000)}
            日志：
            ${context.logs.take(50_000)}
            Git diff：
            ${context.gitDiff.take(60_000)}
        """.trimIndent()

        return request(prompt)
    }

    private fun request(prompt: String): String {

        val message = JsonObject().apply {
            addProperty("role", "user")
            addProperty("content", prompt)
        }
        val body = JsonObject().apply {
            addProperty("model", settings.aiModel)
            add("messages", com.google.gson.JsonArray().apply { add(message) })
            addProperty("temperature", 0.1)
        }
        val provider = runCatching { AiProviderPreset.valueOf(settings.aiProvider) }
            .getOrDefault(AiProviderPreset.CUSTOM)
        val chatPath = if (provider == AiProviderPreset.DEEPSEEK) {
            "/chat/completions"
        } else {
            "/v1/chat/completions"
        }
        val request = HttpRequest.newBuilder(URI.create(settings.aiBaseUrl.trimEnd('/') + chatPath))
            .header("Authorization", "Bearer ${settings.aiApiKey}")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) error("AI HTTP ${response.statusCode()}: ${response.body()}")
        return JsonParser.parseString(response.body()).asJsonObject["choices"].asJsonArray[0]
            .asJsonObject["message"].asJsonObject["content"].asString
    }
}
