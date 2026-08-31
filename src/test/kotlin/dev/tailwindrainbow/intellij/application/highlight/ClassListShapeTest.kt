package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import dev.tailwindrainbow.intellij.domain.theme.ThemeMatcher
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassListShapeTest {
    private val shape =
        ClassListShape(
            ThemeMatcher(
                BuiltInThemes.themes().getValue(BuiltInThemes.DEFAULT_NAME),
            ),
        )

    private fun reads(content: String) = shape.readsAsClassList(content)

    @Test
    fun `reads a class list that carries a variant`() {
        assertTrue(reads("w-full lg:px-1 md:max-w-prose md:mx-auto"))
        assertTrue(reads("w-full md:max-w-1/2"))
        assertTrue(reads("hover:bg-blue-500"))
        assertTrue(reads("md:block hidden"))
        assertTrue(reads("dark:bg-gray-800 flex items-center"))
    }

    @Test
    fun `reads the shapes Tailwind allows around a variant`() {
        assertTrue(reads("[&>*]:mt-2"), "an arbitrary variant")
        assertTrue(reads("!font-bold md:!p-2"), "the important modifier in either position")
        assertTrue(reads("group-hover:underline"), "a scoping prefix modifier")
        assertTrue(reads("data-[state=open]:bg-white"), "an attribute variant")
    }

    @Test
    fun `a string with no variant is not a class list`() {
        assertFalse(reads("mx-auto"), "nothing to colour, so nothing to claim")
        assertFalse(reads("w-full flex"))
        assertFalse(reads(""))
        assertFalse(
            reads("[mask-type:luminance] w-full"),
            "utilities whose only colon is bracketed carry no variant",
        )
    }

    @Test
    fun `prose is not a class list even when it names a variant`() {
        assertFalse(reads("see hover:bg-blue-500 for details"))
        assertFalse(reads("Verhalten: hover:aktiv"))
        assertFalse(reads("hover:aktiv"), "a known variant over a plain word is not a utility")
        assertFalse(reads("use md:max-w-prose here"))
    }

    @Test
    fun `punctuation that is not a variant is not a class list`() {
        assertFalse(reads("https://example.com:8080/path"))
        assertFalse(reads("10:30"))
        assertFalse(reads("user:profile:title"))
        assertFalse(reads("C:/Users/x"))
        assertFalse(reads("unknownvariant:px-2"), "the prefix has to be one the theme knows")
    }

    @Test
    fun `a malformed word disqualifies the whole string`() {
        assertFalse(reads(":px-2"))
        assertFalse(reads("md:"))
        assertFalse(reads("md::px-2"))
    }

    @Test
    fun `the fallback gives up on anything too large to be a class list`() {
        assertFalse(reads("hover:p-1 ".repeat(100)), "more words than a class list has")
        assertFalse(reads("hover:" + "a-".repeat(400)), "longer than a class list runs")
    }
}
