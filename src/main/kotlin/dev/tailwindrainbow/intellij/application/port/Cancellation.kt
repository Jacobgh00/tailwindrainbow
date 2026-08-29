package dev.tailwindrainbow.intellij.application.port

fun interface Cancellation {
    fun check()

    companion object {
        val NONE = Cancellation {}
    }
}
