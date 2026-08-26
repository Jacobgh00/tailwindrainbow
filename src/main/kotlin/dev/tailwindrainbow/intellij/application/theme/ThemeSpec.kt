package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

/**
 * An *untrusted* theme, exactly as a user or another plugin supplied it.
 *
 * Deliberately primitive: every field is the raw value that came out of storage or a text field,
 * none of it validated. [ThemeParser] is the only way to turn this into a
 * [dev.tailwindrainbow.intellij.domain.theme.RainbowTheme], so no unchecked value can ever reach a
 * domain constructor that throws.
 *
 * The flat entry list is also what makes persistence trivial — it maps one-to-one onto a
 * serializable bean, with no nested maps for the XML serializer to choke on.
 */
data class ThemeSpec(
    val name: String,
    val entries: List<StyleEntry>,
)

data class StyleEntry(
    val section: SegmentKind,
    /** The prefix or base class this styles. Ignored for [SegmentKind.ARBITRARY] and [SegmentKind.IMPORTANT]. */
    val key: String,
    val color: String,
    val fontWeight: Int,
    val enabled: Boolean = true,
)

/** One entry that could not be understood. The rest of the theme still loads. */
data class ThemeProblem(
    val themeName: String,
    val section: SegmentKind,
    val key: String,
    val message: String,
)

