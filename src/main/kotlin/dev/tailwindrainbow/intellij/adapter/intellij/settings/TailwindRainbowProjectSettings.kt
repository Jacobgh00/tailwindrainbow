package dev.tailwindrainbow.intellij.adapter.intellij.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import dev.tailwindrainbow.intellij.adapter.settings.persistence.RecognitionState
import dev.tailwindrainbow.intellij.adapter.settings.persistence.toScanSettings
import dev.tailwindrainbow.intellij.adapter.settings.persistence.updateFrom
import dev.tailwindrainbow.intellij.application.highlight.ScanSettings

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

    @Synchronized
    fun recognition(): ScanSettings? {
        if (!storedState.ownRecognition) return null

        return storedState.toScanSettings()
    }

    @Synchronized
    fun update(scan: ScanSettings?) {
        storedState.ownRecognition = scan != null
        val effective = scan ?: ScanSettings()

        storedState.updateFrom(effective)
    }

    class StoredState : RecognitionState {
        var ownRecognition: Boolean = false

        override var maxFileSize: Int = DEFAULTS.maxFileSize

        override var classIdentifiers: MutableList<String> = DEFAULTS.classIdentifiers.sorted().toMutableList()

        override var classFunctions: MutableList<String> = DEFAULTS.classFunctions.sorted().toMutableList()

        override var templateTags: MutableList<String> = DEFAULTS.templateTags.sorted().toMutableList()

        override var supportedExtensions: MutableList<String> =
            DEFAULTS.supportedExtensions.sorted().toMutableList()

        override var readsClassLikeStrings: Boolean = DEFAULTS.readsClassLikeStrings

        private companion object {
            val DEFAULTS = ScanSettings()
        }
    }

    companion object {
        fun getInstance(project: Project): TailwindRainbowProjectSettings = project.service()
    }
}
