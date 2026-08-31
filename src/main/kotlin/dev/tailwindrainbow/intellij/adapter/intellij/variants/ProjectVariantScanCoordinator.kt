package dev.tailwindrainbow.intellij.adapter.intellij.variants

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.concurrency.AppExecutorUtil
import dev.tailwindrainbow.intellij.application.port.Cancellation
import dev.tailwindrainbow.intellij.application.variants.VariantDeclaration
import dev.tailwindrainbow.intellij.application.variants.VariantScanResult
import dev.tailwindrainbow.intellij.application.variants.VariantScanner
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

internal class ProjectVariantScanCoordinator(
    private val project: Project,
    private val scanner: VariantScanner,
    private val lifecycle: Disposable,
) {
    @Volatile
    private var snapshot = VariantSnapshot(VariantScanResult(emptyList(), 0), NEVER_READ)

    private val reading = AtomicBoolean(false)
    private val scanGeneration = AtomicLong()

    fun declarations(): List<VariantDeclaration> {
        scheduleUnlessCurrent()

        return snapshot.scan.declarations
    }

    fun refresh(cancellation: Cancellation): VariantScanResult {
        val generation = scanGeneration.incrementAndGet()
        val scanned = scanUntilStable(cancellation)

        remember(scanned, generation)

        return scanned.result
    }

    private fun scheduleUnlessCurrent() {
        if (snapshot.readAt == modificationCount() || !reading.compareAndSet(false, true)) return

        val generation = scanGeneration.incrementAndGet()

        ReadAction.nonBlocking<ScannedVariants> {
            val readAt = modificationCount()
            ScannedVariants(scanner.scanResult(), readAt)
        }
            .expireWith(lifecycle)
            .submit(AppExecutorUtil.getAppExecutorService())
            .onSuccess { scanned -> remember(scanned, generation) }
            .onProcessed { reading.set(false) }
    }

    private fun scanUntilStable(cancellation: Cancellation): ScannedVariants {
        var latest: ScannedVariants? = null

        repeat(MAX_STABILISATION_ATTEMPTS) {
            cancellation.check()
            val readAt = modificationCount()
            val scanned = ScannedVariants(runReadAction { scanner.scanResult(cancellation) }, readAt)

            if (readAt == modificationCount()) return scanned
            latest = scanned
        }

        return checkNotNull(latest) { "a bounded retry always scans at least once" }
    }

    private fun remember(
        scanned: ScannedVariants,
        generation: Long,
    ) {
        if (generation != scanGeneration.get() || scanned.readAt != modificationCount()) return

        snapshot =
            VariantSnapshot(
                scanned.result.copy(declarations = scanned.result.declarations.toList()),
                scanned.readAt,
            )
    }

    private fun modificationCount(): Long = PsiModificationTracker.getInstance(project).modificationCount

    private data class VariantSnapshot(
        val scan: VariantScanResult,
        val readAt: Long,
    )

    private data class ScannedVariants(
        val result: VariantScanResult,
        val readAt: Long,
    )

    private companion object {
        const val NEVER_READ = -1L
        const val MAX_STABILISATION_ATTEMPTS = 3
    }
}
