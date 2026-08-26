package dev.tailwindrainbow.intellij

import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText
import kotlin.io.path.walk
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The layering rules, enforced rather than documented.
 *
 * Dependencies point inward only: adapter -> application -> domain. Every rule here was broken at
 * least once during development, and a grep only catches what you remember to grep for.
 */
class ArchitectureTest {
    @Test
    fun `domain depends on nothing`() {
        assertNoneOf(
            layer = "/domain/",
            forbidden = listOf("$BASE.application", "$BASE.adapter", "$BASE.bootstrap", "com.intellij"),
            because = "the innermost layer holds the model and its invariants; it must stay usable, " +
                "and testable, with no framework and no outer layer present",
        )
    }

    @Test
    fun `application depends only on domain`() {
        assertNoneOf(
            layer = "/application/",
            forbidden = listOf("$BASE.adapter", "$BASE.bootstrap", "com.intellij"),
            because = "use cases talk to the outside through ports; importing an adapter inverts the " +
                "dependency and makes the use case need an IDE to run",
        )
    }

    @Test
    fun `only adapters and the composition root touch the IntelliJ Platform`() {
        val offenders = sources()
            .filter { it.importsPlatform }
            .filterNot { it.path.contains("/adapter/") || it.path.contains("/bootstrap/") }

        assertTrue(offenders.isEmpty(), "com.intellij escaped the outer layer: ${offenders.names()}")
    }

    @Test
    fun `the IntelliJ adapter contains only code that touches the IntelliJ Platform`() {
        val offenders = sources()
            .filter { it.path.contains("/adapter/intellij/") }
            .filterNot { it.importsPlatform }

        assertTrue(
            offenders.isEmpty(),
            "these sit in the IDE adapter but import nothing from com.intellij, so they are pure " +
                "logic stranded outside the application layer: ${offenders.names()}",
        )
    }

    @Test
    fun `the composition root is the only place that names a concrete adapter`() {
        val offenders = sources()
            .filterNot { it.path.contains("/adapter/") || it.path.contains("/bootstrap/") }
            .filter { "$BASE.adapter" in it.text }

        assertTrue(
            offenders.isEmpty(),
            "wiring belongs in bootstrap/PluginComponents, not scattered: ${offenders.names()}",
        )
    }

    private fun assertNoneOf(layer: String, forbidden: List<String>, because: String) {
        val offenders = sources()
            .filter { layer in it.path }
            .filter { file -> forbidden.any { it in file.text } }

        assertTrue(offenders.isEmpty(), "${offenders.names()} — $because")
    }

    private data class SourceFile(val path: String, val name: String, val text: String) {
        val importsPlatform: Boolean get() = text.lineSequence().any { it.startsWith("import com.intellij") }
    }

    private fun List<SourceFile>.names() = map { it.name }

    @OptIn(kotlin.ExperimentalStdlibApi::class)
    private fun sources(): List<SourceFile> =
        Path.of("src/main/kotlin").walk()
            .filter { it.extension == "kt" }
            .map { SourceFile(it.toString().replace('\\', '/'), it.fileName.toString(), it.readText()) }
            .toList()
            .also { assertTrue(it.isNotEmpty(), "found no sources — wrong working directory?") }

    private companion object {
        const val BASE = "dev.tailwindrainbow.intellij"
    }
}
