package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class AnnotatorRobustnessTest : PaintedFileTest() {
    @Test
    fun `documents built to break the scanner are annotated rather than reported as a plugin error`() {
        val painted =
            HOSTILE_FILES.entries.sumOf { (name, text) -> painted(name, text).size }

        assertTrue(painted > 0, "nothing was painted, so nothing was proved")
    }

    private companion object {
        val HOSTILE_FILES =
            mapOf(
                "empty.html" to "",
                "unterminated.html" to """<div class="hover:bg-red-500""",
                "colons.html" to """<div class="::::">""",
                "arbitrary.html" to """<div class="peer-[aspect-ratio:1/8]:bg-red-500">""",
                "unicode.html" to """<div class="hÖver:bg-red-500 🎨:text-black">""",
                "expression.tsx" to """const a = <div className={cond ? "hover:bg-red-500" : "focus:"} />""",
                "directive.css" to "@apply hover:bg-red-500",
                "bare-directive.css" to "@apply",
                "repeated.html" to """<div class="${"hover:".repeat(2_000)}">""",
                "long-token.html" to """<div class="${"a".repeat(10_000)}:bg-red-500">""",
            )
    }
}
