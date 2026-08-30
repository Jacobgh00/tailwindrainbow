package dev.tailwindrainbow.intellij.adapter.intellij.variants

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.concurrency.AppExecutorUtil
import dev.tailwindrainbow.intellij.application.port.VariantFile
import dev.tailwindrainbow.intellij.application.port.VariantFileSource
import dev.tailwindrainbow.intellij.application.variants.VariantScanner
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

@Service(Service.Level.PROJECT)
class ProjectVariants(private val project: Project) : Disposable {
    @Volatile
    private var snapshot = VariantSnapshot(emptySet(), NEVER_READ)

    private val reading = AtomicBoolean(false)

    private val variantScanner =
        VariantScanner(
            sources =
                listOf(
                    VariantFileSource(::configFiles),
                    VariantFileSource(::styleSheets),
                ),
        )

    fun declared(): Set<String> {
        scheduleUnlessCurrent()

        return snapshot.declared
    }

    fun refresh(): Set<String> {
        val found = runReadAction { variantScanner.scan() }

        remember(found)

        return snapshot.declared
    }

    private fun scheduleUnlessCurrent() {
        if (snapshot.readAt == modificationCount() || !reading.compareAndSet(false, true)) return

        ReadAction.nonBlocking<Set<String>> { variantScanner.scan() }
            .expireWith(this)
            .submit(AppExecutorUtil.getAppExecutorService())
            .onSuccess(::remember)
            .onProcessed { reading.set(false) }
    }

    override fun dispose() = Unit

    private fun remember(found: Set<String>) {
        snapshot = VariantSnapshot(found.toSet(), modificationCount())
    }

    private fun modificationCount(): Long = PsiModificationTracker.getInstance(project).modificationCount

    private fun configFiles(): Sequence<VariantFile> {
        val scope = ProjectScope.getContentScope(project)

        return CONFIG_NAMES.asSequence()
            .flatMap { name -> FilenameIndex.getVirtualFilesByName(name, scope).asSequence() }
            .map { it.asVariantFile() }
    }

    private fun styleSheets(): Sequence<VariantFile> {
        val scope = ProjectScope.getContentScope(project)

        return STYLESHEET_EXTENSIONS.asSequence()
            .flatMap { extension -> FilenameIndex.getAllFilesByExt(project, extension, scope).asSequence() }
            .map { it.asVariantFile() }
    }

    private fun VirtualFile.asVariantFile(): VariantFile = VariantFile(length) { readContents() }

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

    private data class VariantSnapshot(
        val declared: Set<String>,
        val readAt: Long,
    )

    companion object {
        private const val NEVER_READ = -1L

        private val CONFIG_NAMES =
            listOf("tailwind.config.js", "tailwind.config.ts", "tailwind.config.cjs", "tailwind.config.mjs")
        private val STYLESHEET_EXTENSIONS = listOf("css", "pcss", "postcss")

        fun getInstance(project: Project): ProjectVariants = project.service()
    }
}
