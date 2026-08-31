# Tailwind Rainbow

[![Build](https://github.com/Jacobgh00/tailwindrainbow/actions/workflows/build.yml/badge.svg)](https://github.com/Jacobgh00/tailwindrainbow/actions/workflows/build.yml)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

An open-source IntelliJ Platform plugin that colorizes Tailwind CSS variant prefixes, so a long
class list stays readable.

```
hover:bg-blue-500  focus:ring-2  sm:px-4  dark:text-white  [&>*]:mt-2  !font-bold
^^^^^              ^^^^^         ^^       ^^^^             ^^^^^        ^
green              teal          purple   slate            coral        red
```

Every variant gets its own color - `hover`, `focus`, `sm`, `dark`, arbitrary variants such as
`[&>*]`, and the `!` important modifier wherever Tailwind v3 or v4 puts it. Related variants share a
color, so `focus-visible` looks like `focus`.

## Features

- **Where it colors.** Class attributes (`class`, `className`, `class:list`, …), framework bindings
  (`:class`, `v-bind:class`, `x-bind:class`), class helper functions (`cn`, `clsx`, `cva`,
  `twMerge`, …), tagged templates (`` styled.div`…` ``, `` styled(Button)`…` ``), CSS `@apply`
  directives, and values assigned to a class-shaped name such as `buttonClasses`. A string that
  reads as a class list on its own is colored too, which covers lookup tables named for what they
  mean rather than for what they hold.
- **Tailwind v3 and v4**, including container queries, `data-*`, `aria-*`, `supports-*`, `nth-*`,
  `open`, `inert` and `starting`.
- **Four built-in themes** - `default`, `synthwave`, `colour-blind` (Okabe-Ito, verified against all
  three dichromacies) and `editor scheme` (follows the IDE's own syntax colors) - plus any number of
  your own, each based on one of them.
- **Readable on any background.** A color that would not contrast with your editor background is
  darkened or lightened until it does, keeping its hue.
- **Yours to change.** Pick a color for any variant, toggle bold, switch an entry off, or add a
  token no theme lists - including base-class patterns such as `bg-*`.
- **Configurable recognition.** Which attributes, functions, tagged templates and file extensions
  are scanned, and the size above which a file is skipped. A project can keep its own answers and
  commit them.

## Installation

**Settings | Plugins | Marketplace**, search for *Tailwind Rainbow*, **Install**.

## Using it

Find Action (`⇧⌘A` / `Ctrl+Shift+A`) reaches four actions:

- **Select Tailwind Rainbow Theme** - switches themes and previews each one in the editor as you
  arrow through the list. `Esc` changes nothing. The status bar shows the active theme and opens the
  same chooser; when a file is not being colored it says so instead, with the reason in its tooltip.
- **Explain Tailwind Colouring at Caret** - says which theme entry colors the class under the caret.
- **Copy Tailwind Rainbow Diagnostics** - puts the plugin version, IDE build, active theme,
  recognition rules and the current file's scan status on the clipboard: a bug report in one paste.
- **Open Tailwind Rainbow Variant Health** - scans project variant declarations and explains missing,
  disabled or malformed theme entries, wildcard coverage and duplicate declarations.

A variant your project declares - through `@custom-variant`, `addVariant(…)` or a custom screen -
that your theme has no color for is reported as a weak warning where it is used, with a quick fix
that adds it. Switch it off under **Settings | Editor | Inspections | Tailwind Rainbow**.

## Configuration

**Settings | Editor | Tailwind Rainbow**

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

A method called on a class function counts as one, so `el.classList.add("…")` is read like
`clsx(…)`. A name ending in a class identifier counts as one, so `buttonClasses` is recognized and
`superclass` is not.

The last setting colors a string no other rule claims, when every word in it is a Tailwind class and
at least one carries a variant the theme knows. That is what keeps prose out:
`'see hover:bg-blue-500 for details'` is left alone, and so is any colon that is not a variant, such
as `'10:30'` or a URL.

Tick **Use project settings for what is recognized** and these rules move to
`.idea/tailwindRainbow.xml`, which a repository can commit. The theme and its colors always stay
yours: a palette is a preference, not a property of the code.

### Themes and colors

**New…** creates a theme based on a built-in one, storing only the colors you change - so anything
you leave alone keeps following the base, including tokens added in later plugin versions.

The token table lists what the theme colors, grouped by section, each row showing its color as a
swatch and an editable hex value. **+** adds a token, matching either a variant prefix (`hover`,
`min-*`) or a base class (`bg-*`); `*` is a wildcard and the most specific pattern wins. **Reset**
returns one entry to its theme value without touching your other overrides.

A live preview under the table paints a sample class list with the theme as you edit it, including
the adjustment for your editor background. It is editable - paste classes from your own code to see
how they land - and nothing typed there is saved.

**Export** writes a theme to a file in the same shape the VS Code extension uses for
`tailwindRainbow.themes`, and **Import** reads those back, including a whole VS Code
`settings.json`.

Adding a token offers the variants your project declares for itself - `@custom-variant` and
`--breakpoint-*` in a v4 stylesheet, `addVariant(…)` and `screens` in a v3 config. They are offered,
not added.

## Themes from another plugin

Another plugin can ship themes - a shared company palette, say - through the
`dev.tailwindrainbow.themeContributor` extension point:

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

A contributed theme behaves like a built-in one: it can be the base of a user theme, it cannot be
deleted, and any color the user changes wins while everything untouched keeps following the
contribution. Contributions are picked up when settings are applied or when the IDE starts.

## Notes

Colors are applied directly rather than through the IDE's color scheme, because a theme's tokens are
user-defined while a scheme's attribute keys are fixed. So they are not editable under **Settings |
Editor | Color Scheme**, and not carried along when a scheme is exported. Adapting each color to the
editor background is what keeps one palette usable across schemes.

Built against IntelliJ Platform 2025.2 (`since-build` 252) with no upper bound, declaring only
`com.intellij.modules.platform` - so it runs in every JetBrains IDE and keeps working on later
releases.

Release notes live in [CHANGELOG.md](CHANGELOG.md). Building the plugin, the source layout and
what a pull request needs are in [CONTRIBUTING.md](CONTRIBUTING.md).

## Credits

An independent port of the [Tailwind Rainbow](https://github.com/esdete2/tailwind-rainbow) VS Code
extension to the IntelliJ Platform, not affiliated with or endorsed by its authors. Tailwind CSS is
a trademark of Tailwind Labs Inc.; this plugin is not affiliated with or endorsed by Tailwind Labs.

## License

Tailwind Rainbow is open source software licensed under the [Apache License, Version 2.0](LICENSE).

The Apache License applies to the original code in this repository. See [NOTICE](NOTICE) for
upstream attribution. Third-party dependencies and the Gradle wrapper retain their own licenses and
notices.
