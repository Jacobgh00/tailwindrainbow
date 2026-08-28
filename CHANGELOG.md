# Changelog

All notable changes to Tailwind Rainbow are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Highlighting of Tailwind variant prefixes, base classes, arbitrary variants such as `[&>*]`, and
  the `!` important modifier.
- Recognition in class attributes (`class`, `className`, `class:list`, …), class helper functions
  (`cn`, `clsx`, `cva`, `twMerge`, …), tagged templates (`tw`, `css`, `styled`), and CSS `@apply`
  directives.
- Support for HTML, JSX/TSX, Vue, Svelte, Astro, PHP, and CSS/SCSS/Sass/Less/Stylus/PostCSS files.
- Two built-in themes: `default` and `synthwave`.
- User-defined colors. Any variant can be recolored with a color picker and set bold, per theme.
  Overrides are stored per entry, so untouched variants keep following the theme.
- Recognition of compound names that end in a class identifier across a camel case boundary, so
  `const buttonClasses = \`hover:underline\`` and `cardClassName` are highlighted while ordinary
  words such as `superclass` are not.
- Recognition of arrays and objects assigned to a class identifier, so
  `const classes = ['hover:underline', 'lg:p-4']` is highlighted while the same array assigned to
  another name is left alone.
- Recognition of methods called on a class helper, so `el.classList.add("hover:underline")` and
  `classList.toggle(…)` are read the same way as `clsx(…)`.
- Recognition of styled-components tags that carry something: `` styled.div`…` ``,
  `` styled(Button)`…` ``, `` styled.div<Props>`…` ``, and `` styled.input.attrs({…})`…` `` are all
  read as the `styled` tag.
- Recognition of framework class bindings: `:class`, `v-bind:class`, and `x-bind:class`. The value
  of a bound attribute is treated as an expression, so class names are read from the strings inside
  it rather than from the surrounding braces, brackets, and conditions.
- Recognition of the important modifier wherever Tailwind writes it: `!font-bold` and
  `hover:!font-bold` from v3, and `font-bold!` from v4. A codebase mid-migration holds a mixture, so
  all three are read.
- Project-level recognition settings. A project can keep its own class identifiers, helper
  functions, template tags, ignored modifiers, file extensions, and maximum file size in
  `.idea/tailwindRainbow.xml` for a repository to share, while the theme stays with the user.
- A **Select Tailwind Rainbow Theme** action, found through Find Action, that switches themes
  without opening settings and previews each one in the editor while the list is open.
- A `dev.tailwindrainbow.themeContributor` extension point, so other plugins can ship themes. A
  contributed theme can be based on a built-in one, can be the base of a user theme, and is
  overridden entry by entry by the user's own colors.
- Reporting of theme entries the plugin cannot use. They were dropped silently before, so a color
  that never appeared had nothing to explain it; they are now listed in the settings screen, and an
  edit that would add a new one is refused with the reason.
- Themes of your own. A new theme is based on a built-in one and stores only the colors you change,
  so the rest keeps following its base.
- Switching a single theme entry off. The token keeps its color and can be switched back on, which
  is the difference between silencing one variant and deleting it.
- Optional base-class coloring. A base pattern such as `bg-*` colors the utility itself, separately
  from the variants in front of it. Built-in themes ship none, so this is opt-in.
- Adding and removing theme tokens. Any variant prefix or base-class pattern can be given a color,
  including ones no built-in theme lists; added tokens can be removed again, while tokens that come
  from the theme are reset to their inherited color.
- Settings under **Editor | Tailwind Rainbow** for the theme, the maximum file size to scan, and
  the recognized attributes, functions, template tags, ignored prefix modifiers, and file
  extensions.

[Unreleased]: https://github.com/Jacobgh00/tailwindrainbow/commits/main
