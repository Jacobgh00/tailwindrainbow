package dev.tailwindrainbow.intellij.adapter.color

import dev.tailwindrainbow.intellij.domain.theme.rgbToHex
import java.awt.Color

internal fun Color.toHex(): String = rgbToHex(red, green, blue)
