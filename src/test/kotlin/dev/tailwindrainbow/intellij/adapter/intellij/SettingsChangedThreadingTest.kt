package dev.tailwindrainbow.intellij.adapter.intellij

import com.intellij.openapi.application.ApplicationManager
import dev.tailwindrainbow.intellij.adapter.intellij.highlighting.PaintedFileTest
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

class SettingsChangedThreadingTest : PaintedFileTest() {
    @Test
    fun `telling a project the settings changed never touches the UI on the caller's thread`() {
        file("probe.html", """<div class="hover:bg-black"></div>""")
        val target = project.get()
        val failure =
            ApplicationManager.getApplication()
                .executeOnPooledThread<Throwable?> { runCatching { settingsChanged(target) }.exceptionOrNull() }
                .get(30, TimeUnit.SECONDS)

        assertTrue(failure == null, "threw: $failure")
    }
}
