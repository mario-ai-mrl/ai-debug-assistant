package com.example.aidebug.context

object TraceIdExtractor {
    private val patterns = listOf(
        Regex("\\btrace[-_ ]?id[=: ]+([A-Za-z0-9._-]+)", RegexOption.IGNORE_CASE),
        Regex("\\brequest[-_ ]?id[=: ]+([A-Za-z0-9._-]+)", RegexOption.IGNORE_CASE),
        Regex("\\bcorrelation[-_ ]?id[=: ]+([A-Za-z0-9._-]+)", RegexOption.IGNORE_CASE)
    )

    fun extract(text: String): List<String> = patterns
        .flatMap { pattern -> pattern.findAll(text).map { it.groupValues[1] }.toList() }
        .filter { it.length >= 4 }
        .distinct()
        .take(10)
}
