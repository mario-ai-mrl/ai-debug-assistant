package com.example.aidebug.ui

import com.example.aidebug.ai.AiClient
import com.example.aidebug.context.StackTraceContextCollector
import com.example.aidebug.context.GitDiffCollector
import com.example.aidebug.context.TraceIdExtractor
import com.example.aidebug.loki.LokiClient
import com.example.aidebug.model.DebugContext
import com.example.aidebug.settings.DebugSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.*

class DebugToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val problem = JTextArea(5, 60).apply { border = BorderFactory.createTitledBorder("问题描述 / 异常堆栈") }
        val query = JTextField(project.getService(DebugSettings::class.java).current().lokiQuery).apply {
            border = BorderFactory.createTitledBorder("Loki LogQL（可选）")
        }
        val result = JTextArea().apply { isEditable = false; lineWrap = true; wrapStyleWord = true }
        val analyze = JButton("开始分析")
        val openLocation = JButton("跳转到首个堆栈位置").apply { isEnabled = false }
        val copyResult = JButton("复制结果").apply { isEnabled = false }
        val generatePatch = JButton("生成修复补丁").apply { isEnabled = false }
        var firstLocation: StackTraceContextCollector.Location? = null
        var lastContext: DebugContext? = null

        openLocation.addActionListener {
            val location = firstLocation ?: return@addActionListener
            OpenFileDescriptor(project, location.file, location.line - 1, 0).navigate(true)
        }
        copyResult.addActionListener {
            Toolkit.getDefaultToolkit().systemClipboard
                .setContents(StringSelection(result.text), null)
        }
        generatePatch.addActionListener {
            val context = lastContext ?: return@addActionListener
            generatePatch.isEnabled = false
            result.text = result.text + "\n\n正在生成修复补丁..."
            val settings = project.getService(DebugSettings::class.java).current()
            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val patch = AiClient(settings).generatePatch(context)
                    SwingUtilities.invokeLater {
                        result.text = result.text + "\n\n--- 修复补丁 ---\n" + patch
                        copyResult.isEnabled = true
                        if (patch.trim() != "NO_SAFE_PATCH") {
                            PatchPreviewDialog(project, patch).show()
                        }
                    }
                } catch (e: Exception) {
                    SwingUtilities.invokeLater { result.text = result.text + "\n\n补丁生成失败：${e.message}" }
                } finally {
                    SwingUtilities.invokeLater { generatePatch.isEnabled = true }
                }
            }
        }

        analyze.addActionListener {
            analyze.isEnabled = false
            result.text = "正在收集代码、查询日志并请求 AI..."
            val settings = project.getService(DebugSettings::class.java).current()
            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val editor = FileEditorManager.getInstance(project).selectedTextEditor
                    val psiFile = editor?.document?.let {
                        com.intellij.psi.PsiDocumentManager.getInstance(project).getPsiFile(it)
                    }
                    val context = DebugContext(
                        problem = problem.text,
                        fileName = psiFile?.virtualFile?.path.orEmpty(),
                        code = buildString {
                            append(editor?.selectionModel?.selectedText
                                ?: psiFile?.text?.take(80_000).orEmpty())
                            val stackCode = StackTraceContextCollector.collect(project, problem.text)
                            if (stackCode.isNotBlank()) {
                                append("\n\n--- 堆栈定位代码 ---\n").append(stackCode)
                            }
                        },
                        stackTrace = problem.text,
                        logs = run {
                            val ids = TraceIdExtractor.extract(problem.text)
                            val baseQuery = query.text.ifBlank { settings.lokiQuery }
                            val enrichedQuery = if (baseQuery.isNotBlank() && ids.isNotEmpty()) {
                                val alternatives = ids.take(3).joinToString("|") { Regex.escape(it) }
                                "$baseQuery |~ \"$alternatives\""
                            } else baseQuery
                            LokiClient(settings).queryRange(enrichedQuery)
                        },
                        gitDiff = GitDiffCollector.collect(project),
                        traceIds = TraceIdExtractor.extract(problem.text)
                    )
                    lastContext = context
                    val location = StackTraceContextCollector.firstLocation(project, problem.text)
                    val answer = AiClient(settings).analyze(context)
                    SwingUtilities.invokeLater {
                        firstLocation = location
                        openLocation.isEnabled = location != null
                        copyResult.isEnabled = true
                        generatePatch.isEnabled = true
                        result.text = answer
                    }
                } catch (e: Exception) {
                    SwingUtilities.invokeLater { result.text = "分析失败：${e.message}" }
                } finally {
                    SwingUtilities.invokeLater { analyze.isEnabled = true }
                }
            }
        }

        val top = JPanel(BorderLayout()).apply {
            add(JScrollPane(problem), BorderLayout.CENTER)
            add(query, BorderLayout.SOUTH)
        }
        val actions = JPanel().apply {
            add(analyze)
            add(openLocation)
            add(copyResult)
            add(generatePatch)
        }
        val root = JPanel(BorderLayout()).apply {
            add(top, BorderLayout.NORTH)
            add(JScrollPane(result), BorderLayout.CENTER)
            add(actions, BorderLayout.SOUTH)
        }
        toolWindow.contentManager.addContent(ContentFactory.getInstance().createContent(root, "", false))
    }
}
