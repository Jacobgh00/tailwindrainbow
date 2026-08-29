# Changelog

All notable changes to Tailwind Rainbow are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Highlighting of Tailwind variant prefixes, base classes, arbitrary variants such as `[&>*]`, and
  the important modifier in every position Tailwind allows — `!font-bold` and `hover:!font-bold`
  from v3, `font-bold!` from v4.
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
- Support for HTML, JSX/TSX, Vue, Svelte, Astro, PHP, and CSS/SCSS/Sass/Less/Stylus/PostCSS files.
- Two built-in themes, `default` and `synthwave`, and any number of your own based on either. A
  theme of your own stores only the colors you change, so the rest keeps following its base —
  including tokens added in later plugin versions.
- Adding and removing theme tokens, so a variant no built-in theme lists can be colored. Base-class
  patterns such as `bg-*` can be added too, coloring the utility itself alongside its variants;
  built-in themes ship none, so this stays opt-in.
- The theme editor shows a live preview: a sample class list painted with the theme as you edit it,
  including the adjustment that keeps colors readable on your editor background. The sample is
  editable, so classes pasted from your own code can be tried against the palette; nothing typed
  there is saved, and **Restore sample** brings the original back.
- The token table can be searched by typing, and narrowed to one section, which matters once a theme
  lists more tokens than fit on screen.
- The color column shows each token's hex beside its swatch and can be edited in place, including
  pasting a color with or without its hash and shorthand such as `#fff`. **Reset** now sits in the
  table's toolbar, next to add and remove.
- A color, a bold setting, and an on/off switch per token. Switching one off leaves it uncolored
  while keeping the color you picked, which is the difference between silencing a variant and
  deleting it.
- Colors adapt to the editor background: one that would be unreadable against it is darkened or
  lightened, keeping its hue, until it meets the WCAG AA contrast ratio. One that already reads is
  used exactly as chosen.
- The recognition lists can be edited one value per line: each field expands into a small editor
  instead of being a long comma-separated line. A file extension written with a leading dot is stored
  the way a file reports it, and Apply shows the stored, deduplicated list back.
- Settings are validated as they are typed: a maximum file size that is not a positive number is
  reported under the field rather than when Apply is pressed, and emptying the class identifiers says
  what it will cost.
- Settings under **Editor | Tailwind Rainbow** for the theme, the maximum file size to scan, and the
  recognized attributes, functions, template tags, ignored prefix modifiers, and file extensions.
- Project-level recognition settings. A project can keep its own answers in
  `.idea/tailwindRainbow.xml` for a repository to share, while the theme stays with the user.
- Variants your project declares are offered when adding a token, read from `@custom-variant` and
  `--breakpoint-*` in Tailwind v4 stylesheets and from `addVariant(…)` and `screens` in a v3 config.
- Reporting of theme entries the plugin cannot use: a banner in the settings screen says how many
  there are and what is wrong with them, and offers to show the entry or remove them. An edit that
  would add a new one is refused with the reason.
- An **Explain Tailwind Colouring at Caret** action that says which theme entry colours the class
  under the caret, or that nothing matches it.
- A status bar widget showing the active theme, or saying that the current file is not scanned and
  why — the file's extension is not in the list, or it is past the size limit. Clicking it opens the
  same theme chooser as the action.
- A **Select Tailwind Rainbow Theme** action, found through Find Action, that switches themes
  without opening settings and previews each one in the editor while the list is open.
- A `dev.tailwindrainbow.themeContributor` extension point, so other plugins can ship themes. A
  contributed theme can be based on a built-in one, can be the base of a user theme, and is
  overridden entry by entry by the user's own colors.

[Unreleased]: https://github.com/Jacobgh00/tailwindrainbow/commits/main
