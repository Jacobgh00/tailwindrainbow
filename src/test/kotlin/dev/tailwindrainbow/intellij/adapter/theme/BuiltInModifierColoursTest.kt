package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SCOPING_MODIFIERS
import dev.tailwindrainbow.intellij.domain.theme.ThemeMatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BuiltInModifierColoursTest {
    private val builtIns =
        mapOf(
            BuiltInThemes.DEFAULT_NAME to BuiltInThemes.default,
            BuiltInThemes.SYNTHWAVE_NAME to BuiltInThemes.synthwave,
            BuiltInThemes.COLOUR_BLIND_NAME to BuiltInThemes.colourBlind,
        )

    @Test
    fun `every built-in theme colours every scoping modifier`() {
        builtIns.forEach { (name, theme) ->
            SCOPING_MODIFIERS.forEach { modifier ->
                assertNotNull(theme.prefix[modifier], "$name has no colour for '$modifier'")
            }
        }
    }

    @Test
    fun `a scoped variant no longer looks exactly like the plain one`() {
        builtIns.forEach { (name, theme) ->
            val matcher = matcherFor(theme)
            val parts = matcher.matchPrefixParts("group-hover")
            val modifier = parts.modifiers.singleOrNull()?.match
            val variant = parts.variant

            assertNotNull(modifier, "$name does not mark group- at all")
            assertNotNull(variant, "$name stopped colouring the variant inside group-")
            assertTrue(
                modifier.style.color != variant.style.color,
                "$name paints group- and hover the same colour: ${variant.style.color}",
            )
        }
    }

    @Test
    fun `the variant inside a scoped prefix keeps the colour it has on its own`() {
        builtIns.forEach { (name, theme) ->
            val matcher = matcherFor(theme)

            assertEquals(
                matcher.matchPrefix("hover")?.style,
                matcher.matchPrefix("group-hover")?.style,
                "$name changed the hover colour when it was scoped",
            )
        }
    }

    private fun matcherFor(theme: RainbowTheme) = ThemeMatcher(theme)
}
