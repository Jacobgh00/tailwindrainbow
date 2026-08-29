package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.describe
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

internal class ThemeProblemsBanner(
    private val onShow: (ThemeProblem) -> Unit,
    private val onRemove: (List<ThemeProblem>) -> Unit,
) {
    private val holder = JPanel(BorderLayout()).apply { isVisible = false }

    val component: JComponent = holder

    fun show(found: List<ThemeProblem>) {
        holder.removeAll()
        holder.isVisible = found.isNotEmpty()

        if (found.isNotEmpty()) {
            holder.add(bannerFor(found), BorderLayout.CENTER)
        }

        holder.revalidate()
        holder.repaint()
    }

    private fun bannerFor(found: List<ThemeProblem>): InlineBanner =
        InlineBanner(summary(found), EditorNotificationPanel.Status.Warning)
            .showCloseButton(false)
            .addAction(message("problems.action.show")) { onShow(found.first()) }
            .addAction(message("problems.action.remove")) {
                onRemove(found)
                show(emptyList())
            }

    private fun summary(found: List<ThemeProblem>): String {
        val headline =
            if (found.size == 1) {
                message("problems.banner.one")
            } else {
                message("problems.banner.many", found.size)
            }
        val listed = found.take(DETAILS_SHOWN).joinToString("\n") { it.describe() }
        val rest = found.size - DETAILS_SHOWN

        return if (rest > 0) {
            "$headline\n$listed\n" + message("problems.more", rest)
        } else {
            "$headline\n$listed"
        }
    }

    private companion object {
        const val DETAILS_SHOWN = 3
    }
}
