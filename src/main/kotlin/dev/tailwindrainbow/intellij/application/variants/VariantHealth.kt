package dev.tailwindrainbow.intellij.application.variants

import dev.tailwindrainbow.intellij.application.port.ThemeHealthCatalog
import dev.tailwindrainbow.intellij.application.theme.ThemeHealthContext
import dev.tailwindrainbow.intellij.application.theme.ThemeHealthEntry
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.ThemeMatcher
import dev.tailwindrainbow.intellij.domain.theme.wildcardsCovering

data class VariantHealthReport(
    val assessments: List<VariantAssessment>,
    val theme: ThemeHealthContext,
    val scan: VariantScanResult,
) {
    val problems: List<VariantProblem> get() = assessments.flatMap(VariantAssessment::problems)
}

data class VariantAssessment(
    val name: String,
    val declarations: List<VariantDeclaration>,
    val status: VariantStatus,
    val problems: List<VariantProblem>,
)

sealed interface VariantStatus {
    data class Coloured(val match: VariantMatch) : VariantStatus

    data class Disabled(val entry: ThemeHealthEntry.Disabled) : VariantStatus

    data class Invalid(val entry: ThemeHealthEntry.Invalid) : VariantStatus

    data object MissingColour : VariantStatus
}

sealed interface VariantProblem {
    data class DisabledEntry(val entry: ThemeHealthEntry.Disabled) : VariantProblem

    data class InvalidEntry(val entry: ThemeHealthEntry.Invalid) : VariantProblem

    data object DuplicateDeclaration : VariantProblem

    data object MissingColour : VariantProblem
}

enum class VariantMatchKind {
    EXACT,
    WILDCARD,
}

data class VariantMatch(
    val key: String,
    val kind: VariantMatchKind,
    val entry: ThemeHealthEntry.Usable,
)

class VariantHealthAnalyzer(
    private val themes: ThemeHealthCatalog,
    private val ignoredPrefixModifiers: Set<String>,
) {
    fun analyze(
        themeName: String,
        scan: VariantScanResult,
    ): VariantHealthReport {
        val theme = themes.themeHealthNamed(themeName)
        val lookup = EntryLookup(theme, ThemeMatcher(theme.effectiveTheme, ignoredPrefixModifiers))

        return VariantHealthReport(
            assessments =
                scan.declarations
                    .groupBy(VariantDeclaration::name)
                    .values
                    .map { declarations -> assess(declarations, lookup) },
            theme = theme,
            scan = scan,
        )
    }

    private fun assess(
        declarations: List<VariantDeclaration>,
        lookup: EntryLookup,
    ): VariantAssessment {
        val name = declarations.first().name
        val found = lookup.entriesFor(name)

        val status =
            when {
                found.match != null -> VariantStatus.Coloured(found.match)
                found.disabled != null -> VariantStatus.Disabled(found.disabled)
                found.invalid != null -> VariantStatus.Invalid(found.invalid)
                else -> VariantStatus.MissingColour
            }

        return VariantAssessment(
            name = name,
            declarations = declarations,
            status = status,
            problems =
                buildList {
                    if (declarations.size > 1) add(VariantProblem.DuplicateDeclaration)
                    found.invalid?.let { add(VariantProblem.InvalidEntry(it)) }
                    found.disabled?.let { add(VariantProblem.DisabledEntry(it)) }
                    if (status is VariantStatus.MissingColour) add(VariantProblem.MissingColour)
                },
        )
    }

    private data class FoundEntries(
        val match: VariantMatch?,
        val disabled: ThemeHealthEntry.Disabled?,
        val invalid: ThemeHealthEntry.Invalid?,
    )

    private class EntryLookup(
        private val theme: ThemeHealthContext,
        private val matcher: ThemeMatcher,
    ) {
        private val malformedKeys =
            theme.entriesIn(SegmentKind.PREFIX)
                .filterIsInstance<ThemeHealthEntry.Invalid>()
                .mapTo(mutableSetOf(), ThemeHealthEntry.Invalid::key)

        fun entriesFor(name: String): FoundEntries {
            val candidates = matcher.prefixCandidates(name)
            val wildcards = (theme.effectiveTheme.prefix.keys + malformedKeys).wildcardsCovering(candidates.cleaned)
            val keys = (candidates.exact + wildcards).distinct()

            return FoundEntries(
                match = matchOf(name, candidates.exact),
                disabled =
                    keys.firstNotNullOfOrNull {
                        theme.effectiveEntry(SegmentKind.PREFIX, it) as? ThemeHealthEntry.Disabled
                    },
                invalid = keys.firstNotNullOfOrNull { theme.invalidEntries(SegmentKind.PREFIX, it).firstOrNull() },
            )
        }

        private fun matchOf(
            name: String,
            exactCandidates: List<String>,
        ): VariantMatch? {
            val found = matcher.matchPrefix(name)?.takeIf { it.kind == SegmentKind.PREFIX } ?: return null
            val entry = theme.effectiveEntry(SegmentKind.PREFIX, found.key) as? ThemeHealthEntry.Usable ?: return null

            return VariantMatch(
                key = found.key,
                kind = if (found.key in exactCandidates) VariantMatchKind.EXACT else VariantMatchKind.WILDCARD,
                entry = entry,
            )
        }
    }
}
