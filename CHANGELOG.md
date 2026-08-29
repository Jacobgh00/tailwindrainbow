# Changelog

All notable changes to Tailwind Rainbow are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-08-29

First release.

### Added

- Highlighting of Tailwind variant prefixes, arbitrary variants such as `[&>*]`, and the important
  modifier in every position Tailwind allows — `!font-bold` and `hover:!font-bold` from v3,
  `font-bold!` from v4. Related variants share a color, so the structure of a class list is visible
  at a glance rather than read word by word.
- Colors for the variants Tailwind v4 introduced: container queries (`@md:`, `@max-lg:`, `@[400px]:`),
  `data-*`, `aria-*` and `supports-*` attribute variants, `nth-*`, and the `open`, `inert` and
  `starting` states. Every variant Tailwind documents has a color in every built-in theme.
- Arbitrary variants written behind a modifier or a group name — `has-[:checked]:`,
  `group-[.is-open]/menu:`, `not-[&:hover]:` — colored like any other arbitrary variant.
- Recognition of class names in class attributes (`class`, `className`, `class:list`, …), framework
  bindings (`:class`, `v-bind:class`, `x-bind:class`), class helper functions (`cn`, `clsx`, `cva`,
  `twMerge`, …) and methods called on them (`el.classList.add(…)`), tagged templates including ones
  that carry a component, a type, or attributes (`` styled.div`…` ``, `` styled(Button)`…` ``), and
  CSS `@apply` directives including ones wrapped over several lines.
- Recognition of values assigned to a class-shaped name, whether a string, a template literal, an
  array, or an object. A compound name counts across a camel case boundary, so `buttonClasses` and
  `cardClassName` are read while ordinary words such as `superclass` are not.
- A bound attribute is treated as the expression it is, so `:class="{ 'lg:p-4': ok }"` colors
  `lg:p-4` and leaves the braces, brackets, and conditions alone.
- Support for HTML, JSX/TSX, Vue, Svelte, Astro, PHP, and CSS/SCSS/Sass/Less/Stylus/PostCSS files,
  and for files the editor holds without one on disk, such as the copy the IDE makes for an intention
  preview.
- Four built-in themes: `default` and `synthwave`; `colour-blind`, built on the Okabe–Ito palette so
  the variant families stay distinguishable under all three dichromacies; and `editor scheme`, which
  takes its colors from the IDE's own syntax colors and follows you when you switch color scheme.
- Any number of themes of your own, based on any of the four. A theme stores only the colors you
  change, so the rest keeps following its base — including tokens added in later plugin versions.
- A theme editor whose table shows the palette rather than describing it: each token is painted in
  its own color, the swatch opens a color picker for that row, and the hex can be typed with or
  without its hash, shorthand such as `#fff` included. Add, remove, reset, a section filter and a
  find field sit around it.
- A color, a bold setting, and an on/off switch per token. Switching one off leaves it uncolored
  while keeping the color you picked, which is the difference between silencing a variant and
  deleting it.
- Theme tokens you add yourself, so a variant no built-in theme lists can be colored. Base-class
  patterns such as `bg-*` can be added too, coloring the utility itself alongside its variants;
  built-in themes ship none, so this stays opt-in.
- A live preview under the table: a sample class list painted with the theme as you edit it,
  including the adjustment that keeps colors readable on your editor background. The sample is
  editable, so classes pasted from your own code can be tried against the palette; nothing typed
  there is saved, and **Restore sample** brings the original back.
- Colors adapt to the editor background: one that would be unreadable against it is darkened or
  lightened, keeping its hue, until it meets the WCAG AA contrast ratio. One that already reads is
  used exactly as chosen.
- A theme menu beside the picker holding **New…**, **Duplicate**, **Rename…**, **Delete**,
  **Import theme…** and **Export theme…**. Renaming carries every theme based on the renamed one, so
  an inherited palette does not silently fall back. Import reads both a theme this plugin exported
  and a VS Code `settings.json` — every theme under `tailwindRainbow.themes` comes across, so a
  palette tuned in the VS Code extension moves in one dialog.
- Settings under **Editor | Tailwind Rainbow** for the theme and, behind a **What is recognized**
  group, the maximum file size to scan and the recognized attributes, functions, template tags,
  ignored prefix modifiers and file extensions. Each list is edited one value per line; a file
  extension written with a leading dot is stored the way a file reports it.
- Settings validated as they are typed: a maximum file size that is not a positive number is reported
  under the field rather than when Apply is pressed, and emptying the class identifiers or the file
  extensions says what it will cost.
- Project-level recognition settings. A project can keep its own answers in
  `.idea/tailwindRainbow.xml` for a repository to share, while the theme stays with the user.
- An inspection that reports a variant your project declares but your theme has no color for, with a
  quick fix that adds it — so a custom variant is not silently uncolored. Declared variants are read
  from `@custom-variant` and `--breakpoint-*` in Tailwind v4 stylesheets and from `addVariant(…)` and
  `screens` in a v3 config, and are offered when adding a token.
- Reporting of theme entries the plugin cannot use: a banner in the settings screen says how many
  there are and what is wrong with them, and offers to show the entry or remove them. An edit that
  would add a new one is refused with the reason.
- A **Select Tailwind Rainbow Theme** action, found through Find Action, that switches themes without
  opening settings and previews each one in the editor while the list is open. The status bar shows
  the active theme and opens the same chooser, or says the current file is not scanned and why.
- An **Explain Tailwind Colouring at Caret** action that says which theme entry colours the class
  under the caret, or that nothing matches it.
- A **Copy Tailwind Rainbow Diagnostics** action that puts the plugin version, the IDE build, the
  theme in use, the recognition rules and the current file's scan status on the clipboard — the whole
  content of a useful bug report, in one paste. The same facts reach `idea.log`: a theme entry dropped
  as malformed, and a file handed back unpainted because of its size, its extension, or the plugin
  being switched off.
- A `dev.tailwindrainbow.themeContributor` extension point, so other plugins can ship themes. A
  contributed theme can be based on a built-in one, can be the base of a user theme, and is
  overridden entry by entry by the user's own colors.

[Unreleased]: https://github.com/Jacobgh00/tailwindrainbow/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/Jacobgh00/tailwindrainbow/commits/v0.1.0
