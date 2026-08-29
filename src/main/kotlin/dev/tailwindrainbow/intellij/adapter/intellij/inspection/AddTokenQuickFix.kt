package dev.tailwindrainbow.intellij.adapter.intellij.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settingsChanged
import dev.tailwindrainbow.intellij.application.settings.ADDED_TOKEN_COLOR
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

class AddTokenQuickFix(private val variant: String) : LocalQuickFix {
    override fun getFamilyName(): String = message("inspection.uncoloured.fix", variant)

    override fun applyFix(
        project: Project,
        descriptor: ProblemDescriptor,
    ) {
        val settings = TailwindRainbowSettings.getInstance()
        val themeName = settings.current().themeName
        val entry = StyleEntry(SegmentKind.PREFIX, variant, ADDED_TOKEN_COLOR, FontWeight.BOLD.value)
        val existing = settings.themes.overrides()
        val edited = existing.firstOrNull { it.name == themeName } ?: ThemeSpec(themeName, emptyList())

        settings.update(
            settings.current(),
            existing.filterNot { it.name == themeName } + edited.copy(entries = edited.entries + entry),
        )
        settingsChanged()
    }
}
