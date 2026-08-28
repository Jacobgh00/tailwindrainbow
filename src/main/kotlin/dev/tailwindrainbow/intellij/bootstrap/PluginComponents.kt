package dev.tailwindrainbow.intellij.bootstrap

import com.intellij.openapi.project.Project
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowProjectSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.application.highlight.HighlightDocumentService
import dev.tailwindrainbow.intellij.application.port.HighlightDocument
import dev.tailwindrainbow.intellij.application.settings.withProjectRecognition

/**
 * The single place concrete adapters are bound to ports; everything else depends on interfaces,
 * which is what lets the application layer be tested without an IDE.
 */
object PluginComponents {
    fun highlightDocument(project: Project): HighlightDocument {
        val application = TailwindRainbowSettings.getInstance()
        val forProject = TailwindRainbowProjectSettings.getInstance(project)

        return HighlightDocumentService(
            settings = { application.current().withProjectRecognition(forProject.recognition()) },
            themes = application,
        )
    }
}
