package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.ThemeMatcher

/**
 * Orchestration only. [ApplyDirectiveScanner] is separate because `@apply` classes live in plain
 * stylesheet text, outside any token the lexer produces.
 */
class TailwindDocumentScanner {
    fun scan(
        text: String,
        fileExtension: String,
        settings: ScanSettings,
        theme: RainbowTheme,
    ): List<HighlightSegment> {
        val extension = fileExtension.lowercase()

        if (text.length > settings.maxFileSize || extension !in settings.supportedExtensions) {
            return emptyList()
        }

        val profile = SyntaxProfile.of(extension)
        val tokens = DocumentLexer(profile).tokenize(text)
        val parser = TailwindClassParser(ThemeMatcher(theme, settings.ignoredPrefixModifiers))
        val detector = ClassContextDetector(settings)

        return buildList {
            if (profile.hasApplyDirectives) {
                addAll(ApplyDirectiveScanner(parser).scan(text, tokens.filter { it.kind == TokenKind.COMMENT }))
            }

            tokens.filterNot { it.kind == TokenKind.COMMENT }.forEach { token ->
                addAll(nestedAttributeSegments(token, detector, parser))

                if (detector.holdsClassNames(text, token)) {
                    addAll(parser.parse(token.content, token.contentStart))
                }
            }
        }.distinctBy { it.start to it.end }
            .sortedBy(HighlightSegment::start)
    }

    /**
     * Reads `class="…"` attributes written *inside* a token, such as an HTML fragment held in a
     * template literal, where the attribute never appears as a token of its own.
     */
    private fun nestedAttributeSegments(
        token: DocumentToken,
        detector: ClassContextDetector,
        parser: TailwindClassParser,
    ): List<HighlightSegment> =
        buildList {
            val attribute = detector.attributeAssignment ?: return@buildList

            attribute.findAll(token.content).forEach { match ->
                val quote = match.groupValues[1].single()
                val valueStart = match.range.last + 1
                val valueEnd = token.content.indexOfUnescaped(quote, valueStart)

                if (valueEnd >= valueStart) {
                    addAll(parser.parse(token.content.substring(valueStart, valueEnd), token.contentStart + valueStart))
                }
            }
        }
}
