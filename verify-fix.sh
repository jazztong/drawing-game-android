#!/bin/bash

# Drawing App Fix Verification Script
# Tests that completed drawings persist after clicking "Draw Another"

echo "🔍 Drawing App - Fix Verification"
echo "=================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "📋 Checking code for correct behavior..."
echo ""

# Check 1: Verify clearCanvas() is NOT in showCompletion's button handler
echo -n "1. Checking showCompletion() doesn't clear canvas... "
if grep -A 15 "private fun showCompletion()" app/src/main/java/com/jazz/drawinggame/GuidedDrawingActivity.kt | grep -q "drawingView.clearCanvas()"; then
    echo -e "${RED}FAIL${NC}"
    echo "   ❌ clearCanvas() found in showCompletion() - drawing will be erased!"
    exit 1
else
    echo -e "${GREEN}PASS${NC}"
    echo "   ✅ clearCanvas() NOT in 'Draw Another' handler - drawing preserved!"
fi

# Check 2: Verify clearCanvas() IS in startGuidedDrawing
echo -n "2. Checking startGuidedDrawing() clears canvas... "
if grep -A 5 "private fun startGuidedDrawing" app/src/main/java/com/jazz/drawinggame/GuidedDrawingActivity.kt | grep -q "drawingView.clearCanvas()"; then
    echo -e "${GREEN}PASS${NC}"
    echo "   ✅ clearCanvas() called when starting new template!"
else
    echo -e "${RED}FAIL${NC}"
    echo "   ❌ clearCanvas() NOT in startGuidedDrawing() - old drawing won't clear!"
    exit 1
fi

# Check 3: Verify comment is present
echo -n "3. Checking for clarifying comment... "
if grep -q "DON'T clear canvas here" app/src/main/java/com/jazz/drawinggame/GuidedDrawingActivity.kt; then
    echo -e "${GREEN}PASS${NC}"
    echo "   ✅ Comment present - intention documented!"
else
    echo -e "${YELLOW}WARN${NC}"
    echo "   ⚠️  Comment missing - add for clarity"
fi

# Check 4: Verify hideGuide is called
echo -n "4. Checking guide is hidden on completion... "
if grep -A 5 "private fun showCompletion()" app/src/main/java/com/jazz/drawinggame/GuidedDrawingActivity.kt | grep -q "drawingView.hideGuide()"; then
    echo -e "${GREEN}PASS${NC}"
    echo "   ✅ Guide hidden on completion!"
else
    echo -e "${RED}FAIL${NC}"
    echo "   ❌ Guide not hidden - will overlay completed drawing!"
    exit 1
fi

echo ""
echo "=================================="
echo -e "${GREEN}✅ All checks passed!${NC}"
echo ""
echo "📊 Expected Behavior:"
echo "  1. Complete drawing → guide hidden, drawing stays"
echo "  2. Click 'Draw Another' → selection screen, drawing STILL visible"
echo "  3. Select new template → canvas clears, new drawing starts"
echo ""
echo "🧪 To test manually:"
echo "  1. Build: ./gradlew assembleDebug"
echo "  2. Install: adb install -r app/build/outputs/apk/debug/app-debug.apk"
echo "  3. Follow test scenarios in TEST_SCENARIOS.md"
echo ""
echo "📦 Current version: v1.1.5"
echo "🔗 GitHub: https://github.com/jazztong/drawing-game-android"
