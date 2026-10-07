# Customization Guide — FTV TV

Everything you are likely to want to change, with the exact file and line.

---

## 1. Add / remove / rename a server

**File:** `app/src/main/java/com/example/ftvtv/ServerRepository.java`

```java
SERVERS.add(new Server(
        "Circle FTP",              // 1. name shown on the card
        "Movies & Series",         // 2. small subtitle
        "http://circleftp.net/",   // 3. root URL  (KEEP the trailing slash)
        "CF",                      // 4. two letters drawn in the badge
        0xFF2563EB));              // 5. accent colour, 0xFFRRGGBB
```

Nothing else needs editing — the dashboard grid, the D-Pad order, the card
colours and the WebView root URL all read from this list.

**Add a server:** copy one `SERVERS.add(...)` line and change the values.
**Remove:** delete the line.
**Reorder:** move the line up or down; D-Pad order follows list order.

> Always keep the trailing slash. Without it, relative folder links such as
> `href="English Movies/"` resolve one directory too high and you get a 404.

---

## 2. Change the dashboard grid

**File:** `MainActivity.java`

| What | Constant | Default |
|---|---|---|
| Cards per row | `COLUMNS` | `3` |
| Focus zoom amount | `FOCUS_SCALE` | `1.08f` |
| Focus animation speed | `FOCUS_ANIM_MS` | `140` |
| Card size | in `buildDashboard()` | `240 × 150 dp` |

For a phone-friendly 2-column grid, set `COLUMNS = 2`.
For a 4K TV you may prefer bigger cards: `dp(320) × dp(200)`.

---

## 3. Change the focus highlight colour

**File:** `assets/tv_dpad.js` → `injectBaseStyle()`

```javascript
'a:focus, a.ftvtv-focused, ... {' +
'  outline:3px solid #FF0000 !important;' +     // <- change this
'  outline-offset:2px !important;' +
'  background-color:rgba(255,0,0,0.20) !important;' +   // and this
'  box-shadow:0 0 14px rgba(255,0,0,0.85) !important;' + // and this
'}'
```

**File:** `MainActivity.java` → `animateCard()` — the dashboard card border:

```java
g.setStroke((int) dp(focused ? 3 : 2),
        focused ? 0xFFFF0000 : 0xFF2A3342);
//                ^^^^^^^^^^ focused border colour
```

**File:** `res/drawable/resume_pill.xml` and `res/drawable/pill_button.xml` —
the `#FF0000` inside the `state_focused` item.

---

## 4. Make text bigger / smaller on the TV

**File:** `WebViewActivity.java`

```java
private static final boolean ENABLE_TV_MODE = true;   // turn the readability pass on/off
```

**File:** `assets/tv_dpad.js` → `setTvMode()` — tune the numbers:

```javascript
'body{font-size:18px !important;line-height:1.85 !important;}' +
'a{display:inline-block;padding:2px 6px;margin:1px 0;}' +
'pre,code,td{font-size:17px !important;line-height:1.8 !important;}' +
```

---

## 5. Change the video-player behaviour

**File:** `WebViewActivity.java`

```java
/** true -> use the in-app PlayerActivity instead of VLC/MX Player. */
private static final boolean PREFER_BUILTIN_PLAYER = false;

/** true -> always show the "Open with" chooser. */
private static final boolean ALWAYS_SHOW_CHOOSER = false;
```

| You want | `PREFER_BUILTIN_PLAYER` | `ALWAYS_SHOW_CHOOSER` |
|---|---|---|
| One press → VLC directly (remembered default) | `false` | `false` |
| Every time ask which app | `false` | `true` |
| Play inside this app | `true` | ignored |

Users can also pin a player at runtime: **MENU → Choose default video player**
(saved as `preferred_player` in SharedPreferences).

---

## 6. Recognise more video extensions

**File:** `MainActivity.java`

```java
public static final String[] VIDEO_EXTENSIONS = {
        "mp4", "mkv", "avi", "webm", "m3u8", "mpg", "mpeg",
        "mov", "flv", "wmv", "m4v", "ts", "m2ts", "3gp", "ogv", "divx", "vob"
};
```

Add anything your server serves. The check ignores query strings, so
`/movie.mkv?token=abc` still matches.

---

## 7. App name, colours, strings

| What | File |
|---|---|
| App name (`app_name`) | `res/values/strings.xml` |
| Dashboard title / subtitle / hints | `res/values/strings.xml` |
| Colour palette | `res/values/colors.xml` |
| Base theme | `res/values/styles.xml` |
| App icon | `res/drawable/ic_launcher.xml` |
| Android TV banner | `res/drawable/app_banner.xml` |
| Dashboard background gradient | `res/drawable/dashboard_background.xml` |
| Brand badge ("FT") | `res/drawable/brand_badge.xml` + `res/layout/activity_main.xml` |

---

## 8. Use a real PNG icon and banner instead of the vectors

1. **Icon:** export a 512×512 PNG. Create
   `res/mipmap-xxxhdpi/ic_launcher.png` (and the other densities), delete
   `res/drawable/ic_launcher.xml`, then in `AndroidManifest.xml` change
   `android:icon="@drawable/ic_launcher"` → `android:icon="@mipmap/ic_launcher"`.
2. **Banner:** Google requires **320×180 dp** (use 320×180 px at xhdpi).
   Save as `res/drawable-xhdpi/app_banner.png`, delete
   `res/drawable/app_banner.xml`. The manifest reference does not change.

---

## 9. Change the package name

If you want something other than `com.example.ftvtv`:

1. Rename the folder `java/com/example/ftvtv/` to your package path.
2. In **every** `.java` file change `package com.example.ftvtv;`.
3. In `AndroidManifest.xml` change `package="..."` (and each
   `android:name=".MainActivity"` stays relative, so it keeps working).
4. In `app/build.gradle` change `namespace` and `applicationId`.

`com.example.*` cannot be uploaded to Google Play, so change it before publishing.

---

## 10. Add a "recently opened" history list

The app already stores `last_server_index`. To keep a real history table, write
to the existing SharedPreferences in `WebViewActivity.playVideo()`:

```java
getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
    .edit()
    .putString("last_video", videoUrl)
    .putString("last_video_title", lastPathSegment(videoUrl))
    .apply();
```

and read it back in `MainActivity.buildDashboard()` to render an extra card.

---

## 11. Optional: speed up repeat visits with a custom user agent

Some PHP index panels serve different markup per user agent. Change the suffix
in `WebViewActivity.configureWebView()`:

```java
s.setUserAgentString(ua + " FTVTV/1.0 (AndroidTV)");
```

Try a desktop Chrome UA if a server returns an unstyled listing:
```java
s.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0 Safari/537.36");
```

---

## 12. Add a search / jump-to-letter shortcut

`tv_dpad.js` already exposes `focusFirst()` and `focusLast()`. Add two keys in
`WebViewActivity.onKeyDown()`:

```java
case KeyEvent.KEYCODE_CHANNEL_UP:
    webView.evaluateJavascript("if(window.FTVTV){window.FTVTV.focusFirst();}", null);
    return true;
case KeyEvent.KEYCODE_CHANNEL_DOWN:
    webView.evaluateJavascript("if(window.FTVTV){window.FTVTV.focusLast();}", null);
    return true;
```
