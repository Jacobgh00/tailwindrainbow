package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.highlight.TailwindDocumentScanner
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import kotlin.test.Test
import kotlin.test.assertTrue

class TailwindVariantCoverageTest {
    private val documented =
        listOf(
            "hover", "focus", "focus-within", "focus-visible", "active", "visited", "target",
            "first", "last", "only", "odd", "even", "first-of-type", "last-of-type", "only-of-type",
            "empty", "disabled", "enabled", "checked", "indeterminate", "default", "optional",
            "required", "valid", "invalid", "user-valid", "user-invalid", "in-range", "out-of-range",
            "placeholder-shown", "details-content", "autofill", "read-only",
            "before", "after", "first-letter", "first-line", "marker", "selection", "file",
            "backdrop", "placeholder",
            "sm", "md", "lg", "xl", "2xl", "max-sm", "max-lg", "min-[400px]", "max-[600px]",
            "@sm", "@md", "@max-lg", "@[400px]",
            "dark", "motion-safe", "motion-reduce", "contrast-more", "contrast-less",
            "forced-colors", "inverted-colors", "pointer-fine", "pointer-coarse", "pointer-none",
            "any-pointer-fine", "any-pointer-coarse", "portrait", "landscape", "noscript", "print",
            "supports-[display:grid]", "starting",
            "aria-checked", "aria-[sort=ascending]", "data-open", "data-[state=open]",
            "rtl", "ltr", "open", "inert",
            "group-hover", "peer-focus", "has-checked", "not-hover", "in-focus",
            "nth-3", "nth-last-2", "nth-of-type-3", "nth-last-of-type-3", "*", "**",
            "group-has-checked", "peer-has-checked", "group-aria-expanded", "peer-aria-expanded",
            "not-forced-colors", "not-supports-[display:grid]", "any-pointer-none",
            "@3xs", "@7xl", "@min-[400px]", "@max-3xs", "@max-7xl",
            "[&:nth-child(3)]", "[&>*]",
        )

    private val scanner = TailwindDocumentScanner()

    @Test
    fun `every variant Tailwind documents is coloured, written the way it is written in code`() {
        BuiltInThemes.themes().forEach { (name, theme) ->
            val uncoloured = documented.filterNot { paints(it, theme) }

            assertTrue(uncoloured.isEmpty(), "$name colours nothing for: $uncoloured")
        }
    }

    private fun paints(
        variant: String,
        theme: RainbowTheme,
    ): Boolean {
        val source = """<div class="$variant:underline"></div>"""

        return scanner.scan(source, "html", ScanSettings(), theme).isNotEmpty()
    }
}
