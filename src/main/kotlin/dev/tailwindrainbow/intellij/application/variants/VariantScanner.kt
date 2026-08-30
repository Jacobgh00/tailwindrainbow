package dev.tailwindrainbow.intellij.application.variants

import dev.tailwindrainbow.intellij.application.port.VariantFileSource

class VariantScanner(
    sources: List<VariantFileSource>,
    private val limits: VariantScanLimits = VariantScanLimits(),
) {
    private val sources = sources.toList()

    fun scan(): Set<String> =
        sources
            .asSequence()
            .flatMap { it.files() }
            .take(limits.maxFiles)
            .filter { it.size <= limits.maxFileSize }
            .flatMapTo(mutableSetOf()) { variantsDeclaredIn(it.readText()) }
}

data class VariantScanLimits(
    val maxFiles: Int = DEFAULT_MAX_FILES,
    val maxFileSize: Long = DEFAULT_MAX_FILE_SIZE,
) {
    init {
        require(maxFiles > 0) { "maxFiles must be positive" }
        require(maxFileSize > 0) { "maxFileSize must be positive" }
    }

    private companion object {
        const val DEFAULT_MAX_FILES = 200
        const val DEFAULT_MAX_FILE_SIZE = 200_000L
    }
}
