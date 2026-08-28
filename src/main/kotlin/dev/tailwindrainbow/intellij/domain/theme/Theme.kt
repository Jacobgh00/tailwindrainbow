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

        /** Every weight the model accepts. The single definition of the rule [FontWeight] enforces. */
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

/**
 * The one colour format the plugin accepts.
 *
 * Stated once because three layers ask the same question: the model enforcing its invariant, the
 * parser reporting what it had to drop, and the settings screen deciding what it can paint.
 */
fun String.isHexColor(): Boolean = HEX_COLOR.matches(this)

data class RainbowTheme(
    val prefix: Map<String, TextStyle> = emptyMap(),
    val base: Map<String, TextStyle> = emptyMap(),
    val arbitrary: TextStyle? = null,
    val important: TextStyle? = null,
)

/**
 * This palette with [override] laid on top, entry by entry.
 *
 * Entry-wise rather than section-wise on purpose: an override that names one prefix keeps every
 * other colour of the palette underneath it, which is what makes both user edits and derived themes
 * sparse.
 */
fun RainbowTheme.overriddenBy(override: RainbowTheme): RainbowTheme =
    RainbowTheme(
        prefix = prefix + override.prefix,
        base = base + override.base,
        arbitrary = override.arbitrary ?: arbitrary,
        important = override.important ?: important,
    )

/** [kind] is decided by the matcher; never re-derive it by inspecting [key]. */
data class ThemeMatch(
    val key: String,
    val style: TextStyle,
    val kind: SegmentKind,
)
