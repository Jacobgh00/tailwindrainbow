package dev.tailwindrainbow.intellij.adapter.intellij.variants

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import dev.tailwindrainbow.intellij.application.variants.variantsDeclaredIn

/**
 * The variants this project declares, read from the files Tailwind declares them in.
 *
 * Read on demand rather than kept up to date: the only caller is the theme editor asking what to
 * offer, which is a click, not a keystroke. Nothing is cached, so nothing can go stale.
 *
 * Bounded on both counts — how many files are opened and how large each may be — because a
 * repository can hold any number of stylesheets and this runs while a dialog is opening.
 */
@Service(Service.Level.PROJECT)
class ProjectVariants(private val project: Project) {
    fun declared(): Set<String> =
        ReadAction.compute<Set<String>, RuntimeException> {
            val scope = ProjectScope.getContentScope(project)

            (configFiles(scope) + styleSheets(scope))
                .take(MAX_FILES)
                .filter { it.length <= MAX_FILE_SIZE }
                .flatMapTo(mutableSetOf()) { variantsDeclaredIn(it.readText()) }
        }

    private fun configFiles(scope: GlobalSearchScope): List<VirtualFile> =
        CONFIG_NAMES.flatMap { name -> FilenameIndex.getVirtualFilesByName(name, scope) }

    private fun styleSheets(scope: GlobalSearchScope): List<VirtualFile> =
        STYLESHEET_EXTENSIONS.flatMap { extension -> FilenameIndex.getAllFilesByExt(project, extension, scope) }

    private fun VirtualFile.readText(): String = runCatching { String(contentsToByteArray()) }.getOrDefault("")

    companion object {
        private const val MAX_FILES = 200
        private const val MAX_FILE_SIZE = 200_000L

        private val CONFIG_NAMES =
            listOf("tailwind.config.js", "tailwind.config.ts", "tailwind.config.cjs", "tailwind.config.mjs")
        private val STYLESHEET_EXTENSIONS = listOf("css", "pcss", "postcss")

        fun getInstance(project: Project): ProjectVariants = project.service()
    }
}
