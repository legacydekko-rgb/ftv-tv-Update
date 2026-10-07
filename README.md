# FTV TV — Android TV WebView Source Pack

A complete, compilable **Android TV + Android Mobile** app in **Java** that browses
HTTP file-directory (`index of /`) movie servers with a TV remote, and hands video
files to VLC / MX Player instead of downloading them.

This repository is the **deliverable**: the full Android source tree plus a browsable
website (`index.html`) that presents every file with syntax highlighting, one-click
copy, per-file download, and a ZIP builder for the whole pack.

> **Want the APK without installing anything?** The project is wired for **GitHub's
> cloud build**. Upload the ZIP contents to a GitHub repository, press *Run workflow*
> in the Actions tab, and GitHub compiles `app-debug.apk` for you — no Android Studio,
> no Java, no AIDE on your machine. Step-by-step Bengali walkthrough:
> **[`BUILD-APK.md`](BUILD-APK.md)**.
>
> **Note on scope.** This is a static site, so there is no Android SDK here and no APK
> is compiled or shipped *by this page*. The pack is complete, compilable Java/XML, and
> the included GitHub Actions workflow does the compiling on GitHub's servers.

---

## 1. Project name, goals and main features

**Name:** FTV TV (`com.example.ftvtv`)

**Goal:** Turn a TV box into a remote-driven browser for HTTP file-directory servers,
where a film is two or three OK presses away and playback happens in a real video player.

| Feature | How |
|---|---|
| TV-friendly server dashboard | 3-column card grid, built in Java from `ServerRepository` |
| Reliable D-Pad navigation | Per-card `OnKeyListener` indexing the grid (`index ± 1`, `index ± COLUMNS`) |
| Visible focus on a 10-foot screen | Scale to 1.08 + 3 dp `#FF0000` border + elevation |
| Directory browsing | Full-screen `WebView` per server |
| Remote-driven link navigation | Injected `tv_dpad.js`: `outline: 3px solid #FF0000`, geometric link graph |
| Video interception, no downloads | `shouldOverrideUrlLoading` → `Intent.ACTION_VIEW` `video/*` |
| Plays everything | Hand-off to VLC / MX Player, with a plain `http` VIEW fallback |
| Optional in-app player | `PlayerActivity` (`VideoView`), one flag to enable |
| Correct BACK behaviour | `canGoBack()` → up a folder; at the root → dashboard |
| Works on phones too | `touchscreen required="false"`, normal `LAUNCHER` filter as well |
| Zero dependencies | Only `android.*` — compiles unchanged in **AIDE** *and* Android Studio |

---

## 2. Repository layout

```
index.html                     Browsable source pack (this is the "website")
css/style.css                  Site styling
js/app.js                      File browser, tabs, accordion, ZIP builder
files-manifest.js              The list of files shown in the browser

BUILD-APK.md                   ★ Bengali: GitHub cloud build → APK → install
SETUP.md                       English build + install guide (all three paths)
docs/ARCHITECTURE.md           How the app fits together
docs/CUSTOMIZATION.md          12 recipes for common changes
docs/TROUBLESHOOTING.md        12 real failure modes and fixes

.github/workflows/
└── build-apk.yml              ★ GitHub Actions: compiles the APK for you

android/                       >>> THE ANDROID PROJECT (single Gradle module) <<<
├── build.gradle               Root + app module script (AGP 8.2.2)
├── settings.gradle            Gradle entry point (no ':app' — root IS the module)
├── gradle.properties          JVM memory, useAndroidX, nonTransitiveRClass
├── proguard-rules.pro         Release-build keep rules
├── AIDE/
│   ├── AndroidManifest.xml    Flat-layout manifest for AIDE (keeps package="…")
│   └── README-AIDE.txt        AIDE file-placement reference
└── src/main/
    ├── AndroidManifest.xml    Manifest for Gradle (NO package="…" — AGP 8 rule)
    ├── assets/tv_dpad.js      Injected D-Pad engine (MUST be in assets)
    ├── java/com/example/ftvtv/
    │   ├── MainActivity.java      Dashboard + focus engine + video helpers
    │   ├── WebViewActivity.java   Browser + interception + back handling
    │   ├── PlayerActivity.java    Optional in-app player
    │   ├── Server.java            Server model
    │   └── ServerRepository.java  *** EDIT THIS: the server list ***
    └── res/
        ├── layout/            activity_main, activity_webview, activity_player
        ├── values/            strings, colors, styles
        ├── drawable/          backgrounds, focus pills, icon, TV banner
        └── xml/               network_security_config
```

### Why the Gradle layout looks unusual

Gradle's Android plugin looks for sources at `<projectDir>/src/main/…`. Here `projectDir`
is the `android/` folder, so it finds `android/src/main/AndroidManifest.xml`,
`android/src/main/java/…`, `android/src/main/res/…` and `android/src/main/assets/…`
directly. That makes the **root project the Android module itself** — there is no `app/`
sub-module and therefore no `include ':app'` in `settings.gradle`. Build with:

```bash
gradle -p android assembleDebug      # or: cd android && gradle assembleDebug
```

The supplied workflow handles this automatically: if it finds `android/build.gradle` at
the repository root with no root `build.gradle`, it copies `android/.` up one level
first, so uploading either layout works.

---

## 3. Functional entry URIs

### 3.1 Android app (runtime entry points)

| Entry point | Trigger | Behaviour |
|---|---|---|
| `MainActivity` | `LEANBACK_LAUNCHER` / `LAUNCHER` | Server dashboard |
| `WebViewActivity` | Intent with `EXTRA_SERVER_URL` | Full-screen directory browser |
| `PlayerActivity` | Intent with `EXTRA_VIDEO_URL` | In-app playback (optional path) |
| External player | `Intent.ACTION_VIEW` + `video/*` | VLC / MX Player / chooser |

Intent extras:

| Extra | Type | Consumer |
|---|---|---|
| `extra_server_name` | String | `WebViewActivity` header |
| `extra_server_url` | String | `WebViewActivity` root URL |
| `extra_accent` | int (ARGB) | `WebViewActivity` header accent |
| `extra_video_url` | String | `PlayerActivity` |
| `extra_title` | String | `PlayerActivity` title bar |

### 3.2 JavaScript bridge (`tv_dpad.js` → `window.FTVTV`)

| Call | Purpose |
|---|---|
| `FTVTV.navigate('up'\|'down'\|'left'\|'right')` | Move the red highlight |
| `FTVTV.activate()` | Click the highlighted link |
| `FTVTV.focusFirst()` / `focusLast()` | Jump to the first / last link |
| `FTVTV.scrollOrReport(dir)` | Navigate, or scroll the page |
| `FTVTV.setTvMode(bool)` | Larger text / taller link hit areas |
| `FTVTV.count()` | Number of navigable links |
| `FTVTV.currentText()` / `currentHref()` | What OK will open (drives the HUD strip) |
| `window.__FTVTV_JAVA_DPAD__` | Ownership flag: Java consumes the arrow keys |

### 3.3 Static site (this deliverable)

| Path | Purpose |
|---|---|
| `index.html` | Landing page, requirement map, architecture, source browser, guides, ZIP |
| `index.html#file=<path>` | Deep link that opens a specific source file |
| `index.html#overview` … `#download` | Section anchors |
| `css/style.css` | All styling (dark 10-foot-UI theme, responsive) |
| `js/app.js` | Viewer, tabs, accordion, ZIP |
| `files-manifest.js` | File list consumed by `js/app.js` |

The source viewer and the ZIP builder fetch files **directly from the repo**, so the code
displayed is always the real file — there is no second copy to drift out of sync.

---

## 4. Data models, structures and storage

The Android app has no server, no database and no network backend of its own — it is a
client for your existing directory servers.

### `Server` (in-memory model)

| Field | Type | Notes |
|---|---|---|
| `name` | String | Card title |
| `tagline` | String | Card subtitle |
| `url` | String | Root URL — **keep the trailing slash** |
| `initials` | String | 2 letters in the badge |
| `accentColor` | int | `0xFFRRGGBB` |

### Preloaded servers

| Name | URL |
|---|---|
| Circle FTP | `http://circleftp.net/` |
| SamOnline FTP | `http://172.16.50.14/` |
| Local Server | `http://10.16.100.244/` |
| CrazyCTG | `http://crazyctg.com/` |
| KhulnaPlex | `http://khulnaplex.net/` |

### Local storage — `SharedPreferences` file `ftvtv_prefs`

| Key | Type | Purpose |
|---|---|---|
| `last_server_index` | int | Drives the “Resume” pill on the dashboard |
| `preferred_player` | String | Pinned player package, or absent = show the chooser |

### Video extension list (`MainActivity.VIDEO_EXTENSIONS`)

```
mp4 mkv avi webm m3u8 mpg mpeg mov flv wmv m4v ts m2ts 3gp ogv divx vob
```

Query strings and fragments are stripped before matching, so
`/movie.mkv?token=abc` is still recognised.

### `files-manifest.js` (static site)

Array of groups → items, each item `{ path, name, lang, desc }`, where `path` doubles as
the fetch URL and the ZIP entry path.

---

## 5. Currently completed features

- [x] Server dashboard with 5 preloaded servers, one card each
- [x] Custom D-Pad focus engine on the dashboard (no lost focus)
- [x] Scale-up + red glow focus effect on cards
- [x] “Resume last server” pill (long-press to clear)
- [x] Full-screen WebView per server with JS, DOM storage, wide viewport, zoom
- [x] Injected `tv_dpad.js`: `outline: 3px solid #FF0000` on every focused link
- [x] Geometric D-Pad navigation over `<a>` links, with page-scroll fallback
- [x] OK button clicks the highlighted link
- [x] Bottom HUD strip showing what OK will open
- [x] TV readability mode (larger text, taller link rows)
- [x] Video interception for 17 extensions; WebView never downloads
- [x] Hand-off via `Intent.ACTION_VIEW` `video/*`, with an `http` VIEW fallback
- [x] “Install VLC” dialog with a Play Store link and Copy-URL when no player exists
- [x] Optional in-app `PlayerActivity` (`VideoView`) behind one flag
- [x] Pinnable default player (MENU → Choose default video player)
- [x] BACK: up one folder, then dashboard (with a double-press guard)
- [x] HTTP Basic auth dialog for protected listings
- [x] Offline / DNS-failure error panel with Retry and Server list
- [x] Long-press link menu (open / play as video / copy URL)
- [x] Cleartext HTTP support + `network_security_config.xml` allow-list
- [x] Android 11+ `<queries>` block so VLC is discoverable
- [x] `LEANBACK_LAUNCHER` + banner; `touchscreen required="false"`
- [x] Vector icon and TV banner — text-only project, AIDE-safe
- [x] Zero third-party dependencies
- [x] Two manifests (Android Studio + AIDE) and two build guides
- [x] Browsable source site with copy, per-file download and full-pack ZIP

---

## 6. Features not yet implemented

- **No APK is shipped by this page.** There is no Android SDK in this static-site
  environment. Instead the pack includes a GitHub Actions workflow so GitHub's servers
  compile the APK — see `BUILD-APK.md`. You can of course also build locally in Android
  Studio or AIDE.
- **No release (signed) APK.** The workflow produces a **debug** APK, which is signed with
  the debug keystore and installs on any TV via sideloading. Publishing to Google Play
  would need a release keystore and `signingConfigs` — deliberately out of scope.
- **ExoPlayer / Media3 in-app playback.** `PlayerActivity` uses `VideoView`. The
  `media3` dependency lines are commented in `build.gradle`; enabling them breaks AIDE
  compatibility (androidx requirement).
- **Download / offline saving.** Deliberately absent — the brief requires streaming only.
- **Watch history, favourites, resume-position memory.** Only `last_server_index` is stored.
- **Search across servers, or a combined index.** Each server is browsed independently.
- **Auto-discovery of LAN servers** (mDNS/SSDP). Addresses are hard-coded.
- **Cross-origin iframe link navigation.** Impossible from injected JavaScript; such pages
  only get the scroll fallback.
- **Poster artwork / metadata scraping.** Listings are shown exactly as the server returns.
- **Voice search on the TV remote.** No `SearchManager` integration.
- **Credentials are not persisted** between app launches.

---

## 7. Recommended next steps

1. **Build the APK the easy way** — follow `BUILD-APK.md` (Bengali): unzip → upload to a
   GitHub repository → Actions → *Run workflow* → download `FTV-TV-debug-apk`. No local
   tooling required.
2. **Sideload it**, then walk the Part D checklist in `SETUP.md`. That single pass
   catches essentially every configuration mistake.
3. **Edit `ServerRepository.java`** to your real servers, and mirror the host list into
   `res/xml/network_security_config.xml`. Push to GitHub and a fresh APK is built for you.
4. **Install VLC** on the TV and pin it via MENU, so playback is one OK press.
5. **Rename the package** away from `com.example.*` before any public distribution
   (see `docs/CUSTOMIZATION.md` §9).
6. **Swap in real artwork** — a 320×180 PNG banner and a 512×512 icon
   (see `docs/CUSTOMIZATION.md` §8).
7. **If a server uses a JS tree or `<div onclick>` navigation**, broaden the selector in
   `tv_dpad.js` `collectLinks()` (`docs/TROUBLESHOOTING.md` §4).
8. **For perfect MKV/AC3 playback**, either keep the VLC hand-off (recommended) or port
   `PlayerActivity` to ExoPlayer and move to Android Studio.
9. **Add a favourites screen** by persisting `{title, url}` pairs in `SharedPreferences`
   and rendering them as extra dashboard cards.
10. **Consider `SearchManager` / Leanback `SearchFragment`** if you later want
    “search all servers for a title”.
11. **Add a release signing config** if you ever want a Play-Store-ready, signed AAB/APK
    instead of the debug build.

---

## 8. Public URLs

There is **no runtime API and no backend**. The app talks only to your directory servers:

| Purpose | URL |
|---|---|
| Circle FTP | `http://circleftp.net/` |
| SamOnline FTP | `http://172.16.50.14/` (LAN) |
| Local Server | `http://10.16.100.244/` (LAN) |
| CrazyCTG | `http://crazyctg.com/` |
| KhulnaPlex | `http://khulnaplex.net/` |

Publish this documentation site from the **Publish tab** to get a shareable URL for the
source pack. The two LAN addresses are only reachable from inside their own networks.

---

## 9. Legal note

The app is a generic HTTP directory browser with a remote-friendly UI. It ships no content
and no default streams. Use it only with servers you own or are authorised to access, and
only for content you have the right to view.
