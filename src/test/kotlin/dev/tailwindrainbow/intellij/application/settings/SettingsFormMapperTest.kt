package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

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
        val original =
            HighlightSettings(
                enabled = false,
                themeName = "synthwave",
                scan = ScanSettings(maxFileSize = 4096),
            )

        val result = SettingsFormMapper.toSettings(SettingsFormMapper.toForm(original))

        assertIs<FormResult.Valid>(result)
        assertEquals(original, result.settings)
    }

    @Test
    fun `user palettes survive the form and reach the result`() {
        val mine = ThemeSpec("default", listOf(StyleEntry(SegmentKind.PREFIX, "hover", "#abcdef", 700)))

        val result = SettingsFormMapper.toSettings(form().copy(themes = listOf(mine)))

        assertIs<FormResult.Valid>(result)
        assertEquals(listOf(mine), result.themes)
    }

    @Test
    fun `a palette with no overrides left is dropped rather than stored empty`() {
        val emptied = ThemeSpec("default", emptyList())

        val result = SettingsFormMapper.toSettings(form().copy(themes = listOf(emptied)))

        assertIs<FormResult.Valid>(result)
        assertTrue(result.themes.isEmpty())
    }

    @Test
    fun `a theme the user created is kept before it has any overrides`() {
        val fresh = ThemeSpec("midnight", emptyList(), basedOn = "synthwave")

        val result = SettingsFormMapper.toSettings(form().copy(themes = listOf(fresh)))

        assertIs<FormResult.Valid>(result)
        assertEquals(listOf(fresh), result.themes, "dropping it would delete the theme the user just made")
    }

    @Test
    fun `the project's own rules are read out separately from the user's`() {
        val theirs = recognition(classFunctions = "twcx", supportedExtensions = "templ")

        val result = SettingsFormMapper.toSettings(form(projectRecognition = theirs))

        assertIs<FormResult.Valid>(result)
        assertEquals(setOf("twcx"), result.projectScan?.classFunctions)
        assertEquals(setOf("cn"), result.settings.scan.classFunctions, "the user's own rules are left alone")
    }

    @Test
    fun `a project that follows the user stores nothing of its own`() {
        val result = SettingsFormMapper.toSettings(form())

        assertIs<FormResult.Valid>(result)
        assertEquals(null, result.projectScan)
    }

    @Test
    fun `a size the project cannot use is rejected just as the user's would be`() {
        val result = SettingsFormMapper.toSettings(form(projectRecognition = recognition(maxFileSize = "none")))

        assertIs<FormResult.Invalid>(result)
    }

    @Test
    fun `project rules round trip back into the form`() {
        val theirs = ScanSettings(classFunctions = setOf("twcx"))
        val settings = HighlightSettings(enabled = true, themeName = "default", scan = ScanSettings())

        val form = SettingsFormMapper.toForm(settings, projectScan = theirs)

        assertEquals("twcx", form.projectRecognition?.classFunctions)
    }

    @Test
    fun `palettes round trip back into the form`() {
        val mine = ThemeSpec("synthwave", listOf(StyleEntry(SegmentKind.ARBITRARY, "", "#101010", 400)))
        val settings = HighlightSettings(enabled = true, themeName = "synthwave", scan = ScanSettings())

        assertEquals(listOf(mine), SettingsFormMapper.toForm(settings, listOf(mine)).themes)
    }

    private fun form(
        maxFileSize: String = "1000",
        classFunctions: String = "cn",
        supportedExtensions: String = "html",
        projectRecognition: RecognitionForm? = null,
    ) = SettingsForm(
        enabled = true,
        themeName = "default",
        recognition = recognition(maxFileSize, classFunctions, supportedExtensions),
        projectRecognition = projectRecognition,
    )

    private fun recognition(
        maxFileSize: String = "1000",
        classFunctions: String = "cn",
        supportedExtensions: String = "html",
    ) = RecognitionForm(
        maxFileSize = maxFileSize,
        classIdentifiers = "class",
        classFunctions = classFunctions,
        templateTags = "tw",
        ignoredPrefixModifiers = "group",
        supportedExtensions = supportedExtensions,
    )
}
