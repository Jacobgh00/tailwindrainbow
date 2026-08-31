package dev.tailwindrainbow.intellij.adapter.intellij.variants

import com.intellij.testFramework.runInEdtAndGet
import dev.tailwindrainbow.intellij.adapter.intellij.highlighting.PaintedFileTest
import dev.tailwindrainbow.intellij.application.variants.VariantDeclarationKind
import dev.tailwindrainbow.intellij.application.variants.VariantStatus
import dev.tailwindrainbow.intellij.bootstrap.PluginComponents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProjectVariantsThreadingTest : PaintedFileTest() {
    @Test
    fun `asking what a project declares does not read the project on the EDT`() {
        file("edt.css", "@custom-variant is-dragging (&:where(.is-dragging *));")

        val asked = runInEdtAndGet { ProjectVariants.getInstance(project.get()).declared() }

        assertTrue(asked.isEmpty() || "is-dragging" in asked, "whatever it answers, it answers without blocking")
    }

    @Test
    fun `a refreshed declaration carries a navigable project path and offset`() {
        val source = "@custom-variant is-dragging (&:where(.is-dragging *));"
        file("styles.css", source)

        val declaration =
            runInEdtAndGet {
                ProjectVariants.getInstance(project.get()).refreshScan().declarations.single()
            }
        val location = checkNotNull(declaration.location)

        assertEquals(VariantDeclarationKind.CUSTOM_VARIANT, declaration.kind)
        assertTrue(location.path.endsWith("styles.css"), location.path)
        assertEquals(
            "is-dragging",
            source.substring(location.startOffset, location.endOffset),
        )
    }

    @Test
    fun `the health report uses the same project declarations and active theme`() {
        val source = "@custom-variant is-dragging (&:where(.is-dragging *));"
        file("health.css", source)

        val report = runInEdtAndGet { PluginComponents.variantHealth(project.get()) }

        assertEquals("default", report.theme.name)
        assertEquals("is-dragging", report.assessments.single().name)
        assertIs<VariantStatus.MissingColour>(report.assessments.single().status)
    }

    @Test
    fun `does not scan variant declarations from node modules`() {
        file("node_modules/vendor/styles.css", "@custom-variant vendor-only (&:where(*));")

        val declarations =
            runInEdtAndGet {
                ProjectVariants.getInstance(project.get()).refreshScan().declarations
            }

        assertTrue(declarations.none { it.name == "vendor-only" })
    }
}
