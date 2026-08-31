package dev.tailwindrainbow.intellij.adapter.intellij.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.settings.addTokenToCurrentTheme

class AddTokenQuickFix(private val variant: String) : LocalQuickFix {
    override fun getFamilyName(): String = message("inspection.uncoloured.fix", variant)

    override fun applyFix(
        project: Project,
        descriptor: ProblemDescriptor,
    ) {
        addTokenToCurrentTheme(variant)
    }
}
