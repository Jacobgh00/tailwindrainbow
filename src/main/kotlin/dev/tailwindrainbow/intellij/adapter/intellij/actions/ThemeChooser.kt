package dev.tailwindrainbow.intellij.adapter.intellij.actions

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.popup.list.ListPopupImpl
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settingsChanged

internal fun themeChooser(settings: TailwindRainbowSettings): ListPopup {
    val step =
        object : BaseListPopupStep<String>(message("popup.selectTheme.title"), settings.themes.names().toList()) {
            override fun onChosen(
                selectedValue: String,
                finalChoice: Boolean,
            ): PopupStep<*>? {
                settings.chooseTheme(selectedValue)
                settingsChanged()

                return FINAL_CHOICE
            }
        }

    return JBPopupFactory.getInstance().createListPopup(step).previewingSelection(settings)
}

private fun ListPopup.previewingSelection(settings: TailwindRainbowSettings): ListPopup {
    (this as? ListPopupImpl)?.addListSelectionListener {
        settings.previewTheme(list.selectedValue as? String)
        settingsChanged()
    }

    addListener(
        object : JBPopupListener {
            override fun onClosed(event: LightweightWindowEvent) {
                if (!event.isOk) {
                    settings.previewTheme(null)
                    settingsChanged()
                }
            }
        },
    )

    return this
}
