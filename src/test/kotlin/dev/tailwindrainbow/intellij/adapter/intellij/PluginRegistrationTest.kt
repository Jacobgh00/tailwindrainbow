package dev.tailwindrainbow.intellij.adapter.intellij

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.extensionPointFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.runInEdtAndWait
import dev.tailwindrainbow.intellij.adapter.intellij.settings.ui.TailwindRainbowSettingsConfigurable
import dev.tailwindrainbow.intellij.adapter.intellij.statusbar.TAILWIND_STATUS_WIDGET_ID
import dev.tailwindrainbow.intellij.adapter.intellij.theme.ContributedThemes
import dev.tailwindrainbow.intellij.adapter.theme.ThemeContributor
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestApplication
class PluginRegistrationTest {
    private val project = projectFixture()

    private val contributor =
        extensionPointFixture(ExtensionPointName<ThemeContributor>("dev.tailwindrainbow.themeContributor")) {
            ThemeContributor {
                listOf(ThemeSpec("corporate", listOf(StyleEntry(SegmentKind.PREFIX, "hover", "#010101", 700))))
            }
        }

    @Test
    fun `every action is registered, and takes its text from the bundle`() {
        val texts = ACTION_IDS.map { ActionManager.getInstance().getAction(it).templatePresentation.text }

        assertEquals(
            listOf(
                "Select Tailwind Rainbow Theme",
                "Explain Tailwind Colouring at Caret",
                "Copy Tailwind Rainbow Diagnostics",
            ),
            texts,
        )
    }

    @Test
    fun `an action asked about nothing at all answers without failing`() {
        ACTION_IDS.forEach { id ->
            val action = ActionManager.getInstance().getAction(id)
            val event =
                AnActionEvent.createEvent(
                    DataContext.EMPTY_CONTEXT,
                    action.templatePresentation.clone(),
                    "",
                    ActionUiKind.NONE,
                    null,
                )

            runInEdtAndWait { action.update(event) }

            assertFalse(event.presentation.isEnabled, "$id has nothing to act on")
        }
    }

    @Test
    fun `the status bar widget factory is registered under the id the widget reports`() {
        val factory =
            StatusBarWidgetFactory.EP_NAME.extensionList.firstOrNull { it.id == TAILWIND_STATUS_WIDGET_ID }

        assertNotNull(factory, "no factory registered for $TAILWIND_STATUS_WIDGET_ID")
        assertEquals("Tailwind Rainbow", factory.displayName)
    }

    @Test
    fun `a theme contributed through the extension point is read`() {
        contributor.get()

        assertTrue(ContributedThemes.themes().containsKey("corporate"))
    }

    @Test
    fun `the settings screen builds, and starts out unmodified`() {
        val configurable = TailwindRainbowSettingsConfigurable(project.get())

        runInEdtAndWait {
            assertNotNull(configurable.createComponent())
            assertFalse(configurable.isModified, "a screen nobody has touched has nothing to apply")
            configurable.disposeUIResources()
        }
    }

    private companion object {
        val ACTION_IDS =
            listOf(
                "dev.tailwindrainbow.SelectTheme",
                "dev.tailwindrainbow.ExplainColouring",
                "dev.tailwindrainbow.CopyDiagnostics",
            )
    }
}
