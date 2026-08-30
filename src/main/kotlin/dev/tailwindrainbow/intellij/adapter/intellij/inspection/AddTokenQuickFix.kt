package dev.tailwindrainbow.intellij.adapter.intellij.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settingsChanged
import dev.tailwindrainbow.intellij.application.settings.addingEntry
import dev.tailwindrainbow.intellij.application.settings.newThemeEntry
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

class AddTokenQuickFix(private val variant: String) : LocalQuickFix {
    override fun getFamilyName(): String = message("inspection.uncoloured.fix", variant)

    override fun applyFix(
        project: Project,
        descriptor: ProblemDescriptor,
    ) {
        val settings = TailwindRainbowSettings.getInstance()
        val current = settings.current()
        val entry = newThemeEntry(SegmentKind.PREFIX, variant)

        settings.update(
            current,
            settings.themes.overrides().addingEntry(current.themeName, entry),
        )
        settingsChanged()
    }
}
