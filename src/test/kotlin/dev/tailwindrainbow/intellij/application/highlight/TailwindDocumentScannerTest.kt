package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.application.port.Cancellation
import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TailwindDocumentScannerTest {
    private val hover = TextStyle("#00ff00", FontWeight.BOLD)
    private val responsive = TextStyle("#ff00ff", FontWeight.BOLD)
    private val baseStyle = TextStyle("#0000ff", FontWeight.NORMAL)
    private val scanner = TailwindDocumentScanner()
    private val theme = RainbowTheme(prefix = mapOf("hover" to hover, "lg" to responsive))

    @Test
    fun `finds classes in an html class attribute`() {
        val source = "<div class=\"hover:bg-blue-500 lg:text-xl\"></div>"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "html").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `a base rule colours the utility class itself`() {
        val source = "<div class=\"bg-blue-500 lg:bg-blue-500\"></div>"
        val withBase = theme.copy(base = mapOf("bg-*" to baseStyle))

        val segments = scan(source, "html", theme = withBase)

        assertEquals(listOf("bg-blue-500", "lg:", "bg-blue-500"), segments.map { it.sliceOf(source) })
        assertEquals(listOf(baseStyle, responsive, baseStyle), segments.map { it.style })
    }

    @Test
    fun `finds classes in a bound class attribute`() {
        val source = "<div :class=\"hover:bg-blue-500\"></div>"

        assertEquals(listOf("hover:bg-blue-500"), scan(source, "vue").map { it.sliceOf(source) })
    }

    @Test
    fun `finds classes in the long and Alpine binding forms`() {
        val long = "<div v-bind:class=\"hover:bg-blue-500\"></div>"
        val alpine = "<div x-bind:class=\"lg:text-xl\"></div>"

        assertEquals(listOf("hover:bg-blue-500"), scan(long, "vue").map { it.sliceOf(long) })
        assertEquals(listOf("lg:text-xl"), scan(alpine, "html").map { it.sliceOf(alpine) })
    }

    @Test
    fun `a bound attribute colours the strings inside its expression`() {
        val source = "<div :class=\"{ 'hover:bg-blue-500': ok, 'lg:text-xl': other }\"></div>"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "vue").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `a bound attribute holding an array does not colour the array itself`() {
        val source = "<div :class=\"['hover:bg-blue-500', extra]\"></div>"
        val withArbitrary = theme.copy(arbitrary = TextStyle("#ffaa00", FontWeight.BOLD))

        assertEquals(
            listOf("hover:bg-blue-500"),
            scan(source, "vue", theme = withArbitrary).map { it.sliceOf(source) },
        )
    }

    @Test
    fun `a binding marker does not open every attribute ending in class`() {
        val source = "<div :superclass=\"hover:bg-blue-500\"></div>"

        assertTrue(scanSyntaxOnly(source, "vue").isEmpty())
    }

    @Test
    fun `finds strings inside a Svelte class expression`() {
        val source = "<div class={active ? 'hover:bg-blue-500' : 'lg:text-xl'}></div>"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "svelte").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds nested strings passed to class helper functions`() {
        val source = "const value = clsx('hover:bg-blue-500', active && 'lg:text-xl')"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds classes passed to a method on a class helper`() {
        val source = "el.classList.add('hover:bg-blue-500')"

        assertEquals(listOf("hover:bg-blue-500"), scan(source, "ts").map { it.sliceOf(source) })
    }

    @Test
    fun `a method call on something unrelated is left alone`() {
        assertTrue(scanSyntaxOnly("logger.add('hover:bg-blue-500')", "ts").isEmpty())
    }

    @Test
    fun `finds classes in an array assigned to a class identifier`() {
        val source = "const classes = ['hover:bg-blue-500', 'lg:text-xl']"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds classes nested in an object assigned to a class identifier`() {
        val source = "const classes = { sizes: ['lg:text-xl'], state: 'hover:bg-blue-500' }"

        assertEquals(
            listOf("lg:text-xl", "hover:bg-blue-500"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `a collection assigned to something else is left alone`() {
        assertTrue(scanSyntaxOnly("const documentation = ['hover:bg-blue-500']", "ts").isEmpty())
        assertTrue(scanSyntaxOnly("const notes = { first: 'hover:bg-blue-500' }", "ts").isEmpty())
    }

    @Test
    fun `finds classes in a template assigned to a class identifier`() {
        val source = "const buttonClasses = `hover:bg-blue-500 lg:text-xl`"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `a name counts as a class name only across a camel case boundary`() {
        val compound = "const cardClassName = 'hover:bg-blue-500'"

        assertEquals(listOf("hover:bg-blue-500"), scan(compound, "ts").map { it.sliceOf(compound) })
        assertTrue(scanSyntaxOnly("const superclass = 'hover:bg-blue-500'", "ts").isEmpty())
        assertTrue(scanSyntaxOnly("const query = `hover:bg-blue-500`", "ts").isEmpty())
    }

    @Test
    fun `an identifier a user configured is still recognised, whatever its shape`() {
        val settings = ScanSettings(classIdentifiers = setOf("wrapper", "wrapperClasses", "tw"))
        val source = "<div wrapper=\"hover:bg-blue-500\"></div>"
        val nested = "const wrapperClasses = 'lg:text-xl'"

        assertEquals(
            listOf("hover:bg-blue-500"),
            scanner.scan(source, "html", settings, theme).map { it.sliceOf(source) },
        )
        assertEquals(listOf("lg:text-xl"), scanner.scan(nested, "ts", settings, theme).map { it.sliceOf(nested) })
    }

    @Test
    fun `a standalone string that reads as a class list is coloured wherever it sits`() {
        val source = "const documentation = 'hover:bg-blue-500'"

        assertEquals(listOf("hover:bg-blue-500"), scan(source, "ts").map { it.sliceOf(source) })
        assertTrue(scanSyntaxOnly(source, "ts").isEmpty(), "no syntax rule claims it; the content does")
    }

    @Test
    fun `a class list in an object no name or call claims is read`() {
        val source =
            "const sizeByAlignment: ReadonlyRecord<\n" +
                "  ArticleContentModel.Image[\"alignment\"],\n" +
                "  string\n" +
                "> = {\n" +
                "  center: \"w-full lg:px-1 lg:max-w-prose\",\n" +
                "  left: \"w-full hover:max-w-1/2\",\n" +
                "}"

        assertEquals(
            listOf("lg:px-1", "lg:max-w-prose", "hover:max-w-1/2"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `prose that merely mentions a class is left alone`() {
        assertTrue(scan("const note = 'see hover:bg-blue-500 for details'", "ts").isEmpty())
        assertTrue(scan("const note = 'Verhalten: hover:aktiv'", "ts").isEmpty())
        assertTrue(scan("const note = 'hover:aktiv'", "ts").isEmpty(), "a known variant over a word is not a class")
    }

    @Test
    fun `punctuation that is not a variant is left alone`() {
        assertTrue(scan("const url = 'https://example.com:8080/path'", "ts").isEmpty())
        assertTrue(scan("const at = '10:30'", "ts").isEmpty())
        assertTrue(scan("const key = 'user:profile:title'", "ts").isEmpty())
        assertTrue(scan("const path = 'C:/Users/x'", "ts").isEmpty())
    }

    @Test
    fun `content recognition can be switched off`() {
        val source = "const sizeByAlignment = { left: 'hover:bg-blue-500' }"

        assertEquals(listOf("hover:bg-blue-500"), scan(source, "ts").map { it.sliceOf(source) })
        assertTrue(scanSyntaxOnly(source, "ts").isEmpty())
    }

    @Test
    fun `does not match class as a suffix of another attribute name`() {
        val source = "<div data-class=\"hover:bg-blue-500\"></div>"

        assertTrue(scanSyntaxOnly(source, "html").isEmpty())
    }

    @Test
    fun `finds strings assigned to class object properties`() {
        val source = "const options = { classes: 'hover:bg-blue-500' }"

        assertEquals(
            listOf("hover:bg-blue-500"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds classes in a tagged template`() {
        val source = "const styles = tw`hover:bg-blue-500 lg:text-xl`"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds classes in a styled components member template`() {
        val source = "const Button = styled.div`hover:bg-blue-500`"

        assertEquals(listOf("hover:bg-blue-500"), scan(source, "ts").map { it.sliceOf(source) })
    }

    @Test
    fun `finds classes when the tag carries a component, a type, or attributes`() {
        val wrapped = "const Button = styled(BaseButton)`hover:bg-blue-500`"
        val typed = "const Button = styled.div<Props>`hover:bg-blue-500`"
        val withAttrs = "const Input = styled.input.attrs({ type: 'text' })`lg:text-xl`"

        assertEquals(listOf("hover:bg-blue-500"), scan(wrapped, "ts").map { it.sliceOf(wrapped) })
        assertEquals(listOf("hover:bg-blue-500"), scan(typed, "tsx").map { it.sliceOf(typed) })
        assertEquals(listOf("lg:text-xl"), scan(withAttrs, "ts").map { it.sliceOf(withAttrs) })
    }

    @Test
    fun `a template tagged by something else is left alone`() {
        assertTrue(scanSyntaxOnly("const query = sql`hover:bg-blue-500`", "ts").isEmpty())
        assertTrue(scanSyntaxOnly("const raw = String.raw`hover:bg-blue-500`", "ts").isEmpty())
        assertTrue(scanSyntaxOnly("const value = wide ? `hover:bg-blue-500` : other", "ts").isEmpty())
    }

    @Test
    fun `finds class attributes inside an html template string`() {
        val source = "const template = `<div class=\"hover:bg-blue-500\"></div>`"

        assertEquals(
            listOf("hover:bg-blue-500"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds classes in apply directives but skips comments`() {
        val source = ".button { @apply hover:bg-blue-500 lg:text-xl; } /* @apply hover:hidden; */"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "css").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `an apply directive wrapped over several lines is read to its end`() {
        val source = ".button {\n  @apply hover:bg-blue-500\n    lg:text-xl;\n}"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "css").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `an apply directive nobody closed stops rather than running through the file`() {
        val runaway = "x".repeat(600)
        val source = ".button { @apply hover:bg-blue-500 $runaway lg:text-xl"

        assertEquals(
            listOf("hover:bg-blue-500"),
            scan(source, "css").map { it.sliceOf(source) },
            "a missing semicolon must not turn the rest of the file into class names",
        )
    }

    @Test
    fun `skips files larger than the configured limit`() {
        val settings = ScanSettings(maxFileSize = 10)

        assertTrue(scanner.scan("<div class=\"hover:block\"></div>", "html", settings, theme).isEmpty())
    }

    @Test
    fun `empty class identifiers disable attribute detection`() {
        val settings =
            ScanSettings(
                classIdentifiers = emptySet(),
                classFunctions = emptySet(),
                templateTags = emptySet(),
                readsClassLikeStrings = false,
            )

        assertTrue(scanner.scan("<div title=\"hover:block\"></div>", "html", settings, theme).isEmpty())
    }

    @Test
    fun `a long scan can be interrupted part-way rather than only once it has finished`() {
        val source = "<div class=\"hover:bg-blue-500\"></div>\n".repeat(100)
        var checks = 0
        val cancelAfterFirstToken =
            Cancellation {
                checks++
                if (checks == 2) throw ScanCancelled()
            }

        assertFailsWith<ScanCancelled> {
            scanner.scan(source, "html", ScanSettings(), theme, cancelAfterFirstToken)
        }
        assertTrue(checks < 100, "cancellation must stop the scan, not be noticed after every token was read")
    }

    @Test
    fun `an attribute whose value starts on the next line is still an attribute`() {
        val source = "<div\n  className=\n  \"hover:bg-blue-500\">"

        assertEquals(listOf("hover"), scan(source, "html").map { it.themeKey })
    }

    @Test
    fun `a class list nested deeper than one bracket is read`() {
        val source = "const classes = [['hover:bg-blue-500'], ['lg:text-xl']]"

        assertEquals(listOf("hover", "lg"), scan(source, "ts").map { it.themeKey })
    }

    @Test
    fun `a class object is read past a nested pair of braces`() {
        val source = "const classes = { spacing: compute({}), state: 'hover:bg-blue-500' }"

        assertEquals(listOf("hover"), scan(source, "ts").map { it.themeKey })
    }

    @Test
    fun `a class object nested inside another is read all the way down`() {
        val source = "const buttonClasses = { size: { small: 'hover:px-4', large: 'lg:text-xl' } }"

        assertEquals(listOf("hover", "lg"), scan(source, "ts").map { it.themeKey })
    }

    @Test
    fun `a nested object assigned to a name that is not class-shaped is left alone`() {
        val source = "const notes = { size: { small: 'hover:px-4' } }"

        assertEquals(
            emptyList(),
            scanSyntaxOnly(source, "ts").map { it.themeKey },
            "the theme colours hover, so this " +
                "would be reported if the name counted",
        )
    }

    @Test
    fun `the search for an assignment does not walk out of the statement it is in`() {
        val source = "const classes = 1\nlog('hover:bg-blue-500')"

        assertEquals(emptyList(), scanSyntaxOnly(source, "ts").map { it.themeKey }, "the call is not a class helper")
    }

    @Test
    fun `a later assignment to an ordinary name wins over an earlier class-shaped one`() {
        val source = "const classes = 'lg:text-xl'\nconst label = 'hover:bg-blue-500'"

        assertEquals(listOf("lg"), scanSyntaxOnly(source, "ts").map { it.themeKey })
    }

    @Test
    fun `a string sitting after a class name in ordinary code is left alone`() {
        val source = "const classes = 'hover:bg-blue-500'\nlog('nothing here', other)"

        assertEquals(listOf("hover"), scan(source, "ts").map { it.themeKey }, "only the assignment is read")
    }

    @Test
    fun `finds classes in an object assigned to a typed class identifier`() {
        val source = "const alignmentClasses: Alignments = { left: 'hover:mr-3', right: 'lg:ml-3' }"

        assertEquals(
            listOf("hover:mr-3", "lg:ml-3"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds classes through a generic annotation, over the lines it is written on`() {
        val source =
            "const alignmentClasses: ReadonlyRecord<\n" +
                "  ArticleContentModel.Image[\"alignment\"],\n" +
                "  string\n" +
                "> = {\n" +
                "  center: 'lg:mx-auto',\n" +
                "  left: 'hover:float-left',\n" +
                "}"

        assertEquals(
            listOf("lg:mx-auto", "hover:float-left"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds classes in a typed array assigned to a class identifier`() {
        val source = "const alignmentClasses: string[] = ['hover:mr-3']"

        assertEquals(listOf("hover:mr-3"), scan(source, "ts").map { it.sliceOf(source) })
    }

    @Test
    fun `an equals sign inside the annotation does not end the declaration`() {
        val source = "const alignmentClasses: Record<T = string> = { left: 'hover:mr-3' }"

        assertEquals(listOf("hover:mr-3"), scan(source, "ts").map { it.sliceOf(source) })
    }

    @Test
    fun `an arrow in the annotation is not read as the assignment`() {
        val source = "const buttonClasses: (a: X) => Y = { left: 'hover:mr-3' }"

        assertEquals(listOf("hover:mr-3"), scan(source, "ts").map { it.sliceOf(source) })
    }

    @Test
    fun `a conditional type keeps the colons it spends on itself`() {
        val source = "const buttonClasses: A extends B ? C : string[] = ['hover:mr-3']"

        assertEquals(
            listOf("hover:mr-3"),
            scan(source, "ts").map { it.sliceOf(source) },
            "the leftmost colon opens the annotation, not the conditional's",
        )
    }

    @Test
    fun `a typed collection assigned to something else is left alone`() {
        assertTrue(scanSyntaxOnly("const options: string[] = ['hover:mr-3']", "ts").isEmpty())
        assertTrue(scanSyntaxOnly("const notes: Record<string, string> = { first: 'hover:mr-3' }", "ts").isEmpty())
        assertTrue(scanSyntaxOnly("const handler: (a: X) => Y = { left: 'hover:mr-3' }", "ts").isEmpty())
        assertTrue(scanSyntaxOnly("const options: A extends B ? C : string[] = ['hover:mr-3']", "ts").isEmpty())
    }

    @Test
    fun `a destructured target is not read as a class-shaped name`() {
        val source = "const { alignmentClasses }: Alignments = { left: 'hover:mr-3' }"

        assertTrue(scanSyntaxOnly(source, "ts").isEmpty(), "the name being bound is not the name being assigned")
    }

    @Test
    fun `an earlier typed declaration does not lend its name to a later one`() {
        assertTrue(scanSyntaxOnly("const buttonClasses: Foo = 1\nconst notes = { a: 'hover:mr-3' }", "ts").isEmpty())
        assertTrue(scanSyntaxOnly("const buttonClasses: Foo = 1, notes = { a: 'hover:mr-3' }", "ts").isEmpty())
        assertTrue(scanSyntaxOnly("const buttonClasses: Foo = 1; const notes = ['hover:mr-3']", "ts").isEmpty())
    }

    private fun scan(
        source: String,
        extension: String,
        theme: RainbowTheme = this.theme,
    ): List<HighlightSegment> = scanner.scan(source, extension, ScanSettings(), theme)

    private fun scanSyntaxOnly(
        source: String,
        extension: String,
    ): List<HighlightSegment> = scanner.scan(source, extension, SYNTAX_ONLY, theme)
}

private val SYNTAX_ONLY = ScanSettings(readsClassLikeStrings = false)

private class ScanCancelled : RuntimeException()

private fun HighlightSegment.sliceOf(source: String): String = source.substring(start, end)
