package com.example.aidebug.context

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

object StackTraceContextCollector {
    private val framePattern = Regex("at\\s+[\\w.$]+\\([^:()]+:(\\d+)\\)")

    data class Location(val file: VirtualFile, val line: Int)

    fun firstLocation(project: Project, stackTrace: String): Location? {
        val match = framePattern.find(stackTrace) ?: return null
        val fileName = match.value.substringAfterLast('(').substringBefore(':')
        val line = match.groupValues[1].toIntOrNull() ?: return null
        return findFile(project, fileName)?.let { Location(it, line) }
    }

    fun collect(project: Project, stackTrace: String): String {
        if (stackTrace.isBlank()) return ""
        return framePattern.findAll(stackTrace)
            .take(8)
            .mapNotNull { match ->
                val fileName = match.value.substringAfterLast('(').substringBefore(':')
                val line = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
                val file = findFile(project, fileName) ?: return@mapNotNull null
                snippet(file, line)
            }
            .joinToString("\n\n")
    }

    private fun findFile(project: Project, fileName: String): VirtualFile? =
        FilenameIndex.getVirtualFilesByName(
            fileName,
            GlobalSearchScope.projectScope(project)
        ).firstOrNull()

    private fun snippet(file: VirtualFile, line: Int): String {
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return ""
        val first = (line - 4).coerceAtLeast(1)
        val last = (line + 4).coerceAtMost(document.lineCount)
        return buildString {
            append("// ").append(file.path).append(":").append(line).append('\n')
            for (current in first..last) {
                val start = document.getLineStartOffset(current - 1)
                val end = document.getLineEndOffset(current - 1)
                val text = document.getText(com.intellij.openapi.util.TextRange(start, end))
                append(if (current == line) ">> " else "   ")
                    .append(current).append(": ").append(text).append('\n')
            }
        }
    }
}
