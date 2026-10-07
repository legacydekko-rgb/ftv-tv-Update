package com.example.ftvtv;

/**
 * ============================================================================
 * FILE: app/src/main/java/com/example/ftvtv/Server.java
 * ============================================================================
 * A tiny immutable model describing one file-directory (FTP/HTTP index) server.
 *
 * All servers live in one place: {@link ServerRepository}. Edit that file to add
 * or remove servers - you never have to touch the UI code.
 * ============================================================================
 */
public class Server {

    /** Name shown on the dashboard card, e.g. "Dhaka Flix". */
    public final String name;

    /** Subtitle shown under the name, e.g. "Movies & Series". */
    public final String tagline;

    /** Root URL that gets loaded into the WebView. Keep the trailing slash. */
    public final String url;

    /** Two/three letters drawn on the card, e.g. "DF". */
    public final String initials;

    /** ARGB colour used for the card accent + glow. */
    public final int accentColor;

    public Server(String name, String tagline, String url, String initials, int accentColor) {
        this.name = name;
        this.tagline = tagline;
        this.url = url;
        this.initials = initials;
        this.accentColor = accentColor;
    }

    /** @return the host part of the URL, handy for log messages. */
    public String host() {
        String h = url.replace("http://", "").replace("https://", "");
        int slash = h.indexOf('/');
        return slash > 0 ? h.substring(0, slash) : h;
    }
}
