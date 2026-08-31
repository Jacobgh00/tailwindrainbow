package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

sealed interface ThemeHealthLayer {
    val provenance: ThemeEntryProvenance
    val sourceName: String

    data class ResolvedThemes(
        val themes: Map<String, RainbowTheme>,
        override val provenance: ThemeEntryProvenance,
        override val sourceName: String,
    ) : ThemeHealthLayer

    data class Specifications(
        val specs: List<ThemeSpec>,
        override val provenance: ThemeEntryProvenance,
        override val sourceName: String,
    ) : ThemeHealthLayer
}
