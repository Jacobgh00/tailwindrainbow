package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings

/**
 * The settings a file is highlighted with.
 *
 * A project may keep its own recognition rules — which attributes, helpers, and file types hold
 * Tailwind classes — because those describe the codebase and are worth committing next to it. The
 * theme and its colours stay with the user, whichever project is open: a palette is a preference,
 * not a property of the code.
 *
 * A null [projectScan] means the project has not claimed the rules and the user's apply.
 */
fun HighlightSettings.withProjectRecognition(projectScan: ScanSettings?): HighlightSettings =
    if (projectScan == null) this else copy(scan = projectScan)
