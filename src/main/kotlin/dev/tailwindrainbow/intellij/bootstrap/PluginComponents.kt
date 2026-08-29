package dev.tailwindrainbow.intellij.bootstrap

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.tailwindrainbow.intellij.adapter.intellij.scannedLength
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowProjectSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.application.diagnostics.Diagnostics
import dev.tailwindrainbow.intellij.application.diagnostics.ScannedFile
import dev.tailwindrainbow.intellij.application.highlight.HighlightDocumentService
import dev.tailwindrainbow.intellij.application.highlight.statusFor
import dev.tailwindrainbow.intellij.application.port.HighlightDocument
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.settings.withProjectRecognition

object PluginComponents {
    private val PLUGIN = PluginId.getId("dev.tailwindrainbow")

    fun highlightDocument(project: Project): HighlightDocument =
        HighlightDocumentService(
            settings = { effectiveSettings(project) },
            themes = TailwindRainbowSettings.getInstance(),
            cancellation = { ProgressManager.checkCanceled() },
            log = { extension, status -> thisLogger().debug("A .$extension file went unpainted: $status") },
        )

    fun effectiveSettings(project: Project): HighlightSettings =
        TailwindRainbowSettings.getInstance()
            .current()
            .withProjectRecognition(TailwindRainbowProjectSettings.getInstance(project).recognition())

    fun diagnostics(
        project: Project,
        file: VirtualFile?,
    ): Diagnostics {
        val settings = effectiveSettings(project)

        return Diagnostics(
            pluginVersion = pluginVersion(),
            ide = runningIde(),
            settings = settings,
            recognitionFromProject = TailwindRainbowProjectSettings.getInstance(project).recognition() != null,
            file = file?.let { scannedFile(it, settings) },
            themeProblems = TailwindRainbowSettings.getInstance().themes.problems(),
        )
    }

    private fun scannedFile(
        file: VirtualFile,
        settings: HighlightSettings,
    ): ScannedFile {
        val extension = file.extension.orEmpty()

        return ScannedFile(extension, settings.statusFor(extension, file.scannedLength()))
    }

    private fun pluginVersion(): String = PluginManagerCore.getPlugin(PLUGIN)?.version ?: "unknown"

    private fun runningIde(): String = with(ApplicationInfo.getInstance()) { "$fullApplicationName ($build)" }
}
