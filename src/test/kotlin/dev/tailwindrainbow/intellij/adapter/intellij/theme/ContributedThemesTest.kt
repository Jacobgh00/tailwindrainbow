package dev.tailwindrainbow.intellij.adapter.intellij.theme

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.extensionPointFixture
import dev.tailwindrainbow.intellij.adapter.theme.ThemeContributor
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private val CONTRIBUTORS = ExtensionPointName<ThemeContributor>("dev.tailwindrainbow.themeContributor")

@TestApplication
class ContributedThemesTest {
    private var thrownByContributor: RuntimeException? = null

    private val failing =
        extensionPointFixture(CONTRIBUTORS) {
            ThemeContributor { thrownByContributor?.let { throw it } ?: emptyList() }
        }

    private val healthy =
        extensionPointFixture(CONTRIBUTORS) {
            ThemeContributor {
                listOf(ThemeSpec("sunrise", listOf(StyleEntry(SegmentKind.PREFIX, "hover", "#ff8800", 700))))
            }
        }

    @AfterTest
    fun forgetFailure() {
        thrownByContributor = null
    }

    @Test
    fun `a contributor that cancels is not treated as a contributor that broke`() {
        failing.get()
        healthy.get()
        thrownByContributor = ProcessCanceledException()

        assertFailsWith<ProcessCanceledException> { ContributedThemes.themes() }
    }

    @Test
    fun `a contributor that breaks costs only its own themes`() {
        failing.get()
        healthy.get()
        thrownByContributor = IllegalStateException("no themes today")

        val themes = ContributedThemes.themes()

        assertTrue(themes.containsKey("sunrise"), "the other contributor survives, got: ${themes.keys}")
    }
}
