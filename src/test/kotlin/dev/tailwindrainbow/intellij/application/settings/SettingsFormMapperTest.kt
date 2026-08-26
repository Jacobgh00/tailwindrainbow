package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Validation used to live inside the Swing class, where reaching it meant constructing widgets.
 * It is ordinary logic and now tested as such.
 */
class SettingsFormMapperTest {
    @Test
    fun `a comma separated list becomes a set, trimmed`() {
        val result = SettingsFormMapper.toSettings(form(classFunctions = " cn , clsx ,, cva "))

        assertIs<FormResult.Valid>(result)
        assertEquals(setOf("cn", "clsx", "cva"), result.settings.scan.classFunctions)
    }

    @Test
    fun `extensions are lowercased so HTML and html are the same setting`() {
        val result = SettingsFormMapper.toSettings(form(supportedExtensions = "HTML, Vue, tsx"))

        assertIs<FormResult.Valid>(result)
        assertEquals(setOf("html", "vue", "tsx"), result.settings.scan.supportedExtensions)
    }

    @Test
    fun `a non numeric size is rejected with a message, not an exception`() {
        val result = SettingsFormMapper.toSettings(form(maxFileSize = "lots"))

        assertIs<FormResult.Invalid>(result)
        assertTrue(result.message.contains("greater than zero"))
    }

    @Test
    fun `zero and negative sizes are rejected`() {
        assertIs<FormResult.Invalid>(SettingsFormMapper.toSettings(form(maxFileSize = "0")))
        assertIs<FormResult.Invalid>(SettingsFormMapper.toSettings(form(maxFileSize = "-1")))
    }

    @Test
    fun `an empty size field is rejected rather than defaulting silently`() {
        assertIs<FormResult.Invalid>(SettingsFormMapper.toSettings(form(maxFileSize = "")))
    }

    @Test
    fun `settings survive a round trip through the form`() {
        val original = HighlightSettings(
            enabled = false,
            themeName = "synthwave",
            scan = ScanSettings(maxFileSize = 4096),
        )

        val result = SettingsFormMapper.toSettings(SettingsFormMapper.toForm(original))

        assertIs<FormResult.Valid>(result)
        assertEquals(original, result.settings)
    }

    private fun form(
        maxFileSize: String = "1000",
        classFunctions: String = "cn",
        supportedExtensions: String = "html",
    ) = SettingsForm(
        enabled = true,
        themeName = "default",
        maxFileSize = maxFileSize,
        classIdentifiers = "class",
        classFunctions = classFunctions,
        templateTags = "tw",
        ignoredPrefixModifiers = "group",
        supportedExtensions = supportedExtensions,
    )
}
