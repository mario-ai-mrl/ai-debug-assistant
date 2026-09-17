package com.example.aidebug.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.example.aidebug.context.PatchApplyService
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities

class PatchPreviewDialog(
    project: Project,
    private val patch: String
) : DialogWrapper(project, true) {

    private val projectRef = project

    init {
        title = "AI 修复补丁预览"
        setOKButtonText("关闭")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val text = JBTextArea(patch).apply {
            isEditable = false
            lineWrap = false
            font = font.deriveFont(13f)
        }
        val apply = JButton("确认并应用补丁")
        apply.addActionListener {
            val answer = Messages.showYesNoDialog(
                projectRef,
                "应用后会修改工作区文件，是否继续？建议先确认 Git 工作区可回滚。",
                "确认应用修复补丁",
                Messages.getQuestionIcon()
            )
            if (answer != Messages.YES) return@addActionListener
            apply.isEnabled = false
            ApplicationManager.getApplication().executeOnPooledThread {
                val result = PatchApplyService.apply(projectRef, patch)
                SwingUtilities.invokeLater {
                    if (result.success) {
                        Messages.showInfoMessage(projectRef, result.message, "AI Debug Assistant")
                        close(OK_EXIT_CODE)
                    } else {
                        apply.isEnabled = true
                        Messages.showErrorDialog(projectRef, result.message, "补丁未应用")
                    }
                }
            }
        }
        return JPanel(BorderLayout()).apply {
            add(JBScrollPane(text).apply { preferredSize = Dimension(900, 560) }, BorderLayout.CENTER)
            add(apply, BorderLayout.SOUTH)
        }
    }
}
