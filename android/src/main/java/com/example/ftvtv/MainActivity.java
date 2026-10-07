package com.example.ftvtv;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * ============================================================================
 * FILE: app/src/main/java/com/example/ftvtv/MainActivity.java
 * ============================================================================
 * The HOME SERVER SELECTION DASHBOARD.
 *
 * Responsibilities
 *  1. Renders one focusable card per server defined in ServerRepository.
 *  2. Implements a deterministic D-Pad focus engine (UP/DOWN/LEFT/RIGHT/OK) so
 *     remote navigation never "gets lost" the way default focus search can.
 *  3. Gives the focused card a scale-up + coloured glow effect.
 *  4. Provides the shared video-link interception helpers used by
 *     WebViewActivity (see {@link #looksLikeVideoUrl} and
 *     {@link #openVideoInExternalPlayer}).
 *  5. Remembers the last server you opened and offers a "Resume" button.
 * ============================================================================
 */
public class MainActivity extends Activity {

    /* ---------------------------------------------------------------------
     * Tunables
     * ------------------------------------------------------------------- */

    /** Number of cards per row on the dashboard grid. 3 fits both TV and phone. */
    private static final int COLUMNS = 3;

    /** Scale applied to the focused card. */
    private static final float FOCUS_SCALE = 1.08f;

    /** How long the focus animation runs, in ms. */
    private static final int FOCUS_ANIM_MS = 140;

    /**
     * File extensions that are treated as "playable video".
     * Add anything your servers actually serve.
     */
    public static final String[] VIDEO_EXTENSIONS = {
            "mp4", "mkv", "avi", "webm", "m3u8", "mpg", "mpeg",
            "mov", "flv", "wmv", "m4v", "ts", "m2ts", "3gp", "ogv", "divx", "vob"
    };

    /** SharedPreferences file used for the "resume last server" feature. */
    public static final String PREFS = "ftvtv_prefs";
    public static final String KEY_LAST_SERVER = "last_server_index";

    /* ---------------------------------------------------------------------
     * Views / state
     * ------------------------------------------------------------------- */

    private GridLayout grid;
    private TextView resumeBar;

    /* =====================================================================
     * LIFECYCLE
     * =================================================================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grid = (GridLayout) findViewById(R.id.server_grid);
        resumeBar = (TextView) findViewById(R.id.resume_bar);

        grid.setColumnCount(COLUMNS);
        buildDashboard();
        setupResumeBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Coming back from the WebView: refresh the "last watched server" hint.
        updateResumeBarText();
    }

    /* =====================================================================
     * DASHBOARD CONSTRUCTION
     * =================================================================== */

    private void buildDashboard() {
        grid.removeAllViews();

        final List<Server> servers = ServerRepository.all();

        // Pre-compute card size so every card is identical and the grid is tidy.
        int cardWidth = (int) dp(240);
        int cardHeight = (int) dp(150);
        int gap = (int) dp(18);

        for (int i = 0; i < servers.size(); i++) {
            Server server = servers.get(i);

            View card = createServerCard(server, i);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = cardWidth;
            lp.height = cardHeight;
            lp.setMargins(gap / 2, gap / 2, gap / 2, gap / 2);
            card.setLayoutParams(lp);

            final int index = i;
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openServer(index);
                }
            });

            // --- D-Pad focus engine -----------------------------------------
            card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    animateCard(v, hasFocus);
                    // The ScrollView's own focus scrolling keeps the focused card
                    // on screen, so nothing else is needed here.
                }
            });

            // Explicit spatial navigation: far more reliable on TV than the
            // framework's default focus search across a GridLayout.
            card.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return false;
                    }
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            moveFocus(index - 1);
                            return true;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            moveFocus(index + 1);
                            return true;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            moveFocus(index - COLUMNS);
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            moveFocus(index + COLUMNS);
                            return true;
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_NUMPAD_ENTER:
                        case KeyEvent.KEYCODE_BUTTON_A:
                            openServer(index);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            grid.addView(card);
        }

        // Give the first card the initial focus so the remote works immediately.
        grid.post(new Runnable() {
            @Override
            public void run() {
                View first = grid.getChildAt(0);
                if (first != null) {
                    first.requestFocus();
                }
            }
        });
    }

    /**
     * Builds a single dashboard card.
     *
     * Card layout (a FrameLayout so the accent stripe can overlay the background):
     *   [ accent stripe ][ initials ][ server name ][ tagline ][ host ]
     */
    private View createServerCard(Server server, int index) {
        FrameLayout card = new FrameLayout(this);
        card.setFocusable(true);
        card.setFocusableInTouchMode(false);
        card.setClickable(true);
        card.setClipToPadding(false);
        card.setClipChildren(false);

        // ---- Background: rounded dark card with a 2dp border ----------------
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(14));
        bg.setColor(0xFF141A24);
        bg.setStroke((int) dp(2), 0xFF2A3342);
        card.setBackground(bg);

        // ---- Accent stripe on the left edge ---------------------------------
        View stripe = new View(this);
        GradientDrawable stripeBg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{server.accentColor, blend(server.accentColor, Color.BLACK, 0.45f)});
        stripeBg.setCornerRadii(new float[]{dp(14), dp(14), 0, 0, 0, 0, dp(14), dp(14)});
        stripe.setBackground(stripeBg);
        FrameLayout.LayoutParams stripeLp = new FrameLayout.LayoutParams(
                (int) dp(8), ViewGroup.LayoutParams.MATCH_PARENT);
        stripeLp.gravity = Gravity.START;
        stripe.setLayoutParams(stripeLp);
        card.addView(stripe);

        // ---- Inner vertical content column ----------------------------------
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_VERTICAL);
        column.setPadding((int) dp(26), (int) dp(16), (int) dp(16), (int) dp(16));
        FrameLayout.LayoutParams colLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        column.setLayoutParams(colLp);
        card.addView(column);

        // Initials badge
        TextView initials = new TextView(this);
        initials.setText(server.initials);
        initials.setTextColor(Color.WHITE);
        initials.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        initials.setTypeface(initials.getTypeface(), android.graphics.Typeface.BOLD);
        initials.setGravity(Gravity.CENTER);
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setShape(GradientDrawable.OVAL);
        badgeBg.setColor(server.accentColor);
        initials.setBackground(badgeBg);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                (int) dp(40), (int) dp(40));
        badgeLp.bottomMargin = (int) dp(12);
        initials.setLayoutParams(badgeLp);
        column.addView(initials);

        // Server name
        TextView name = new TextView(this);
        name.setText(server.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        name.setTypeface(name.getTypeface(), android.graphics.Typeface.BOLD);
        name.setSingleLine(true);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        column.addView(name);

        // Tagline
        TextView tagline = new TextView(this);
        tagline.setText(server.tagline);
        tagline.setTextColor(0xFF8B96A8);
        tagline.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tagline.setSingleLine(true);
        column.addView(tagline);

        // Host, in the server's accent colour
        TextView host = new TextView(this);
        host.setText(server.host());
        host.setTextColor(server.accentColor);
        host.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        host.setSingleLine(true);
        host.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams hostLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hostLp.topMargin = (int) dp(8);
        host.setLayoutParams(hostLp);
        column.addView(host);

        return card;
    }

    /* =====================================================================
     * FOCUS HANDLING
     * =================================================================== */

    /** Moves focus to a card by index, clamped to the available range. */
    private void moveFocus(int index) {
        if (grid == null || grid.getChildCount() == 0) {
            return;
        }
        if (index < 0 || index >= grid.getChildCount()) {
            return; // already at an edge - keep focus where it is
        }
        View target = grid.getChildAt(index);
        if (target != null) {
            target.requestFocus();
        }
    }

    /**
     * Scale-up + glow animation for the focused card.
     * On TV the focused element must be obvious from 3 metres away, hence the
     * border colour change in addition to the scale.
     */
    private void animateCard(View card, boolean focused) {
        float targetScale = focused ? FOCUS_SCALE : 1f;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", targetScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", targetScale);
        scaleX.setDuration(FOCUS_ANIM_MS);
        scaleY.setDuration(FOCUS_ANIM_MS);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        scaleX.start();
        scaleY.start();

        // Elevation gives the "lifted off the page" look on API 21+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(focused ? dp(16) : dp(0));
        }

        // Recolour the border: bright red glow when focused, muted when not.
        android.graphics.drawable.Drawable d = card.getBackground();
        if (d instanceof GradientDrawable) {
            GradientDrawable g = (GradientDrawable) d;
            g.setStroke((int) dp(focused ? 3 : 2),
                    focused ? 0xFFFF0000 : 0xFF2A3342);
        }

        if (focused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Subtle accent-coloured glow (state list tint) around the card.
            card.setStateListAnimator(null);
        }
    }

    /* =====================================================================
     * RESUME BAR
     * =================================================================== */

    private void setupResumeBar() {
        if (resumeBar == null) {
            return;
        }
        resumeBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idx = lastServerIndex();
                if (idx >= 0) {
                    openServer(idx);
                }
            }
        });
        resumeBar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Long-press clears the remembered server.
                getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                        .remove(KEY_LAST_SERVER).apply();
                updateResumeBarText();
                Toast.makeText(MainActivity.this, "Resume cleared", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        updateResumeBarText();
    }

    private int lastServerIndex() {
        int idx = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_LAST_SERVER, -1);
        if (idx < 0 || idx >= ServerRepository.size()) {
            return -1;
        }
        return idx;
    }

    private void updateResumeBarText() {
        if (resumeBar == null) {
            return;
        }
        int idx = lastServerIndex();
        if (idx < 0) {
            resumeBar.setVisibility(View.GONE);
        } else {
            resumeBar.setVisibility(View.VISIBLE);
            resumeBar.setText(getString(R.string.resume_hint,
                    ServerRepository.get(idx).name));
        }
    }

    /* =====================================================================
     * NAVIGATION
     * =================================================================== */

    /** Launches the full-screen WebView directory browser for the given server. */
    public void openServer(int index) {
        Server server = ServerRepository.get(index);

        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putInt(KEY_LAST_SERVER, index).apply();

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_SERVER_NAME, server.name);
        intent.putExtra(WebViewActivity.EXTRA_SERVER_URL, server.url);
        intent.putExtra(WebViewActivity.EXTRA_ACCENT, server.accentColor);
        startActivity(intent);
    }

    /* =====================================================================
     * SHARED VIDEO-LINK LOGIC (used by WebViewActivity too)
     * =================================================================== */

    /**
     * @return true when the URL path ends in one of {@link #VIDEO_EXTENSIONS}.
     *         Query strings are ignored, so ".../movie.mkv?token=abc" matches.
     */
    public static boolean looksLikeVideoUrl(String url) {
        if (url == null) {
            return false;
        }
        String lower = url.toLowerCase();

        // Strip query string and fragment.
        int cut = lower.length();
        int q = lower.indexOf('?');
        if (q >= 0 && q < cut) cut = q;
        int h = lower.indexOf('#');
        if (h >= 0 && h < cut) cut = h;
        String path = lower.substring(0, cut);

        for (String ext : VIDEO_EXTENSIONS) {
            if (path.endsWith("." + ext)) {
                return true;
            }
        }
        // Some index servers append extra text after the extension, e.g.
        // "/movie.mkv/play" - a looser contains-check catches those too.
        for (String ext : VIDEO_EXTENSIONS) {
            if (path.contains("." + ext + "/")) {
                return true;
            }
        }
        return false;
    }

    /** Guesses the MIME type from the file extension, defaulting to "video/*". */
    public static String guessMimeType(String url) {
        String lower = url.toLowerCase();
        int cut = lower.length();
        int q = lower.indexOf('?');
        if (q >= 0) cut = q;
        int slash = lower.lastIndexOf('.', cut - 1);
        if (slash < 0) {
            return "video/*";
        }
        String ext = lower.substring(slash + 1, cut);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return (mime == null || !mime.startsWith("video")) ? "video/*" : mime;
    }

    /**
     * Fires Intent.ACTION_VIEW with MIME "video/*" so Android shows the
     * "Open with" chooser (VLC, MX Player, built-in player...).
     *
     * IMPORTANT: the intent is built with FLAG_ACTIVITY_NEW_TASK because we may
     * be starting it from a non-Activity context. For an HTTP/HTTPS stream we do
     * NOT call setDataAndType(Uri, "video/*") directly on some OEM builds - it
     * confuses the resolver - so we try the typed intent first and fall back to
     * a plain http VIEW intent (a browser, or VLC's own http handler, picks it up).
     *
     * @param context activity or application context
     * @param videoUrl the direct video URL
     * @param title    server name, used in the chooser header
     * @return true if something was launched
     */
    public static boolean openVideoInExternalPlayer(Context context,
                                                   String videoUrl,
                                                   String title) {
        if (videoUrl == null || videoUrl.length() == 0) {
            return false;
        }
        Uri uri = Uri.parse(videoUrl);
        String mime = guessMimeType(videoUrl);

        // ---------- Attempt 1: explicit video/* intent ----------------------
        Intent videoIntent = new Intent(Intent.ACTION_VIEW);
        videoIntent.setDataAndType(uri, mime);
        videoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (isIntentResolvable(context, videoIntent)) {
            try {
                Intent chooser = Intent.createChooser(videoIntent,
                        title == null ? "Play with" : "Play with  -  " + title);
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(chooser);
                return true;
            } catch (ActivityNotFoundException e) {
                // fall through to attempt 2
            }
        }

        // ---------- Attempt 2: plain http VIEW ------------------------------
        // VLC and MX Player both register as http/https handlers, so this is a
        // very reliable fallback for streamed links.
        Intent httpIntent = new Intent(Intent.ACTION_VIEW, uri);
        httpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isIntentResolvable(context, httpIntent)) {
            try {
                Intent chooser = Intent.createChooser(httpIntent,
                        title == null ? "Open with" : "Open with  -  " + title);
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(chooser);
                return true;
            } catch (ActivityNotFoundException e) {
                // fall through
            }
        }

        Toast.makeText(context,
                "No video player found.\nInstall VLC for Android or MX Player.",
                Toast.LENGTH_LONG).show();
        return false;
    }

    /** Safe wrapper around PackageManager.resolveActivity (never throws). */
    public static boolean isIntentResolvable(Context context, Intent intent) {
        try {
            PackageManager pm = context.getPackageManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ResolveInfo info = pm.resolveActivity(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                return info != null;
            } else {
                List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
                return list != null && !list.isEmpty();
            }
        } catch (Exception e) {
            return false;
        }
    }

    /* =====================================================================
     * BACK BUTTON
     * =================================================================== */

    @Override
    public void onBackPressed() {
        // We are already at the dashboard root: confirm before leaving the app.
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_title)
                .setMessage(R.string.exit_message)
                .setPositiveButton(R.string.exit_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.exit_no, null)
                .show();
    }

    /* =====================================================================
     * SMALL UTILITIES
     * =================================================================== */

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    /** Blends colour {@code a} toward {@code b} by {@code ratio} (0..1). */
    private static int blend(int a, int b, float ratio) {
        float ir = 1f - ratio;
        int r = (int) (Color.red(a) * ir + Color.red(b) * ratio);
        int g = (int) (Color.green(a) * ir + Color.green(b) * ratio);
        int bl = (int) (Color.blue(a) * ir + Color.blue(b) * ratio);
        return Color.rgb(r, g, bl);
    }
}
