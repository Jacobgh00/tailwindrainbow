package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.highlight.TailwindClassParser
import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.ThemeMatcher

const val PREVIEW_SAMPLE = "hover:bg-blue-500 sm:hover:underline [&>*]:mt-2 font-bold! lg:text-xl"

fun previewSegments(
    theme: RainbowTheme,
    sample: String = PREVIEW_SAMPLE,
): List<HighlightSegment> {
    val matcher = ThemeMatcher(theme, ScanSettings().ignoredPrefixModifiers)

    return TailwindClassParser(matcher).parse(sample)
}
