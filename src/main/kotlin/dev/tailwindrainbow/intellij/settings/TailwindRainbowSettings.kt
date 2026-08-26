package dev.tailwindrainbow.intellij.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import dev.tailwindrainbow.intellij.domain.ScanSettings

@Service(Service.Level.APP)
@State(name = "TailwindRainbowSettings", storages = [Storage("tailwindRainbow.xml")])

class TailwindRainbowSettings : PersistentStateComponent<TailwindRainbowSettings.StoredState> {
    private val storedState = StoredState()

    override fun getState(): StoredState = storedState

    override fun loadState(state: StoredState) {
        XmlSerializerUtil.copyBean(state, storedState)
    }

    @Synchronized
    fun snapshot(): SettingsSnapshot = SettingsSnapshot(
        enabled = storedState.enabled,
        themeName = storedState.themeName,
        scanSettings = ScanSettings(
            maxFileSize = storedState.maxFileSize,
            classIdentifiers = storedState.classIdentifiers.toSet(),
            classFunctions = storedState.classFunctions.toSet(),
            templateTags = storedState.templateTags.toSet(),
            ignoredPrefixModifiers = storedState.ignoredPrefixModifiers.toSet(),
            supportedExtensions = storedState.supportedExtensions.toSet(),
        ),
    )

    @Synchronized
    fun update(snapshot: SettingsSnapshot) {
        storedState.enabled = snapshot.enabled
        storedState.themeName = snapshot.themeName
        storedState.maxFileSize = snapshot.scanSettings.maxFileSize
        storedState.classIdentifiers = snapshot.scanSettings.classIdentifiers.sorted().toMutableList()
        storedState.classFunctions = snapshot.scanSettings.classFunctions.sorted().toMutableList()
        storedState.templateTags = snapshot.scanSettings.templateTags.sorted().toMutableList()
        storedState.ignoredPrefixModifiers = snapshot.scanSettings.ignoredPrefixModifiers.sorted().toMutableList()
        storedState.supportedExtensions = snapshot.scanSettings.supportedExtensions.sorted().toMutableList()
    }

    class StoredState {
        var enabled: Boolean = true
        var themeName: String = "default"
        var maxFileSize: Int = 1_000_000
        var classIdentifiers: MutableList<String> = ScanSettings.DEFAULT_CLASS_IDENTIFIERS.sorted().toMutableList()
        var classFunctions: MutableList<String> = ScanSettings.DEFAULT_CLASS_FUNCTIONS.sorted().toMutableList()
        var templateTags: MutableList<String> = ScanSettings.DEFAULT_TEMPLATE_TAGS.sorted().toMutableList()
        var ignoredPrefixModifiers: MutableList<String> =
            ScanSettings.DEFAULT_IGNORED_PREFIX_MODIFIERS.sorted().toMutableList()
        var supportedExtensions: MutableList<String> = ScanSettings.DEFAULT_SUPPORTED_EXTENSIONS.sorted().toMutableList()
    }

    companion object {
        fun getInstance(): TailwindRainbowSettings = service()
    }
}

data class SettingsSnapshot(
    val enabled: Boolean,
    val themeName: String,
    val scanSettings: ScanSettings,
)
