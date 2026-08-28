package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings

fun HighlightSettings.withProjectRecognition(projectScan: ScanSettings?): HighlightSettings =
    if (projectScan == null) this else copy(scan = projectScan)
