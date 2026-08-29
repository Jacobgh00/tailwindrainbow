package dev.tailwindrainbow.intellij.adapter.intellij.settings

import com.intellij.openapi.components.State
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.util.xmlb.XmlSerializer
import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import org.jdom.Element
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestApplication
class SettingsSerialisationTest {
    private val project = projectFixture()

    private val midnight =
        ThemeSpec(
            name = "midnight",
            entries =
                listOf(
                    StyleEntry(SegmentKind.PREFIX, "hover", "#abcdef", 700),
                    StyleEntry(SegmentKind.BASE, "bg-*", "#123456", 400, enabled = false),
                    StyleEntry(SegmentKind.ARBITRARY, "", "#0f0f0f", 700),
                ),
            basedOn = "synthwave",
        )

    @Test
    fun `settings survive being written to XML and read back`() {
        val written = TailwindRainbowSettings()
        written.update(
            HighlightSettings(
                enabled = false,
                themeName = "midnight",
                scan =
                    ScanSettings(
                        maxFileSize = 4096,
                        classFunctions = setOf("twcx"),
                        supportedExtensions = setOf("vue"),
                        readsClassLikeStrings = false,
                    ),
            ),
            listOf(midnight),
        )

        val read = reload(written)

        assertEquals(written.current(), read.current())
        assertEquals(listOf(midnight), read.themes.overrides(), "a theme keeps its base, its keys, and its off switch")
    }

    @Test
    fun `a theme stored before bases existed still loads, based on itself`() {
        val written = TailwindRainbowSettings()
        written.update(written.current(), listOf(midnight))

        val xml = XmlSerializer.serialize(written.state)
        assertEquals("synthwave", xml.optionValue("basedOn"), "the base is written, so removing it means something")

        xml.dropOptions("basedOn")
        assertNull(xml.optionValue("basedOn"), "an old settings file simply has no such option")

        val read = TailwindRainbowSettings()
        read.loadState(XmlSerializer.deserialize(xml, TailwindRainbowSettings.StoredState::class.java))

        assertEquals("midnight", read.themes.overrides().single().basedOn)
    }

    @Test
    fun `a project keeps the recognition rules it claimed`() {
        val claimed =
            ScanSettings(
                classIdentifiers = setOf("class"),
                supportedExtensions = setOf("templ"),
                readsClassLikeStrings = false,
            )
        val written = TailwindRainbowProjectSettings()
        written.update(claimed)

        val read = TailwindRainbowProjectSettings()
        read.loadState(
            XmlSerializer.deserialize(
                XmlSerializer.serialize(written.state),
                TailwindRainbowProjectSettings.StoredState::class.java,
            ),
        )

        assertEquals(claimed, read.recognition())
    }

    @Test
    fun `a project that never claimed anything reads back as following the IDE`() {
        val written = TailwindRainbowProjectSettings()

        val read = TailwindRainbowProjectSettings()
        read.loadState(
            XmlSerializer.deserialize(
                XmlSerializer.serialize(written.state),
                TailwindRainbowProjectSettings.StoredState::class.java,
            ),
        )

        assertNull(read.recognition())
        assertNull(TailwindRainbowProjectSettings.getInstance(project.get()).recognition())
    }

    @Test
    fun `the project's rules are stored where a repository can commit them`() {
        val state = TailwindRainbowProjectSettings::class.java.getAnnotation(State::class.java)

        assertEquals("tailwindRainbow.xml", state.storages.single().value, "the promise is a file inside .idea")
    }

    private fun reload(written: TailwindRainbowSettings): TailwindRainbowSettings {
        val xml = XmlSerializer.serialize(written.state)

        return TailwindRainbowSettings().apply {
            loadState(XmlSerializer.deserialize(xml, TailwindRainbowSettings.StoredState::class.java))
        }
    }

    private fun Element.optionValue(named: String): String? =
        children.firstNotNullOfOrNull { child ->
            if (child.getAttributeValue("name") == named) child.getAttributeValue("value") else child.optionValue(named)
        }

    private fun Element.dropOptions(named: String) {
        children.toList().forEach { child ->
            if (child.getAttributeValue("name") == named) removeContent(child) else child.dropOptions(named)
        }
    }
}
