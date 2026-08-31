package dev.tailwindrainbow.intellij.adapter.intellij.actions

import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.theme.ThemeEntryProvenance
import dev.tailwindrainbow.intellij.application.variants.VariantAssessment
import dev.tailwindrainbow.intellij.application.variants.VariantDeclaration
import dev.tailwindrainbow.intellij.application.variants.VariantDeclarationKind
import dev.tailwindrainbow.intellij.application.variants.VariantHealthReport
import dev.tailwindrainbow.intellij.application.variants.VariantMatchKind
import dev.tailwindrainbow.intellij.application.variants.VariantProblem
import dev.tailwindrainbow.intellij.application.variants.VariantStatus

internal fun canOpen(declaration: VariantDeclaration): Boolean = declaration.location != null

internal fun VariantAssessment.describe(): String {
    val duplicate =
        if (declarations.size > 1) message("variantHealth.declarationCount", declarations.size) else ""

    return message("variantHealth.assessment", name, status.describe(), duplicate)
}

internal fun VariantAssessment.describeDetails(report: VariantHealthReport): String =
    buildString {
        appendLine(describe())
        appendLine(message("variantHealth.details.theme", report.theme.name))
        appendLine(status.describeDetails())
        problems.forEach { problem -> appendLine(message("variantHealth.details.problem", problem.describe())) }
        appendLine(message("variantHealth.details.declarations"))
        declarations.forEach { appendLine(message("variantHealth.details.declaration", it.describe())) }
    }

internal fun VariantDeclaration.describe(): String =
    location?.let { message("variantHealth.declaration", kind.label(), it.path, it.startOffset) }
        ?: message("variantHealth.declaration.unknown", kind.label())

private fun VariantStatus.describe(): String =
    when (this) {
        is VariantStatus.Coloured -> {
            val wildcard =
                if (match.kind == VariantMatchKind.WILDCARD) message("variantHealth.wildcardSuffix") else ""

            message("variantHealth.coloured", match.key, match.entry.provenance.label(), wildcard)
        }
        is VariantStatus.Disabled -> message("variantHealth.disabled", entry.key)
        is VariantStatus.Invalid -> message("variantHealth.invalid", entry.key)
        VariantStatus.MissingColour -> message("variantHealth.missing")
    }

private fun VariantStatus.describeDetails(): String =
    when (this) {
        is VariantStatus.Coloured ->
            message("variantHealth.details.winning", match.key) + "\n" +
                message("variantHealth.details.provenance", match.entry.provenance.label(), match.entry.sourceName)

        is VariantStatus.Disabled ->
            message("variantHealth.details.disabled", entry.key) + "\n" +
                message("variantHealth.details.provenance", entry.provenance.label(), entry.sourceName)

        is VariantStatus.Invalid ->
            message("variantHealth.details.invalid", entry.key, entry.problem) + "\n" +
                message("variantHealth.details.provenance", entry.provenance.label(), entry.sourceName)

        VariantStatus.MissingColour -> message("variantHealth.problem.missing")
    }

private fun VariantProblem.describe(): String =
    when (this) {
        is VariantProblem.DisabledEntry -> message("variantHealth.problem.disabled", entry.key)
        is VariantProblem.InvalidEntry -> message("variantHealth.problem.invalid", entry.key, entry.problem)
        VariantProblem.DuplicateDeclaration -> message("variantHealth.problem.duplicate")
        VariantProblem.MissingColour -> message("variantHealth.problem.missing")
    }

private fun VariantDeclarationKind.label(): String =
    when (this) {
        VariantDeclarationKind.CUSTOM_VARIANT -> message("variantHealth.declaration.customVariant")
        VariantDeclarationKind.BREAKPOINT -> message("variantHealth.declaration.breakpoint")
        VariantDeclarationKind.SCREEN -> message("variantHealth.declaration.screen")
        VariantDeclarationKind.ADD_VARIANT -> message("variantHealth.declaration.addVariant")
    }

private fun ThemeEntryProvenance.label(): String =
    when (this) {
        ThemeEntryProvenance.BUILT_IN -> message("variantHealth.provenance.builtIn")
        ThemeEntryProvenance.CONTRIBUTED -> message("variantHealth.provenance.contributed")
        ThemeEntryProvenance.BASE -> message("variantHealth.provenance.base")
        ThemeEntryProvenance.USER_OVERRIDE -> message("variantHealth.provenance.user")
    }
