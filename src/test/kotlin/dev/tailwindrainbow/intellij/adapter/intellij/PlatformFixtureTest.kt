package dev.tailwindrainbow.intellij.adapter.intellij

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowProjectSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestApplication
class PlatformFixtureTest {
    private val project = projectFixture()

    @Test
    fun `a fixture project starts, so the plugin can be tested inside one`() {
        assertNotNull(project.get().basePath)
    }

    @Test
    fun `the plugin's own services are registered in the test application`() {
        val settings = TailwindRainbowSettings.getInstance()

        assertEquals("default", settings.current().themeName)
        assertNotNull(settings.themes.themeNamed("default").prefix["hover"], "built-in themes resolve")
    }

    @Test
    fun `the project service is registered too, and starts out following the IDE`() {
        assertNull(TailwindRainbowProjectSettings.getInstance(project.get()).recognition())
    }
}
