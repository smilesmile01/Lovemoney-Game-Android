# LoveMoney Game App

A native Android WebView application for the LoveMoney game with optimized performance and native features.

## Prerequisites

- JDK 11 or higher
- Android Studio Arctic Fox or newer
- Android SDK with minimum API 24 (Android 7.0)
- Gradle 8.0+

## Development Setup

1. Clone the repository
```bash
git clone <repository-url>
cd lovemoney-app
```

2. Open project in Android Studio
- File → Open → Select project directory
- Wait for Gradle sync to complete

3. Run in development mode
```bash
./gradlew assembleDebug
```

The debug APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Version Management

### Update Version

Edit `app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 2      // Increment for each release
    versionName = "1.1.0" // Semantic version (MAJOR.MINOR.PATCH)
}
```

### APK Naming Convention

Generated APKs follow this naming pattern:
- Release: `LoveMoney-v{version}-{versionCode}-{date}-release.apk`
- Debug: `LoveMoney-v{version}-DEBUG-{versionCode}-{date}-debug.apk`

Examples:
- `LoveMoney-v1.0.0-1-20250914-release.apk`
- `LoveMoney-v1.0.0-DEBUG-1-20250914-debug.apk`

## Building Release APK

### 1. Keystore Configuration

The project includes a pre-configured keystore for signing releases:
- Location: `lovemoney-release.keystore`
- Alias: `lovemoney`
- Password: `lovemoney2025`

**Important**: For production releases, generate your own keystore:
```bash
keytool -genkey -v -keystore your-release.keystore -alias your-alias -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Build Signed APK

```bash
# Clean previous builds
./gradlew clean

# Build release APK
./gradlew assembleRelease
```

The signed APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

### 3. Verify APK Signature

```bash
# Verify the APK is properly signed
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Check APK details
aapt dump badging app/build/outputs/apk/release/app-release.apk
```

## Distribution

### Direct APK Installation

1. Enable "Unknown Sources" on target device:
   - Settings → Security → Unknown Sources (Enable)

2. Transfer APK to device via:
   - USB cable
   - Email attachment
   - Cloud storage link
   - QR code with download URL

3. Install APK on device

### APK File Location
After building, find your signed APK at:
```
app/build/outputs/apk/release/LoveMoney-v{version}-{versionCode}-{date}-release.apk
```

Example:
```
app/build/outputs/apk/release/LoveMoney-v1.0.0-1-20250914-release.apk
```

## Building App Bundle (AAB) for Google Play

```bash
# Build AAB for Play Store submission
./gradlew bundleRelease
```

The AAB file will be at: `app/build/outputs/bundle/release/app-release.aab`

## Configuration

### ProGuard Configuration

ProGuard is enabled for release builds to optimize and obfuscate code.
Configuration: `app/proguard-rules.pro`

## Testing

### Run on Emulator
1. Create AVD in Android Studio
2. Run: `./gradlew installDebug`

### Run on Physical Device
1. Enable Developer Options and USB Debugging
2. Connect device via USB
3. Run: `./gradlew installDebug`

## Build Variants

- **Debug**: Development build with debugging enabled
  ```bash
  ./gradlew assembleDebug
  # Output: app/build/outputs/apk/debug/LoveMoney-v{version}-DEBUG-{versionCode}-{date}-debug.apk
  ```

- **Release**: Production build with ProGuard and signing
  ```bash
  ./gradlew assembleRelease
  # Output: app/build/outputs/apk/release/LoveMoney-v{version}-{versionCode}-{date}-release.apk
  ```

## Troubleshooting

### Build Failures
```bash
# Clean build cache
./gradlew clean
rm -rf .gradle
rm -rf app/build

# Rebuild
./gradlew assembleRelease
```

### Signing Issues
Ensure keystore file exists and credentials match:
```kotlin
// app/build.gradle.kts
signingConfigs {
    create("release") {
        storeFile = file("../lovemoney-release.keystore")
        storePassword = "lovemoney2025"
        keyAlias = "lovemoney"
        keyPassword = "lovemoney2025"
    }
}
```

## Project Structure

```
lovemoney-app/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/lovemoney/lovemoney/
│   │       │   ├── GameActivity.kt        # Main game WebView
│   │       │   ├── SplashActivity.kt      # Splash screen
│   │       │   └── GameJSInterface.kt     # JS bridge
│   │       ├── res/                       # Resources
│   │       └── AndroidManifest.xml        # App manifest
│   ├── build.gradle.kts                   # App build config
│   └── proguard-rules.pro                 # ProGuard rules
├── lovemoney-release.keystore             # Release signing key
├── build.gradle.kts                        # Project build config
└── settings.gradle.kts                     # Project settings
```

## Security Notes

- Never commit production keystores to version control
- Keep keystore passwords secure
- Use different keystores for development and production
- Enable certificate pinning for production APIs
- Regularly update dependencies for security patches

## License

Proprietary - All rights reserved