package dev.tailwindrainbow.intellij.application.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecognitionWorkspaceTest {
    private val application = recognition("cn", "html")
    private val project = recognition("twcx", "templ")

    @Test
    fun `loading application rules displays and returns application rules`() {
        val workspace = RecognitionWorkspace.load(application, null)

        assertEquals(RecognitionOwner.APPLICATION, workspace.owner)
        assertEquals(application, workspace.displayed)
        assertEquals(application, workspace.applicationRules())
        assertNull(workspace.projectRules())
    }

    @Test
    fun `loading project rules displays project rules and keeps application rules`() {
        val workspace = RecognitionWorkspace.load(application, project)

        assertEquals(RecognitionOwner.PROJECT, workspace.owner)
        assertEquals(project, workspace.displayed)
        assertEquals(application, workspace.applicationRules())
        assertEquals(project, workspace.projectRules())
    }

    @Test
    fun `unsaved project edits survive switching to application rules and back`() {
        val editedProject = project.copy(classFunctions = "edited")
        val workspace =
            RecognitionWorkspace
                .load(application, project)
                .updateDisplayed(editedProject)
                .select(RecognitionOwner.APPLICATION)
                .select(RecognitionOwner.PROJECT)

        assertEquals(editedProject, workspace.displayed)
        assertEquals(editedProject, workspace.projectRules())
        assertEquals(application, workspace.applicationRules())
    }

    @Test
    fun `unsaved application edits survive switching to project rules and back`() {
        val editedApplication = application.copy(classFunctions = "edited")
        val workspace =
            RecognitionWorkspace
                .load(application, project)
                .select(RecognitionOwner.APPLICATION)
                .updateDisplayed(editedApplication)
                .select(RecognitionOwner.PROJECT)
                .select(RecognitionOwner.APPLICATION)

        assertEquals(editedApplication, workspace.displayed)
        assertEquals(editedApplication, workspace.applicationRules())
        assertEquals(null, workspace.projectRules())
    }

    @Test
    fun `selecting project ownership from application rules seeds project rules`() {
        val workspace = RecognitionWorkspace.load(application, null).select(RecognitionOwner.PROJECT)

        assertEquals(application, workspace.projectRules())
        assertEquals(application, workspace.displayed)
    }

    @Test
    fun `clearing project ownership returns no project result`() {
        val workspace = RecognitionWorkspace.load(application, project).select(RecognitionOwner.APPLICATION)

        assertNull(workspace.projectRules())
        assertEquals(application, workspace.applicationRules())
    }

    @Test
    fun `loading fresh settings discards the previous hidden project form`() {
        val workspace =
            RecognitionWorkspace
                .load(application, project)
                .updateDisplayed(project.copy(classFunctions = "stale"))
                .select(RecognitionOwner.APPLICATION)
                .let { RecognitionWorkspace.load(application, null) }

        assertEquals(application, workspace.displayed)
        assertNull(workspace.projectRules())
    }

    private fun recognition(
        classFunctions: String,
        supportedExtensions: String,
    ) = RecognitionForm(
        maxFileSize = "1000",
        classIdentifiers = "class",
        classFunctions = classFunctions,
        templateTags = "tw",
        supportedExtensions = supportedExtensions,
        readsClassLikeStrings = true,
    )
}
