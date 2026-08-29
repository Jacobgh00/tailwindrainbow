package dev.tailwindrainbow.intellij.adapter.intellij

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

internal fun VirtualFile.scannedLength(): Int = cachedDocument()?.textLength ?: length.toInt()

internal fun PsiFile.scannedExtension(): String? = viewProvider.virtualFile.extension

private fun VirtualFile.cachedDocument() = FileDocumentManager.getInstance().getCachedDocument(this)
