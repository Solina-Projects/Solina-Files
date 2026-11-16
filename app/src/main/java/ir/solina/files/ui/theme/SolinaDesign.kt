package ir.solina.files.ui.theme

import ir.solina.files.core.model.AppearancePrefs
import ir.solina.files.core.model.ContrastLevel
import ir.solina.files.core.model.ThemeSeeds
import ir.solina.files.core.model.ThemeStyle

/**
 * Solina Expressive v2 — design tokens for the Solina-Files redesign
 * (branch design/redesign-v2, Phase 1).
 *
 * Decisions, all reversible from Settings → Appearance:
 * - Seed: expressive purple #6750A4 (same winner as Solina-Player).
 * - Wallpaper colours OFF by default: they made the UI washed-out pale pink.
 * - Style EXPRESSIVE, contrast MEDIUM (was TonalSpot / Standard).
 * - Font: Vazirmatn (see Type.kt, fetched by app/fetch-vazirmatn.sh).
 * - Folders: their own purple hue in KindPalette, not the seed container.
 * - Phone portrait: single pane (dual pane stays a setting for tablets /
 *   landscape — its default was already off in SettingsRepository).
 * - One floating "+" action in the explorer; the old FAB column that covered
 *   list content is removed in Phase 3.
 */
object SolinaDesign {
    const val SEED_ARGB: Int = ThemeSeeds.SOLINA_EXPRESSIVE
    val STYLE: ThemeStyle = ThemeStyle.EXPRESSIVE
    val CONTRAST: ContrastLevel = ContrastLevel.MEDIUM
    const val WALLPAPER_COLORS_DEFAULT: Boolean = false

    /** The Phase 1 defaults, as a ready-made [AppearancePrefs]. */
    fun defaultAppearance(): AppearancePrefs = AppearancePrefs(
        wallpaperColors = WALLPAPER_COLORS_DEFAULT,
        seed = SEED_ARGB,
        style = STYLE,
        contrast = CONTRAST,
    )
}
