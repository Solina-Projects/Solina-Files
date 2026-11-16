# Solina Files — Redesign v2 (branch `design/redesign-v2`)

Full phased redesign in Kotlin. `main` is untouched; merge only when the user
says so. Plan and mockups: `~/workspace/solina-files-design/` (PLAN.md,
mockups.html).

## Phase 1 — design system (this commit)
- **Seed:** expressive purple `#6750A4` (same winner as Solina-Player),
  style `EXPRESSIVE`, contrast `MEDIUM` (`AppearancePrefs` defaults).
- **Wallpaper colours OFF by default.** They were the reason the UI came out
  washed-out pale pink on most phones; the Solina seed owns the palette now.
  Users can still turn wallpaper colours back on in Settings → Appearance.
- **Font:** Vazirmatn for all text styles (`ui/theme/Type.kt`).
  The TTF files are not committed: `app/fetch-vazirmatn.sh` downloads them
  (SHA-256 verified, SIL OFL 1.1) into `app/src/main/res/font/` before the
  build — same pattern as `native/fetch-pdfium.sh`. The fetch runs from a
  `fetchVazirmatn` Gradle task wired into `preBuild`, so no workflow file
  change is needed; the files are git-ignored. Font files must be lowercase
  `vazirmatn_*.ttf` — Android resource names allow only a–z, 0–9, `_`.
- **Folders:** their own purple hue (265) in `KindPalette` instead of the
  seed's primary container — previously every folder was the same brown.
- **Tokens:** `ui/theme/SolinaDesign.kt` documents the v2 decisions.
- **CI:** the GitHub App has no `workflows` permission, so the workflow files
  cannot be updated via API and CI-on-push for this branch is NOT enabled.
  Verification runs through draft PR #11 (`design/redesign-v2` → `main`,
  DO NOT MERGE), whose `pull_request` trigger fires on every push
  (synchronize). A green run alone is not proof — install the debug APK
  artifact on-device (POCO F3). Phase 1 verified green on 2026-11-14
  (CI & APK Build run 43491029060).

## Phase 2 — Home + drawer (this commit)
- **Home (`feature/home/HomeScreen.kt`):** all UI labels Persian; category
  colours from `KindPalette` (one hue per file kind) instead of hardcoded
  random colours; shortcut icons tinted by kind; storage bar thicker
  (14 dp) on a higher-contrast track.
- **Drawer (`ui/drawer/SolinaDrawer.kt`):** Places tiles share one rounded
  shape (20 dp) and take their colour from `KindPalette` — the old tiles
  each had a random morph shape and an analogous palette, so
  `rememberPlacePalette`, `MorphShape` and the per-place shapes are gone.
  Drawer groups use one fixed icon shape (12 dp). The hardcoded Persian
  "استفاده‌شده" in the storage card is now `R.string.drawer_used`
  (values + values-fa).
- **Not done yet:** launcher icon is still the copied JPG, and the stray
  root images (`Polish_*`, `179289*.jpg`) are still in the repo — Phase 4.

## Screenshot fixes (2026-11-16, from the user's single-pane screenshot)
- **Theme defaults really applied:** `SettingsRepository.readAppearance` used
  the OLD fallbacks (wallpaper on, green seed, TonalSpot, Standard), so the
  Phase 1 purple never reached a fresh install — the screenshot was still
  washed-out pale pink. Fallbacks now match SolinaDesign.
- **Folders:** `FileRow` tinted folders with the theme primary (brown under
  the wallpaper palette); now they use their KindPalette colour like files.
- **Layout toggle moved:** the grid/list button left the floating column and
  lives in the path header's end corner (`PaneColumn.PathHeader`), with its
  list/grid + density menu floating under the header. The other two floating
  buttons stay where they were.
- **Language:** the app is Persian-first — a fresh install now defaults to
  Persian (first launch only, `AppLocales.applyDefaultIfUnset`); Settings →
  Language still switches to English/System.
- **Path header redesigned** (same size): leading storage mark, title/path,
  badges, layout button.
- **Dates:** Gregorian short dates now spell the month ("Nov 8, 2026" /
  "۸ نوامبر ۲۰۲۶") instead of the ambiguous "26-11-08".

## Next phases
- **Phase 3:** Explorer — single pane by default on phone portrait
  (dual pane stays a setting for tablets/landscape; its default was already
  off), one floating "+" action, row shows date + count/size.
- **Phase 4:** Structure cleanup, dead-code removal, tests for sensitive
  areas (vault, archives, network). No feature is removed in the redesign.
