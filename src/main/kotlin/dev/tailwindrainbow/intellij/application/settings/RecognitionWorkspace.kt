package dev.tailwindrainbow.intellij.application.settings

enum class RecognitionOwner {
    APPLICATION,
    PROJECT,
}

data class RecognitionWorkspace(
    val application: RecognitionForm,
    val project: RecognitionForm?,
    val owner: RecognitionOwner,
) {
    init {
        require(owner != RecognitionOwner.PROJECT || project != null) {
            "project-owned recognition needs project rules"
        }
    }

    val displayed: RecognitionForm
        get() =
            when (owner) {
                RecognitionOwner.APPLICATION -> application
                RecognitionOwner.PROJECT -> requireNotNull(project)
            }

    fun updateDisplayed(form: RecognitionForm): RecognitionWorkspace =
        when (owner) {
            RecognitionOwner.APPLICATION -> copy(application = form)
            RecognitionOwner.PROJECT -> copy(project = form)
        }

    fun select(nextOwner: RecognitionOwner): RecognitionWorkspace =
        when (nextOwner) {
            RecognitionOwner.APPLICATION -> copy(project = displayed, owner = nextOwner)
            RecognitionOwner.PROJECT -> copy(project = project ?: displayed, owner = nextOwner)
        }

    fun applicationRules(): RecognitionForm = application

    fun projectRules(): RecognitionForm? = project.takeIf { owner == RecognitionOwner.PROJECT }

    companion object {
        fun load(
            application: RecognitionForm,
            project: RecognitionForm?,
        ): RecognitionWorkspace =
            RecognitionWorkspace(
                application = application,
                project = project,
                owner = if (project == null) RecognitionOwner.APPLICATION else RecognitionOwner.PROJECT,
            )
    }
}
