package dev.tailwindrainbow.intellij.application.highlight

/**
 * What a file extension implies about the syntax inside it.
 *
 * The single place file-type knowledge lives. Supporting a new language means adding a row to the
 * tables below, not another conditional in the lexer and another in the scanner.
 */
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

        /** Languages where `#` starts a line comment. */
        private val HASH_COMMENT_EXTENSIONS = setOf("php")

        /** Languages where Tailwind classes may appear in an `@apply` directive. */
        private val STYLESHEET_EXTENSIONS =
            setOf("css", "scss", "sass", "less", "styl", "stylus", "pcss", "postcss")
    }
}
