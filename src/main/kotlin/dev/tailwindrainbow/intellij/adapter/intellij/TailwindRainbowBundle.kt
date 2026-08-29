package dev.tailwindrainbow.intellij.adapter.intellij

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.TailwindRainbowBundle"

object TailwindRainbowBundle : DynamicBundle(BUNDLE) {
    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg parameters: Any,
    ): String = getMessage(key, *parameters)
}
