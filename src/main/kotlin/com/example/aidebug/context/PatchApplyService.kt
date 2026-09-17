package com.example.aidebug.context

import com.intellij.openapi.project.Project
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

object PatchApplyService {
    data class Result(val success: Boolean, val message: String)

    fun apply(project: Project, patch: String): Result {
        val basePath = project.basePath ?: return Result(false, "当前项目没有本地路径")
        val normalized = patch
            .replace(Regex("^```(?:diff)?\\s*", RegexOption.MULTILINE), "")
            .replace(Regex("```\\s*$", RegexOption.MULTILINE), "")
            .trim()
        if (!normalized.startsWith("diff --git") && !normalized.startsWith("--- ")) {
            return Result(false, "不是可应用的 unified diff")
        }

        return runGit(basePath, "apply", "--check", input = normalized)
            .takeIf { it.success }
            ?.let { runGit(basePath, "apply", "--whitespace=nowarn", input = normalized) }
            ?: Result(false, "补丁预检失败，未修改文件")
    }

    private fun runGit(basePath: String, vararg args: String, input: String): Result {
        return try {
            val process = ProcessBuilder(listOf("git") + args)
                .directory(File(basePath))
                .redirectErrorStream(true)
                .start()
            process.outputStream.use { it.write(input.toByteArray(StandardCharsets.UTF_8)) }
            val finished = process.waitFor(8, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return Result(false, "git apply 超时")
            }
            val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
            if (process.exitValue() == 0) Result(true, "补丁已应用")
            else Result(false, output.ifBlank { "git apply 执行失败" })
        } catch (e: Exception) {
            Result(false, e.message ?: "无法执行 git apply")
        }
    }
}
