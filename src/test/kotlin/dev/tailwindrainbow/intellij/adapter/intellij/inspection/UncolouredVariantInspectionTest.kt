package dev.tailwindrainbow.intellij.adapter.intellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.testFramework.runInEdtAndGet
import dev.tailwindrainbow.intellij.adapter.intellij.highlighting.PaintedFileTest
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.adapter.intellij.variants.ProjectVariants
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UncolouredVariantInspectionTest : PaintedFileTest() {
    private val settings = TailwindRainbowSettings.getInstance()
    private val stored = settings.current()

    @AfterTest
    fun restoreSettings() = settings.update(stored, emptyList())

    @Test
    fun `a variant this project declares, with no colour, is reported where it is used`() {
        file("app.css", "@custom-variant is-dragging (&:where(.is-dragging *));")
        val page = file("uses.html", """<div class="is-dragging:opacity-50 hover:underline"></div>""")

        assertTrue("is-dragging" in ProjectVariants.getInstance(project.get()).refresh(), "declared")

        val problems =
            runInEdtAndGet {
                UncolouredVariantInspection().checkFile(page, InspectionManager.getInstance(project.get()), false)
            }

        assertEquals(1, problems?.size, "only the variant with no colour is reported")
        assertTrue(problems!!.single().descriptionTemplate.contains("is-dragging"))
    }

    @Test
    fun `once the quick fix has run, the variant has a colour and nothing is reported`() {
        file("fixed.css", "@custom-variant theme-midnight (&:where([data-theme=midnight] *));")
        val page = file("fixed.html", """<div class="theme-midnight:bg-black"></div>""")
        ProjectVariants.getInstance(project.get()).refresh()
        val manager = InspectionManager.getInstance(project.get())

        val before = runInEdtAndGet { UncolouredVariantInspection().checkFile(page, manager, false) }
        assertEquals(1, before?.size)

        runInEdtAndGet { AddTokenQuickFix("theme-midnight").applyFix(project.get(), before!!.single()) }

        val after = runInEdtAndGet { UncolouredVariantInspection().checkFile(page, manager, false) }
        assertTrue(after.isNullOrEmpty(), "the theme now colours it")
    }
}
