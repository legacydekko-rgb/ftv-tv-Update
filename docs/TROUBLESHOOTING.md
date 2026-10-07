# Troubleshooting — FTV TV

Every symptom below is one that actually happens with HTTP file-directory
(`index of /`) servers on Android TV. Fixes are ordered from most to least likely.

---

## 1. The page is blank / "net::ERR_CLEARTEXT_NOT_PERMITTED"

**Cause:** Android 9 (API 28) and newer block plain HTTP by default.

**Fix:** both of these must be present.

`AndroidManifest.xml`:
```xml
<application
    android:usesCleartextTraffic="true"
    android:networkSecurityConfig="@xml/network_security_config"
    ... >
```

`res/xml/network_security_config.xml` — add your host to the allow-list:
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">circleftp.net</domain>
    <!-- add every new server host here too -->
</domain-config>
```

---

## 2. "No video player found" even though VLC is installed

**Cause:** Android 11+ **package visibility**. Without a `<queries>` block,
`queryIntentActivities()` returns an empty list and the app cannot see VLC.

**Fix:** the `<queries>` block in `AndroidManifest.xml` must contain the
`http`/`https` VIEW intents **and** the `video/*` VIEW intent:

```xml
<queries>
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="http" />
    </intent>
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <data android:mimeType="video/*" />
    </intent>
    <package android:name="org.videolan.vlc" />
</queries>
```

The code additionally falls back to a plain `Intent.ACTION_VIEW` with the raw
URL, because both VLC and MX Player register as http/https handlers — so even a
strictly-typed `video/*` intent failing will still hand off correctly.

---

## 3. The remote's arrow keys do nothing on the directory page

Checklist:

1. **`assets/tv_dpad.js` is missing.** The app toasts `Missing asset: tv_dpad.js`.
   In AIDE the folder must be `<project>/assets/tv_dpad.js`. In Android Studio
   it must be `app/src/main/assets/tv_dpad.js`.
2. **JavaScript is disabled.** Verify in `configureWebView()`:
   `s.setJavaScriptEnabled(true);`
3. **The page has no `<a href>` links.** Some index generators use
   `<div onclick="...">` or `<form>` navigation instead. See §4.
4. **The page renders links inside an iframe.** The injection runs in the top
   document only. See §4 for the iframe workaround.

Quick diagnostic: long-press a link to open the link menu, or watch the bottom
HUD strip — if it stays empty, the page has no detectable anchors.

---

## 4. The page uses iframes, `<div>` clicks or a JavaScript tree

Add a snippet to the end of `assets/tv_dpad.js` to make other clickable
elements navigable too. Change the selector in `collectLinks()`:

```javascript
// original
var nodes = document.querySelectorAll('a[href]');

// broaden it — anything that looks clickable
var nodes = document.querySelectorAll(
    'a[href], [onclick], [role="link"], tr[data-href], .file, .item'
);
```

For same-origin iframes you can inject into the child frame as well:

```javascript
function injectIntoFrames() {
    var frames = document.querySelectorAll('iframe');
    for (var i = 0; i < frames.length; i++) {
        try {
            var doc = frames[i].contentDocument;
            if (!doc || doc.__FTVTV_DPAD_READY__) { continue; }
            // re-run the whole injection inside the frame
            var s = doc.createElement('script');
            s.textContent = '(' + arguments.callee.caller.toString() + ')()';
            doc.head.appendChild(s);
        } catch (e) { /* cross-origin: not accessible */ }
    }
}
```

Cross-origin iframes cannot be reached from JavaScript at all. If a server does
that, the fallback is the built-in scrolling in `jsNav()` — the page still
scrolls, and the user can use the touch/mouse pointer on a TV box.

---

## 5. One arrow press moves the highlight two steps

**Cause:** both Java *and* the injected page script are handling the key.

**Fix:** Java sets a flag immediately after injecting:

```java
webView.evaluateJavascript("window.__FTVTV_JAVA_DPAD__ = true;", null);
```

and `tv_dpad.js` bails out of its own `keydown` listener when it sees it:

```javascript
document.addEventListener('keydown', function (e) {
    if (window.__FTVTV_JAVA_DPAD__) { return; }   // Java owns the D-Pad
    ...
}, true);
```

If you delete one side, delete the other — otherwise you get double-stepping or
no stepping at all.

---

## 6. Video opens the built-in downloader / "Save file?" dialog

**Cause:** the extension is not in the recognised list, or the link is not a
direct file URL.

**Fix:** add the extension to `MainActivity.VIDEO_EXTENSIONS`:

```java
public static final String[] VIDEO_EXTENSIONS = {
    "mp4", "mkv", "avi", "webm", "m3u8", "mpg", "mpeg",
    "mov", "flv", "wmv", "m4v", "ts", "m2ts", "3gp", "ogv", "divx", "vob",
    "your_extension_here"
};
```

Also check `handleUrl()` in `WebViewActivity` — the `downloadMarkers` array
blocks URLs containing `?download`, `&download`, `force_download`. If your server
uses a different marker, add it there (and remove it if it is a false positive).

---

## 7. MKV plays but has no audio / "cannot play this file"

**Cause:** `VideoView` (the platform player) does not support AC3, DTS, EAC3 or
many HEVC profiles. This is normal and is exactly why the default flow hands the
URL to an external player.

**Fixes, in order of preference:**

1. **Use VLC** (recommended). Set it as the remembered default:
   `MENU` → *Choose default video player* → *VLC for Android*.
2. **Switch to ExoPlayer** for in-app playback:
   - uncomment the three `media3` lines in `app/build.gradle`
   - in `PlayerActivity`, replace the `VideoView` with
     `androidx.media3.ui.PlayerView` and build an `ExoPlayer`:
     ```java
     ExoPlayer player = new ExoPlayer.Builder(this).build();
     playerView.setPlayer(player);
     player.setMediaItem(MediaItem.fromUri(videoUrl));
     player.prepare();
     player.play();
     ```
   Note: ExoPlayer also needs `implementation 'androidx.media3:media3-exoplayer-hls'`
   for `.m3u8` streams. ExoPlayer requires androidx, so the project then no
   longer compiles in AIDE — use Android Studio.
3. Add a software decoder build of MX Player on the TV.

---

## 8. "Host not found" / "Cannot connect" for the LAN servers

`172.16.50.14` and `10.16.100.244` are private addresses.

- The TV/box must be on the **same** Wi-Fi or Ethernet network.
- Guest Wi-Fi networks usually block client-to-client traffic — use the main SSID.
- Some routers enable "AP isolation"; turn it off.
- Verify from a phone on the same network by opening the URL in a browser.
- If the server uses a non-standard port, include it: `http://10.16.100.244:8080/`

---

## 9. Login popup appears (401 Unauthorized)

The app handles it: `onReceivedHttpAuthRequest` shows a username/password dialog
and calls `handler.proceed(user, pass)`. Credentials are not stored between
launches — if the server re-challenges, the dialog reappears.

To hard-code credentials for a fixed server, add to `startLoading()`:

```java
headers.put("Authorization", "Basic " +
        android.util.Base64.encodeToString(
            "user:password".getBytes(), android.util.Base64.NO_WRAP));
```

(Only do this for a server you own — the value is readable in the APK.)

---

## 10. The app does not appear on the Android TV home screen

Check, in `AndroidManifest.xml`:

```xml
<uses-feature android:name="android.software.leanback" android:required="false" />
<uses-feature android:name="android.hardware.touchscreen" android:required="false" />
...
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
</intent-filter>
```

- `touchscreen required="false"` is the one people forget — without it the Play
  Store hides the app from TV devices entirely.
- `android:banner` is required for the TV row tile. A missing banner means the
  app may be rejected for the TV launcher.
- Sideloaded apps appear under **Settings → Apps → See all apps**, and only get
  a home-row tile on some launchers. Use *Apps* → *FTV TV* to launch it.

---

## 11. Video pauses as soon as it starts

Some TV boxes treat the hand-off as a background app. Two things help:

- In VLC: *Settings → Video → "Allow background playback"* / disable
  "Pause on app switch".
- The app already sets `FLAG_KEEP_SCREEN_ON` in both `WebViewActivity` and
  `PlayerActivity`.

---

## 12. AIDE says "cannot find symbol: R" or duplicate manifest

- **Duplicate manifest:** you have both `AndroidManifest.xml` at the project root
  *and* `app/src/main/AndroidManifest.xml`. Delete one.
- **`R` not found:** a resource XML has a syntax error, so `aapt` failed. Look at
  the top of the AIDE compile log for the real error (usually a stray character
  or a mismatched tag in `res/values/*.xml`).
- **`cannot find symbol: class Build`** or similar: the SDK platform is not
  installed in AIDE — let AIDE download it (it asks on first build).
