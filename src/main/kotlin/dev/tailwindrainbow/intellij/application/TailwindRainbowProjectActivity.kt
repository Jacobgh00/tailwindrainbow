package dev.tailwindrainbow.intellij.application

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.tailwindrainbow.intellij.editor.TailwindRainbowProjectService

class TailwindRainbowProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<TailwindRainbowProjectService>()
    }
}