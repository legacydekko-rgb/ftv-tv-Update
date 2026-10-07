package com.example.ftvtv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ============================================================================
 * FILE: app/src/main/java/com/example/ftvtv/ServerRepository.java
 * ============================================================================
 */
public final class ServerRepository {

    private ServerRepository() {
        // no instances
    }

    private static final List<Server> SERVERS = new ArrayList<Server>();

    static {
        // name, tagline, url, initials, accent colour (0xFFRRGGBB)
        
        SERVERS.add(new Server("Circle FTP",        "Movies & Series",      "http://circleftp.net/",          "CF", 0xFF2563EB));
        SERVERS.add(new Server("SamOnline FTP",     "LAN Media Server",     "http://172.16.50.14/",           "SO", 0xFF7C3AED));
        SERVERS.add(new Server("Dhaka Flix",        "LAN Media Server",     "http://10.16.100.244/",          "DF", 0xFF059669));
        SERVERS.add(new Server("CrazyCTG",          "Movies & Series",      "http://crazyctg.com/",           "CC", 0xFFDC2626));
        SERVERS.add(new Server("KhulnaPlex",        "Movies & Series",      "http://khulnaplex.net/",         "KP", 0xFFEA580C));
        SERVERS.add(new Server("Discovery Movies",  "Movies & Series",      "https://movies.discoveryftp.net/m/","DM", 0xFF0284C7));
        SERVERS.add(new Server("CTG Movie",         "Movies & Series",      "http://ctgmovies.com/",          "CM", 0xFFDC2626));
        SERVERS.add(new Server("SAM FTP",           "Movies & Series",      "http://samftp.com/",             "SF", 0xFF7C3AED));
        SERVERS.add(new Server("Discovery FTP",     "Movies & Series",      "http://discoveryftp.net/",       "DF", 0xFF0284C7));
        SERVERS.add(new Server("FTPBD Server",      "BDIX FTP Server",      "http://ftpbd.net/",              "FB", 0xFF2563EB));
        SERVERS.add(new Server("Cognet FTP",        "BDIX Server",          "http://103.153.175.254/",        "CG", 0xFF059669));
        SERVERS.add(new Server("Carnival FTP",      "BDIX Server",          "http://103.106.238.74/",         "CN", 0xFF4F46E5));
        SERVERS.add(new Server("Link3 FTP",         "Cinehub24",            "http://www.cinehub24.com/",      "L3", 0xFF0891B2));
        SERVERS.add(new Server("Dhaka FTP",         "BDIX FTP Server",      "http://dhakaftp.com/",           "DF", 0xFF4F46E5));
        SERVERS.add(new Server("FuntimeBD FTP",     "Movies & Series",      "http://funtimebd.com/",          "FT", 0xFFE11D48));
        SERVERS.add(new Server("Quick Online FTP",  "BDIX Server",          "http://quickonlineftp.com/",     "QO", 0xFFD97706));
        SERVERS.add(new Server("DFNBD FTP",         "BDIX FTP Server",      "http://bn.dfnbd.net/",           "DB", 0xFF0891B2));
        SERVERS.add(new Server("Net At Home",       "Movies & Series",      "http://www.netathomebd.com/",    "NH", 0xFF2563EB));
        SERVERS.add(new Server("Vdo Mela FTP",      "Movies & Series",      "http://vdomela.com/",            "VM", 0xFFCA8A04));
        SERVERS.add(new Server("iHub FTP",          "Live & Movies",        "http://ihub.live/",              "IH", 0xFF7C3AED));
        SERVERS.add(new Server("Bongo BD",          "Streaming Platform",   "https://www.bongobd.com/en/",    "BG", 0xFFE11D48));
        SERVERS.add(new Server("iFlixHD",           "Movies & Series",      "https://iflixhd.top/",           "IH", 0xFF9333EA));
        SERVERS.add(new Server("HDiFlix",           "Movies & Series",      "https://hdiflix.top/",           "HI", 0xFF9333EA));
        SERVERS.add(new Server("Show Time BD",      "Movies & Series",      "http://showtimebd.com/",         "ST", 0xFFD97706));
        SERVERS.add(new Server("Movie Mela FTP",    "Movies & Series",      "http://www.moviemela.live/",     "MM", 0xFFDB2777));
        SERVERS.add(new Server("FSebox FTP",        "Exord EBOX",           "http://fs.ebox.live/",           "FE", 0xFF9333EA));
        SERVERS.add(new Server("Movie Box FTP",     "Movies & Series",      "http://movieboxbd.com/",         "MB", 0xFF0891B2));
