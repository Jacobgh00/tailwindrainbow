package dev.tailwindrainbow.intellij.adapter.intellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.scannedExtension
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.adapter.intellij.variants.ProjectVariants
import dev.tailwindrainbow.intellij.application.highlight.UncolouredVariants
import dev.tailwindrainbow.intellij.bootstrap.PluginComponents

class UncolouredVariantInspection : LocalInspectionTool() {
    override fun checkFile(
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean,
    ): Array<ProblemDescriptor>? {
        val extension = file.scannedExtension() ?: return null
        val effective = PluginComponents.effectiveSettings(file.project)

        if (!effective.enabled) {
            return null
        }

        val declared = ProjectVariants.getInstance(file.project).declared()
        val theme = TailwindRainbowSettings.getInstance().themes.themeNamed(effective.themeName)

        val found =
            UncolouredVariants(effective.scan, theme, declared) { ProgressManager.checkCanceled() }
                .inside(file.text, extension)
                .map { variant ->
                    manager.createProblemDescriptor(
                        file,
                        TextRange(variant.start, variant.end),
                        message("inspection.uncoloured.message", variant.name, effective.themeName),
                        ProblemHighlightType.WEAK_WARNING,
                        isOnTheFly,
                        AddTokenQuickFix(variant.name),
                    )
                }

        return found.toTypedArray()
    }
}
