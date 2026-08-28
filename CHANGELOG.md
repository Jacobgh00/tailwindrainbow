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
- Optional base-class coloring. A base pattern such as `bg-*` colors the utility itself, separately
  from the variants in front of it. Built-in themes ship none, so this is opt-in.
- Adding and removing theme tokens. Any variant prefix or base-class pattern can be given a color,
  including ones no built-in theme lists; added tokens can be removed again, while tokens that come
  from the theme are reset to their inherited color.
- Settings under **Editor | Tailwind Rainbow** for the theme, the maximum file size to scan, and
  the recognized attributes, functions, template tags, ignored prefix modifiers, and file
  extensions.

[Unreleased]: https://github.com/Jacobgh00/tailwindrainbow/commits/main
