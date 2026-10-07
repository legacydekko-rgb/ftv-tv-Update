============================================================================
 FTV TV  -  AIDE BUILD GUIDE  (text-only project, no Gradle needed)
============================================================================

WHY THIS WORKS IN AIDE
----------------------
Every source file in this project uses ONLY the Android platform SDK.
There is no androidx / AppCompat / Material / ExoPlayer dependency, so AIDE's
built-in build system can compile it without any extra library setup.

You must install the "AIDE" app with the Java/Android SDK support
(the free "AIDE - IDE for Android Java C++" from the Play Store is enough),
and have the Android SDK platform installed inside AIDE
(AIDE offers to download it on first run).


--------------------------------------------------------------------------
STEP 1 - CREATE THE PROJECT IN AIDE
--------------------------------------------------------------------------
1. Open AIDE -> "Create new project"
2. Choose "Android App" -> "Empty App" (NOT "Empty Android Studio project")
3. Application name   : FTV TV
4. Package name       : com.example.ftvtv
5. Minimum SDK        : 21   (Android 5.0)
6. Target SDK         : 34
7. Tap Create.

AIDE creates a skeleton with a MainActivity. We are going to replace it.


--------------------------------------------------------------------------
STEP 2 - DELETE AIDE'S DEFAULT FILES
--------------------------------------------------------------------------
Delete these generated files so they do not conflict:
    res/layout/main.xml        (or res/layout/activity_main.xml)
    res/values/strings.xml     (we ship our own - overwrite is fine too)
    res/values/styles.xml      (we ship our own)
    src/com/example/ftvtv/MainActivity.java   (we ship our own)

If AIDE created an "app/src/main/..." tree instead of the flat "src/..." tree,
see the note at the bottom of this file.


--------------------------------------------------------------------------
STEP 3 - COPY THE FILES IN
--------------------------------------------------------------------------
Place every file EXACTLY at the path shown. AIDE's file browser has a
"New file" / "New folder" option; long-press a file to paste content.

  AndroidManifest.xml                                  <- from AIDE/AndroidManifest.xml

  src/com/example/ftvtv/MainActivity.java
  src/com/example/ftvtv/WebViewActivity.java
  src/com/example/ftvtv/PlayerActivity.java
  src/com/example/ftvtv/Server.java
  src/com/example/ftvtv/ServerRepository.java

  res/layout/activity_main.xml
  res/layout/activity_webview.xml
  res/layout/activity_player.xml

  res/values/strings.xml
  res/values/colors.xml
  res/values/styles.xml

  res/drawable/dashboard_background.xml
  res/drawable/brand_badge.xml
  res/drawable/resume_pill.xml
  res/drawable/pill_button.xml
  res/drawable/ic_launcher.xml
  res/drawable/app_banner.xml

  res/xml/network_security_config.xml

  assets/tv_dpad.js                                    <- VERY IMPORTANT


IMPORTANT ABOUT assets/tv_dpad.js
---------------------------------
The JavaScript file MUST be inside the project's assets folder or the D-Pad
highlighting will not work (the app will show "Missing asset: tv_dpad.js").

In AIDE the assets folder is a plain folder named "assets" at the project root:
        <project>/assets/tv_dpad.js

AIDE does not always create it automatically - create the folder yourself with
"New folder". AIDE packs everything in it into the APK's assets/ directory.


--------------------------------------------------------------------------
STEP 4 - BUILD
--------------------------------------------------------------------------
Tap the "Run" button (the green triangle) in AIDE.

First build downloads the SDK components and takes a few minutes.
If AIDE reports an error, open the "Compile" log and match it against
docs/TROUBLESHOOTING.md.


--------------------------------------------------------------------------
STEP 5 - SIDELOAD ONTO THE TV
--------------------------------------------------------------------------
AIDE builds the APK to:
        <project>/bin/FTV TV.apk        (or app-debug.apk)

Options to install it on the TV:
  a) AIDE's own "Install" / "Run" button (works on the same device only)
  b) Copy the APK to a USB stick -> use a file manager on the TV
  c) ADB over the network from a PC:
         adb connect 192.168.1.50:5555
         adb install -r "FTV TV.apk"
  d) Install "Downloader" (by AFTVnews) on the TV and pull the APK from a URL

After installing, the app appears in the Android TV home row under
"Apps" because of the LEANBACK_LAUNCHER intent filter.


--------------------------------------------------------------------------
NOTE - IF AIDE CREATED A GRADLE-STYLE TREE
--------------------------------------------------------------------------
Some AIDE versions create:
        app/src/main/java/...      instead of  src/...
        app/src/main/res/...       instead of  res/...
        app/src/main/AndroidManifest.xml
        app/src/main/assets/...

If yours did, drop the "app/src/main/" prefix and use exactly the same
relative paths as this guide, with the manifest at the project ROOT.

Both layouts are supplied so you can use whichever tree AIDE gives you:
  - Android Studio tree : app/src/main/AndroidManifest.xml
  - AIDE tree           : AndroidManifest.xml  (AIDE/AndroidManifest.xml here)

NEVER have both manifests in one project - the build will fail with
"duplicate AndroidManifest.xml".


--------------------------------------------------------------------------
QUICK SANITY CHECKLIST
--------------------------------------------------------------------------
[ ] assets/tv_dpad.js exists and is not empty
[ ] AndroidManifest.xml declares android:banner="@drawable/app_banner"
[ ] android.software.leanback is required="false"
[ ] android.hardware.touchscreen is required="false"
[ ] android:usesCleartextTraffic="true"  (your servers are plain HTTP)
[ ] <queries> block present (Android 11+ needs it to find VLC/MX Player)
[ ] INTERNET permission granted
[ ] The five server URLs are in ServerRepository.java
