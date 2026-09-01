package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.application.port.Cancellation
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import dev.tailwindrainbow.intellij.domain.theme.ThemeMatcher

data class UncolouredVariant(
    val name: String,
    val start: Int,
    val end: Int,
)

class UncolouredVariants(
    private val settings: ScanSettings,
    private val theme: RainbowTheme,
    private val declared: Set<String>,
    private val cancellation: Cancellation = Cancellation.NONE,
) {
    fun inside(
        text: String,
        fileExtension: String,
    ): List<UncolouredVariant> {
        val matcher = ThemeMatcher(theme)
        val uncoloured = declared.filter { matcher.matchPrefix(it) == null }

        if (uncoloured.isEmpty()) {
            return emptyList()
        }

        val probe = RainbowTheme(prefix = uncoloured.associateWith { PROBE })

        return TailwindDocumentScanner()
            .scan(text, fileExtension, settings, probe, cancellation)
            .asSequence()
            .filter { it.kind == SegmentKind.PREFIX && it.themeKey in uncoloured }
            .map { UncolouredVariant(it.themeKey, it.matchStart, it.matchEnd) }
            .distinct()
            .toList()
    }

    private companion object {
        val PROBE = TextStyle("#000000", FontWeight.NORMAL)
    }
}
