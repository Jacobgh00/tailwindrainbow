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

        assertEquals(setOf("pointer-coarse", "tablet"), scanner.scan())
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

        assertTrue(scanner.scan().isEmpty())
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

        assertEquals(setOf("first"), scanner.scan())
    }

    @Test
    fun `copies sources before scanning`() {
        val sources = mutableListOf(source("@custom-variant first (&:where(*));"))
        val scanner = VariantScanner(sources)
        sources.clear()

        assertEquals(setOf("first"), scanner.scan())
    }

    private fun source(text: String) = VariantFileSource { sequenceOf(VariantFile(text.length.toLong()) { text }) }
}
