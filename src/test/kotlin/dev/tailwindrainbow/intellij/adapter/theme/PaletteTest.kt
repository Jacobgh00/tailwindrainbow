package dev.tailwindrainbow.intellij.adapter.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class PaletteTest {
    @Test
    fun `named palette members map to their semantic variants`() {
        val theme = palette().toTheme()

        assertEquals("#000001", theme.prefix.getValue("sm").color)
        assertEquals("#000002", theme.prefix.getValue("md").color)
        assertEquals("#000003", theme.prefix.getValue("lg").color)
        assertEquals("#000004", theme.prefix.getValue("xl").color)
        assertEquals("#000005", theme.prefix.getValue("2xl").color)
        assertEquals("#000006", theme.prefix.getValue("hover").color)
        assertEquals("#000007", theme.prefix.getValue("focus").color)
        assertEquals("#000008", theme.prefix.getValue("active").color)
        assertEquals("#000009", theme.prefix.getValue("placeholder").color)
        assertEquals("#00000a", theme.prefix.getValue("checked").color)
        assertEquals("#00000b", theme.prefix.getValue("valid").color)
        assertEquals("#00000c", theme.prefix.getValue("invalid").color)
        assertEquals("#00000d", theme.prefix.getValue("disabled").color)
        assertEquals("#00000e", theme.prefix.getValue("required").color)
        assertEquals("#00000f", theme.prefix.getValue("first").color)
        assertEquals("#000010", theme.prefix.getValue("last").color)
        assertEquals("#000011", theme.prefix.getValue("only").color)
        assertEquals("#000012", theme.prefix.getValue("odd").color)
        assertEquals("#000013", theme.prefix.getValue("even").color)
        assertEquals("#000014", theme.prefix.getValue("nth-*").color)
        assertEquals("#000015", theme.prefix.getValue("data-*").color)
        assertEquals("#000016", theme.prefix.getValue("aria-*").color)
        assertEquals("#000017", theme.prefix.getValue("supports-*").color)
    }

    @Test
    fun `every scoping modifier maps to the one modifier colour`() {
        val theme = palette().toTheme()

        listOf("group", "peer", "has", "in", "not").forEach { modifier ->
            assertEquals("#000024", theme.prefix.getValue(modifier).color, "'$modifier' is not painted as a scope")
        }
    }

    private fun palette() =
        Palette(
            arbitrary = "#000018",
            important = "#000019",
            universal = "#00001a",
            breakpoints =
                BreakpointColors(
                    sm = "#000001",
                    md = "#000002",
                    lg = "#000003",
                    xl = "#000004",
                    twoXl = "#000005",
                ),
            container = "#00001b",
            before = "#00001c",
            after = "#00001d",
            interaction = InteractionColors(hover = "#000006", focus = "#000007", active = "#000008"),
            visited = "#00001e",
            formStates =
                FormStateColors(
                    placeholder = "#000009",
                    checked = "#00000a",
                    valid = "#00000b",
                    invalid = "#00000c",
                    disabled = "#00000d",
                    required = "#00000e",
                ),
            structural =
                StructuralColors(
                    first = "#00000f",
                    last = "#000010",
                    only = "#000011",
                    odd = "#000012",
                    even = "#000013",
                    nth = "#000014",
                ),
            attributes = AttributeColors(data = "#000015", aria = "#000016", supports = "#000017"),
            modifier = "#000024",
            open = "#00001f",
            inert = "#000020",
            starting = "#000021",
            media = "#000022",
            direction = "#000023",
        )
}
