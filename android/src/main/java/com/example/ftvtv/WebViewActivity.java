package com.example.ftvtv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 * FILE: app/src/main/java/com/example/ftvtv/WebViewActivity.java
 * ============================================================================
 * Full-screen browser for an HTTP file-directory (index) server, driven entirely
 * by an Android TV remote.
 *
 * Feature map
 *  [1] WebView configuration ....... JavaScript, DOM storage, wide viewport, mixed content
 *  [2] D-Pad engine ................ arrow keys move the link highlight, OK opens it
 *  [3] Video interception .......... shouldOverrideUrlLoading -> Intent.ACTION_VIEW "video/*"
 *  [4] Back handling ............... previous folder first, dashboard when at root
 *  [5] HTTP Basic auth ............. some FTP-HTTP bridges prompt for user/password
 *  [6] Error / loading UI .......... offline LAN server, DNS failure, progress bar
 *
 * >>> Set PREFER_BUILTIN_PLAYER to true if you want the app's own PlayerActivity
 *     (no external app needed) instead of handing the URL to VLC / MX Player.
 * ============================================================================
 */
public class WebViewActivity extends Activity {

    /* ---------------------------------------------------------------------
     * Tunables
     * ------------------------------------------------------------------- */

    /** true  -> open videos in the in-app PlayerActivity (bundled ExoPlayer). */
    private static final boolean PREFER_BUILTIN_PLAYER = false;

    /** true  -> always ask which app to use; false -> open the default player directly. */
    private static final boolean ALWAYS_SHOW_CHOOSER = false;

    /** Enlarge text / link hit areas for 10-foot TV viewing. */
    private static final boolean ENABLE_TV_MODE = true;

    /** Vertical scroll step when no link exists in the pressed direction (px). */
    private static final int SCROLL_STEP = 260;

    /** How often we re-ask the page where the highlight is (ms). */
    private static final int HUD_REFRESH_MS = 250;

    public static final String EXTRA_SERVER_NAME = "extra_server_name";
    public static final String EXTRA_SERVER_URL = "extra_server_url";
    public static final String EXTRA_ACCENT = "extra_accent";

    private static final String ASSET_DPAD_JS = "tv_dpad.js";

    /* ---------------------------------------------------------------------
     * Views / state
     * ------------------------------------------------------------------- */

    private WebView webView;
    private ProgressBar progressBar;
    private TextView titleView;
    private TextView hudView;         // shows the highlighted item, TV-friendly
    private View errorPanel;
    private TextView errorMessage;

    private String serverName = "Server";
    private String rootUrl = "";
    private int accentColor = 0xFF2563EB;

    /** Guards the "double back to exit" behaviour inside the WebView. */
    private long lastBackPress = 0L;

    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable hudUpdater = new Runnable() {
        @Override
        public void run() {
            updateHud();
            handler.postDelayed(this, HUD_REFRESH_MS);
        }
    };

    /* =====================================================================
     * LIFECYCLE
     * =================================================================== */

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        // Read the server the user picked on the dashboard.
        Intent intent = getIntent();
        if (intent != null) {
            serverName = intent.getStringExtra(EXTRA_SERVER_NAME) != null
                    ? intent.getStringExtra(EXTRA_SERVER_NAME) : serverName;
            rootUrl = intent.getStringExtra(EXTRA_SERVER_URL) != null
                    ? intent.getStringExtra(EXTRA_SERVER_URL) : "";
            accentColor = intent.getIntExtra(EXTRA_ACCENT, accentColor);
        }

        webView = (WebView) findViewById(R.id.web_view);
        progressBar = (ProgressBar) findViewById(R.id.loading_progress);
        titleView = (TextView) findViewById(R.id.header_title);
        hudView = (TextView) findViewById(R.id.hud_text);
        errorPanel = findViewById(R.id.error_panel);
        errorMessage = (TextView) findViewById(R.id.error_message);

        titleView.setText(serverName);
        titleView.setTextColor(accentColor);

        // Tint the little accent bar in the header with the server's colour.
        View accentBar = findViewById(R.id.header_accent);
        if (accentBar != null) {
            android.graphics.drawable.GradientDrawable barBg =
                    new android.graphics.drawable.GradientDrawable();
            barBg.setColor(accentColor);
            barBg.setCornerRadius(getResources().getDisplayMetrics().density * 3f);
            accentBar.setBackground(barBg);
        }

        findViewById(R.id.error_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideError();
                webView.loadUrl(rootUrl);
            }
        });
        findViewById(R.id.error_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // back to the server dashboard
            }
        });

        configureWebView();
        startLoading(rootUrl);

        // Keep the screen awake while browsing a directory (no video is playing
        // yet, but users often leave the listing open while picking a film).
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(hudUpdater, HUD_REFRESH_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(hudUpdater);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (webView != null) {
            // Detach the WebView so its renderer process can be reclaimed.
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent != null) {
                parent.removeView(webView);
            }
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    /* =====================================================================
     * [1] WEBVIEW CONFIGURATION
     * =================================================================== */

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings s = webView.getSettings();

        // --- JavaScript & storage (required by the D-Pad injection) ---------
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // localStorage / sessionStorage
        s.setDatabaseEnabled(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);

        // --- Viewport / zoom ------------------------------------------------
        s.setUseWideViewPort(true);            // honour the page's <meta viewport>
        s.setLoadWithOverviewMode(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(true);
        s.setTextZoom(100);

        // --- Caching: directories change often, so prefer the network --------
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        // --- Media ----------------------------------------------------------
        s.setMediaPlaybackRequiresUserGesture(false); // autoplay friendly
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);

        // --- The LAN servers are plain HTTP, so mixed content must be allowed -
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // --- User agent: present as a desktop-ish TV browser -----------------
        // Some index generators serve a different (or no) listing to unknown
        // user agents. Keeping a desktop UA gives the widest compatibility.
        String ua = s.getUserAgentString();
        if (ua != null && !ua.contains("FTVTV")) {
            s.setUserAgentString(ua + " FTVTV/1.0 (AndroidTV)");
        }

        // --- Cookies (needed by some panels that set a session cookie) -------
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        // --- Clients --------------------------------------------------------
        webView.setWebViewClient(new TvWebViewClient());
        webView.setWebChromeClient(new TvChromeClient());

        webView.setBackgroundColor(Color.parseColor("#0B0F14"));
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);

        // Long-press (or a mouse right-click on a TV box) shows a small menu.
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView.HitTestResult hit = webView.getHitTestResult();
                if (hit != null && hit.getExtra() != null) {
                    showLinkMenu(hit.getExtra());
                    return true;
                }
                return false;
            }
        });
    }

    /** Loads a URL with a desktop-ish header set (helps a few PHP index scripts). */
    private void startLoading(String url) {
        hideError();
        if (url == null || url.length() == 0) {
            showError("No server URL configured.");
            return;
        }
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Accept-Language", "en-US,en;q=0.9");
        webView.loadUrl(url, headers);
    }

    /* =====================================================================
     * [3] VIDEO LINK INTERCEPTION
     * =================================================================== */

    private class TvWebViewClient extends WebViewClient {

        /**
         * Called for every link click / redirect.
         * Returning true means "I handled it, do NOT let the WebView navigate".
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request != null && request.getUrl() != null
                    ? request.getUrl().toString() : null;
            return handleUrl(url);
        }

        /** Legacy overload - still called on older WebView versions (AIDE devices). */
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUrl(url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            hudView.setText("");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            // The DOM exists now: inject the D-Pad engine, then put the highlight
            // on the first link so the remote works without any extra press.
            injectTvDpad();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request,
                                    WebResourceError error) {
            super.onReceivedError(view, request, error);
            // Only surface main-frame failures, not a single missing thumbnail.
            if (request != null && request.isForMainFrame()) {
                showError(describeError(error != null ? error.getErrorCode() : -1));
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            showError(description != null ? description : "Network error (" + errorCode + ")");
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler,
                                              String host, String realm) {
            // Some HTTP-FTP bridges protect the listing with Basic auth.
            showAuthDialog(handler, host, realm);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          WebResourceRequest request) {
            // Hook point if you later want to log or rewrite directory requests.
            return super.shouldInterceptRequest(view, request);
        }
    }

    /**
     * The single decision point for a clicked link.
     *
     * @return true  -> we handled it (video player launched, or scheme blocked)
     *         false -> let the WebView navigate normally (a folder link)
     */
    private boolean handleUrl(String url) {
        if (url == null) {
            return false;
        }
        String lower = url.toLowerCase();

        // ---------- 1. Playable video file ---------------------------------
        if (MainActivity.looksLikeVideoUrl(url)) {
            playVideo(url);
            return true;   // never let the WebView try to download it
        }

        // ---------- 2. Other non-http schemes ------------------------------
        if (lower.startsWith("magnet:") || lower.startsWith("ftp://")) {
            // Not playable in a WebView; hand to the system if possible.
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, "No app can open this link", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (lower.startsWith("intent:") || lower.startsWith("market:")
                || lower.startsWith("tel:") || lower.startsWith("mailto:")
                || lower.startsWith("whatsapp:")) {
            try {
                Intent i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, "Link not supported", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        // ---------- 3. Anything that is clearly a download, not a folder ----
        // Index pages often expose "download" helpers that would otherwise save
        // the file to /sdcard instead of streaming it.
        String[] downloadMarkers = {"?download", "&download", "/download?", "force_download"};
        for (String marker : downloadMarkers) {
            if (lower.contains(marker)) {
                Toast.makeText(this, "Downloads are disabled - streaming only",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        // ---------- 4. Normal folder / page navigation ----------------------
        return false;
    }

    /** Launches the video: in-app player, or an external one via ACTION_VIEW. */
    private void playVideo(final String videoUrl) {
        if (PREFER_BUILTIN_PLAYER) {
            Intent i = new Intent(this, PlayerActivity.class);
            i.putExtra(PlayerActivity.EXTRA_VIDEO_URL, videoUrl);
            i.putExtra(PlayerActivity.EXTRA_TITLE, lastPathSegment(videoUrl));
            startActivity(i);
            return;
        }

        if (ALWAYS_SHOW_CHOOSER) {
            MainActivity.openVideoInExternalPlayer(this, videoUrl, serverName);
            return;
        }

        // Try the remembered default player first for a one-press experience.
        String preferred = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
                .getString("preferred_player", null);
        if (preferred != null && launchWithPackage(preferred, videoUrl)) {
            return;
        }

        // Otherwise show the system chooser (VLC, MX Player, ...).
        boolean launched = MainActivity.openVideoInExternalPlayer(this, videoUrl, serverName);
        if (!launched) {
            showPlayerMissingDialog(videoUrl);
        }
    }

    /** Directly targets a specific player package (no chooser). */
    private boolean launchWithPackage(String packageName, String videoUrl) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setPackage(packageName);
            i.setDataAndType(Uri.parse(videoUrl), MainActivity.guessMimeType(videoUrl));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (MainActivity.isIntentResolvable(this, i)) {
                startActivity(i);
                return true;
            }
            // Some players only register the plain http intent.
            Intent http = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            http.setPackage(packageName);
            http.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (MainActivity.isIntentResolvable(this, http)) {
                startActivity(http);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Shown when no player at all is installed: offers the Play Store link. */
    private void showPlayerMissingDialog(final String videoUrl) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("No video player found")
                .setMessage("Install VLC for Android (free) to play:\n\n"
                        + lastPathSegment(videoUrl))
                .setPositiveButton("Get VLC", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=org.videolan.vlc")));
                        } catch (Exception e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=org.videolan.vlc")));
                        }
                    }
                })
                .setNeutralButton("Copy URL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        android.content.ClipboardManager cm =
                                (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("video url", videoUrl));
                        Toast.makeText(WebViewActivity.this, "URL copied", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static String lastPathSegment(String url) {
        try {
            String clean = url;
            int q = clean.indexOf('?');
            if (q > 0) clean = clean.substring(0, q);
            int slash = clean.lastIndexOf('/');
            String name = slash >= 0 ? clean.substring(slash + 1) : clean;
            return Uri.decode(name);
        } catch (Exception e) {
            return url;
        }
    }

    /* =====================================================================
     * [2] D-PAD ENGINE
     * =================================================================== */

    /**
     * Injects tv_dpad.js and tells it that Java owns the D-Pad keys.
     * Called after every page load, so the red-outline styling is always present.
     */
    private void injectTvDpad() {
        if (webView == null) {
            return;
        }
        String js = readAsset(ASSET_DPAD_JS);
        if (js == null) {
            return;
        }
        webView.evaluateJavascript(js, null);

        // Contract: Java consumes the arrow keys -> disable the in-page handler.
        webView.evaluateJavascript("window.__FTVTV_JAVA_DPAD__ = true;", null);

        // TV readability pass, then highlight the first link.
        webView.evaluateJavascript(
                "if(window.FTVTV){window.FTVTV.setTvMode(" + ENABLE_TV_MODE + ");" +
                        "window.FTVTV.focusFirst();}", null);
    }

    /** Reads a file from app/src/main/assets/. */
    private String readAsset(String name) {
        try {
            java.io.InputStream is = getAssets().open(name);
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
            is.close();
            return new String(bos.toByteArray(), "UTF-8");
        } catch (Exception e) {
            Toast.makeText(this, "Missing asset: " + name, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * Central key handler.
     *
     * Strategy: ask the page to move its highlight first. If the page has no
     * link in that direction it scrolls instead. Only if the page reports "I
     * cannot do anything" do we let the WebView perform its default scrolling,
     * which keeps the code robust on every kind of directory page.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {

            case KeyEvent.KEYCODE_DPAD_UP:
                return jsNav("up");

            case KeyEvent.KEYCODE_DPAD_DOWN:
                return jsNav("down");

            case KeyEvent.KEYCODE_DPAD_LEFT:
                return jsNav("left");

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return jsNav("right");

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                webView.evaluateJavascript(
                        "if(window.FTVTV){window.FTVTV.activate();}", null);
                return true;

            case KeyEvent.KEYCODE_PAGE_UP:
                webView.evaluateJavascript(
                        "if(window.FTVTV){window.FTVTV.scrollOrReport('up');}", null);
                return true;

            case KeyEvent.KEYCODE_PAGE_DOWN:
                webView.evaluateJavascript(
                        "if(window.FTVTV){window.FTVTV.scrollOrReport('down');}", null);
                return true;

            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                // Nothing playing inside the WebView - ignore politely.
                return true;

            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_INFO:
                showPageMenu();
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Sends a navigation command to the page.
     *
     * We call {@code scrollOrReport(dir)} rather than {@code navigate(dir)}: it
     * first tries to move the highlight, and when the page has no link in that
     * direction it scrolls the document instead. That way the arrow keys always
     * do something sensible, even on a listing with no focusable links at all.
     *
     * evaluateJavascript() is asynchronous, so Java cannot read the boolean
     * result. Returning true unconditionally is deliberate: it stops the WebView
     * from also scrolling, which would make the highlight and the viewport drift
     * apart.
     */
    private boolean jsNav(String direction) {
        if (webView == null) {
            return false;
        }
        final String js =
                "(function(){var d='" + direction + "';" +
                        "if(window.FTVTV){window.FTVTV.scrollOrReport(d);}else{" +
                        "  var s=" + SCROLL_STEP + ";" +
                        "  var x=(d==='left')?-s:(d==='right')?s:0;" +
                        "  var y=(d==='up')?-s:(d==='down')?s:0;" +
                        "  window.scrollBy(x,y);" +
                        "}})();";
        webView.evaluateJavascript(js, null);
        return true;
    }

    /**
     * Reads the currently highlighted link back from the page and shows it in
     * the HUD strip, so the user always knows what OK will open.
     */
    private void updateHud() {
        if (webView == null || hudView == null) {
            return;
        }
        webView.evaluateJavascript(
                "if(window.FTVTV){window.FTVTV.currentText();}else{''}",
                new android.webkit.ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (value == null || value.length() == 0
                                || "null".equals(value) || "\"\"".equals(value)) {
                            hudView.setText("");
                            return;
                        }
                        // evaluateJavascript returns a JSON-encoded string.
                        String text = jsonUnescape(value);
                        if (text.length() > 90) {
                            text = text.substring(0, 90) + "...";
                        }
                        hudView.setText(text);
                    }
                });
    }

    /**
     * Minimal unescape for the JSON string returned by evaluateJavascript().
     *
     * The backslash is built from its char code ((char) 92) instead of being
     * written literally, which keeps this method free of double-escaped Java
     * literals and therefore easy to read and to copy/paste.
     */
    private static String jsonUnescape(String value) {
        if (value == null) {
            return "";
        }
        final char bs = (char) 92;   // the backslash character
        String text = value;
        // evaluateJavascript wraps its result in double quotes.
        if (text.length() > 1 && text.charAt(0) == '"'
                && text.charAt(text.length() - 1) == '"') {
            text = text.substring(1, text.length() - 1);
        }
        text = text.replace(bs + "u003C", "<")
                   .replace(bs + "u003E", ">")
                   .replace(bs + "u0026", "&")
                   .replace(bs + "\"", "\"")
                   .replace(bs + "n", " ")
                   .replace(bs + "t", " ")
                   .replace("" + bs + bs, "" + bs);
        return text;
    }

    /* =====================================================================
     * CHROME CLIENT (progress + popups)
     * =================================================================== */

    private class TvChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            progressBar.setProgress(newProgress);
        }

        /** Some index panels open the video in a new window/tab. */
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, android.os.Message resultMsg) {
            // Redirect the popup request back into our own interception logic.
            WebView.HitTestResult result = view.getHitTestResult();
            String data = result != null ? result.getExtra() : null;
            if (data != null) {
                handleUrl(data);
            }
            return false;
        }
    }

    /* =====================================================================
     * [4] BACK BUTTON
     * =================================================================== */

    @Override
    public void onBackPressed() {
        // 1. Inside the WebView history -> go back one folder.
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        // 2. At the root of this server -> return to the dashboard.
        //    A second Back press (within 2 s) leaves the app entirely.
        long now = System.currentTimeMillis();
        if (now - lastBackPress < 2000) {
            super.onBackPressed();
            return;
        }
        lastBackPress = now;
        Toast.makeText(this, "Press Back again to return to the server list",
                Toast.LENGTH_SHORT).show();
    }

    /* =====================================================================
     * MENUS / DIALOGS
     * =================================================================== */

    private void showPageMenu() {
        final String[] items = {
                "Reload page",
                "Go to server root",
                "Open this server in a browser",
                "Choose default video player",
                "Back to server list"
        };
        new android.app.AlertDialog.Builder(this)
                .setTitle(serverName)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        switch (which) {
                            case 0:
                                webView.reload();
                                break;
                            case 1:
                                startLoading(rootUrl);
                                break;
                            case 2:
                                try {
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(rootUrl));
                                    startActivity(i);
                                } catch (Exception e) {
                                    Toast.makeText(WebViewActivity.this,
                                            "No browser found", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 3:
                                chooseDefaultPlayer();
                                break;
                            case 4:
                                finish();
                                break;
                        }
                    }
                })
                .show();
    }

    /** Lets the user pin one player (VLC / MX) so playback is a single press. */
    private void chooseDefaultPlayer() {
        final String[] labels = {
                "Always ask (system chooser)",
                "VLC for Android",
                "MX Player",
                "MX Player Pro"
        };
        final String[] packages = {
                null,
                "org.videolan.vlc",
                "com.mxtech.videoplayer.ad",
                "com.mxtech.videoplayer.pro"
        };
        new android.app.AlertDialog.Builder(this)
                .setTitle("Default video player")
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        if (packages[which] == null) {
                            getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
                                    .edit().remove("preferred_player").apply();
                        } else {
                            getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
                                    .edit().putString("preferred_player", packages[which]).apply();
                        }
                        Toast.makeText(WebViewActivity.this,
                                "Saved: " + labels[which], Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /** Small context menu for a long-pressed link. */
    private void showLinkMenu(final String url) {
        final String[] items = {"Open link", "Play as video", "Copy URL", "Cancel"};
        new android.app.AlertDialog.Builder(this)
                .setTitle(lastPathSegment(url))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            if (!handleUrl(url)) {
                                webView.loadUrl(url);
                            }
                        } else if (which == 1) {
                            playVideo(url);
                        } else if (which == 2) {
                            android.content.ClipboardManager cm =
                                    (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            cm.setPrimaryClip(android.content.ClipData.newPlainText("url", url));
                            Toast.makeText(WebViewActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    /** HTTP Basic auth prompt. */
    private void showAuthDialog(final HttpAuthHandler handler, String host, String realm) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (getResources().getDisplayMetrics().density * 20);
        box.setPadding(pad, pad, pad, pad);

        final EditText user = new EditText(this);
        user.setHint("Username");
        user.setInputType(InputType.TYPE_CLASS_TEXT);
        box.addView(user);

        final EditText pass = new EditText(this);
        pass.setHint("Password");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        box.addView(pass);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Login required - " + host + (realm != null ? " (" + realm + ")" : ""))
                .setView(box)
                .setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        handler.proceed(user.getText().toString(), pass.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        handler.cancel();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /* =====================================================================
     * [6] ERROR UI
     * =================================================================== */

    private void showError(String message) {
        if (errorPanel == null) {
            return;
        }
        errorPanel.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        errorMessage.setText(message + "\n\n" + rootUrl);
    }

    private void hideError() {
        if (errorPanel != null) {
            errorPanel.setVisibility(View.GONE);
        }
    }

    private String describeError(int code) {
        // Common WebViewClient error codes.
        switch (code) {
            case -1:  return "Unknown error.";
            case -2:  return "Host not found. Check that the server address is correct.";
            case -3:  return "Cannot connect. The server may be offline.";
            case -4:  return "Connection lost while loading.";
            case -5:  return "Connection timed out.";
            case -6:  return "Connection failed.";
            case -7:  return "Too many redirects.";
            case -8:  return "Server timed out (HTTP 504).";
            case -9:  return "Server refused the connection (HTTP 500).";
            case -10: return "Unsupported authentication scheme.";
            case -11: return "Invalid SSL certificate.";
            case -12: return "Authentication failed.";
            case -13: return "Proxy authentication required.";
            case -14: return "Server is unreachable.";
            case -15: return "The URL is not valid.";
            default:  return "Load failed (code " + code + ").";
        }
    }

    /* =====================================================================
     * DEBUG HELPERS
     * =================================================================== */

    /** Logs the current WebView URL - handy when a server redirects somewhere odd. */
    @SuppressWarnings("unused")
    private void logCurrentUrl() {
        if (webView != null) {
            android.util.Log.d("FTVTV", "url=" + webView.getUrl()
                    + " canGoBack=" + webView.canGoBack());
        }
    }

    /** Exposes the default-chooser path for the ALWAYS_SHOW_CHOOSER branch. */
    @SuppressWarnings("unused")
    private void openWithChooser(String videoUrl) {
        MainActivity.openVideoInExternalPlayer(this, videoUrl, serverName);
    }
}
