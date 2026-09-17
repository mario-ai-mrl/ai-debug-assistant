package com.example.aidebug.settings

import com.example.aidebug.ai.AiProviderPreset
import com.example.aidebug.ai.ModelCatalogClient
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JOptionPane

class DebugSettingsConfigurable(private val project: Project) : Configurable {
    private val settings get() = project.getService(DebugSettings::class.java)
    private var panel: DialogPanel? = null
    private var aiBaseUrl = ""
    private var aiApiKey = ""
    private var aiModel = ""
    private var lokiBaseUrl = ""
    private var lokiToken = ""
    private var lokiQuery = ""
    private val providerBox = JComboBox(AiProviderPreset.values())
    private val modelBox = JComboBox<String>()

    override fun getDisplayName(): String = "AI Debug Assistant"

    override fun createComponent(): JComponent {
        val current = settings.current()
        aiBaseUrl = current.aiBaseUrl
        aiApiKey = current.aiApiKey
        aiModel = current.aiModel
        lokiBaseUrl = current.lokiBaseUrl
        lokiToken = current.lokiToken
        lokiQuery = current.lokiQuery
        providerBox.selectedItem = runCatching {
            AiProviderPreset.valueOf(current.aiProvider)
        }.getOrDefault(AiProviderPreset.DEEPSEEK)
        modelBox.isEditable = true
        modelBox.removeAllItems()
        modelBox.addItem(aiModel)
        modelBox.selectedItem = aiModel
        providerBox.addActionListener {
            val provider = providerBox.selectedItem as? AiProviderPreset ?: return@addActionListener
            if (provider != AiProviderPreset.CUSTOM) {
                aiBaseUrl = provider.defaultBaseUrl
                aiModel = provider.defaultModel
                modelBox.removeAllItems()
                modelBox.addItem(aiModel)
                modelBox.selectedItem = aiModel
            }
        }

        return panel {
            group("AI Provider") {
                row("Provider:") {
                    cell(providerBox)
                    button("刷新模型") {
                        val provider = providerBox.selectedItem as? AiProviderPreset ?: return@button
                        // The refresh button can be used before the user presses Apply.
                        // Commit the form first so the freshly typed key is available.
                        this@DebugSettingsConfigurable.apply()
                        val liveSettings = settings.current()
                        val selectedModel = modelBox.selectedItem?.toString().orEmpty()
                        Thread {
                            try {
                                val models = ModelCatalogClient(
                                    provider,
                                    liveSettings.aiBaseUrl,
                                    liveSettings.aiApiKey
                                ).fetchModels()
                                javax.swing.SwingUtilities.invokeLater {
                                    modelBox.removeAllItems()
                                    models.forEach(modelBox::addItem)
                                    modelBox.selectedItem = selectedModel.ifBlank { models.firstOrNull().orEmpty() }
                                }
                            } catch (error: Exception) {
                                javax.swing.SwingUtilities.invokeLater {
                                    JOptionPane.showMessageDialog(
                                        null,
                                        error.message ?: "无法查询模型",
                                        "AI Debug Assistant",
                                        JOptionPane.ERROR_MESSAGE
                                    )
                                }
                            }
                        }.start()
                    }
                }
                row("Base URL:") {
                    textField().bindText(::aiBaseUrl).align(Align.FILL)
                }
                row("Model:") {
                    cell(modelBox).align(Align.FILL)
                }
                row("API Key:") {
                    passwordField().bindText(::aiApiKey).align(Align.FILL)
                }
            }
            group("Loki") {
                row("Base URL:") {
                    textField().bindText(::lokiBaseUrl).align(Align.FILL)
                }
                row("Bearer Token:") {
                    passwordField().bindText(::lokiToken).align(Align.FILL)
                }
                row("Default LogQL:") {
                    textField().bindText(::lokiQuery).align(Align.FILL)
                }
            }
        }.also { panel = it }
    }

    override fun isModified(): Boolean {
        if (panel?.isModified() == true) return true
        val current = settings.current()
        val selectedModel = modelBox.selectedItem?.toString().orEmpty()
        return current.aiProvider != (providerBox.selectedItem as? AiProviderPreset)?.name ||
            current.aiBaseUrl != aiBaseUrl || current.aiApiKey != aiApiKey ||
            current.aiModel != selectedModel || current.lokiBaseUrl != lokiBaseUrl ||
            current.lokiToken != lokiToken || current.lokiQuery != lokiQuery
    }

    override fun apply() {
        // Commit UI DSL bindings before reading the field values. Without this,
        // the API key typed into the password field remains only in the widget.
        panel?.apply()
        val current = settings.getState()
        current.aiProvider = (providerBox.selectedItem as? AiProviderPreset)?.name ?: AiProviderPreset.CUSTOM.name
        current.aiBaseUrl = aiBaseUrl.trim()
        current.aiModel = modelBox.selectedItem?.toString()?.trim().orEmpty()
        current.lokiBaseUrl = lokiBaseUrl.trim()
        current.lokiQuery = lokiQuery.trim()
        settings.saveSecrets(aiApiKey, lokiToken)
    }

    override fun reset() {
        panel?.reset()
    }

    override fun disposeUIResources() {
        panel = null
    }
}
