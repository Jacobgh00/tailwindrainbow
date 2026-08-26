package dev.tailwindrainbow.intellij.adapter.intellij.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XCollection
import dev.tailwindrainbow.intellij.adapter.intellij.settings.persistence.StoredTheme
import dev.tailwindrainbow.intellij.adapter.theme.UserThemeCatalog
import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.port.SettingsProvider
import dev.tailwindrainbow.intellij.application.port.ThemeCatalog
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

@Service(Service.Level.APP)
@State(name = "TailwindRainbowSettings", storages = [Storage("tailwindRainbow.xml")])
class TailwindRainbowSettings :
    PersistentStateComponent<TailwindRainbowSettings.StoredState>,
    SettingsProvider,
    ThemeCatalog {
    private val storedState = StoredState()

    /** Palette resolution lives here; this class only persists preferences. */
    val themes = UserThemeCatalog()

    @Synchronized
    override fun getState(): StoredState = storedState

    @Synchronized
    override fun loadState(state: StoredState) {
        XmlSerializerUtil.copyBean(state, storedState)
        themes.refresh(storedSpecs())
    }

    override fun themeNamed(name: String): RainbowTheme = themes.themeNamed(name)

    @Synchronized
    override fun current(): HighlightSettings =
        HighlightSettings(
            enabled = storedState.enabled,
            themeName = storedState.themeName,
            scan =
                ScanSettings(
                    maxFileSize = storedState.maxFileSize,
                    classIdentifiers = storedState.classIdentifiers.toSet(),
                    classFunctions = storedState.classFunctions.toSet(),
                    templateTags = storedState.templateTags.toSet(),
                    ignoredPrefixModifiers = storedState.ignoredPrefixModifiers.toSet(),
                    supportedExtensions = storedState.supportedExtensions.toSet(),
                ),
        )

    /**
     * The palette a user override sits on top of: built-ins only.
     *
     * Deliberately not [themeNamed], which returns the merged result — the editor must show what
     * would remain if the user reset a row, so it cannot include the user's own edits.
     */
    @Synchronized
    fun update(
        snapshot: HighlightSettings,
        userThemes: List<ThemeSpec> = storedSpecs(),
    ) {
        storedState.enabled = snapshot.enabled
        storedState.themeName = snapshot.themeName
        storedState.maxFileSize = snapshot.scan.maxFileSize
        storedState.classIdentifiers = snapshot.scan.classIdentifiers.sorted().toMutableList()
        storedState.classFunctions = snapshot.scan.classFunctions.sorted().toMutableList()
        storedState.templateTags = snapshot.scan.templateTags.sorted().toMutableList()
        storedState.ignoredPrefixModifiers = snapshot.scan.ignoredPrefixModifiers.sorted().toMutableList()
        storedState.supportedExtensions = snapshot.scan.supportedExtensions.sorted().toMutableList()
        storedState.themes = userThemes.mapTo(mutableListOf(), StoredTheme::of)
        this.themes.refresh(storedSpecs())
    }

    private fun storedSpecs(): List<ThemeSpec> = storedState.themes.map(StoredTheme::toSpec)

    class StoredState {
        var enabled: Boolean = true
        var themeName: String = "default"
        var maxFileSize: Int = ScanSettings().maxFileSize
        var classIdentifiers: MutableList<String> = ScanSettings.DEFAULT_CLASS_IDENTIFIERS.sorted().toMutableList()
        var classFunctions: MutableList<String> = ScanSettings.DEFAULT_CLASS_FUNCTIONS.sorted().toMutableList()
        var templateTags: MutableList<String> = ScanSettings.DEFAULT_TEMPLATE_TAGS.sorted().toMutableList()
        var ignoredPrefixModifiers: MutableList<String> =
            ScanSettings.DEFAULT_IGNORED_PREFIX_MODIFIERS.sorted().toMutableList()

        @get:XCollection(style = XCollection.Style.v2)
        var themes: MutableList<StoredTheme> = mutableListOf()

        var supportedExtensions: MutableList<String> =
            ScanSettings.DEFAULT_SUPPORTED_EXTENSIONS.sorted().toMutableList()
    }

    companion object {
        fun getInstance(): TailwindRainbowSettings = service()
    }
}
