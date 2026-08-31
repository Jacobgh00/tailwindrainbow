package dev.tailwindrainbow.intellij.application.variants

data class VariantDeclaration(
    val name: String,
    val kind: VariantDeclarationKind,
    val location: VariantSourceLocation?,
)

enum class VariantDeclarationKind {
    CUSTOM_VARIANT,
    BREAKPOINT,
    SCREEN,
    ADD_VARIANT,
}

data class VariantSourceLocation(
    val path: String,
    val startOffset: Int,
    val endOffset: Int,
) {
    init {
        require(path.isNotBlank()) { "path must not be blank" }
        require(startOffset >= 0) { "startOffset must not be negative" }
        require(endOffset >= startOffset) { "endOffset must not precede startOffset" }
    }
}
