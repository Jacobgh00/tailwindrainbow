package dev.tailwindrainbow.intellij.adapter.intellij

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile

/**
 * What the plugin would hand the scanner: the editor's copy while one is open, the file on disk
 * otherwise. A file being edited is the one that matters, and it is the one that differs.
 */
internal fun VirtualFile.scannedLength(): Int = cachedDocument()?.textLength ?: length.toInt()

private fun VirtualFile.cachedDocument() = FileDocumentManager.getInstance().getCachedDocument(this)
