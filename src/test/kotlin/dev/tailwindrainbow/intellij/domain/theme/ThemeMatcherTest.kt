package dev.tailwindrainbow.intellij.domain.theme

import kotlin.collections.mapOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ThemeMatcherTest {
    private val exact = TextStyle("#111111", FontWeight.BOLD)
    private val wildcard = TextStyle("#222222", FontWeight.BOLD)
    private val arbitrary = TextStyle("#333333", FontWeight.BOLD)

    private val matcher =
        ThemeMatcher(
            theme =
                RainbowTheme(
                    prefix =
                        mapOf(
                            "hover" to exact,
                            "min-*" to wildcard,
                        ),
                    base =
                        mapOf(
                            "bg-blue-500" to exact,
                            "bg-*" to wildcard,
                        ),
                    arbitrary = arbitrary,
                ),
            ignoredPrefixModifiers = setOf("group", "peer"),
        )

    @Test
    fun `exact prefix matches before wildcard`() {
        assertEquals(ThemeMatch("hover", exact, SegmentKind.PREFIX), matcher.matchPrefix("hover"))
    }

    @Test
    fun `wildcard prefix matches dynamic variants`() {
        assertEquals(ThemeMatch("min-*", wildcard, SegmentKind.PREFIX), matcher.matchPrefix("min-[480px]"))
    }

    @Test
    fun `ignored modifier resolves to the underlying prefix`() {
        assertEquals(ThemeMatch("hover", exact, SegmentKind.PREFIX), matcher.matchPrefix("group-hover"))
    }

    @Test
    fun `named group resolves to the unnamed prefix`() {
        assertEquals(ThemeMatch("hover", exact, SegmentKind.PREFIX), matcher.matchPrefix("hover/card"))
    }

    @Test
    fun `exact base class wins over wildcard`() {
        assertEquals(ThemeMatch("bg-blue-500", exact, SegmentKind.BASE), matcher.matchBase("bg-blue-500"))
    }

    @Test
    fun `standalone arbitrary class uses arbitrary style`() {
        assertEquals(
            ThemeMatch("arbitrary", arbitrary, SegmentKind.ARBITRARY),
            matcher.matchBase("[mask-type:luminance]"),
        )
    }

    @Test
    fun `a bracketed prefix behind an ignored modifier is still arbitrary`() {
        assertEquals(
            ThemeMatch("arbitrary", arbitrary, SegmentKind.ARBITRARY),
            matcher.matchPrefix("peer-[:checked]"),
        )
    }

    @Test
    fun `a bracketed prefix carrying a group name is still arbitrary`() {
        assertEquals(
            ThemeMatch("arbitrary", arbitrary, SegmentKind.ARBITRARY),
            matcher.matchPrefix("group-[.is-open]/menu"),
        )
    }

    @Test
    fun `a bracket holding a slash is not mistaken for a group name`() {
        assertEquals(
            ThemeMatch("arbitrary", arbitrary, SegmentKind.ARBITRARY),
            matcher.matchPrefix("peer-[aspect-ratio:1/8]"),
        )
    }

    @Test
    fun `unknown token has no match`() {
        assertNull(matcher.matchPrefix("unknown"))
        assertNull(matcher.matchBase("flex"))
    }

    @Test
    fun `literal star variants do not behave as catch-all wildcards`() {
        val starMatcher =
            ThemeMatcher(
                theme = RainbowTheme(prefix = mapOf("*" to exact, "**" to wildcard)),
                ignoredPrefixModifiers = emptySet(),
            )

        assertEquals(ThemeMatch("*", exact, SegmentKind.PREFIX), starMatcher.matchPrefix("*"))
        assertEquals(ThemeMatch("**", wildcard, SegmentKind.PREFIX), starMatcher.matchPrefix("**"))
        assertNull(starMatcher.matchPrefix("focus"))
    }

    @Test
    fun `the matcher decides the segment kind, callers never infer it from the key`() {
        assertEquals(SegmentKind.ARBITRARY, matcher.matchPrefix("[&>*]")?.kind)
        assertEquals(SegmentKind.PREFIX, matcher.matchPrefix("min-[480px]")?.kind)
        assertEquals(SegmentKind.BASE, matcher.matchBase("bg-teal-100")?.kind)
    }
}
