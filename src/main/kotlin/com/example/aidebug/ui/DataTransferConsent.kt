package com.example.aidebug.ui

import com.example.aidebug.settings.DebugSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

object DataTransferConsent {
    fun confirm(project: Project, settings: DebugSettings.State, purpose: String): Boolean {
        val message = """
            即将向以下 AI 服务发送项目数据，用于$purpose：
            ${settings.aiBaseUrl}

            可能包含问题描述、当前文件或选中代码、堆栈附近代码、Git diff，
            以及从 ${settings.lokiBaseUrl.ifBlank { "未配置的 Loki" }} 查询到的日志。
            内容可能含有敏感信息。请先检查项目与服务的使用政策。

            是否同意本次发送？
        """.trimIndent()
        return Messages.showYesNoDialog(
            project,
            message,
            "确认发送调试数据",
            "同意并继续",
            "取消",
            Messages.getWarningIcon()
        ) == Messages.YES
    }
}
