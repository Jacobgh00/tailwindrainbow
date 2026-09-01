# Changelog

All notable changes to Tailwind Rainbow are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.0] - 2026-09-01

### Added

- **Open Tailwind Rainbow Variant Health**, a cancellable project report for declared variants,
  theme matches, missing/disabled/malformed entries, wildcard coverage and duplicate declarations,
  with actions to add a colour, open a declaration and refresh the report.
- A colour for a scoping modifier, painted apart from the variant it scopes, so `group-hover:`
  reads as both a hover and an ancestor-scoped one instead of looking identical to a plain
  `hover:`. Covers `group`, `peer`, `has`, `in` and `not`, which share one colour per built-in
  theme and appear in the theme editor as ordinary tokens.

### Removed

- The **Ignored prefix modifiers** setting. `group`, `peer`, `has`, `in` and `not` are Tailwind's
  own syntax rather than a preference, so they are now fixed in the plugin.

## [0.1.0] - 2026-08-30

First release.

### Added

- Highlighting of Tailwind variant prefixes, arbitrary variants such as `[&>*]`, and the important
  modifier in every position Tailwind allows - `!font-bold`, `hover:!font-bold`, `font-bold!`.
  Related variants share a color.
- Colors for the variants Tailwind v4 introduced: container queries (`@md:`, `@max-lg:`,
  `@[400px]:`), `data-*`, `aria-*` and `supports-*`, `nth-*`, and `open`, `inert` and `starting`.
- Arbitrary variants written behind a modifier or a group name - `has-[:checked]:`,
  `group-[.is-open]/menu:`, `not-[&:hover]:`.
- Recognition of class names in class attributes, framework bindings (`:class`, `v-bind:class`,
  `x-bind:class`), class helper functions and methods called on them (`el.classList.add(…)`), tagged
  templates including ones carrying a component or a type (`` styled(Button)`…` ``), CSS `@apply`
  directives wrapped over several lines, and values assigned to a class-shaped name - string,
  template literal, array or object. A compound name counts across a camel case boundary, so
  `buttonClasses` is read and `superclass` is not.
- Support for HTML, JSX/TSX, Vue, Svelte, Astro, PHP, and CSS/SCSS/Sass/Less/Stylus/PostCSS files.
- Four built-in themes: `default`, `synthwave`, `colour-blind` (Okabe-Ito, holding the variant
  families apart under all three dichromacies) and `editor scheme`, which follows the IDE's syntax
  colors.
- Any number of themes of your own, storing only the colors you change so the rest keeps following
  the base - including tokens added in later plugin versions.
- A theme editor showing the palette rather than describing it: each token painted in its own color,
  a swatch that opens a color picker, a hex field, and add, remove, reset, section filter and find
  around it. Per token: a color, a bold setting, and an on/off switch.
- Theme tokens you add yourself, including base-class patterns such as `bg-*`. Built-in themes ship
  none, so base-class coloring stays opt-in.
- A live, editable preview of the theme under the table. Nothing typed there is saved.
- Colors adapted to the editor background: one that would be unreadable is darkened or lightened,
  keeping its hue, until it meets the WCAG AA contrast ratio.
- Theme management beside the picker: **New…**, **Duplicate**, **Rename…**, **Delete**, **Import
  theme…** and **Export theme…**. Renaming carries every theme based on the renamed one. Import reads
  both this plugin's export and a VS Code `settings.json`.
- Settings under **Editor | Tailwind Rainbow** for the theme and, behind **What is recognized**, the
  maximum file size and the recognized attributes, functions, template tags, ignored prefix modifiers
  and file extensions - validated as they are typed.
- Project-level recognition settings in `.idea/tailwindRainbow.xml` for a repository to share, while
  the theme stays with the user.
- An inspection reporting a variant your project declares but your theme has no color for, with a
  quick fix that adds it. Declared variants are read from `@custom-variant` and `--breakpoint-*` in
  v4 stylesheets and from `addVariant(…)` and `screens` in a v3 config, and are offered when adding a
  token.
- Reporting of theme entries the plugin cannot use: a banner saying how many and what is wrong, with
  the option to show or remove them.
- **Select Tailwind Rainbow Theme**, which switches themes and previews each one in the editor; a
  status bar widget showing the active theme, or why the current file is not scanned; **Explain
  Tailwind Colouring at Caret**; and **Copy Tailwind Rainbow Diagnostics**, which puts a whole bug
  report on the clipboard.
- A `dev.tailwindrainbow.themeContributor` extension point, so other plugins can ship themes.
- Class names are read from a string that reads as a class list on its own, so a lookup table no
  attribute, helper or class-shaped name claims - `{ left: 'w-full md:max-w-1/2' }` - is coloured for
  what it holds. Every word has to be a Tailwind class and at least one has to carry a known variant,
  which keeps prose and non-variant colons such as `'10:30'` or a URL out of it. **Colour strings that
  read as a class list**, under *Settings | Editor | Tailwind Rainbow*, switches it off.

[Unreleased]: https://github.com/Jacobgh00/tailwindrainbow/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/Jacobgh00/tailwindrainbow/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/Jacobgh00/tailwindrainbow/commits/v0.1.0
