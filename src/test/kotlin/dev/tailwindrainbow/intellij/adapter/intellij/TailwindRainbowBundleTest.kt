package dev.tailwindrainbow.intellij.adapter.intellij

import java.util.PropertyResourceBundle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TailwindRainbowBundleTest {
    private val bundle =
        PropertyResourceBundle(
            checkNotNull(javaClass.getResourceAsStream("/messages/TailwindRainbowBundle.properties")),
        )

    @Test
    fun `plugin xml resolves its own text through the bundle`() {
        val pluginXml = checkNotNull(javaClass.getResourceAsStream("/META-INF/plugin.xml")).reader().readText()
        val actionId = Regex("""<action id="([^"]+)"""").find(pluginXml)?.groupValues?.get(1)

        assertEquals("dev.tailwindrainbow.SelectTheme", actionId)
        listOf("configurable.displayName", "action.$actionId.text", "action.$actionId.description")
            .forEach { assertTrue(bundle.containsKey(it), "plugin.xml needs $it") }
    }

    @Test
    fun `every message the code asks for exists`() {
        val used =
            java.io.File("src/main/kotlin")
                .walkTopDown()
                .filter { it.extension == "kt" }
                .flatMap { Regex("""message\("([^"]+)"""").findAll(it.readText()) }
                .map { it.groupValues[1] }
                .toSet()

        assertTrue(used.isNotEmpty(), "found no messages — has the bundle moved?")
        used.forEach { assertTrue(bundle.containsKey(it), "missing translation for $it") }
    }

    @Test
    fun `no user-visible text is left behind in the IDE adapter`() {
        val offenders =
            java.io.File("src/main/kotlin/dev/tailwindrainbow/intellij/adapter/intellij")
                .walkTopDown()
                .filter { it.extension == "kt" }
                .flatMap { file ->
                    Regex("""(JBLabel|JBCheckBox|JButton|ValidationInfo|setTitle|setOKButtonText|row)\("([^"]{4,})"""")
                        .findAll(file.readText())
                        .map { "${file.name}: ${it.groupValues[2]}" }
                }
                .toList()

        assertTrue(offenders.isEmpty(), "these should come from the bundle: $offenders")
    }
}
