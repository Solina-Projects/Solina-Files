package ir.solina.files.ui

import androidx.compose.runtime.staticCompositionLocalOf
import ir.solina.files.core.data.SolinaSettings
import ir.solina.files.core.model.UiDensity

val LocalUiDensity = staticCompositionLocalOf { UiDensity.DEFAULT }

val LocalSettings = staticCompositionLocalOf { SolinaSettings() }
