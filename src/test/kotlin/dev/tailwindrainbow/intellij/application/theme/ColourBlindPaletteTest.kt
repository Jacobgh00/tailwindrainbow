package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertTrue

class ColourBlindPaletteTest {
    private val families =
        mapOf(
            "breakpoint" to "lg",
            "interaction" to "hover",
            "form state" to "valid",
            "position" to "only",
            "attribute" to "data-*",
            "pseudo-element" to "before",
            "starting" to "starting",
        )

    @Test
    fun `the families stay apart under every dichromacy the palette claims`() {
        val tooClose = conflictsIn(BuiltInThemes.colourBlind)

        assertTrue(tooClose.isEmpty(), "colour-blind confuses: $tooClose")
    }

    @Test
    fun `the check is not vacuous, because the default theme does not pass it`() {
        val tooClose = conflictsIn(BuiltInThemes.default)

        assertTrue(
            tooClose.isNotEmpty(),
            "default passed a check written for a palette that was designed to; the threshold is too low",
        )
    }

    private fun conflictsIn(theme: RainbowTheme): List<String> {
        val colours = families.mapValues { (_, key) -> checkNotNull(theme.prefix[key]) { "no $key" }.color }

        return SIMULATIONS.flatMap { (vision, matrix) ->
            colours.entries.toList().pairs().mapNotNull { (one, other) ->
                val apart = distance(simulate(one.value, matrix), simulate(other.value, matrix))

                "${one.key} and ${other.key} under $vision (${apart.toInt()})".takeIf { apart < MIN_DISTANCE }
            }
        }
    }

    private fun <T> List<T>.pairs(): List<Pair<T, T>> =
        flatMapIndexed { index, item ->
            drop(index + 1).map { item to it }
        }

    private fun simulate(
        color: String,
        matrix: Array<DoubleArray>,
    ): DoubleArray {
        val channels = color.removePrefix("#").chunked(2).map { it.toInt(RADIX).toDouble() }

        return DoubleArray(channels.size) { row ->
            matrix[row].mapIndexed { column, weight -> weight * channels[column] }.sum().coerceIn(0.0, FULL)
        }
    }

    private fun distance(
        one: DoubleArray,
        other: DoubleArray,
    ): Double = sqrt(one.indices.sumOf { (one[it] - other[it]) * (one[it] - other[it]) })

    private companion object {
        const val RADIX = 16
        const val FULL = 255.0

        val SIMULATIONS =
            mapOf(
                "protanopia" to
                    arrayOf(
                        doubleArrayOf(0.152286, 1.052583, -0.204868),
                        doubleArrayOf(0.114503, 0.786281, 0.099216),
                        doubleArrayOf(-0.003882, -0.048116, 1.051998),
                    ),
                "deuteranopia" to
                    arrayOf(
                        doubleArrayOf(0.367322, 0.860646, -0.227968),
                        doubleArrayOf(0.280085, 0.672501, 0.047413),
                        doubleArrayOf(-0.011820, 0.042940, 0.968881),
                    ),
                "tritanopia" to
                    arrayOf(
                        doubleArrayOf(1.255528, -0.076749, -0.178779),
                        doubleArrayOf(-0.078411, 0.930809, 0.147602),
                        doubleArrayOf(0.004733, 0.691367, 0.303900),
                    ),
            )

        /** Chosen with headroom: the palette's closest pair sits at 41, the default theme's at 4. */
        const val MIN_DISTANCE = 30.0
    }
}
