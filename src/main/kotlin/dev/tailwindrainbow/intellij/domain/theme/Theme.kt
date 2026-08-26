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
        require(HEX_COLOR.matches(color)) { "Color must use #RRGGBB format: $color" }
    }

    private companion object {
        val HEX_COLOR = Regex("^#[0-9a-fA-F]{6}$")
    }
}

data class RainbowTheme(
    val prefix: Map<String, TextStyle> = emptyMap(),
    val base: Map<String, TextStyle> = emptyMap(),
    val arbitrary: TextStyle? = null,
    val important: TextStyle? = null,
)

/** [kind] is decided by the matcher; never re-derive it by inspecting [key]. */
data class ThemeMatch(
    val key: String,
    val style: TextStyle,
    val kind: SegmentKind,
)
