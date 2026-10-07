# Architecture — FTV TV

## Overview

```
                    ┌──────────────────────────────┐
                    │        MainActivity          │  ← LEANBACK_LAUNCHER
                    │   Server selection dashboard │
                    │   custom D-Pad focus engine  │
                    └───────────────┬──────────────┘
                                    │ Intent + extras
                                    │ (name, url, accent)
                                    ▼
                    ┌──────────────────────────────┐
                    │      WebViewActivity         │
                    │  full-screen directory view  │
                    │                              │
                    │  WebView  ──injects──▶ tv_dpad.js
                    │     │                        │
                    │     │ shouldOverrideUrlLoading
                    │     ▼                        │
                    │  looksLikeVideoUrl() ?       │
                    │     yes ─▶ video hand-off    │
                    │     no  ─▶ navigate folder   │
                    └───────────────┬──────────────┘
                                    │ Intent.ACTION_VIEW "video/*"
                    ┌───────────────┴──────────────┐
                    ▼                              ▼
        ┌──────────────────────┐      ┌────────────────────────┐
        │  External player     │      │   PlayerActivity       │
        │  VLC / MX Player     │      │   (optional, in-app)   │
        │  (default path)      │      │   VideoView            │
        └──────────────────────┘      └────────────────────────┘
```

## Files

| File | Role |
|---|---|
| `MainActivity.java` | Dashboard UI, D-Pad focus engine, shared video-URL helpers |
| `WebViewActivity.java` | WebView setup, JS injection, video interception, back handling |
| `PlayerActivity.java` | Optional in-app `VideoView` player |
| `Server.java` | Immutable server model |
| `ServerRepository.java` | **The one file you edit to change the server list** |
| `assets/tv_dpad.js` | Injected: red focus outline, geometric link navigation |
| `res/layout/*.xml` | Dashboard, WebView and player layouts |
| `res/drawable/*.xml` | Backgrounds, focus-state pills, icon, TV banner |
| `res/xml/network_security_config.xml` | Cleartext HTTP allow-list |

## The D-Pad key path

```
Remote key
   │
   ▼
Activity.onKeyDown()                     ← WebViewActivity / MainActivity
   │  KEYCODE_DPAD_UP/DOWN/LEFT/RIGHT
   ▼
webView.evaluateJavascript(
    "window.FTVTV.navigate('up')")       ← async, no return value
   │
   ▼
tv_dpad.js  navigate(dir)
   │  geometric scoring over every visible <a href>
   ├── a link exists in that direction ─▶ focusIndex(i) → red outline + scroll
   └── no link in that direction      ─▶ pageScroll(dir)  (page scrolls)
```

`MainActivity` does not use the WebView path — it has its own synchronous
`OnKeyListener` per card that indexes directly into the `GridLayout`
(`index ± 1` for left/right, `index ± COLUMNS` for up/down). That is why card
navigation is pixel-perfect and never "jumps" the way default focus search can.

### Why the flag exists

Both Java and JavaScript can consume arrow keys. If both do, one press moves the
highlight twice. The contract:

```java
webView.evaluateJavascript("window.__FTVTV_JAVA_DPAD__ = true;", null);
```

```javascript
document.addEventListener('keydown', function (e) {
    if (window.__FTVTV_JAVA_DPAD__) { return; }   // Java owns it
    ...
}, true);
```

Java is the single owner. The JS listener exists only as a safety net if the
injection is used in a plain browser context (useful for testing `tv_dpad.js`
standalone in Chrome DevTools).

## Video interception

`shouldOverrideUrlLoading` is the only place a link decision is made:

```
handleUrl(url)
 ├─ looksLikeVideoUrl(url)         → playVideo()                 return true
 ├─ magnet: / ftp:                 → system Intent.ACTION_VIEW   return true
 ├─ intent: market: tel: mailto:   → Intent.parseUri + start     return true
 ├─ contains ?download / force_... → toast, blocked              return true
 └─ otherwise                      → return false  (folder link, WebView navigates)
```

`looksLikeVideoUrl()` strips `?query` and `#fragment` first, then checks the
extension list, then also catches `.../movie.mkv/play` style URLs.

`playVideo()` resolves in this order:

1. `PREFER_BUILTIN_PLAYER == true` → `PlayerActivity` (in-app)
2. A pinned player package (`preferred_player` in SharedPreferences) → direct launch
3. `MainActivity.openVideoInExternalPlayer()` → typed `video/*` chooser
4. If unresolvable → plain `http` VIEW chooser (VLC/MX both register for it)
5. Still nothing → "Install VLC" dialog with a Play Store link and a Copy-URL button

Step 4 is the important one: on Android 11+, `setDataAndType(uri, "video/*")` on
an **http** URI can resolve to nothing because players register the http scheme
rather than a MIME type. Falling back to a plain scheme intent is what makes the
hand-off work reliably on real TV boxes.

## Back button

```
BACK pressed
 ├─ webView.canGoBack()  → webView.goBack()          (up one folder)
 └─ at the server root
     ├─ first press      → toast "press Back again"
     └─ second press ≤2 s → super.onBackPressed()     (returns to dashboard)
```

The 2-second guard prevents a stray double-press from kicking the user out of
the app while browsing a deep folder tree.

## Resource strategy

- **Zero library dependencies.** Only `android.*` is imported — no AppCompat,
  no Material, no ExoPlayer. This is deliberate so the project compiles in AIDE.
- Theme parent is `@android:style/Theme.Material.NoActionBar` (platform theme).
- Focus feedback uses `state_focused` in XML selectors plus programmatic
  `GradientDrawable.setStroke()` on the dashboard cards.
- Icons are VectorDrawables, so the repo stays text-only and AIDE-safe.

## Persistence (SharedPreferences `ftvtv_prefs`)

| Key | Type | Purpose |
|---|---|---|
| `last_server_index` | int | Drives the "Resume" pill on the dashboard |
| `preferred_player` | String | Package name of the pinned player (or absent = chooser) |
