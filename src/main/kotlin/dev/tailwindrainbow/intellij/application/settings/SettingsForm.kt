package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec

data class RecognitionForm(
    val maxFileSize: String,
    val classIdentifiers: String,
    val classFunctions: String,
    val templateTags: String,
    val ignoredPrefixModifiers: String,
    val supportedExtensions: String,
)

data class SettingsForm(
    val enabled: Boolean,
    val themeName: String,
    val recognition: RecognitionForm,
    val projectRecognition: RecognitionForm? = null,
    val themes: List<ThemeSpec> = emptyList(),
)

sealed interface FormResult {
    data class Valid(
        val settings: HighlightSettings,
        val themes: List<ThemeSpec>,
        val projectScan: ScanSettings?,
    ) : FormResult

    data class Invalid(val message: String) : FormResult
}

fun maxFileSizeProblem(text: String): String? = if (text.toFileSizeOrNull() == null) SIZE_MESSAGE else null

fun classIdentifiersWarning(text: String): String? =
    if (text.isBlank()) "Nothing will be recognized in attributes such as class=\"…\"" else null

private const val SIZE_MESSAGE = "Maximum file size must be a whole number greater than zero"

private fun String.toFileSizeOrNull(): Int? = trim().toIntOrNull()?.takeIf { it > 0 }

object SettingsFormMapper {
    fun toSettings(form: SettingsForm): FormResult {
        val scan = form.recognition.toScanSettings() ?: return FormResult.Invalid(SIZE_MESSAGE)
        val projectScan =
            form.projectRecognition?.let { it.toScanSettings() ?: return FormResult.Invalid(SIZE_MESSAGE) }

        return FormResult.Valid(
            settings = HighlightSettings(enabled = form.enabled, themeName = form.themeName, scan = scan),
            themes = form.themes.filterNot(ThemeSpec::isRedundant),
            projectScan = projectScan,
        )
    }

    fun toForm(
        settings: HighlightSettings,
        themes: List<ThemeSpec> = emptyList(),
        projectScan: ScanSettings? = null,
    ): SettingsForm =
        SettingsForm(
            enabled = settings.enabled,
            themeName = settings.themeName,
            recognition = settings.scan.toForm(),
            projectRecognition = projectScan?.toForm(),
            themes = themes,
        )

    private fun RecognitionForm.toScanSettings(): ScanSettings? {
        val size = maxFileSize.toFileSizeOrNull() ?: return null

        return ScanSettings(
            maxFileSize = size,
            classIdentifiers = classIdentifiers.toValues(),
            classFunctions = classFunctions.toValues(),
            templateTags = templateTags.toValues(),
            ignoredPrefixModifiers = ignoredPrefixModifiers.toValues(),
            supportedExtensions = supportedExtensions.toValues().map { it.toFileExtension() }.toSet(),
        )
    }

    private fun ScanSettings.toForm() =
        RecognitionForm(
            maxFileSize = maxFileSize.toString(),
            classIdentifiers = classIdentifiers.toText(),
            classFunctions = classFunctions.toText(),
            templateTags = templateTags.toText(),
            ignoredPrefixModifiers = ignoredPrefixModifiers.toText(),
            supportedExtensions = supportedExtensions.toText(),
        )

    private fun String.toValues(): Set<String> = split(',').map(String::trim).filter(String::isNotEmpty).toSet()

    private fun String.toFileExtension(): String = lowercase().removePrefix(".")

    private fun Set<String>.toText(): String = joinToString(", ")
}
