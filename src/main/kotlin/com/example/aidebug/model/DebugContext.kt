package com.example.aidebug.model

data class DebugContext(
    val problem: String,
    val fileName: String = "",
    val code: String = "",
    val stackTrace: String = "",
    val logs: String = "",
    val gitDiff: String = "",
    val traceIds: List<String> = emptyList()
)
