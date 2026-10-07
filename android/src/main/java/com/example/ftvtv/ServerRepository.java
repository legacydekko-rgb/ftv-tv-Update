package com.example.ftvtv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ============================================================================
 * FILE: app/src/main/java/com/example/ftvtv/ServerRepository.java
 * ============================================================================
 * >>> THIS IS THE ONLY FILE YOU NEED TO EDIT TO CHANGE YOUR SERVER LIST <<<
 *
 * Add a line to the SERVERS list, rebuild, done. The dashboard grid, the
 * D-Pad order and the WebView all read from here automatically.
 *
 * Tips
 *  - Always keep the trailing slash: "http://site.com/" (not "http://site.com")
 *    so relative folder links resolve correctly inside the WebView.
 *  - The LAN addresses (172.16.x.x / 10.16.x.x) only work while the device is on
 *    the same Wi-Fi/Ethernet network as the server.
 * ============================================================================
 */
public final class ServerRepository {

    private ServerRepository() {
        // no instances
    }

    private static final List<Server> SERVERS = new ArrayList<Server>();

    static {
        // name, tagline, url, initials, accent colour (0xFFRRGGBB)
        SERVERS.add(new Server("Circle FTP",   "Movies & Series",      "http://circleftp.net/",   "CF", 0xFF2563EB));
        SERVERS.add(new Server("SamOnline FTP","Movies & Series",      "http://172.16.50.14/",    "SO", 0xFF7C3AED));
        SERVERS.add(new Server("Local Server", "LAN Media Server",     "http://10.16.100.244/",   "LS", 0xFF059669));
        SERVERS.add(new Server("CrazyCTG",     "Movies & Series",      "http://crazyctg.com/",    "CC", 0xFFDC2626));
        SERVERS.add(new Server("KhulnaPlex",   "Movies & Series",      "http://khulnaplex.net/",  "KP", 0xFFEA580C));
    }

    /** @return an unmodifiable view of every configured server, in display order. */
    public static List<Server> all() {
        return Collections.unmodifiableList(SERVERS);
    }

    public static int size() {
        return SERVERS.size();
    }

    public static Server get(int index) {
        return SERVERS.get(index);
    }
}
