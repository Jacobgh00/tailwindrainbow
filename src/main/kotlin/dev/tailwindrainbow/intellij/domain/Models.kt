package dev.tailwindrainbow.intellij.domain

data class FontWeight(val value: Int) {
    init {
        require(value in 100..900 && value % 100 == 0) {
            "Font weight must be a multiple of 100 between 100 and 900"
        }
    }

    companion object {
        val NORMAL = FontWeight(400)
        val BOLD = FontWeight(700)

        fun of(value: Int): FontWeight = FontWeight(value)
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

data class ThemeMatch(
    val key: String,
    val style: TextStyle,
)

enum class SegmentKind {
    PREFIX,
    BASE,
    ARBITRARY,
    IMPORTANT,
}

data class HighlightSegment(
    val start: Int,
    val end: Int,
    val themeKey: String,
    val style: TextStyle,
    val kind: SegmentKind,
) {
    init {
        require(start >= 0) { "Segment start must not be negative" }
        require(end > start) { "Segment end must be greater than start" }
    }
}

data class ScanSettings(
    val maxFileSize: Int = 1_000_000,
    val classIdentifiers: Set<String> = DEFAULT_CLASS_IDENTIFIERS,
    val classFunctions: Set<String> = DEFAULT_CLASS_FUNCTIONS,
    val templateTags: Set<String> = DEFAULT_TEMPLATE_TAGS,
    val ignoredPrefixModifiers: Set<String> = DEFAULT_IGNORED_PREFIX_MODIFIERS,
    val supportedExtensions: Set<String> = DEFAULT_SUPPORTED_EXTENSIONS,
) {
    init {
        require(maxFileSize > 0) { "Maximum file size must be positive" }
    }

    companion object {
        val DEFAULT_CLASS_IDENTIFIERS = setOf(
            "class",
            "className",
            "class:",
            "className:",
            "class:list",
            "classlist",
            "classes",
            "css",
            "style",
        )

        val DEFAULT_CLASS_FUNCTIONS = setOf(
            "cn",
            "clsx",
            "cva",
            "classNames",
            "classList",
            "classnames",
            "twMerge",
            "tw",
            "cls",
            "cc",
            "cx",
            "classname",
            "styled",
            "css",
            "theme",
            "variants",
        )

        val DEFAULT_TEMPLATE_TAGS = setOf("tw", "css", "styled")
        val DEFAULT_IGNORED_PREFIX_MODIFIERS = setOf("group", "peer", "has", "in", "not")
        val DEFAULT_SUPPORTED_EXTENSIONS = setOf(
            "html",
            "htm",
            "js",
            "jsx",
            "ts",
            "tsx",
            "vue",
            "svelte",
            "astro",
            "php",
            "css",
            "scss",
            "sass",
            "less",
            "styl",
            "stylus",
            "pcss",
            "postcss",
        )
    }
}