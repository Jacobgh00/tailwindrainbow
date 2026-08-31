package dev.tailwindrainbow.intellij.domain.theme

/**
 * The variants that scope another variant rather than standing on their own, as in
 * `group-hover:` or `peer-checked:`. Tailwind defines this set, so it is a fact about
 * the framework rather than something a user chooses.
 */
val SCOPING_MODIFIERS = setOf("group", "peer", "has", "in", "not")
