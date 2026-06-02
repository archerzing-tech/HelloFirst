#!/bin/bash
set -e

JAVA_HOME=$HOME/tools/jdk17
ANDROID_HOME=$HOME/tools/android-sdk
BUILD_TOOLS=$ANDROID_HOME/build-tools/34.0.0
PLATFORM=$ANDROID_HOME/platforms/android-34
PROJECT=$HOME/work/hellofirst
OUTPUT=$PROJECT/build/outputs

AAPT2=$BUILD_TOOLS/aapt2
D8=$BUILD_TOOLS/d8
ZIPALIGN=$BUILD_TOOLS/zipalign
APKSIGNER=$BUILD_TOOLS/apksigner
JAVA=$JAVA_HOME/bin/java
JAVAC=$JAVA_HOME/bin/javac
export PATH=$JAVA_HOME/bin:$PATH

rm -rf $OUTPUT
mkdir -p $OUTPUT/dex $OUTPUT/obj $OUTPUT/apk $OUTPUT/res-compiled

echo "=== Compile all resources ==="
for res in $PROJECT/app/src/main/res/values/*.xml; do
    $AAPT2 compile -o $OUTPUT/res-compiled --dir $PROJECT/app/src/main/res
    break
done

echo "=== Link resources ==="
$AAPT2 link \
    -o $OUTPUT/obj/unsigned.apk \
    -I $PLATFORM/android.jar \
    --manifest $PROJECT/app/src/main/AndroidManifest.xml \
    -R $OUTPUT/res-compiled/*.flat \
    --java $OUTPUT/obj \
    --auto-add-overlay

echo "=== Compile Java sources ==="
$JAVAC -d $OUTPUT/obj/classes \
    -classpath $PLATFORM/android.jar:$OUTPUT/obj \
    -sourcepath $PROJECT/app/src/main/java \
    $PROJECT/app/src/main/java/com/example/hellofirst/MainActivity.java \
    $OUTPUT/obj/com/example/hellofirst/R.java

echo "=== Convert to DEX ==="
$JAVA -cp $BUILD_TOOLS/lib/d8.jar com.android.tools.r8.D8 \
    --lib $PLATFORM/android.jar \
    --min-api 29 \
    --output $OUTPUT/dex \
    $OUTPUT/obj/classes/com/example/hellofirst/*.class

echo "=== Package DEX into APK ==="
cd $OUTPUT/dex
zip -q $OUTPUT/obj/unsigned.apk classes.dex
cd $PROJECT

echo "=== Align ==="
$ZIPALIGN -f -p 4 $OUTPUT/obj/unsigned.apk $OUTPUT/apk/unsigned-aligned.apk

echo "=== Generate debug keystore ==="
KEYSTORE=$OUTPUT/apk/debug.keystore
if [ ! -f "$KEYSTORE" ]; then
    $JAVA_HOME/bin/keytool -genkey -v -keystore $KEYSTORE \
        -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass android -keypass android \
        -dname "CN=Android Debug,O=Android,C=US" 2>/dev/null
fi

echo "=== Sign APK ==="
$APKSIGNER sign \
    --ks $KEYSTORE \
    --ks-pass pass:android \
    --ks-key-alias androiddebugkey \
    --key-pass pass:android \
    --out $OUTPUT/apk/HelloFirst.apk \
    $OUTPUT/apk/unsigned-aligned.apk

echo ""
echo "=== DONE ==="
echo "APK: $OUTPUT/apk/HelloFirst.apk"
ls -lh $OUTPUT/apk/HelloFirst.apk
