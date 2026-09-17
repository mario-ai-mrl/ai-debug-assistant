package com.example.aidebug.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.credentialStore.CredentialAttributes

@Service
@State(name = "AiDebugAssistantSettings", storages = [Storage("ai-debug-assistant.xml")])
class DebugSettings : PersistentStateComponent<DebugSettings.State> {
    data class State(
        var aiProvider: String = "DEEPSEEK",
        var aiBaseUrl: String = "https://api.deepseek.com",
        var aiApiKey: String = "",
        var aiModel: String = "deepseek-flash",
        var lokiBaseUrl: String = "",
        var lokiToken: String = "",
        var lokiQuery: String = ""
    )

    private var state = State()
    fun current(): State = state.copy(
        aiApiKey = secret("ai-api-key") ?: state.aiApiKey,
        lokiToken = secret("loki-token") ?: state.lokiToken
    )

    fun saveSecrets(aiApiKey: String, lokiToken: String) {
        PasswordSafe.instance.setPassword(CredentialAttributes(KEY_PREFIX + "ai-api-key"), aiApiKey)
        PasswordSafe.instance.setPassword(CredentialAttributes(KEY_PREFIX + "loki-token"), lokiToken)
        state.aiApiKey = ""
        state.lokiToken = ""
    }

    private fun secret(name: String): String? =
        PasswordSafe.instance.getPassword(CredentialAttributes(KEY_PREFIX + name))

    companion object {
        private const val KEY_PREFIX = "AI Debug Assistant:"
    }
    override fun getState(): State = state
    override fun loadState(state: State) { this.state = state }
}
