package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.application.port.Cancellation
import dev.tailwindrainbow.intellij.application.port.HighlightDocument
import dev.tailwindrainbow.intellij.application.port.ScanLog
import dev.tailwindrainbow.intellij.application.port.SettingsProvider
import dev.tailwindrainbow.intellij.application.port.ThemeCatalog
import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment

class HighlightDocumentService(
    private val settings: SettingsProvider,
    private val themes: ThemeCatalog,
    private val scanner: TailwindDocumentScanner = TailwindDocumentScanner(),
    private val cancellation: Cancellation = Cancellation.NONE,
    private val log: ScanLog = ScanLog.NONE,
) : HighlightDocument {
    override fun highlight(
        text: String,
        fileExtension: String,
    ): List<HighlightSegment> {
        val current = settings.current()
        val status = current.statusFor(fileExtension, text.length)

        if (status != ScanStatus.SCANNED) {
            log.skipped(fileExtension, status)

            return emptyList()
        }

        return scanner.scan(
            text = text,
            fileExtension = fileExtension,
            settings = current.scan,
            theme = themes.themeNamed(current.themeName),
            cancellation = cancellation,
        )
    }
}
