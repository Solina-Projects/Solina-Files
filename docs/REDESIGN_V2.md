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
  build — same pattern as `native/fetch-pdfium.sh`. CI runs it in both
  workflows; the files are git-ignored.
- **Folders:** their own purple hue (265) in `KindPalette` instead of the
  seed's primary container — previously every folder was the same brown.
- **Tokens:** `ui/theme/SolinaDesign.kt` documents the v2 decisions.
- **CI:** `build.yml` and `ci.yml` now also run on pushes to
  `design/redesign-v2`, so every Phase 1 push produces a debug APK artifact
  to test on-device (POCO F3). A green run alone is not proof — install it.

## Next phases
- **Phase 2:** Home + drawer (storage card, Places grid, shorter drawer,
  all-Persian copy).
- **Phase 3:** Explorer — single pane by default on phone portrait
  (dual pane stays a setting for tablets/landscape; its default was already
  off), one floating "+" action, row shows date + count/size.
- **Phase 4:** Structure cleanup, dead-code removal, tests for sensitive
  areas (vault, archives, network). No feature is removed in the redesign.
