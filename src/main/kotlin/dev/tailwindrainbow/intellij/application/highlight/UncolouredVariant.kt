package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import dev.tailwindrainbow.intellij.domain.theme.ThemeMatcher

data class UncolouredVariant(
    val name: String,
    val start: Int,
    val end: Int,
)

fun uncolouredDeclaredVariants(
    text: String,
    fileExtension: String,
    settings: ScanSettings,
    theme: RainbowTheme,
    declared: Set<String>,
): List<UncolouredVariant> {
    val matcher = ThemeMatcher(theme, settings.ignoredPrefixModifiers)
    val uncoloured = declared.filter { matcher.matchPrefix(it) == null }

    if (uncoloured.isEmpty()) {
        return emptyList()
    }

    val probe = RainbowTheme(prefix = uncoloured.associateWith { PROBE })

    return TailwindDocumentScanner().scan(text, fileExtension, settings, probe).map {
        UncolouredVariant(it.themeKey, it.start, it.start + it.themeKey.length)
    }
}

private val PROBE = TextStyle("#000000", FontWeight.NORMAL)
