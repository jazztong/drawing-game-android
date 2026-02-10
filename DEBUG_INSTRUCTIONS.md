# Drawing Game - Debug Version Instructions

## Apology

I apologize for claiming the fix was complete without proper verification. You're absolutely right - I should have tested it myself before saying it was done. Since I don't have access to an Android device/emulator in this environment, I've created a **debug version with comprehensive logging** so we can see exactly what's happening.

## What This Version Does

This version (v1.1.7-debug) adds detailed logging to track:
- When onSizeChanged() is called
- When bitmaps are created/saved/restored  
- When views change visibility
- When "Draw Another" is clicked
- When new templates start

## How to Get Logs

### Option 1: Via Android Studio
1. Install the APK on your device
2. Open Android Studio
3. Go to **View → Tool Windows → Logcat**
4. Filter by tag: `GuidedDrawing` (you'll see GuidedDrawingView and GuidedDrawingActivity)

### Option 2: Via Command Line (adb)
```bash
# Install APK
adb install -r app-debug.apk

# Start the app

# Watch logs in real-time
adb logcat -s GuidedDrawingView:D GuidedDrawingActivity:D
```

### Option 3: Save Logs to File
```bash
# Clear old logs
adb logcat -c

# Start app and use it

# Save logs to file
adb logcat -d -s GuidedDrawingView:D GuidedDrawingActivity:D > drawing-game-logs.txt
```

## Test Scenario

1. Start guided drawing mode
2. Select **Cat**
3. Draw a few strokes (don't need to complete)
4. Click **"Next Step"** until you reach completion
5. You'll see **"🎉 Amazing!"**
6. **IMPORTANT:** Look at the screen - is the drawing visible?
7. Click **"Draw Another"**
8. **CRITICAL:** Is the drawing still visible on the selection screen?
9. Select **House**
10. Does the old drawing clear now?

## What to Look For in Logs

### When completing drawing:
```
GuidedDrawingActivity: showCompletion: Drawing completed!
GuidedDrawingView: hideGuide: Guide hidden
```

### When clicking "Draw Another":
```
GuidedDrawingActivity: Draw Another clicked - NOT clearing canvas
GuidedDrawingActivity: Switching to template selection
GuidedDrawingView: onVisibilityChanged: visibility=GONE hasBitmap=true
```

### When showing selection screen:
```
GuidedDrawingView: onVisibilityChanged: visibility=VISIBLE hasBitmap=true
GuidedDrawingView: onSizeChanged: w=1080 h=1920 oldw=1080 oldh=1920
GuidedDrawingView: hadBitmap=true oldSize=1080x1920
GuidedDrawingView: Restoring old bitmap (sizes match)
```

### When starting new template:
```
GuidedDrawingActivity: startGuidedDrawing: Starting House
GuidedDrawingView: clearCanvas: Clearing canvas
GuidedDrawingView: onVisibilityChanged: visibility=VISIBLE hasBitmap=true
```

## Key Questions

1. **Is the bitmap being restored?**
   - Look for: `"Restoring old bitmap (sizes match)"`
   - If you see: `"NOT restoring - size mismatch!"` then that's the problem

2. **Is onSizeChanged even being called?**
   - Look for: `"onSizeChanged: w=... h=..."`
   - If not called, the fix won't trigger

3. **Is the view being detached/reattached?**
   - Look for: `"onDetachedFromWindow"` or `"onAttachedToWindow"`
   - If detached, bitmap might be lost

## Expected Log Flow

**Complete Drawing:**
```
showCompletion: Drawing completed!
hideGuide: Guide hidden
```

**Click "Draw Another":**
```
Draw Another clicked - NOT clearing canvas
Switching to template selection
onVisibilityChanged: visibility=GONE hasBitmap=true
onVisibilityChanged: visibility=VISIBLE hasBitmap=true  (maybe)
onSizeChanged: w=1080 h=1920 oldw=1080 oldh=1920
hadBitmap=true oldSize=1080x1920
Restoring old bitmap (sizes match)  ← KEY LINE!
```

**Start New Template:**
```
startGuidedDrawing: Starting House
clearCanvas: Clearing canvas
Switching to drawing area
onVisibilityChanged: visibility=VISIBLE hasBitmap=true
```

## What to Send Me

Please send me:
1. A description of what you see visually (does drawing stay or disappear?)
2. The relevant log output (especially around "Draw Another" click)
3. Any warnings or errors in the logs

This will help me understand exactly what's happening and fix it properly.

## My Commitment

I will not claim it's fixed again until:
1. You confirm the logs show correct behavior
2. You confirm the drawing actually stays visible
3. OR I can reproduce and fix the actual issue based on the log data

Thank you for your patience, and sorry for the multiple failed attempts.
