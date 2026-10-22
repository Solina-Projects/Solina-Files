# Solina Files

**Solina Files** is an open-source file manager for Android, developed under the **Solin** project identity. It aims to make everyday file organization and advanced file workflows clear, capable, and convenient.

<p align="center">
  <a href="https://github.com/Solina-Projects/Solina-Files/actions/workflows/build.yml"><img src="https://github.com/Solina-Projects/Solina-Files/actions/workflows/build.yml/badge.svg" alt="Solina Files build status"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-AGPL--3.0-2456B3" alt="License: GNU AGPL v3.0 or later"></a>
</p>

## Project identity

- **Project:** Solin
- **Application:** Solina Files
- **Android application ID:** `ir.solina.files`
- **Repository:** [Solina-Projects/Solina-Files](https://github.com/Solina-Projects/Solina-Files)

Solin is the project identity; Solina Files is its Android file-management application.

## Features

Solina Files is being developed around these file-management areas:

- **File browsing:** navigate folders and manage files on device storage.
- **Dual-pane workflow:** work with two locations to make file-to-file operations easier.
- **File operations:** organize, copy, move, rename, delete, search, and select multiple items where supported.
- **Storage overview:** browse storage locations and inspect files and folders.
- **Archives:** work with supported archive formats.
- **Android apps:** inspect supported application package information and related tools.
- **Media and documents:** access supported image, audio, video, and document files.
- **Network access:** support for configured network-storage protocols where available.
- **Privacy and access:** device-dependent options for protected storage and advanced access.
- **Interface:** Material Design components, light and dark appearance, and Persian right-to-left usability.

Feature availability may vary by version, device, permissions, and build configuration. Only features present in the installed build should be considered available.

## Download and builds

Check the repository's [Releases](https://github.com/Solina-Projects/Solina-Files/releases) for published versions and the [Actions](https://github.com/Solina-Projects/Solina-Files/actions) page for automated build results and development artifacts.

A successful workflow run is required before treating a build as verified. Development builds and release builds may differ in signing, optimization, and supported configurations.

## Build from source

### Requirements

- Android SDK and Android NDK
- JDK 17
- Rust toolchain with Android targets
- `cargo-ndk`

### Build steps

From the repository root, prepare the Rust targets and native dependencies, then run the Gradle task:

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cargo install cargo-ndk --locked
native/fetch-pdfium.sh
native/build.sh
./gradlew :app:assembleDebug
```

The build environment may require additional setup depending on the host operating system and the native dependencies used by the selected build.

## Contributing and reporting issues

Bug reports, reproducible build failures, and focused improvement proposals are welcome through the repository's [Issues](https://github.com/Solina-Projects/Solina-Files/issues) page.

When reporting a problem, include the app version, Android version, device details, steps to reproduce, and relevant logs. Do not include passwords, private files, access tokens, or other sensitive information.

## License

Solina Files is distributed under the [GNU Affero General Public License v3.0 or later](LICENSE). Third-party components may have their own license terms; consult [THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES) and the relevant component notices for details.
