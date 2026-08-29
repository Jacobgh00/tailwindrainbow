package dev.tailwindrainbow.intellij.adapter.intellij.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message

internal class TailwindStatusWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = TAILWIND_STATUS_WIDGET_ID

    override fun getDisplayName(): String = message("widget.name")

    override fun createWidget(project: Project): StatusBarWidget = TailwindStatusWidget(project)
}
