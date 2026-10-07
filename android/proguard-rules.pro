# ============================================================================
# FILE: android/proguard-rules.pro
# ============================================================================
# Only used for release builds (minifyEnabled true). The default build config
# has minification OFF, so this file is a safety net for when you turn it on.

# Keep the app's own classes. Reflection is used for the focus/animation code
# and for SharedPreferences keys, and stripping them causes crashes that only
# appear in release builds.
-keep class com.example.ftvtv.** { *; }

# If you ever add addJavascriptInterface(...) bridges to the WebView, this keeps
# the methods reachable from JavaScript. (This project uses evaluateJavascript
# instead, but the rule is harmless and prevents a classic release-only bug.)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep the WebView / JavaScript related platform entry points untouched.
-keepclassmembers class * extends android.webkit.WebViewClient {
    public *;
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public *;
}

# Keep enum values used in SharedPreferences comparisons.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
