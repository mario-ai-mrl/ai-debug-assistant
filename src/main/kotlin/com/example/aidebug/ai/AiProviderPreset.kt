package com.example.aidebug.ai

enum class AiProviderPreset(
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultModel: String
) {
    DEEPSEEK("DeepSeek", "https://api.deepseek.com", "deepseek-flash"),
    OPENAI("OpenAI", "https://api.openai.com", "gpt-4o-mini"),
    OLLAMA("Ollama (本地)", "http://localhost:11434", "llama3.2"),
    CUSTOM("Custom / OpenAI Compatible", "", "");

    override fun toString(): String = displayName
}
