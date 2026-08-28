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

                when (detector.classify(text, token)) {
                    ClassContent.NONE -> Unit
                    ClassContent.CLASS_NAMES -> addAll(parser.parse(token.content, token.contentStart))
                    ClassContent.EXPRESSION -> addAll(expressionSegments(token, profile, parser))
                }
            }
        }.distinctBy { it.start to it.end }
            .sortedBy(HighlightSegment::start)
    }

    /**
     * Reads the class names out of a bound attribute such as `:class="{ 'p-4': ok }"`.
     *
     * The value is an expression, so only its string literals can hold class names — found with the
     * same lexer that finds strings in a document. An expression without any string is taken as a
     * plain class list, which is how `:class="p-4"` still works.
     */
    private fun expressionSegments(
        token: DocumentToken,
        profile: SyntaxProfile,
        parser: TailwindClassParser,
    ): List<HighlightSegment> {
        val strings = DocumentLexer(profile).tokenize(token.content).filterNot { it.kind == TokenKind.COMMENT }

        if (strings.isEmpty()) {
            return parser.parse(token.content, token.contentStart)
        }

        return strings.flatMap { parser.parse(it.content, token.contentStart + it.contentStart) }
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
                val quote = match.groups[QUOTE_GROUP]?.value?.single() ?: return@forEach
                val valueStart = match.range.last + 1
                val valueEnd = token.content.indexOfUnescaped(quote, valueStart)

                if (valueEnd >= valueStart) {
                    addAll(parser.parse(token.content.substring(valueStart, valueEnd), token.contentStart + valueStart))
                }
            }
        }
}
