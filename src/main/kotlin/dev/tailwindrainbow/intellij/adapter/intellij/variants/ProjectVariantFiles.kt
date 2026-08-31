package dev.tailwindrainbow.intellij.adapter.intellij.variants

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.ProjectScope
import dev.tailwindrainbow.intellij.application.port.VariantFile
import dev.tailwindrainbow.intellij.application.port.VariantFileSource
import java.io.IOException

internal class ProjectVariantFiles(private val project: Project) {
    private val projectFileIndex = ProjectFileIndex.getInstance(project)

    fun sources(): List<VariantFileSource> =
        listOf(
            VariantFileSource(::configFiles),
            VariantFileSource(::styleSheets),
        )

    private fun configFiles(): Sequence<VariantFile> {
        val scope = ProjectScope.getContentScope(project)

        return CONFIG_NAMES.asSequence()
            .flatMap { name -> FilenameIndex.getVirtualFilesByName(name, scope).asSequence() }
            .filter(::isProjectSource)
            .map { it.asVariantFile() }
    }

    private fun styleSheets(): Sequence<VariantFile> {
        val scope = ProjectScope.getContentScope(project)

        return STYLESHEET_EXTENSIONS.asSequence()
            .flatMap { extension -> FilenameIndex.getAllFilesByExt(project, extension, scope).asSequence() }
            .filter(::isProjectSource)
            .map { it.asVariantFile() }
    }

    /** Content scope can contain dependency directories, so it is broader than project-authored code. */
    private fun isProjectSource(file: VirtualFile): Boolean =
        !projectFileIndex.isExcluded(file) &&
            generateSequence(file) { it.parent }.none { it.name == NODE_MODULES_DIRECTORY }

    private fun VirtualFile.asVariantFile(): VariantFile = VariantFile(length, path) { readContents() }

    private fun VirtualFile.readContents(): String =
        try {
            String(contentsToByteArray())
        } catch (unreadable: IOException) {
            thisLogger().debug("Skipped $path", unreadable)
            ""
        } catch (gone: InvalidVirtualFileAccessException) {
            thisLogger().debug("Skipped $path", gone)
            ""
        }

    private companion object {
        val CONFIG_NAMES =
            listOf("tailwind.config.js", "tailwind.config.ts", "tailwind.config.cjs", "tailwind.config.mjs")
        val STYLESHEET_EXTENSIONS = listOf("css", "pcss", "postcss")
        const val NODE_MODULES_DIRECTORY = "node_modules"
    }
}
