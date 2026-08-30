package dev.tailwindrainbow.intellij.application.port

data class VariantFile(
    val size: Long,
    val readText: () -> String,
)

fun interface VariantFileSource {
    fun files(): Sequence<VariantFile>
}
