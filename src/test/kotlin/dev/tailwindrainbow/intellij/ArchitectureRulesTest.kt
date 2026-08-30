package dev.tailwindrainbow.intellij

import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText
import kotlin.io.path.walk
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The dependency direction, enforced rather than left in the package names.
 *
 * The test deliberately checks imports between layers instead of requiring every file in a package
 * to use a particular implementation. An adapter can be a small translation helper without being
 * forced to import the IntelliJ Platform merely to satisfy its directory.
 */
class ArchitectureRulesTest {
    @Test
    fun `domain depends on neither outer layers nor platform details`() {
        assertNoneIn(
            layer = "/domain/",
            forbidden = OUTER_LAYERS + PLATFORM_TYPES + SERIALISATION_TYPES,
            because = "the domain must remain usable without an IDE, UI toolkit, or file-format library",
        )
    }

    @Test
    fun `application depends on domain and ports, not adapters or platform details`() {
        assertNoneIn(
            layer = "/application/",
            forbidden = APPLICATION_OUTER_LAYERS + PLATFORM_TYPES + SERIALISATION_TYPES,
            because = "application policy must talk to external systems through its own ports",
        )
    }

    @Test
    fun `only adapters and bootstrap touch the IntelliJ Platform`() {
        val offenders =
            sources()
                .filter { it.imports.any { imported -> imported.startsWith(INTELLIJ) } }
                .filterNot { it.path.contains("/adapter/") || it.path.contains("/bootstrap/") }

        assertTrue(offenders.isEmpty(), "the IntelliJ Platform escaped the outer layer: ${offenders.names()}")
    }

    @Test
    fun `concrete adapters are named only from the outer layers`() {
        val offenders =
            sources()
                .filterNot { it.path.contains("/adapter/") || it.path.contains("/bootstrap/") }
                .filter { it.imports.any { imported -> imported.startsWith(ADAPTERS) } }

        assertTrue(
            offenders.isEmpty(),
            "wiring belongs in adapter or bootstrap, not in domain/application: ${offenders.names()}",
        )
    }

    private fun assertNoneIn(
        layer: String,
        forbidden: List<String>,
        because: String,
    ) {
        val offenders =
            sources()
                .filter { layer in it.path }
                .filter { file ->
                    file.imports.any { imported ->
                        forbidden.any { prefix -> imported.startsWith(prefix) }
                    }
                }

        assertTrue(offenders.isEmpty(), "${offenders.names()} — $because")
    }

    private data class SourceFile(
        val path: String,
        val name: String,
        val imports: List<String>,
    )

    private fun List<SourceFile>.names(): List<String> = map(SourceFile::name)

    private fun sources(): List<SourceFile> =
        Path.of("src/main/kotlin")
            .walk()
            .filter { it.extension == "kt" }
            .map { path ->
                SourceFile(
                    path = path.toString().replace('\\', '/'),
                    name = path.fileName.toString(),
                    imports = path.readText().lineSequence().mapNotNull(::importedType).toList(),
                )
            }.toList()
            .also { assertTrue(it.isNotEmpty(), "found no sources — wrong working directory?") }

    private companion object {
        const val INTELLIJ = "com.intellij."
        const val ADAPTERS = "dev.tailwindrainbow.intellij.adapter."
        const val BOOTSTRAP = "dev.tailwindrainbow.intellij.bootstrap."

        val OUTER_LAYERS =
            listOf(
                "dev.tailwindrainbow.intellij.application.",
                ADAPTERS,
                BOOTSTRAP,
            )
        val APPLICATION_OUTER_LAYERS = listOf(ADAPTERS, BOOTSTRAP)
        val PLATFORM_TYPES = listOf(INTELLIJ, "java.awt.", "javax.swing.")
        val SERIALISATION_TYPES = listOf("com.google.gson.", "org.jdom.")
    }
}

private fun importedType(line: String): String? {
    val trimmed = line.trim()
    if (!trimmed.startsWith("import ")) return null

    return trimmed.removePrefix("import ")
}
