package dev.tailwindrainbow.intellij.application.highlight

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
