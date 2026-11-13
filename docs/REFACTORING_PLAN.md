# Solina Files modernization plan

## Approved direction

- Preserve all existing user-facing capabilities and platform integrations.
- Use Material 3 Expressive as the visual foundation.
- Complete the redesign and modernization in incremental, reviewable changes on `refactor/material3-performance`.
- Do not remove features or dependencies solely because they appear large; first verify call sites, packaging impact, runtime loading, and replacement coverage.

## Workstreams

### 1. Explorer performance and state
- [x] Remove recursive filesystem traversal from per-row composition.
- Keep directory listing, sorting, filtering, thumbnail decoding, and metadata work off the main thread.
- Introduce bounded, cancellable work and explicit cache invalidation for expensive metadata.
- [x] Add distinct pane-specific state streams and collect each pane independently in its content composable.
- [ ] Further reduce broad screen-level UI state subscriptions and profile recompositions.
- Preserve stable LazyColumn/LazyGrid keys and scroll restoration.

### 2. Material 3 design system
- [x] Add shared semantic spacing tokens and apply them to explorer rows and pane layouts.
- [x] Localize explorer file-row icon accessibility descriptions in English and Persian.
- [ ] Review typography, icon sizing/semantics, shapes, elevation, and motion across all features; retain Material 3 defaults where already appropriate.
- Normalize row, grid, toolbar, selection, dialog, and bottom-sheet layouts.
- Audit RTL, Persian strings, dynamic color, dark/light themes, and large font/display sizes.
- Keep accessibility labels, touch targets, keyboard, and screen-reader behavior.

### 3. Feature modularization and startup
- Map dependency ownership before moving code.
- Separate feature implementation behind stable interfaces where it reduces startup coupling.
- [x] Defer TextMate theme loading until a theme is selected; keep the initial theme available at editor startup.
- [x] Defer sound-pool creation and sample loading until sound effects are enabled.
- [x] Confirm native library loading is lazy through `NativeBridge.ensureLoaded()`.
- [x] Confirm PDFium initialization and its dedicated render dispatcher are lazy.
- [x] Confirm archive listing/extraction is initiated by `ArchiveViewModel` operations rather than application startup.
- [ ] Review media tools, APK analysis, and network clients for avoidable eager initialization.
- Evaluate Android dynamic feature delivery only for features that can safely be optional; retain an offline-capable core.

### 4. Dependency and package-size audit
- Generate dependency insight and APK/AAB size reports for each ABI.
- Remove only verified unused transitive/direct dependencies.
- Check duplicate classes, native libraries, resource shrinking, and R8 rules.
- Preserve native Rust, PDFium, root/Shizuku, archive, network, editor, and APK functionality.

### 5. Verification
- Run unit tests, lint, debug/release builds, and native checks.
- Add regression tests around filesystem listing, navigation cancellation, selection, sorting, and metadata.
- Use Macrobenchmark and Compose tracing/Recomposition inspection on representative devices.
- Compare startup, scroll frame times, memory, and APK size against a recorded baseline.

## Lazy-initialization audit

- `NativeBridge` calls `System.loadLibrary("solina_native")` only through `ensureLoaded()` when a native operation or availability check is requested.
- `Pdfium` creates its single-thread dispatcher via Kotlin `by lazy` and loads PDFium/native libraries only from `ensure()`.
- Archive I/O is started by archive view-model actions (`open`/`reload`/`materialize`), not by `SolinaFilesApp.onCreate()`.
- Keep these existing on-demand boundaries; do not add redundant wrappers around them.

## First change

The explorer previously recursively walked every readable directory from each visible single-pane file row to calculate descendant size/count. That work could be repeated as rows entered composition and could traverse very large trees. The first change removes that recursive scan from composition and uses the already available immediate `FileItem.childCount` when present. This deliberately avoids changing file operations or directory contents. A dedicated, bounded on-demand folder statistics feature can be introduced later if product requirements still need recursive sizes.

## Acceptance criteria

- No loss of existing file-management, archive, network, editor, media, PDF, APK, root, Shizuku, or vault functionality.
- No recursive directory scan triggered by composing a file row.
- No filesystem, decoding, or analysis work on the UI thread.
- No unverified dependency removals.
- CI passes for debug build and lint; release build and tests pass where CI prerequisites are available.
- Visual behavior is consistent across light/dark themes, RTL/LTR, phones, and tablets.
