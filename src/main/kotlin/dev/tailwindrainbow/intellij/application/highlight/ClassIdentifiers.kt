package dev.tailwindrainbow.intellij.application.highlight

internal fun Set<String>.searchWords(): List<String> {
    val words = map { it.trimEnd(':') }.filter(String::isNotEmpty).distinct()

    return words.filterNot { word -> words.any { it != word && it.length < word.length && word.contains(it, true) } }
}
