package dev.tailwindrainbow.intellij.adapter.intellij.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import dev.tailwindrainbow.intellij.application.highlight.ScanSettings

/**
 * The recognition rules a project keeps for itself.
 *
 * Stored in the project rather than in the IDE because which helpers and file types hold Tailwind
 * classes is a property of the codebase: committing `.idea/tailwindRainbow.xml` gives everyone who
 * opens the repository the same answer. Themes are deliberately not here — see
 * [dev.tailwindrainbow.intellij.application.settings.withProjectRecognition].
 */
@Service(Service.Level.PROJECT)
@State(name = "TailwindRainbowProjectSettings", storages = [Storage("tailwindRainbow.xml")])
class TailwindRainbowProjectSettings : PersistentStateComponent<TailwindRainbowProjectSettings.StoredState> {
    private val storedState = StoredState()

    @Synchronized
    override fun getState(): StoredState = storedState

    @Synchronized
    override fun loadState(state: StoredState) {
        XmlSerializerUtil.copyBean(state, storedState)
    }

    /** The rules this project keeps, or null while it follows the ones set in the IDE. */
    @Synchronized
    fun recognition(): ScanSettings? {
        if (!storedState.ownRecognition) return null

        return ScanSettings(
            maxFileSize = storedState.maxFileSize,
            classIdentifiers = storedState.classIdentifiers.toSet(),
            classFunctions = storedState.classFunctions.toSet(),
            templateTags = storedState.templateTags.toSet(),
            ignoredPrefixModifiers = storedState.ignoredPrefixModifiers.toSet(),
            supportedExtensions = storedState.supportedExtensions.toSet(),
        )
    }

    /** Null hands the rules back to the IDE-wide settings, keeping nothing behind in the project. */
    @Synchronized
    fun update(scan: ScanSettings?) {
        storedState.ownRecognition = scan != null
        val effective = scan ?: ScanSettings()

        storedState.maxFileSize = effective.maxFileSize
        storedState.classIdentifiers = effective.classIdentifiers.sorted().toMutableList()
        storedState.classFunctions = effective.classFunctions.sorted().toMutableList()
        storedState.templateTags = effective.templateTags.sorted().toMutableList()
        storedState.ignoredPrefixModifiers = effective.ignoredPrefixModifiers.sorted().toMutableList()
        storedState.supportedExtensions = effective.supportedExtensions.sorted().toMutableList()
    }

    class StoredState {
        var ownRecognition: Boolean = false
        var maxFileSize: Int = ScanSettings().maxFileSize
        var classIdentifiers: MutableList<String> = ScanSettings.DEFAULT_CLASS_IDENTIFIERS.sorted().toMutableList()
        var classFunctions: MutableList<String> = ScanSettings.DEFAULT_CLASS_FUNCTIONS.sorted().toMutableList()
        var templateTags: MutableList<String> = ScanSettings.DEFAULT_TEMPLATE_TAGS.sorted().toMutableList()
        var ignoredPrefixModifiers: MutableList<String> =
            ScanSettings.DEFAULT_IGNORED_PREFIX_MODIFIERS.sorted().toMutableList()
        var supportedExtensions: MutableList<String> =
            ScanSettings.DEFAULT_SUPPORTED_EXTENSIONS.sorted().toMutableList()
    }

    companion object {
        fun getInstance(project: Project): TailwindRainbowProjectSettings = project.service()
    }
}
