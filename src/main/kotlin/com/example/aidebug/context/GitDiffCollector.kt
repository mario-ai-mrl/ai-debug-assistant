package com.example.aidebug.context

import com.intellij.openapi.project.Project
import java.io.File
import java.util.concurrent.TimeUnit

object GitDiffCollector {
    fun collect(project: Project): String {
        val basePath = project.basePath ?: return ""
        return try {
            val process = ProcessBuilder("git", "diff", "--no-ext-diff")
                .directory(File(basePath))
                .redirectErrorStream(true)
                .start()
            val finished = process.waitFor(5, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return ""
            }
            if (process.exitValue() != 0) return ""
            process.inputStream.bufferedReader().use { it.readText() }.take(60_000)
        } catch (_: Exception) {
            ""
        }
    }
}
