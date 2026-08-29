# Tailwind Rainbow

An IntelliJ Platform plugin that colorizes Tailwind CSS variant prefixes, so a long class list
stays readable.

Every variant — `hover`, `focus`, `sm`, `dark`, arbitrary values such as `[&>*]`, and the `!`
important modifier wherever Tailwind puts it (`!font-bold`, `hover:!font-bold`, `font-bold!`) — is
painted in its own color. The structure of a class list becomes visible at a glance instead of
having to be read word by word.

```
hover:bg-blue-500  focus:ring-2  sm:px-4  dark:text-white  [&>*]:mt-2  !font-bold
^^^^^              ^^^^^         ^^       ^^^^             ^^^^^        ^
green              teal          purple   slate            coral        red
```

## Features

- **Variant highlighting** in class attributes, class helper functions, tagged templates, and CSS
  `@apply` directives, including ones wrapped over several lines. A tagged template is recognized by
  what it hangs off, so `` styled.div`…` ``,
  `` styled(Button)`…` ``, and `` styled.div<Props>`…` `` all count as `styled`.
- **Framework bindings** — `:class`, `v-bind:class`, and `x-bind:class`. A bound attribute holds an
  expression, so the class names are read out of the strings inside it: `:class="{ 'lg:p-4': ok }"`
  colors `lg:p-4` and leaves the braces and the condition alone.
- **Tailwind v3 and v4.** The important modifier is read in every position either version allows, an
  `@apply` directive ends where CSS says it does rather than at the end of a line, and the built-in
  themes color what v4 added — container queries, `data-*`, `aria-*`, `supports-*`, `nth-*`, `open`,
  `inert` and `starting`.
- **Four built-in themes** — `default`, `synthwave`, `colour-blind` and `editor scheme` — plus any
  number of your own, each started from one of them. Every declared theme colors every variant
  Tailwind documents, with related variants sharing a color: `focus-visible` is the color of `focus`,
  and every pointer and motion query is the color of `dark`.
- **A palette for color-blind readers.** `colour-blind` is built on the Okabe–Ito colors, one hue per
  variant family, and a test simulates protanopia, deuteranopia and tritanopia to hold the families
  apart.
- **A theme that follows your editor.** `editor scheme` takes its colors from the IDE's own syntax
  colors — keyword, string, number, metadata — and recomputes when you switch color scheme.
- **User-defined colors.** Pick a color for any variant with a color picker, toggle bold, or switch
  an entry off so that variant is left alone. Overrides are stored per entry, so a theme you
  customize keeps inheriting everything you did not touch.
- **User-defined tokens.** Add a variant no theme lists — `focus-visible`, `aria-*`, a variant of
  your own — with `*` usable as a wildcard.
- **Optional base-class coloring.** Add a base pattern such as `bg-*` and the utility itself is
  colored, alongside its variants: in `lg:bg-blue-500`, `lg:` takes the variant color and
  `bg-blue-500` the base one. No built-in theme colors base classes, because variants stop standing
  out once everything else is colored too.
- **Readable on any editor background.** A color that would not contrast with the background you
  are using is darkened or lightened until it does, keeping its hue. On a dark scheme the built-in
  palettes are used exactly as they are; on a light one they adapt.
- **Configurable recognition** — which attributes, helper functions, tagged templates, ignored
  modifiers, and file extensions are scanned, and the file size above which scanning is skipped.
  A project can keep its own answers and commit them.

## Installation

From the IDE: **Settings | Plugins | Marketplace**, search for *Tailwind Rainbow*, and click
**Install**.

To install a local build:

```bash
./gradlew buildPlugin
```

Then **Settings | Plugins | ⚙ | Install Plugin from Disk…** and choose
`build/distributions/tailwindrainbow-<version>.zip`.

## Switching themes

**Find Action** (`⇧⌘A` / `Ctrl+Shift+A`) → **Select Tailwind Rainbow Theme** lists every theme and
previews each one in the editor as you arrow through it. The preview is discarded unless you pick a
theme, so leaving with `Esc` changes nothing.

A variant your project declares — through `@custom-variant`, `addVariant(…)` or a custom screen — that
your theme has no color for is reported as a weak warning where it is used, with a quick fix that adds
it to the theme. Switch it off under **Settings | Editor | Inspections | Tailwind Rainbow** if you
would rather not know.

**Explain Tailwind Colouring at Caret**, also through Find Action, says which theme entry colours the
class under the caret — useful when a wildcard matched something unexpected, or when a variant is not
colored and you want to know whether the theme lists it at all.

**Copy Tailwind Rainbow Diagnostics** puts the plugin version, the IDE build, the theme in use, the
recognition rules and whether the current file was scanned — with the reason when it was not — on the
clipboard. It is the whole content of a useful bug report in one paste.

The status bar shows the active theme and opens the same chooser when clicked. When a file is not
being colored it says so there instead of naming a theme, and its tooltip gives the reason — the
file's extension is not in the supported list, the file is past the size limit, or the plugin is
switched off.

## Configuration

**Settings | Editor | Tailwind Rainbow**

**Enable Tailwind Rainbow** turns the coloring off without uninstalling anything.

### What is recognized

| Setting | Default |
| --- | --- |
| Theme | `default` |
| Maximum file size | 1000000 characters |
| Class identifiers | `class`, `className`, `class:`, `className:`, `class:list`, `classlist`, `classes`, `css`, `style` |
| Class functions | `cn`, `clsx`, `cva`, `classNames`, `classList`, `classnames`, `twMerge`, `tw`, `cls`, `cc`, `cx`, `classname`, `styled`, `css`, `theme`, `variants` |
| Template tags | `tw`, `css`, `styled` |
| Ignored prefix modifiers | `group`, `peer`, `has`, `in`, `not` |
| Supported extensions | `html`, `htm`, `js`, `jsx`, `ts`, `tsx`, `vue`, `svelte`, `astro`, `php`, `css`, `scss`, `sass`, `less`, `styl`, `stylus`, `pcss`, `postcss` |
| Colour strings that read as a class list | on |

Each list is comma-separated. A method called on one of the class functions counts as one too, so
`el.classList.add("…")` is read like `clsx(…)`.

A value assigned to one of the class identifiers is recognized whether it is a string, a template
literal, an array, or an object: `const classes = ['hover:underline', 'lg:p-4']` is highlighted, and
a declaration that carries a type annotation reads the same as one that does not. A name that ends in
a class identifier counts too, as long as it reads as one: `buttonClasses` and `cardClassName` are
recognized, `superclass` is not.

A string none of those rules claims is read when it reads as a class list on its own, which covers the
lookup tables a codebase names for what they mean rather than for what they hold:

```ts
const sizeByAlignment: ReadonlyRecord<Alignment, string> = {
  left: "w-full md:max-w-1/2",   // coloured, though nothing here is called a class
}
```

Every word has to be a Tailwind class and at least one has to carry a variant the theme knows. That is
what keeps prose out — `'see hover:bg-blue-500 for details'` is left alone, and so is any colon that is
not a variant, such as `'10:30'`, `'user:profile:title'` or a URL. It is consulted only for strings the
rules above already turned down, so nothing recognized before is recognized differently. Untick
**Colour strings that read as a class list** to switch it off.

These rules can belong to the project instead of the IDE. Tick **Use project settings for what is
recognized** and they are stored in `.idea/tailwindRainbow.xml`, which a repository can commit so
that everyone who opens it recognizes the same helpers and file types. Unticking hands them back to
your IDE-wide settings. The theme and its colors always stay yours: a palette is a preference, not a
property of the code.

### Themes and colors

**New…** creates a theme of your own, based on a built-in one. It stores only the colors you change,
so everything you leave alone keeps following the base theme — including tokens added in later
plugin versions. **Delete** removes a theme you made; built-in themes cannot be deleted.

A preview under the table shows a sample class list painted with the theme as you edit it, including
the adjustment made for your editor background — so the colors you pick are shown the way the editor
will actually paint them. The sample is editable: paste classes from your own code to see how they
land, or to find out that a variant is not colored because no token matches it. Nothing typed there
is saved, and **Restore sample** brings the original back.

The token table lists what the selected theme colors, grouped by section. Type to search it, or use
**Show** to narrow it to one section. Each row shows its color as
a swatch and a hex value that can be edited in place — with or without the hash, and `#fff` style
shorthand is expanded — or picked with the color picker below. **Reset**, in the table's toolbar,
returns a single entry to its theme value without discarding your other overrides. Unticking **Enabled** leaves a token uncolored while keeping the color you picked, so it
can be switched back on later.

Use **+** to add a token, choosing whether it matches a variant prefix (`hover`, `focus-visible`,
`min-*`) or a base class (`bg-*`, `text-lg`); patterns may use `*` as a wildcard, and the most
specific pattern wins — a `bg-blue-500` entry beats `bg-*`. **−** removes a token you added; tokens
that come from the theme are reset rather than removed.

**Export** writes the selected theme to a file, and **Import** reads themes back in under their own
names. The file holds colors in the same shape the VS Code extension uses for its
`tailwindRainbow.themes` setting, so a palette can move between the two editors — and Import accepts
a whole VS Code `settings.json`, bringing across every theme defined in it. An export is
self-contained: every color the theme shows, rather than only the ones you changed.

Adding a token offers the variants your project declares for itself — `@custom-variant` and
`--breakpoint-*` in a Tailwind v4 stylesheet, `addVariant(…)` and `screens` in a v3 config — so a
project-specific variant is a pick from a list rather than something to type from memory. They are
offered, not added: which of them deserve a color, and which color, stays your decision.

If a stored theme holds an entry the plugin cannot use — a color that is not `#RRGGBB`, say, after
the settings file was edited by hand — a banner above the token table says so and what is wrong.
**Show the entry** selects it so it can be fixed, and **Remove them** drops the unusable entries,
which takes effect when you apply. The entries are skipped rather than applied in the meantime.

## Contributing themes from another plugin

A plugin can add themes of its own — a company palette shared across a team, for instance — through
the `dev.tailwindrainbow.themeContributor` extension point:

```xml
<idea-plugin>
    <depends>dev.tailwindrainbow</depends>

    <extensions defaultExtensionNs="dev.tailwindrainbow">
        <themeContributor implementation="com.example.CorporateThemes"/>
    </extensions>
</idea-plugin>
```

```kotlin
class CorporateThemes : ThemeContributor {
    override fun themes() =
        listOf(
            ThemeSpec(
                name = "corporate",
                basedOn = "default",
                entries = listOf(StyleEntry(SegmentKind.PREFIX, "hover", "#0055ff", 700)),
            ),
        )
}
```

A contributed theme behaves like a built-in one: it appears in the theme list, it can be the base of
a user theme, it cannot be deleted from settings, and any color the user changes in it wins while
everything untouched keeps following the contribution. Malformed entries are reported in the
settings screen rather than thrown, and a contributor that fails costs only its own themes.

Contributions are picked up when settings are applied or when the IDE starts, not the instant a
plugin is installed.

## Colors and IDE color schemes

Colors are applied directly rather than through the IDE's color scheme, because a theme's tokens are
user-defined and can be added at any time, while a scheme's attribute keys are fixed. Two
consequences worth knowing: the plugin's colors are not editable under **Settings | Editor | Color
Scheme**, and they are not carried along when a color scheme is exported or shared. Adapting each
color to the editor background is what keeps a single palette usable across schemes.

## Compatibility

Built against IntelliJ Platform 2025.2 (`since-build` 252) with no upper bound, so it keeps working
on later releases rather than being disabled the day a new IDE ships. It declares only
`com.intellij.modules.platform`, so it runs in every JetBrains IDE.

## Architecture

The source is layered, and the layering is enforced by tests rather than by convention.

```
domain/       Themes, segment kinds, matching. Depends on nothing.
application/  Scanning, parsing, theme resolution, settings mapping. Depends only on domain.
  port/       The interfaces the application needs the outside world to satisfy.
adapter/      Implementations of those ports. Imports com.intellij; nothing inward does.
bootstrap/    Composition root. The only place that names a concrete adapter.
```

`ArchitectureTest` asserts those statements, and two more: that no code outside `adapter/` and
`bootstrap/` touches the platform, and that nothing sits in the IDE adapter without needing to be
there. They fail on a violation, which is how two dependency inversions, a misplaced class, and a
documentation link pointing the wrong way were caught during development.

Highlighting runs through an `Annotator`, so the platform's `DaemonCodeAnalyzer` owns debouncing,
cancellation, and highlight lifetime. The plugin does not manage the markup model itself.

## Building

```bash
./gradlew build           # compile, lint, test
./gradlew runIde          # launch a sandbox IDE with the plugin installed
./gradlew buildPlugin     # produce the distributable zip
./gradlew verifyPlugin    # run the JetBrains Plugin Verifier against the IDE this builds against
./gradlew patchChangelog  # promote Unreleased to the current version, before a release
```

The plugin declares only `com.intellij.modules.platform`, so it loads in every JetBrains IDE.
`-PpluginVerificationTarget=wide` verifies it against the ones whose users write Tailwind — IntelliJ
IDEA Community and Ultimate, WebStorm, and PhpStorm — at their latest release:

```bash
./gradlew verifyPlugin -PpluginVerificationTarget=wide
```

Each IDE is a separate download of about a gigabyte, so that target is not part of the build that
runs on every pull request. It runs weekly, on demand through the **Wide verification** workflow, and
as part of a release unless the release is dispatched with wide verification turned off.

Release notes are generated from [CHANGELOG.md](CHANGELOG.md). `patchPluginXml` renders the
section matching the current version — or `Unreleased` if that version has not been stamped yet —
into `<change-notes>`, which becomes the Marketplace **What's New** tab. Do not write notes into
`plugin.xml`; they would be overwritten.

Static analysis is ktlint and detekt; both run in CI on every pull request, along with the tests
and the Plugin Verifier. Kotlin warnings are errors, so a deprecated platform API fails the build
rather than waiting to be noticed — which is what usually happens to a warning. Publishing to JetBrains Marketplace is a separate, manually dispatched
workflow — nothing is released by merging.

## Credits

A port of the [Tailwind Rainbow](https://github.com/esdete2/tailwind-rainbow) VS Code extension to
the IntelliJ Platform.

## License

Apache License 2.0. See [LICENSE](LICENSE).
