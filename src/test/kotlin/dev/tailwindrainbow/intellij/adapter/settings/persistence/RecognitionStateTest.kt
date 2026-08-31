package dev.tailwindrainbow.intellij.adapter.settings.persistence

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class RecognitionStateTest {
    @Test
    fun `stored recognition values become immutable scan settings`() {
        val state =
            state().apply {
                maxFileSize = 4096
                classIdentifiers = mutableListOf("class", "class")
                classFunctions = mutableListOf("cn")
                templateTags = mutableListOf("tw")
                supportedExtensions = mutableListOf("html")
                readsClassLikeStrings = false
            }

        assertEquals(
            ScanSettings(
                maxFileSize = 4096,
                classIdentifiers = setOf("class"),
                classFunctions = setOf("cn"),
                templateTags = setOf("tw"),
                supportedExtensions = setOf("html"),
                readsClassLikeStrings = false,
            ),
            state.toScanSettings(),
        )
    }

    @Test
    fun `scan settings become sorted XML-safe lists`() {
        val state = state()

        state.updateFrom(
            ScanSettings(
                maxFileSize = 4096,
                classIdentifiers = setOf("className", "class"),
                classFunctions = setOf("twMerge", "cn"),
                templateTags = setOf("styled", "tw"),
                supportedExtensions = setOf("vue", "html"),
                readsClassLikeStrings = false,
            ),
        )

        assertEquals(4096, state.maxFileSize)
        assertEquals(listOf("class", "className"), state.classIdentifiers)
        assertEquals(listOf("cn", "twMerge"), state.classFunctions)
        assertEquals(listOf("styled", "tw"), state.templateTags)
        assertEquals(listOf("html", "vue"), state.supportedExtensions)
        assertEquals(false, state.readsClassLikeStrings)
    }

    private fun state(): RecognitionState =
        object : RecognitionState {
            override var maxFileSize = ScanSettings().maxFileSize
            override var classIdentifiers = mutableListOf<String>()
            override var classFunctions = mutableListOf<String>()
            override var templateTags = mutableListOf<String>()
            override var supportedExtensions = mutableListOf<String>()
            override var readsClassLikeStrings = true
        }
}
