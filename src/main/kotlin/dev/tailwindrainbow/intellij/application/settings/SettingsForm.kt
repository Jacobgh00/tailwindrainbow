package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec

/**
 * Every field is a String because a text field can hold anything; `maxFileSize` in particular may
 * be empty, negative, or not a number at all. Keeping this separate from [HighlightSettings] is
 * what lets the view stay dumb and the validation stay pure.
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
    val themes: List<ThemeSpec> = emptyList(),
)

sealed interface FormResult {
    data class Valid(val settings: HighlightSettings, val themes: List<ThemeSpec>) : FormResult

    data class Invalid(val message: String) : FormResult
}

/**
 * Pure on purpose: the only part of the settings screen with real logic, unit-tested without
 * constructing a single Swing component.
 */
object SettingsFormMapper {
    fun toSettings(form: SettingsForm): FormResult {
        val maxFileSize = form.maxFileSize.trim().toIntOrNull()

        if (maxFileSize == null || maxFileSize <= 0) {
            return FormResult.Invalid("Maximum file size must be a whole number greater than zero")
        }

        return FormResult.Valid(
            themes = form.themes.filter { it.entries.isNotEmpty() },
            settings =
                HighlightSettings(
                    enabled = form.enabled,
                    themeName = form.themeName,
                    scan =
                        ScanSettings(
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

    fun toForm(
        settings: HighlightSettings,
        themes: List<ThemeSpec> = emptyList(),
    ): SettingsForm =
        SettingsForm(
            enabled = settings.enabled,
            themeName = settings.themeName,
            maxFileSize = settings.scan.maxFileSize.toString(),
            classIdentifiers = settings.scan.classIdentifiers.toText(),
            classFunctions = settings.scan.classFunctions.toText(),
            templateTags = settings.scan.templateTags.toText(),
            ignoredPrefixModifiers = settings.scan.ignoredPrefixModifiers.toText(),
            supportedExtensions = settings.scan.supportedExtensions.toText(),
            themes = themes,
        )

    private fun String.toValues(): Set<String> = split(',').map(String::trim).filter(String::isNotEmpty).toSet()

    private fun Set<String>.toText(): String = joinToString(", ")
}
