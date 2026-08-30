package dev.tailwindrainbow.intellij.adapter.settings.persistence

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings

internal interface RecognitionState {
    var maxFileSize: Int
    var classIdentifiers: MutableList<String>
    var classFunctions: MutableList<String>
    var templateTags: MutableList<String>
    var ignoredPrefixModifiers: MutableList<String>
    var supportedExtensions: MutableList<String>
    var readsClassLikeStrings: Boolean
}

internal fun RecognitionState.toScanSettings(): ScanSettings =
    ScanSettings(
        maxFileSize = maxFileSize,
        classIdentifiers = classIdentifiers.toSet(),
        classFunctions = classFunctions.toSet(),
        templateTags = templateTags.toSet(),
        ignoredPrefixModifiers = ignoredPrefixModifiers.toSet(),
        supportedExtensions = supportedExtensions.toSet(),
        readsClassLikeStrings = readsClassLikeStrings,
    )

internal fun RecognitionState.updateFrom(scan: ScanSettings) {
    maxFileSize = scan.maxFileSize
    classIdentifiers = scan.classIdentifiers.sorted().toMutableList()
    classFunctions = scan.classFunctions.sorted().toMutableList()
    templateTags = scan.templateTags.sorted().toMutableList()
    ignoredPrefixModifiers = scan.ignoredPrefixModifiers.sorted().toMutableList()
    supportedExtensions = scan.supportedExtensions.sorted().toMutableList()
    readsClassLikeStrings = scan.readsClassLikeStrings
}
