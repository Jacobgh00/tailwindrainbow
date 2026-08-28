package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec

/**
 * What the plugin looks at and how far: the fields a project may keep its own answers to.
 *
 * Every field is a String because a text field can hold anything; `maxFileSize` in particular may
 * be empty, negative, or not a number at all.
 */
data class RecognitionForm(
    val maxFileSize: String,
    val classIdentifiers: String,
    val classFunctions: String,
    val templateTags: String,
    val ignoredPrefixModifiers: String,
    val supportedExtensions: String,
)

/**
 * The settings screen as text.
 *
 * [projectRecognition] is null while the project follows the application's rules. Both sides are
 * carried at once because the screen edits one of them while the other waits: turning the project's
 * rules on must not overwrite what the user set globally.
 *
 * Keeping this separate from [HighlightSettings] is what lets the view stay dumb and the validation
 * stay pure.
 */
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

/**
 * Pure on purpose: the only part of the settings screen with real logic, unit-tested without
 * constructing a single Swing component.
 */
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

    private const val SIZE_MESSAGE = "Maximum file size must be a whole number greater than zero"

    private fun RecognitionForm.toScanSettings(): ScanSettings? {
        val size = maxFileSize.trim().toIntOrNull()?.takeIf { it > 0 } ?: return null

        return ScanSettings(
            maxFileSize = size,
            classIdentifiers = classIdentifiers.toValues(),
            classFunctions = classFunctions.toValues(),
            templateTags = templateTags.toValues(),
            ignoredPrefixModifiers = ignoredPrefixModifiers.toValues(),
            supportedExtensions = supportedExtensions.toValues().map(String::lowercase).toSet(),
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

    private fun Set<String>.toText(): String = joinToString(", ")
}
