package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import com.intellij.codeInsight.daemon.impl.AnnotationHolderImpl
import com.intellij.lang.annotation.AnnotationSession
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.runInEdtAndGet
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestApplication
class InMemoryCopyTest {
    private val project = projectFixture()

    @Test
    fun `a copy that never reached disk is annotated like the file it came from`() {
        val copy = inMemoryCopy("""<div class="hover:bg-blue-500"></div>""")

        assertNull(copy.virtualFile, "the case this is about: a file with nothing behind it")
        assertTrue(annotationsOn(copy) > 0, "nothing was painted on the copy")
    }

    private fun inMemoryCopy(text: String): PsiFile =
        runInEdtAndGet {
            PsiFileFactory.getInstance(project.get())
                .createFileFromText("sample.html", PlainTextFileType.INSTANCE, text)
        }

    /**
     * The modern replacement for these takes a daemon pass, and a file with no document cannot have one
     * — which is the whole point of the case under test.
     */
    @Suppress("DEPRECATION")
    private fun annotationsOn(file: PsiFile): Int =
        ReadAction.compute<Int, RuntimeException> {
            val holder = AnnotationHolderImpl(AnnotationSession(file), false)

            holder.runAnnotatorWithContext(file) { _, _ -> TailwindRainbowAnnotator().annotate(file, holder) }

            holder.size
        }
}
