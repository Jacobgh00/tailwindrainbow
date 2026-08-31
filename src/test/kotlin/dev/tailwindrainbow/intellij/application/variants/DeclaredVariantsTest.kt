package dev.tailwindrainbow.intellij.application.variants

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclaredVariantsTest {
    @Test
    fun `a v4 custom variant is declared in css`() {
        val css = "@custom-variant pointer-coarse (@media (pointer: coarse));"

        assertEquals(setOf("pointer-coarse"), variantsDeclaredIn(css))
    }

    @Test
    fun `a v4 breakpoint declares a responsive variant`() {
        val css =
            """
            @theme {
              --breakpoint-tablet: 40rem;
              --breakpoint-wide: 96rem;
              --color-brand: #0055ff;
            }
            """.trimIndent()

        assertEquals(setOf("tablet", "wide"), variantsDeclaredIn(css))
    }

    @Test
    fun `a v3 plugin variant is declared in the config`() {
        val config = """plugin(({ addVariant }) => addVariant('supports-grid', '@supports (display: grid)'))"""

        assertEquals(setOf("supports-grid"), variantsDeclaredIn(config))
    }

    @Test
    fun `v3 screens are variants, quoted or not`() {
        val config =
            """
            module.exports = {
              theme: {
                screens: { tablet: '640px', 'wide-screen': '1280px' },
                colors: { brand: '#0055ff' },
              },
            }
            """.trimIndent()

        assertEquals(setOf("tablet", "wide-screen"), variantsDeclaredIn(config))
    }

    @Test
    fun `a whole v4 stylesheet gives up every variant it declares`() {
        val css =
            """
            @import "tailwindcss";

            @theme {
              --breakpoint-tablet: 40rem;
              --color-brand: oklch(0.7 0.1 250);
            }

            @custom-variant pointer-coarse (@media (pointer: coarse));
            @custom-variant theme-midnight (&:where([data-theme="midnight"] *));

            @utility card {
              border-radius: 0.5rem;
            }
            """.trimIndent()

        assertEquals(setOf("tablet", "pointer-coarse", "theme-midnight"), variantsDeclaredIn(css))
    }

    @Test
    fun `a whole v3 config gives up every variant it declares`() {
        val config =
            """
            const plugin = require('tailwindcss/plugin')

            module.exports = {
              darkMode: 'class',
              theme: {
                screens: { tablet: '640px', desktop: '1280px' },
                extend: { colors: { brand: '#0055ff' } },
              },
              plugins: [
                plugin(function ({ addVariant }) {
                  addVariant('supports-grid', '@supports (display: grid)')
                  addVariant('optional', '&:optional')
                }),
              ],
            }
            """.trimIndent()

        assertEquals(
            setOf("tablet", "desktop", "supports-grid", "optional"),
            variantsDeclaredIn(config),
        )
    }

    @Test
    fun `text that declares nothing yields nothing`() {
        assertTrue(variantsDeclaredIn("const hover = 'nothing to see'").isEmpty())
        assertTrue(variantsDeclaredIn("").isEmpty())
    }

    @Test
    fun `a screen location points at the screen key rather than the whole block`() {
        val config = "theme: { screens: { tablet: '640px' } }"

        val declaration = variantDeclarationsIn(config, "tailwind.config.js").single()
        val location = checkNotNull(declaration.location)

        assertEquals(VariantDeclarationKind.SCREEN, declaration.kind)
        assertEquals("tablet", config.substring(location.startOffset, location.endOffset))
    }

    @Test
    fun `a screens block does not swallow what follows it`() {
        val config = "screens: { sm: '40rem' },\n fontFamily: { display: ['Inter'] }"

        assertEquals(setOf("sm"), variantsDeclaredIn(config))
    }
}
