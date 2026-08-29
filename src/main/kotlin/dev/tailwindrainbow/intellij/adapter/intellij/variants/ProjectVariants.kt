package dev.tailwindrainbow.intellij.adapter.intellij.variants

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.concurrency.AppExecutorUtil
import dev.tailwindrainbow.intellij.application.variants.variantsDeclaredIn
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

@Service(Service.Level.PROJECT)
class ProjectVariants(private val project: Project) {
    @Volatile
    private var known: Set<String> = emptySet()

    @Volatile
    private var knownAt: Long = NEVER_READ

    private val reading = AtomicBoolean(false)

    fun declared(): Set<String> {
        scheduleUnlessCurrent()

        return known
    }

    fun refresh(): Set<String> {
        val found = ReadAction.compute<Set<String>, RuntimeException> { read() }

        remember(found)

        return found
    }

    private fun scheduleUnlessCurrent() {
        if (knownAt == modificationCount() || !reading.compareAndSet(false, true)) return

        ReadAction.nonBlocking<Set<String>> { read() }
            .expireWith(project)
            .submit(AppExecutorUtil.getAppExecutorService())
            .onSuccess(::remember)
            .onProcessed { reading.set(false) }
    }

    private fun remember(found: Set<String>) {
        known = found
        knownAt = modificationCount()
    }

    private fun modificationCount(): Long = PsiModificationTracker.getInstance(project).modificationCount

    private fun read(): Set<String> =
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

    private fun VirtualFile.readText(): String =
        try {
            String(contentsToByteArray())
        } catch (unreadable: IOException) {
            thisLogger().debug("Skipped $path", unreadable)
            ""
        } catch (gone: InvalidVirtualFileAccessException) {
            thisLogger().debug("Skipped $path", gone)
            ""
        }

    companion object {
        private const val NEVER_READ = -1L
        private const val MAX_FILES = 200
        private const val MAX_FILE_SIZE = 200_000L

        private val CONFIG_NAMES =
            listOf("tailwind.config.js", "tailwind.config.ts", "tailwind.config.cjs", "tailwind.config.mjs")
        private val STYLESHEET_EXTENSIONS = listOf("css", "pcss", "postcss")

        fun getInstance(project: Project): ProjectVariants = project.service()
    }
}
