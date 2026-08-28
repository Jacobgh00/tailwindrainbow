package dev.tailwindrainbow.intellij.application.highlight

internal data class SyntaxProfile(
    val usesHashComments: Boolean,
    val hasApplyDirectives: Boolean,
) {
    companion object {
        fun of(extension: String): SyntaxProfile =
            SyntaxProfile(
                usesHashComments = extension in HASH_COMMENT_EXTENSIONS,
                hasApplyDirectives = extension in STYLESHEET_EXTENSIONS,
            )

        private val HASH_COMMENT_EXTENSIONS = setOf("php")

        private val STYLESHEET_EXTENSIONS =
            setOf("css", "scss", "sass", "less", "styl", "stylus", "pcss", "postcss")
    }
}
