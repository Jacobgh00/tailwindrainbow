package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.application.highlight.ScanStatus

fun interface ScanLog {
    fun skipped(
        fileExtension: String,
        status: ScanStatus,
    )

    companion object {
        val NONE = ScanLog { _, _ -> }
    }
}
