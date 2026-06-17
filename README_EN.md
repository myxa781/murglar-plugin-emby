# Murglar Plugin — Emby

An extension for [Murglar](https://github.com/badmannersteam/murglar-plugins) that allows you to browse and stream music directly from your Emby server.

---

## Features

* Favorite tracks, albums, and artists
* Playlists
* Browse music libraries by folders
* All tracks, albums, and artists (with pagination)
* Search for tracks, albums, and artists
* Like / Dislike support
* Album artwork
* Direct streaming of original audio files (no transcoding)
* Supported audio formats:

  * MP3
  * FLAC
  * AAC / M4A
  * OGG
  * WAV
  * WMA

---

## Building

### Requirements

* JDK 17 or newer
* Android SDK (required only for building the Android APK)
* Signing keystore (required only for Android builds)

### Build Steps

```bash
# 1. Configure local.properties
cp local.template.properties local.properties

# Edit local.properties and set:
# - sdk.dir
# - keystore parameters

# 2. Build
./gradlew clean build
```

### Build Artifacts

| Platform      | Output                                                             |
| ------------- | ------------------------------------------------------------------ |
| Desktop (JAR) | `emby-core/build/libs/murglar-plugin-emby-1.jar`                   |
| Android (APK) | `emby-android/build/outputs/apk/release/murglar-plugin-emby-1.apk` |

### Create a Keystore (if needed)

```bash
keytool -genkey -v \
-keystore keystore.jks \
-alias key \
-keyalg RSA \
-keysize 2048 \
-validity 10000
```

---

## Authentication in Murglar

Open the **Emby** plugin in Murglar and tap **Sign In**.

Two authentication methods are available.

### Option 1 — API Key (Recommended)

1. Open the Emby Dashboard.
2. Navigate to **Advanced → API Keys**.
3. Create a new API key.
4. In Murglar, enter:

* **Server URL** — for example:
  `http://192.168.1.10:8096`
* **API Key** — the key you generated.

### Option 2 — Username & Password

Enter the following:

* Server URL
* Username
* Password

using your Emby account credentials.

---

## Project Structure

```text
emby-core/                Platform-independent plugin logic (JAR)
emby-android/             Android wrapper (APK)
local.template.properties Local configuration template
```

---

## Versions

| Component             | Version |
| --------------------- | ------- |
| Murglar Plugins API   | 7.0     |
| Plugin Version        | 1       |
| Kotlin                | 2.2.0   |
| Android Gradle Plugin | 8.2.2   |
