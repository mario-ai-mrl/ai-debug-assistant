package com.example.aidebug.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager

class AnalyzeSelectionAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val window = ToolWindowManager.getInstance(e.project ?: return).getToolWindow("AI Debug Assistant")
        window?.show()
    }
}
