package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.popup.JBPopupFactory
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import javax.swing.JButton
import javax.swing.JComponent

internal interface ThemeCommands {
    fun create()

    fun duplicate()

    fun rename()

    fun delete()

    fun import()

    fun export()

    fun ownsSelected(): Boolean
}

internal class ThemeMenu(private val commands: ThemeCommands) {
    val component: JComponent =
        JButton(AllIcons.General.GearPlain).apply {
            toolTipText = message("settings.theme.menu")
            accessibleContext.accessibleName = message("settings.theme.menu")
            addActionListener { showMenu() }
        }

    private fun showMenu() {
        val group =
            DefaultActionGroup(
                item("settings.theme.new", commands::create),
                item("settings.theme.duplicate", commands::duplicate),
                item("settings.theme.rename", commands::rename, commands::ownsSelected),
                item("settings.theme.delete", commands::delete, commands::ownsSelected),
                Separator.getInstance(),
                item("themeFile.import", commands::import),
                item("themeFile.export", commands::export),
            )

        JBPopupFactory.getInstance()
            .createActionGroupPopup(
                null,
                group,
                DataManager.getInstance().getDataContext(component),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true,
            ).showUnderneathOf(component)
    }

    private fun item(
        key: String,
        run: () -> Unit,
        enabled: () -> Boolean = { true },
    ): AnAction =
        object : DumbAwareAction(message(key)) {
            override fun getActionUpdateThread() = ActionUpdateThread.EDT

            override fun update(event: AnActionEvent) {
                event.presentation.isEnabled = enabled()
            }

            override fun actionPerformed(event: AnActionEvent) = run()
        }
}
