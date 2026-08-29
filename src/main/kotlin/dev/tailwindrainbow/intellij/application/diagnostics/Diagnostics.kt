package dev.tailwindrainbow.intellij.application.diagnostics

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.highlight.ScanStatus
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.describe

data class ScannedFile(
    val extension: String,
    val status: ScanStatus,
)

data class Diagnostics(
    val pluginVersion: String,
    val ide: String,
    val settings: HighlightSettings,
    val recognitionFromProject: Boolean,
    val file: ScannedFile?,
    val themeProblems: List<ThemeProblem>,
)

fun Diagnostics.report(): String =
    buildString {
        appendLine("Tailwind Rainbow $pluginVersion")
        appendLine("IDE: $ide")
        appendLine("Colouring: ${if (settings.enabled) "on" else "off"}")
        appendLine("Theme: ${settings.themeName}")
        appendLine("File: ${file.described()}")
        appendLine("Recognition rules (${recognitionOwner()}):")
        settings.scan.rules().forEach { appendLine("  $it") }
        append(themeProblems.section())
    }

private fun Diagnostics.recognitionOwner(): String = if (recognitionFromProject) "from this project" else "from the IDE"

private fun ScannedFile?.described(): String = this?.let { ".$extension — ${status.described()}" } ?: "no file open"

private fun ScanStatus.described(): String =
    when (this) {
        ScanStatus.SCANNED -> "scanned"
        ScanStatus.DISABLED -> "not scanned, colouring is switched off"
        ScanStatus.NOT_SUPPORTED -> "not scanned, the extension is not in the supported list"
        ScanStatus.TOO_LARGE -> "not scanned, past the maximum file size"
    }

private fun ScanSettings.rules(): List<String> =
    listOf(
        "Maximum file size: $maxFileSize",
        "Class identifiers: ${classIdentifiers.listed()}",
        "Class functions: ${classFunctions.listed()}",
        "Template tags: ${templateTags.listed()}",
        "Ignored prefix modifiers: ${ignoredPrefixModifiers.listed()}",
        "Supported file extensions: ${supportedExtensions.listed()}",
    )

private fun List<ThemeProblem>.section(): String =
    if (isEmpty()) "Theme problems: none" else joinToString("\n", prefix = "Theme problems:\n") { "  ${it.describe()}" }

private fun Set<String>.listed(): String = sorted().joinToString(", ")
