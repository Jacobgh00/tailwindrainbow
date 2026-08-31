package dev.tailwindrainbow.intellij.adapter.intellij.theme

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.ExtensionPointName
import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import dev.tailwindrainbow.intellij.adapter.theme.ThemeContributor
import dev.tailwindrainbow.intellij.application.port.ThemeDefinitionSource
import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.application.theme.SpecThemeSource
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import java.util.concurrent.CancellationException

object ContributedThemes : ThemeSource, ThemeDefinitionSource {
    private val EXTENSION_POINT = ExtensionPointName<ThemeContributor>("dev.tailwindrainbow.themeContributor")

    override val sourceName: String = "contributed themes"

    override fun themes(): Map<String, RainbowTheme> = SpecThemeSource(specs(), BuiltInThemes).themes()

    override fun specs(): List<ThemeSpec> = EXTENSION_POINT.extensionList.flatMap { it.themesOrNone() }

    @Suppress("TooGenericExceptionCaught")
    private fun ThemeContributor.themesOrNone(): List<ThemeSpec> =
        try {
            themes()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            thisLogger().warn("Theme contributor ${javaClass.name} failed", failure)
            emptyList()
        }
}
