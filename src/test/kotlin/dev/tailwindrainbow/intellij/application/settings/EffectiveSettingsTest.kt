package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class EffectiveSettingsTest {
    private val mine =
        HighlightSettings(
            enabled = true,
            themeName = "synthwave",
            scan = ScanSettings(classFunctions = setOf("cn")),
        )

    @Test
    fun `without project rules the user's own apply`() {
        assertEquals(mine, mine.withProjectRecognition(null))
    }

    @Test
    fun `the project's rules replace the user's, and only those`() {
        val theirs = ScanSettings(classFunctions = setOf("twcx"), supportedExtensions = setOf("templ"))

        val effective = mine.withProjectRecognition(theirs)

        assertEquals(theirs, effective.scan)
        assertEquals("synthwave", effective.themeName, "a palette is the user's, whichever project is open")
        assertEquals(true, effective.enabled)
    }
}
