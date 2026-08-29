package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.application.port.HighlightSettings

enum class ScanStatus {
    DISABLED,
    NOT_SUPPORTED,
    TOO_LARGE,
    SCANNED,
}

fun ScanSettings.statusFor(
    fileExtension: String,
    textLength: Int,
): ScanStatus =
    when {
        fileExtension.lowercase() !in supportedExtensions -> ScanStatus.NOT_SUPPORTED
        textLength > maxFileSize -> ScanStatus.TOO_LARGE
        else -> ScanStatus.SCANNED
    }

fun HighlightSettings.statusFor(
    fileExtension: String,
    textLength: Int,
): ScanStatus = if (!enabled) ScanStatus.DISABLED else scan.statusFor(fileExtension, textLength)
