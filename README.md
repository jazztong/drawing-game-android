# Drawing Game for Kids 🎨

A colorful native Android drawing app designed for young children (ages 5+). Built with Kotlin for smooth performance and intuitive touch controls.

## Features

### Free Drawing Mode
- ✨ **9 Vibrant Colors**: Red, orange, yellow, green, blue, purple, pink, brown, black
- 🖌️ **3 Brush Sizes**: Small, medium, large
- 💾 **Save to Gallery**: Store drawings in device gallery
- 📂 **Load Drawings**: Pick saved images from gallery to continue editing
- 🧹 **Clear Canvas**: Start fresh anytime
- 🎨 **Custom App Icon**: Colorful palette design
- 🌈 **Rainbow Gradient UI**: Eye-catching interface for kids
- ✅ **Touch Precision**: Fixed coordinate scaling for accurate drawing

### AI-Powered Guided Drawing Mode (NEW!)
- 🤖 **Smart Learning**: AI watches and helps kids learn to draw
- 📝 **Step-by-Step Instructions**: Clear guidance for each step
- 👁️ **Visual Guides**: Semi-transparent trace overlays
- ⭐ **Automatic Progress Detection**: Gemini AI recognizes when step is complete
- 💬 **Encouraging Feedback**: Positive messages to boost confidence
- 🎯 **5 Templates**: Cat, House, Flower, Sun, Fish
- ✋ **Manual Control**: "Next Step" button always available

## Technical Details

- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Language**: Kotlin
- **Architecture**: Multi-Activity with custom Views
- **AI Integration**: Google Gemini 2.0 Flash Exp Vision API
- **Async Processing**: Kotlin Coroutines
- **Permissions**: READ_MEDIA_IMAGES, READ_EXTERNAL_STORAGE, INTERNET

## How Guided Drawing Works

1. **Template Selection**: Choose from 5 kid-friendly templates
2. **Visual Guide**: Semi-transparent dashed overlay shows what to draw
3. **Draw Freely**: Kid draws over the guide with any color/size
4. **AI Analysis**: After 3 seconds of pause, Gemini Vision API analyzes the drawing
5. **Smart Progression**: AI decides if step is complete (very lenient for kids!)
6. **Encouragement**: Shows positive messages and auto-advances
7. **Manual Override**: "Next Step" button always available if needed

The AI is intentionally forgiving - any reasonable attempt counts as success to keep kids motivated!

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
