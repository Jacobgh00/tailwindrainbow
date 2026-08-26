package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings

/**
 * Settings exactly as typed, before anything is validated.
 *
 * Every field is a String because a text field can hold anything; `maxFileSize` in particular may
 * be empty, negative, or not a number at all. Keeping the raw shape separate from
 * [HighlightSettings] is what lets the view stay dumb and the validation stay pure.
 */
data class SettingsForm(
    val enabled: Boolean,
    val themeName: String,
    val maxFileSize: String,
    val classIdentifiers: String,
    val classFunctions: String,
    val templateTags: String,
    val ignoredPrefixModifiers: String,
    val supportedExtensions: String,
)

/** The outcome of validating a [SettingsForm]. */
sealed interface FormResult {
    data class Valid(val settings: HighlightSettings) : FormResult

    data class Invalid(val message: String) : FormResult
}

/**
 * Converts between what the user typed and what the highlighter needs.
 *
 * Pure on purpose: this is the only part of the settings screen with real logic, and it is
 * unit-tested without constructing a single Swing component.
 */
object SettingsFormMapper {
    fun toSettings(form: SettingsForm): FormResult {
        val maxFileSize = form.maxFileSize.trim().toIntOrNull()

        if (maxFileSize == null || maxFileSize <= 0) {
            return FormResult.Invalid("Maximum file size must be a whole number greater than zero")
        }

        return FormResult.Valid(
            HighlightSettings(
                enabled = form.enabled,
                themeName = form.themeName,
                scan = ScanSettings(
                    maxFileSize = maxFileSize,
                    classIdentifiers = form.classIdentifiers.toValues(),
                    classFunctions = form.classFunctions.toValues(),
                    templateTags = form.templateTags.toValues(),
                    ignoredPrefixModifiers = form.ignoredPrefixModifiers.toValues(),
                    supportedExtensions = form.supportedExtensions.toValues().map(String::lowercase).toSet(),
                ),
            ),
        )
    }

    fun toForm(settings: HighlightSettings): SettingsForm = SettingsForm(
        enabled = settings.enabled,
        themeName = settings.themeName,
        maxFileSize = settings.scan.maxFileSize.toString(),
        classIdentifiers = settings.scan.classIdentifiers.toText(),
        classFunctions = settings.scan.classFunctions.toText(),
        templateTags = settings.scan.templateTags.toText(),
        ignoredPrefixModifiers = settings.scan.ignoredPrefixModifiers.toText(),
        supportedExtensions = settings.scan.supportedExtensions.toText(),
    )

    private fun String.toValues(): Set<String> =
        split(',').map(String::trim).filter(String::isNotEmpty).toSet()

    private fun Set<String>.toText(): String = joinToString(", ")
}
