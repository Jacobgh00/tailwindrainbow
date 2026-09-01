package dev.tailwindrainbow.intellij.domain.theme

data class FontWeight(val value: Int) {
    init {
        require(value in RANGE && value % STEP == 0) {
            "Font weight must be a multiple of $STEP between ${RANGE.first} and ${RANGE.last}, was $value"
        }
    }

    companion object {
        private val RANGE = 100..900
        private const val STEP = 100

        val NORMAL = FontWeight(400)
        val BOLD = FontWeight(700)
        val BLACK = FontWeight(900)

        val ALL: Set<Int> = (RANGE step STEP).toSet()

        fun of(value: Int): FontWeight = FontWeight(value)

        fun isValid(value: Int): Boolean = value in ALL
    }
}

data class TextStyle(
    val color: String,
    val fontWeight: FontWeight = FontWeight.NORMAL,
    val enabled: Boolean = true,
) {
    init {
        require(color.isHexColor()) { "Color must use #RRGGBB format: $color" }
    }
}

private val HEX_COLOR = Regex("^#[0-9a-fA-F]{6}$")

fun String.isHexColor(): Boolean = HEX_COLOR.matches(this)

fun String.toHexColorOrNull(): String? {
    val digits = trim().removePrefix("#").lowercase()
    val expanded = if (digits.length == SHORTHAND_DIGITS) digits.flatMap { listOf(it, it) }.joinToString("") else digits

    return "#$expanded".takeIf(String::isHexColor)
}

private const val SHORTHAND_DIGITS = 3

data class RainbowTheme(
    val prefix: Map<String, TextStyle> = emptyMap(),
    val base: Map<String, TextStyle> = emptyMap(),
    val arbitrary: TextStyle? = null,
    val important: TextStyle? = null,
)

fun RainbowTheme.overriddenBy(override: RainbowTheme): RainbowTheme =
    RainbowTheme(
        prefix = prefix + override.prefix,
        base = base + override.base,
        arbitrary = override.arbitrary ?: arbitrary,
        important = override.important ?: important,
    )

data class ThemeMatch(
    val key: String,
    val style: TextStyle,
    val kind: SegmentKind,
)

data class ModifierSegment(val match: ThemeMatch?, val width: Int)

data class PrefixParts(
    val modifiers: List<ModifierSegment>,
    val variant: ThemeMatch?,
    val scopingModifierWidth: Int,
)

data class PrefixCandidates(
    val exact: List<String>,
    val cleaned: String,
)
