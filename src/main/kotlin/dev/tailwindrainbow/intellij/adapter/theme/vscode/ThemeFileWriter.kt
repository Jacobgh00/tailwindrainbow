package dev.tailwindrainbow.intellij.adapter.theme.vscode

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

internal fun RainbowTheme.toThemeFile(name: String): String {
    val sections =
        JsonObject().apply {
            add(PREFIX, prefix.asJson())
            add(BASE, base.asJson())
            arbitrary?.let { add(ARBITRARY, it.asJson()) }
            important?.let { add(IMPORTANT, it.asJson()) }
        }

    return GsonBuilder().setPrettyPrinting().create().toJson(JsonObject().apply { add(name, sections) })
}

private fun Map<String, TextStyle>.asJson(): JsonObject {
    return JsonObject().apply { forEach { (key, style) -> add(key, style.asJson()) } }
}

private fun TextStyle.asJson(): JsonObject =
    JsonObject().apply {
        addProperty(COLOR, color)
        addProperty(FONT_WEIGHT, fontWeight.value.toString())
        if (!enabled) addProperty(ENABLED, false)
    }
