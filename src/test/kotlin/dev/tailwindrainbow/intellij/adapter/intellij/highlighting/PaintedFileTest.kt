package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.WriteIntentReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.EdtTestUtil
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.sourceRootFixture

data class Painted(val text: String, val color: String)

@TestApplication
abstract class PaintedFileTest {
    val project = projectFixture()
    private val module = project.moduleFixture()
    private val sourceRoot = module.sourceRootFixture()

    fun painted(
        fileName: String,
        text: String,
    ): List<Painted> =
        EdtTestUtil.runInEdtAndGet<List<Painted>, Throwable> {
            WriteIntentReadAction.compute<List<Painted>, Throwable> { paint(fileName, text) }
        }

    private fun paint(
        fileName: String,
        text: String,
    ): List<Painted> {
        val file = write(fileName, text)
        val editor = openEditor(file)

        return try {
            CodeInsightTestFixtureImpl.instantiateAndRun(file, editor, IntArray(0), false)
                .map { Painted(text.substring(it.startOffset, it.endOffset), it.forcedTextAttributes.hex()) }
        } finally {
            EditorFactory.getInstance().releaseEditor(editor)
        }
    }

    private fun write(
        fileName: String,
        text: String,
    ): PsiFile =
        WriteAction.computeAndWait<PsiFile, RuntimeException> {
            val virtualFile = VfsTestUtil.createFile(sourceRoot.get().virtualFile, fileName, text)

            checkNotNull(PsiManager.getInstance(project.get()).findFile(virtualFile)) { "no PSI for $fileName" }
        }

    private fun openEditor(file: PsiFile): Editor {
        val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file.virtualFile))

        return EditorFactory.getInstance().createEditor(document, project.get(), file.virtualFile, true)
    }
}

private fun com.intellij.openapi.editor.markup.TextAttributes?.hex(): String {
    val color = this?.foregroundColor ?: return ""

    return "#%02x%02x%02x".format(color.red, color.green, color.blue)
}
