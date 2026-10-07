# FTV TV — Setup Guide

Complete build instructions for both toolchains. Follow the one you use, in order.

---

## What you are building

An Android TV + mobile app that:

1. Shows a **D-Pad dashboard** with one card per server (5 preloaded).
2. Opens the server's HTTP file-directory listing in a **full-screen WebView**.
3. Lets the **remote's arrow keys** move a red-outlined highlight
   (`outline: 3px solid #FF0000`) across every `<a>` link, with **OK** to open.
4. **Intercepts** links ending in `.mp4 .mkv .avi .webm .m3u8` (and more) and hands them
   to **VLC / MX Player** through `Intent.ACTION_VIEW` with `video/*` — never downloads them.
5. Uses **BACK** to go up one folder, and returns to the dashboard at the server root.

**Prerequisites:** a GitHub account (for the cloud build), *or* Android Studio, *or* AIDE.
You also need VLC for Android on the TV, and a USB cable or network ADB for sideloading.

> **Just want the APK?** Use **Part 0** below. It needs no developer tools on your machine
> at all. The full Bengali walkthrough is in **[`BUILD-APK.md`](BUILD-APK.md)**.

---

## Part 0 — Cloud build (easiest — nothing to install)

GitHub's servers compile the APK. You only upload and download.

1. **Download and extract** `ftv-tv-android-source.zip` from the website's Download section.
2. **Create a GitHub repository.** [github.com](https://github.com) → `+` → *New repository* →
   name `ftv-tv` → **Public** → do **not** add a README → Create.
3. **Upload the files.** Click *uploading an existing file*, then drag the **contents** of the
   extracted folder — not the folder itself. You should see `build.gradle`, `settings.gradle`,
   `src/` and `.github/` at the top level.
   - **Windows hides `.github`.** Enable *View → Show → Hidden items*. If it will not upload,
     create it by hand: *Add file → Create new file* → name it
     `.github/workflows/build-apk.yml` → paste the file's contents.
4. **Run it.** Repo → **Actions** → *Build FTV TV APK* → *Run workflow*. (It also runs
   automatically on every push.)
5. **Wait 3–6 minutes** for the yellow dot to become a green tick.
6. **Download the APK.** Open the finished run → **Artifacts** → `FTV-TV-debug-apk`. That ZIP
   contains **`app-debug.apk`** — your APK. Artifacts expire after 90 days.

Then continue to **Part C** to install it on the TV.

### How the workflow finds your sources

The workflow promotes the `android/` folder to the repository root automatically when it
finds `android/build.gradle` and no root `build.gradle`. So both of these layouts work:

```
repo/build.gradle  repo/settings.gradle  repo/src/main/...     ← android/ contents uploaded
repo/android/build.gradle  repo/android/src/main/...           ← android/ folder uploaded
```

The one thing that does **not** work is an extra wrapper level (`repo/ftv-tv-android/…`),
because then neither `build.gradle` is where Gradle looks for it.

---

## Part A — Android Studio

### A1. Create the project

**Shortcut:** the supplied `android/` folder is already a complete, buildable single-module
Gradle project. **File → Open** → select the `android` folder (the one containing
`src`, `build.gradle` and `settings.gradle`) and skip straight to **A7**. There is no `app/`
sub-module, so there is nothing to paste.

To build it from the command line instead:

```bash
gradle -p android assembleDebug      # or: cd android && gradle assembleDebug
```

Otherwise, to assemble the project by hand:

1. **File → New → New Project → Empty Views Activity**.
2. Set:
   - **Name:** `FTV TV`
   - **Package name:** `com.example.ftvtv`
   - **Language:** Java
   - **Minimum SDK:** API 21 (Android 5.0)
   - **Build configuration language:** Groovy DSL (so the supplied `build.gradle` drops in unchanged)
3. Finish, then wait for the first Gradle sync to complete.

### A2. Add the Java sources

Copy all five files from the pack into `app/src/main/java/com/example/ftvtv/`:

| File | Purpose |
|---|---|
| `MainActivity.java` | Dashboard + D-Pad focus engine + video helpers |
| `WebViewActivity.java` | WebView browser + JS injection + video interception |
| `PlayerActivity.java` | Optional in-app player |
| `Server.java` | Server model |
| `ServerRepository.java` | **Edit this to change the server list** |

> The generated `MainActivity.java` must be **deleted or overwritten** — two classes in
> the same package will not compile.

### A3. Add the resources

Copy into `app/src/main/res/`, overwriting what Android Studio generated:

```
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
```

Then delete the generated `res/values/themes.xml` if it exists — the pack uses
`res/values/styles.xml` instead, and a leftover `themes.xml` referencing
`Theme.Material3.*` will conflict with the platform theme.

> **Why a platform theme?** `styles.xml` inherits from
> `@android:style/Theme.Material.NoActionBar` instead of an AppCompat/Material theme.
> That is what lets the exact same source compile in AIDE. If you want Material 3,
> change the parent — but then the project needs the Material dependency and no longer
> builds in AIDE.

### A4. Create the assets folder — the step people miss

```
app/src/main/
├── assets/
│   └── tv_dpad.js      ← MUST be exactly here
├── java/
├── res/
└── AndroidManifest.xml
```

If this folder is wrong or empty the app still runs, but it toasts
**“Missing asset: tv_dpad.js”** and no link ever gets the red outline.
In Android Studio: right-click `app/src/main` → **New → Folder → Assets Folder**.

### A5. Replace the manifest

Copy the pack's `android/src/main/AndroidManifest.xml` over
`app/src/main/AndroidManifest.xml`.

> **Do not add a `package="…"` attribute.** Android Gradle Plugin 8 rejects it with
> *“Namespace not specified”* / *“package attribute is not supported”*. The application id
> comes from `namespace` and `applicationId` in `build.gradle` instead. Only the AIDE copy
> of the manifest (`android/AIDE/AndroidManifest.xml`) keeps `package="…"`, because AIDE
> does not read Gradle at all.

### A6. Replace the build script

Copy the pack's `android/build.gradle` over `app/build.gradle`, or simply verify these
three values match:

```gradle
android {
    namespace 'com.example.ftvtv'   // AGP 8: required here, forbidden in the manifest
    compileSdk 34
    defaultConfig {
        applicationId "com.example.ftvtv"
        minSdk 21
        targetSdk 34
    }
}
```

There are **no dependencies** to add — the app uses only the platform SDK. Also copy
`settings.gradle`, `gradle.properties` and `proguard-rules.pro` if you want the same
configuration the cloud build uses.

### A7. Build

- **Run** ▶ button, or
- `./gradlew assembleDebug`

APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

**Expected lint warnings** (not errors):

- `android:screenOrientation="landscape"` — “ignored on large screens”. TVs are landscape; keep it.
- `setJavaScriptEnabled` — normal for a WebView app.
- `usesCleartextTraffic` — required, because your servers are plain HTTP.

The supplied `build.gradle` sets `lint { abortOnError false }` so these warnings never fail
the build (which matters for the cloud build).

---

## Part B — AIDE (on-device)

AIDE keeps a **flat** project layout, so the paths differ. The pack ships a second,
self-contained manifest for exactly this.

### B1. Create the project

AIDE → **Create new project** → **Android App → Empty App**:

- Application name: `FTV TV`
- Package name: `com.example.ftvtv`
- Minimum SDK: `21`
- Target SDK: `34`

### B2. Delete AIDE's generated files

- `src/com/example/ftvtv/MainActivity.java`
- `res/layout/main.xml` (or `activity_main.xml`)
- its generated `res/values/strings.xml` and `styles.xml`

### B3. Copy the files

```
AndroidManifest.xml                    ← from android/AIDE/AndroidManifest.xml
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
assets/tv_dpad.js                      ← create the "assets" folder yourself
```

> **Never have both manifests.** AIDE uses `AndroidManifest.xml` at the project root;
> Android Studio uses `app/src/main/AndroidManifest.xml`. Two manifests = build failure.

> **If AIDE created an `app/src/main/…` tree** instead of the flat `src/…` tree, use the
> Android Studio paths but keep the manifest at the project root. Both manifests are
> supplied so either layout works.

### B4. Build and run

Tap the green triangle. The first build downloads SDK components and takes a few minutes.
The APK is written to `<project>/bin/`.

> **Leave the Gradle files out of the AIDE project.** AIDE does not read `build.gradle`,
> `settings.gradle`, `gradle.properties` or `proguard-rules.pro`; they exist for Android
> Studio and the cloud build. Adding them to an AIDE project causes confusing errors.

---

## Part C — Install on the Android TV

### C1. Get the APK onto the device

**Option 1 — ADB over the network** (best for repeat builds):

```bash
adb connect 192.168.1.50:5555     # your TV's IP
adb install -r app-debug.apk
```

Enable *Developer options → USB debugging* on the TV first. On newer boxes you may also
need *Network debugging*.

**Option 2 — USB stick.** Copy the APK to a FAT32 stick, plug it into the TV and open it
with a file manager (X-plore, Solid Explorer, FX).

**Option 3 — Downloader app.** Install *Downloader* by AFTVnews on the TV and enter a
direct URL to the APK.

### C2. Launch it

The `LEANBACK_LAUNCHER` intent filter puts the app in the TV's **Apps** row. If your
launcher does not list sideloaded apps there, open
*Settings → Apps → See all apps → FTV TV*.

### C3. Install a video player

**VLC for Android** is strongly recommended: it handles MKV containers, AC3, DTS and EAC3
audio, and HEVC — none of which the platform `VideoView` supports.

Then, optionally, pin it so playback is a single OK press:
open a server, press **MENU → Choose default video player → VLC for Android**.

---

## Part D — Verification checklist

Run through this on the actual TV. It catches every mistake this app can make.

| # | Check | Expected |
|---|---|---|
| 1 | App appears in the TV Apps row | Yes, with the banner tile |
| 2 | Dashboard shows 5 cards | Circle FTP, SamOnline FTP, Local Server, CrazyCTG, KhulnaPlex |
| 3 | Arrow keys on the dashboard | Focus moves; card scales up with a red border |
| 4 | OK on a card | Opens that server's directory listing full-screen |
| 5 | Arrow keys in the listing | A red 3 px outline moves link to link |
| 6 | The HUD strip at the bottom | Shows the name of the highlighted link |
| 7 | OK on a folder | Descends into it |
| 8 | BACK inside a folder | Returns to the previous folder |
| 9 | BACK at the server root | Toast; a second press returns to the dashboard |
| 10 | OK on a `.mkv` / `.mp4` | VLC opens and starts playing — **no download** |
| 11 | MENU in the listing | Options dialog appears |
| 12 | Offline server (e.g. unplug the LAN) | Friendly error panel with Retry / Server list |

### Quick diagnostic if step 5 fails

1. Does the app toast **“Missing asset: tv_dpad.js”** on load? → fix `assets/`.
2. Long-press a link: does the link menu appear? If yes, the DOM is fine and the issue is
   key handling; if no, the page has no `<a href>` links (see TROUBLESHOOTING §4).
3. Does the bottom HUD strip stay empty while pressing arrows? → the page has no detectable
   anchors; broaden the selector in `tv_dpad.js` `collectLinks()`.

---

## Where to go next

| Document | Contents |
|---|---|
| `BUILD-APK.md` | **Bengali walkthrough** — GitHub cloud build → APK → install on the TV |
| `docs/ARCHITECTURE.md` | Activity flow, D-Pad key path, video interception decision tree |
| `docs/CUSTOMIZATION.md` | 12 recipes: servers, colours, grid, players, icons, package name |
| `docs/TROUBLESHOOTING.md` | 12 real failure modes with causes and fixes |
| `android/AIDE/README-AIDE.txt` | AIDE-specific file placement reference |
| `index.html` | Browsable version of everything above, with copy buttons and a ZIP builder |
