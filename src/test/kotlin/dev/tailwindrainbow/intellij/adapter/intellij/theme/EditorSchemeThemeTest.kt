package dev.tailwindrainbow.intellij.adapter.intellij.theme

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.testFramework.junit5.TestApplication
import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import dev.tailwindrainbow.intellij.domain.theme.isHexColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestApplication
class EditorSchemeThemeTest {
    private val schemes = EditorColorsManager.getInstance().allSchemes

    @Test
    fun `it colours every token the declared themes colour`() {
        val theme = EditorSchemeThemes.themes().getValue(EditorSchemeThemes.NAME)

        assertEquals(BuiltInThemes.default.prefix.keys, theme.prefix.keys)
        assertNotNull(theme.arbitrary)
        assertNotNull(theme.important)
    }

    @Test
    fun `every colour it takes from the scheme is one the platform can decode`() {
        val theme = EditorSchemeThemes.themes().getValue(EditorSchemeThemes.NAME)
        val unreadable = theme.prefix.filterValues { !it.color.isHexColor() }

        assertTrue(unreadable.isEmpty(), "not colours: $unreadable")
    }

    @Test
    fun `a theme built from one scheme differs from one built from another`() {
        val (light, dark) = twoSchemes()

        assertTrue(
            EditorSchemeThemes.themeOf(light) != EditorSchemeThemes.themeOf(dark),
            "${light.name} and ${dark.name} produced the same palette",
        )
    }

    @Test
    fun `families keep colours of their own rather than collapsing onto the editor foreground`() {
        val theme = EditorSchemeThemes.themes().getValue(EditorSchemeThemes.NAME)
        val distinct = theme.prefix.values.map { it.color }.distinct()

        assertTrue(distinct.size >= FAMILIES, "only ${distinct.size} colours across the whole palette")
    }

    private fun twoSchemes(): Pair<EditorColorsScheme, EditorColorsScheme> {
        val byBackground = schemes.distinctBy { it.defaultBackground.rgb }

        assertTrue(byBackground.size >= 2, "the test application offers one scheme: ${schemes.map { it.name }}")

        return byBackground[0] to byBackground[1]
    }

    private companion object {
        const val FAMILIES = 8
    }
}
