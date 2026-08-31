package dev.tailwindrainbow.intellij.adapter.intellij.variants

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import dev.tailwindrainbow.intellij.application.port.Cancellation
import dev.tailwindrainbow.intellij.application.variants.VariantDeclaration
import dev.tailwindrainbow.intellij.application.variants.VariantScanResult
import dev.tailwindrainbow.intellij.application.variants.VariantScanner

@Service(Service.Level.PROJECT)
class ProjectVariants(private val project: Project) : Disposable {
    private val scanCoordinator by lazy {
        ProjectVariantScanCoordinator(
            project = project,
            scanner = VariantScanner(ProjectVariantFiles(project).sources()),
            lifecycle = this,
        )
    }

    fun declared(): Set<String> = scanCoordinator.declarations().names()

    fun refresh(): Set<String> = refreshScan().declarations.names()

    fun refreshScan(cancellation: Cancellation = Cancellation.NONE): VariantScanResult {
        return scanCoordinator.refresh(cancellation)
    }

    override fun dispose() = Unit

    companion object {
        fun getInstance(project: Project): ProjectVariants = project.service()
    }
}

private fun List<VariantDeclaration>.names(): Set<String> = mapTo(linkedSetOf(), VariantDeclaration::name)
