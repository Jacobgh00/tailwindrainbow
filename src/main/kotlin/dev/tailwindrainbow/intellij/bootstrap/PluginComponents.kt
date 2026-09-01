package dev.tailwindrainbow.intellij.bootstrap

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.tailwindrainbow.intellij.adapter.intellij.scannedLength
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowProjectSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.adapter.intellij.variants.ProjectVariants
import dev.tailwindrainbow.intellij.application.diagnostics.Diagnostics
import dev.tailwindrainbow.intellij.application.diagnostics.ScannedFile
import dev.tailwindrainbow.intellij.application.highlight.HighlightDocumentService
import dev.tailwindrainbow.intellij.application.highlight.HighlightingSnapshot
import dev.tailwindrainbow.intellij.application.highlight.statusFor
import dev.tailwindrainbow.intellij.application.port.Cancellation
import dev.tailwindrainbow.intellij.application.port.HighlightDocument
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.port.SettingsProvider
import dev.tailwindrainbow.intellij.application.port.ThemeCatalog
import dev.tailwindrainbow.intellij.application.settings.withProjectRecognition
import dev.tailwindrainbow.intellij.application.variants.VariantHealthAnalyzer
import dev.tailwindrainbow.intellij.application.variants.VariantHealthReport

object PluginComponents {
    private const val VERSION_RESOURCE = "/tailwind-rainbow-version.txt"

    fun highlightDocument(project: Project): HighlightDocument =
        highlightDocument(
            settings = { effectiveSettings(project) },
            themes = TailwindRainbowSettings.getInstance(),
        )

    internal fun highlightingSnapshot(project: Project): HighlightingSnapshot {
        val settings = effectiveSettings(project)

        return HighlightingSnapshot(
            settings = settings,
            theme = TailwindRainbowSettings.getInstance().themeNamed(settings.themeName),
        )
    }

    internal fun highlightDocument(snapshot: HighlightingSnapshot): HighlightDocument =
        highlightDocument(
            settings = { snapshot.settings },
            themes = { snapshot.theme },
        )

    private fun highlightDocument(
        settings: SettingsProvider,
        themes: ThemeCatalog,
    ): HighlightDocument =
        HighlightDocumentService(
            settings = settings,
            themes = themes,
            cancellation = { ProgressManager.checkCanceled() },
            log = { extension, status -> thisLogger().debug("A .$extension file went unpainted: $status") },
        )

    fun effectiveSettings(project: Project): HighlightSettings =
        TailwindRainbowSettings.getInstance()
            .current()
            .withProjectRecognition(TailwindRainbowProjectSettings.getInstance(project).recognition())

    fun variantHealth(
        project: Project,
        cancellation: Cancellation = Cancellation.NONE,
    ): VariantHealthReport {
        val settings = effectiveSettings(project)
        val scan = ProjectVariants.getInstance(project).refreshScan(cancellation)

        return VariantHealthAnalyzer(
            themes = TailwindRainbowSettings.getInstance().themes,
        ).analyze(settings.themeName, scan)
    }

    fun diagnostics(
        project: Project,
        file: VirtualFile?,
    ): Diagnostics {
        val settingsService = TailwindRainbowSettings.getInstance()
        val projectRecognition = TailwindRainbowProjectSettings.getInstance(project).recognition()
        val settings = settingsService.current().withProjectRecognition(projectRecognition)

        return Diagnostics(
            pluginVersion = pluginVersion(),
            ide = runningIde(),
            settings = settings,
            recognitionFromProject = projectRecognition != null,
            file = file?.let { scannedFile(it, settings) },
            themeProblems = settingsService.themes.problems(),
        )
    }

    private fun scannedFile(
        file: VirtualFile,
        settings: HighlightSettings,
    ): ScannedFile {
        val extension = file.extension.orEmpty()

        return ScannedFile(extension, settings.statusFor(extension, file.scannedLength()))
    }

    /**
     * Read from a resource the build writes, not from the plugin descriptor: every platform method
     * that hands one out is either marked internal or deprecated in 2026.2.
     */
    private fun pluginVersion(): String =
        javaClass.getResourceAsStream(VERSION_RESOURCE)?.use { it.reader().readText().trim() } ?: "unknown"

    private fun runningIde(): String = with(ApplicationInfo.getInstance()) { "$fullApplicationName ($build)" }
}
