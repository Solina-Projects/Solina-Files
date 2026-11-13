# Solina Files (سولینا فایلز) — Architecture, Security & Performance Overhaul

**Branch:** `refactor/architecture-v2` (built on `refactor/material3-performance`)  
**Product Branding:** Solina / Solina Files (سولینا فایلز)

## 1. Executive Summary
This branch delivers security hardening, automated JVM unit tests, CI test verification, and the modular architecture roadmap for **Solina Files**:

1. **Credential Backup Leak Closed (`backup_rules.xml`, `data_extraction_rules.xml`)**:
   Explicitly excludes `datastore/solina_remotes.preferences_pb` (where saved remote server credentials live) and `vault/` from Android Cloud Backup and device-to-device transfers.
2. **Cleartext Network Transport Hardened (`network_security_config.xml`)**:
   Sets `<base-config cleartextTrafficPermitted="false">` by default across external hosts while scoping cleartext loopback exclusively to `localhost` and `127.0.0.1` for local viewer/server previews.
3. **JVM Unit Test Suite Added (`app/src/test/java/ir/solina/files/`)**:
   - `RemotePathsAndUriTest`: Traversal normalization, backslash cleanup, breadcrumb generation, URI parsing, and validation against slashes in storage IDs.
   - `RemoteConnectionsTest`: Conditional credential scrubbing (`savePassword = false`) and resilient JSON encode/decode.
   - `ExplorerStateTest`: `NavHistory` bounds (`MAX = 64`), `directionOf`, `childTowards`, `visibleItems` filtering/sorting, and `FolderSorts` serialization.
4. **CI Gate Updated (`.github/workflows/build.yml`)**:
   Executes `./gradlew --stacktrace :app:testDebugUnitTest` prior to `:app:assembleDebug` on `main`, `refactor/**`, and `pull_request` builds.

## 2. Target Modularization Roadmap (APK Slimming)
To eliminate cold-start and base-APK bloat (`~49 MB` potential savings):
- Keep `:app` focused on the core Explorer, Home (MediaStore + bounded LRU folder cache), Search, Vault, and Settings.
- Extract `jadx`, `r8`, `dex2jar`, `smali` into `:feature:apk-inspector` (on-demand Play Feature Delivery).
- Extract `ftpserver-core`, `ktor-server-cio`, `sshd` into `:feature:net-servers` (on-demand).
- Lazy-load `sora-editor`, TextMate grammars, and Oniguruma native libraries upon opening code files.
