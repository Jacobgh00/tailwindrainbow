package dev.tailwindrainbow.intellij.application.variants

import dev.tailwindrainbow.intellij.application.port.VariantFile
import dev.tailwindrainbow.intellij.application.port.VariantFileSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VariantScannerTest {
    @Test
    fun `combines variants from every source`() {
        val scanner =
            VariantScanner(
                listOf(
                    source("@custom-variant pointer-coarse (&:where(*));"),
                    source("--breakpoint-tablet: 40rem;"),
                ),
            )

        assertEquals(setOf("pointer-coarse", "tablet"), scanner.declaredNames())
    }

    @Test
    fun `does not read files over the size limit`() {
        var read = false
        val scanner =
            VariantScanner(
                sources =
                    listOf(
                        VariantFileSource {
                            sequenceOf(
                                VariantFile(200_001L) {
                                    read = true
                                    "@custom-variant too-large (&:where(*));"
                                },
                            )
                        },
                    ),
            )

        assertTrue(scanner.declaredNames().isEmpty())
        assertFalse(read)
    }

    @Test
    fun `limits the number of files in source order`() {
        val scanner =
            VariantScanner(
                sources =
                    listOf(
                        source("@custom-variant first (&:where(*));"),
                        source("@custom-variant second (&:where(*));"),
                    ),
                limits = VariantScanLimits(maxFiles = 1),
            )

        assertEquals(setOf("first"), scanner.declaredNames())
    }

    @Test
    fun `flags a scan cut short by the file limit`() {
        val scanner =
            VariantScanner(
                sources =
                    listOf(
                        source("@custom-variant first (&:where(*));"),
                        source("@custom-variant second (&:where(*));"),
                    ),
                limits = VariantScanLimits(maxFiles = 1),
            )

        val result = scanner.scanResult()

        assertEquals(1, result.scannedFileCount)
        assertTrue(result.reachedFileLimit)
    }

    @Test
    fun `counts files left unread for being too large`() {
        val scanner =
            VariantScanner(
                sources = listOf(VariantFileSource { sequenceOf(VariantFile(200_001L) { "" }) }),
            )

        val result = scanner.scanResult()

        assertEquals(0, result.scannedFileCount)
        assertEquals(1, result.oversizedFileCount)
        assertFalse(result.reachedFileLimit)
    }

    @Test
    fun `stops pulling files from a source once the limit is reached`() {
        var pulled = 0
        val endless =
            VariantFileSource {
                generateSequence {
                    pulled++
                    VariantFile(1L) { "" }
                }
            }

        VariantScanner(listOf(endless), VariantScanLimits(maxFiles = 5)).scanResult()

        assertEquals(6, pulled, "five files, plus the one that proves the scan was cut short")
    }

    @Test
    fun `copies sources before scanning`() {
        val sources = mutableListOf(source("@custom-variant first (&:where(*));"))
        val scanner = VariantScanner(sources)
        sources.clear()

        assertEquals(setOf("first"), scanner.declaredNames())
    }

    @Test
    fun `retains declaration kind and source location`() {
        val text = "@custom-variant pointer-coarse (&:where(*));"
        val scanner =
            VariantScanner(
                listOf(
                    VariantFile(
                        size = text.length.toLong(),
                        readText = { text },
                        path = "styles/app.css",
                    ).let { file -> VariantFileSource { sequenceOf(file) } },
                ),
            )

        val declaration = scanner.scanResult().declarations.single()
        val location = checkNotNull(declaration.location)

        assertEquals(VariantDeclarationKind.CUSTOM_VARIANT, declaration.kind)
        assertEquals("styles/app.css", location.path)
        assertEquals("pointer-coarse", text.substring(location.startOffset, location.endOffset))
    }

    private fun VariantScanner.declaredNames() = scanResult().declarations.map(VariantDeclaration::name).toSet()

    private fun source(text: String) = VariantFileSource { sequenceOf(VariantFile(text.length.toLong()) { text }) }
}
