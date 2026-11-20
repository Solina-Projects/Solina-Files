package ir.solina.files.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import ir.solina.files.core.data.SolinaSettings
import ir.solina.files.core.model.AppearancePrefs
import ir.solina.files.core.model.BehaviorPrefs
import ir.solina.files.core.model.ListPrefs
import ir.solina.files.core.model.UiDensity

val LocalUiDensity = staticCompositionLocalOf { UiDensity.DEFAULT }

val LocalSettings = staticCompositionLocalOf { SolinaSettings() }

// PERF_AUDIT_2 item 15: fine-grained slices of LocalSettings. Every row
// used to read the whole SolinaSettings, so any single field change
// recomposed all visible rows. Rows now read only the slices they need;
// LocalSettings stays as-is for all other readers.
//
// These MUST be compositionLocalOf (tracked reads), not
// staticCompositionLocalOf: a static local invalidates its whole provider
// scope on any value change, which would erase the win. With tracked reads
// and structural equality, a row reading LocalListPrefs only recomposes
// when the ListPrefs value itself changes — e.g. changing an appearance
// field leaves settings.list untouched (same instance), so list readers
// are not invalidated.
val LocalListPrefs = compositionLocalOf { ListPrefs() }

val LocalAppearancePrefs = compositionLocalOf { AppearancePrefs() }

val LocalBehaviorPrefs = compositionLocalOf { BehaviorPrefs() }
