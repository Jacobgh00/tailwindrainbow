# Tailwind Rainbow

An IntelliJ Platform plugin that colorizes Tailwind CSS variant prefixes, so a long class list
stays readable.

Every variant — `hover`, `focus`, `sm`, `dark`, arbitrary values such as `[&>*]`, and the `!`
important modifier — is painted in its own color. The structure of a class list becomes visible at
a glance instead of having to be read word by word.

```
hover:bg-blue-500  focus:ring-2  sm:px-4  dark:text-white  [&>*]:mt-2  !font-bold
^^^^^              ^^^^^         ^^       ^^^^             ^^^^^        ^
green              teal          purple   slate            coral        red
```

## Features

- **Variant highlighting** in class attributes, class helper functions, tagged templates, and CSS
  `@apply` directives. A tagged template is recognized by what it hangs off, so `` styled.div`…` ``,
  `` styled(Button)`…` ``, and `` styled.div<Props>`…` `` all count as `styled`.
- **Framework bindings** — `:class`, `v-bind:class`, and `x-bind:class`. A bound attribute holds an
  expression, so the class names are read out of the strings inside it: `:class="{ 'lg:p-4': ok }"`
  colors `lg:p-4` and leaves the braces and the condition alone.
- **Two built-in themes** — `default` and `synthwave` — plus any number of your own, each started
  from one of them.
- **User-defined colors.** Pick a color for any variant with a color picker, toggle bold, or switch
  an entry off so that variant is left alone. Overrides are stored per entry, so a theme you
  customize keeps inheriting everything you did not touch.
- **User-defined tokens.** Add a variant no theme lists — `focus-visible`, `aria-*`, a variant of
  your own — with `*` usable as a wildcard.
- **Optional base-class coloring.** Add a base pattern such as `bg-*` and the utility itself is
  colored, alongside its variants: in `lg:bg-blue-500`, `lg:` takes the variant color and
  `bg-blue-500` the base one. No built-in theme colors base classes, because variants stop standing
  out once everything else is colored too.
- **Configurable recognition** — which attributes, helper functions, tagged templates, and file
  extensions are scanned, and the file size above which scanning is skipped.

## Installation

From the IDE: **Settings | Plugins | Marketplace**, search for *Tailwind Rainbow*, and click
**Install**.

To install a local build:

```bash
./gradlew buildPlugin
```

Then **Settings | Plugins | ⚙ | Install Plugin from Disk…** and choose
`build/distributions/tailwindrainbow-<version>.zip`.

## Configuration

**Settings | Editor | Tailwind Rainbow**

**New…** creates a theme of your own, based on a built-in one. It stores only the colors you change,
so everything you leave alone keeps following the base theme — including tokens added in later
plugin versions. **Delete** removes a theme you made; built-in themes cannot be deleted.

| Setting | Default |
| --- | --- |
| Theme | `default` |
| Maximum file size | 1000000 characters |
| Class identifiers | `class`, `className`, `class:`, `className:`, `class:list`, `classlist`, `classes`, `css`, `style` |
| Class functions | `cn`, `clsx`, `cva`, `classNames`, `classList`, `classnames`, `twMerge`, `tw`, `cls`, `cc`, `cx`, `classname`, `styled`, `css`, `theme`, `variants` |
| | A method called on one of these counts too, so `el.classList.add("…")` is recognized. |
| Template tags | `tw`, `css`, `styled` |
| Ignored prefix modifiers | `group`, `peer`, `has`, `in`, `not` |
| Supported extensions | `html`, `htm`, `js`, `jsx`, `ts`, `tsx`, `vue`, `svelte`, `astro`, `php`, `css`, `scss`, `sass`, `less`, `styl`, `stylus`, `pcss`, `postcss` |

A value assigned to one of the class identifiers is recognized whether it is a string, an array, or
an object: `const classes = ['hover:underline', 'lg:p-4']` is highlighted, while the same array
assigned to `notes` is not.

Each list is comma-separated. Editing a color opens a color picker; **Reset** returns a single
entry to its theme value without discarding your other overrides.

The token table lists what the selected theme colors, grouped by section. Use **+** to add a token,
choosing whether it matches a variant prefix (`hover`, `focus-visible`, `min-*`) or a base class
(`bg-*`, `text-lg`); patterns may use `*` as a wildcard, and the most specific pattern wins — a
`bg-blue-500` entry beats `bg-*`. **−** removes a token you added. Tokens that come from the theme
are reset rather than removed. Unticking **Enabled** leaves a token uncolored while keeping the
color you picked, so it can be switched back on later.

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
adapter/      Implementations of those ports. The only code that imports com.intellij.
bootstrap/    Composition root. The only place that names a concrete adapter.
```

`ArchitectureTest` asserts each of those five statements. They fail on a violation, which is how
two dependency inversions and one misplaced class were caught during development.

Highlighting runs through an `Annotator`, so the platform's `DaemonCodeAnalyzer` owns debouncing,
cancellation, and highlight lifetime. The plugin does not manage the markup model itself.

## Building

```bash
./gradlew build           # compile, lint, test
./gradlew runIde          # launch a sandbox IDE with the plugin installed
./gradlew buildPlugin     # produce the distributable zip
./gradlew verifyPlugin    # run the JetBrains Plugin Verifier
./gradlew patchChangelog  # promote Unreleased to the current version, before a release
```

Release notes are generated from [CHANGELOG.md](CHANGELOG.md). `patchPluginXml` renders the
section matching the current version — or `Unreleased` if that version has not been stamped yet —
into `<change-notes>`, which becomes the Marketplace **What's New** tab. Do not write notes into
`plugin.xml`; they would be overwritten.

Static analysis is ktlint and detekt; both run in CI on every pull request, along with the tests
and the Plugin Verifier. Publishing to JetBrains Marketplace is a separate, manually dispatched
workflow — nothing is released by merging.

## Credits

A port of the [Tailwind Rainbow](https://github.com/esdete2/tailwind-rainbow) VS Code extension to
the IntelliJ Platform.

## License

Apache License 2.0. See [LICENSE](LICENSE).
