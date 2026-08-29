package dev.tailwindrainbow.intellij.adapter.intellij

import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.runInEdtAndGet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestApplication
class ScannedFileTest {
    private val project = projectFixture()

    private val markup = """<div class="hover:bg-blue-500"></div>"""

    @Test
    fun `a file that exists only in memory still names its type`() {
        val inMemory =
            runInEdtAndGet {
                PsiFileFactory.getInstance(project.get())
                    .createFileFromText("sample.html", PlainTextFileType.INSTANCE, markup)
            }

        assertNull(inMemory.virtualFile, "the case this is about: an editor over no file on disk")
        assertEquals("html", inMemory.scannedExtension())
    }

    @Test
    fun `a scratch buffer names its type too`() {
        val scratch =
            WriteAction.computeAndWait<com.intellij.openapi.vfs.VirtualFile, RuntimeException> {
                checkNotNull(
                    ScratchRootType.getInstance()
                        .createScratchFile(project.get(), "scratch.html", PlainTextLanguage.INSTANCE, markup),
                )
            }
        val file = runInEdtAndGet { checkNotNull(PsiManager.getInstance(project.get()).findFile(scratch)) }

        assertEquals("html", file.scannedExtension())
    }
}
