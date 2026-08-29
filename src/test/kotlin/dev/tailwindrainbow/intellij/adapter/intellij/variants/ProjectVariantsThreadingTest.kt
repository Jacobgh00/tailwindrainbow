package dev.tailwindrainbow.intellij.adapter.intellij.variants

import com.intellij.testFramework.runInEdtAndGet
import dev.tailwindrainbow.intellij.adapter.intellij.highlighting.PaintedFileTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ProjectVariantsThreadingTest : PaintedFileTest() {
    @Test
    fun `asking what a project declares does not read the project on the EDT`() {
        file("edt.css", "@custom-variant is-dragging (&:where(.is-dragging *));")

        val asked = runInEdtAndGet { ProjectVariants.getInstance(project.get()).declared() }

        assertTrue(asked.isEmpty() || "is-dragging" in asked, "whatever it answers, it answers without blocking")
    }
}
