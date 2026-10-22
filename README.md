# Solina Files

Solina Files is an open-source Android file manager built from the upstream [Strata](https://github.com/CluvexStudio/Strata) project. This fork is being adapted under the Solina Files name while preserving applicable upstream license and third-party notices.

<p align="center">
  <a href="https://github.com/Solina-Projects/Solina-Files/actions/workflows/build.yml"><img src="https://github.com/Solina-Projects/Solina-Files/actions/workflows/build.yml/badge.svg" alt="Build"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-AGPL--3.0-0F3D2E" alt="License: AGPL-3.0"></a>
</p>

## Features

- **File management:** dual-pane browsing, batch operations, bookmarks, search, permissions, recycle bin and file encryption
- **Archives:** browse and extract common archive formats, including ZIP, 7z, RAR and TAR
- **Android packages:** app details, split APK installation, signing tools, DEX and Smali editing
- **Viewers and editors:** code and hex editors, PDF and image viewing, audio and video playback
- **Network storage:** FTP, SFTP, SMB and WebDAV, plus local Wi-Fi sharing
- **Advanced access:** supported root and Shizuku integrations
- **Privacy:** encrypted vault and app lock
- **Interface:** Material 3, dynamic colors, light/dark themes, and right-to-left Persian support

Features can vary by build and device. Refer to the source and issue tracker for current implementation details.

## Build from source

Requirements: Android SDK/NDK, JDK 17, Rust with Android targets, and `cargo-ndk`.

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cargo install cargo-ndk --locked
native/fetch-pdfium.sh
native/build.sh
./gradlew :app:assembleDebug
```

## Licensing and attribution

Solina Files is based on Strata and retains its GNU AGPL v3.0-or-later license and applicable third-party notices. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES). Changes made in this fork are maintained by Solina-Projects.