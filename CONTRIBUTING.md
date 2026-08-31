# Contributing

Thanks for reading the code. Bug reports and ideas are welcome; the notes below say what happens to
them and what a pull request needs before it can be merged.

## License

Tailwind Rainbow is open source software licensed under the [Apache License, Version 2.0](LICENSE).

You keep the copyright in your contribution. By submitting a pull request, you agree that the
contribution may be used, modified and distributed under the Apache License, Version 2.0, as
described in section 5 of that license. No copyright assignment or separate contributor license
agreement is required.

Only submit work you have the right to license. Please don't copy code from another project unless
its license permits that use; preserve any required attribution and mention the source in the pull
request.

## Reporting a bug

Run **Copy Tailwind Rainbow Diagnostics** (Find Action, `⇧⌘A` / `Ctrl+Shift+A`) and paste the result
into the issue. It carries the plugin version, the IDE build, the active theme, the recognition
settings and whether the current file was scanned, with the reason when it was not - which is most
of what a report needs.

Then add: what you expected to be coloured, what happened instead, and the smallest snippet that
shows it. A snippet matters more than a screenshot here, because the bug is usually in how the text
was parsed.

## Suggesting a variant or a theme

Colours are opinions, so a theme change needs a reason beyond taste: a variant family that reads as
another one, a colour that fails against a common editor background, or a Tailwind variant that no
built-in theme covers. Variants your own project declares don't need a report - add them yourself as
theme tokens.

## Building

```bash
./gradlew build           # compile, lint, test
./gradlew runIde          # launch a sandbox IDE with the plugin installed
./gradlew buildPlugin     # produce the distributable zip
./gradlew verifyPlugin    # run the JetBrains Plugin Verifier
```

Requires JDK 21. `verifyPlugin` checks against the IDE the plugin builds against;
`-PpluginVerificationTarget=wide` checks IntelliJ IDEA Community and Ultimate, WebStorm and PhpStorm
at their latest release, each a separate download of about a gigabyte.

Static analysis is ktlint and detekt, and Kotlin warnings are errors - so a deprecated platform API
fails the build rather than waiting to be noticed. Both run in CI on every pull request, along with
the tests and the Plugin Verifier.

## Git hooks

The repository includes a versioned pre-commit hook that runs `./gradlew check`. Enable it once per
checkout:

```bash
git config core.hooksPath .githooks
```

The hook then runs automatically before each commit.

## How the source is laid out

```
domain/       Themes, segment kinds, matching. Depends on nothing.
application/  Scanning, parsing, theme resolution, settings mapping. Depends only on domain.
  port/       The interfaces the application needs the outside world to satisfy.
adapter/      Implementations of those ports and external formats. IntelliJ adapters import the
              platform; nothing inward does.
bootstrap/    Composition root. The only place that names a concrete adapter.
```

`ArchitectureRulesTest` asserts those statements, and that no code outside `adapter/` and
`bootstrap/` names the platform or a concrete adapter. It fails the build on a violation, which is
how misplaced code gets caught. If a change makes that test fail, the fix is usually to move the code
rather than to relax the rule.

Highlighting runs through an `Annotator`, so the platform's `DaemonCodeAnalyzer` owns debouncing,
cancellation and highlight lifetime. The plugin does not manage the markup model itself.

## Pull requests

- Keep it to one change. A pull request that fixes a bug and reformats a file is two.
- Add a test. Parsing changes especially: the failing input belongs in the suite before the fix.
- `./gradlew build` has to pass before review.
- Don't edit `CHANGELOG.md`'s released sections or `plugin.xml`'s change notes. Release notes come
  from the `Unreleased` section, which the maintainer stamps at release time.

Merging is at the maintainer's discretion, and an open pull request is not a promise that it will
land. If you are about to spend real time on something, open an issue first and ask.
