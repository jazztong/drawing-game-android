# Drawing Game for Kids 🎨

A colorful native Android drawing app designed for young children (ages 5+). Built with Kotlin for smooth performance and intuitive touch controls.

## Features

- ✨ **9 Vibrant Colors**: Red, orange, yellow, green, blue, purple, pink, brown, black
- 🖌️ **3 Brush Sizes**: Small, medium, large
- 💾 **Save to Gallery**: Store drawings in device gallery
- 📂 **Load Drawings**: Pick saved images from gallery to continue editing
- 🧹 **Clear Canvas**: Start fresh anytime
- 🎨 **Custom App Icon**: Colorful palette design
- 🌈 **Rainbow Gradient UI**: Eye-catching interface for kids
- ✅ **Touch Precision**: Fixed coordinate scaling for accurate drawing

## Technical Details

- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Language**: Kotlin
- **Architecture**: Single Activity with custom DrawingView
- **Permissions**: READ_MEDIA_IMAGES, READ_EXTERNAL_STORAGE (Android 12+)

## Building

```bash
./gradlew assembleDebug
```

APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Installation

Transfer the APK to your Android device and install. You may need to enable "Install from Unknown Sources" in device settings.

## Known Issues

- Debug APK may show "invalid package" error on some devices (Honor X5)
  - Solution: Build signed release APK or uninstall previous versions first

## Built For

Jazz's 5-year-old daughter 💕

## License

MIT License - Feel free to use and modify
