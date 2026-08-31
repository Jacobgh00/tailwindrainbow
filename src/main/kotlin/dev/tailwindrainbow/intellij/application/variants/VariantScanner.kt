package dev.tailwindrainbow.intellij.application.variants

import dev.tailwindrainbow.intellij.application.port.Cancellation
import dev.tailwindrainbow.intellij.application.port.VariantFileSource

class VariantScanner(
    sources: List<VariantFileSource>,
    private val limits: VariantScanLimits = VariantScanLimits(),
) {
    private val sources = sources.toList()

    fun scanResult(cancellation: Cancellation = Cancellation.NONE): VariantScanResult {
        val found =
            sources.asSequence()
                .flatMap(VariantFileSource::files)
                .onEach { cancellation.check() }
                .take(limits.maxFiles + 1)
                .toList()
        val considered = found.take(limits.maxFiles)
        val readable = considered.filter { it.size <= limits.maxFileSize }

        return VariantScanResult(
            declarations =
                readable.flatMap { file ->
                    cancellation.check()
                    variantDeclarationsIn(file.readText(), file.path)
                },
            scannedFileCount = readable.size,
            oversizedFileCount = considered.size - readable.size,
            reachedFileLimit = found.size > limits.maxFiles,
        )
    }
}

data class VariantScanResult(
    val declarations: List<VariantDeclaration>,
    val scannedFileCount: Int,
    val oversizedFileCount: Int = 0,
    val reachedFileLimit: Boolean = false,
) {
    init {
        require(scannedFileCount >= 0) { "scannedFileCount must not be negative" }
        require(oversizedFileCount >= 0) { "oversizedFileCount must not be negative" }
    }
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
