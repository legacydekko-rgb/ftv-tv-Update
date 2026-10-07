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
        SERVERS.add(new Server("Movie Haat",        "Race Online",          "http://www.moviehaat.net/",      "MH", 0xFFDC2626));
        SERVERS.add(new Server("Media Gallery",     "Direct IP Server",     "http://58.84.34.38/",            "MG", 0xFF059669));
        SERVERS.add(new Server("FTP Media",         "LAN Server",           "http://10.1.1.1/",               "FM", 0xFF059669));
        SERVERS.add(new Server("Natural BD",        "Movies & Series",      "http://www.naturalbd.com/",      "NB", 0xFF0891B2));
        SERVERS.add(new Server("Nagordola FTP",     "Carnival Net",         "http://www.nagordola.com.bd/",   "ND", 0xFF4F46E5));
        SERVERS.add(new Server("Tajpata Movie",     "File Server",          "http://file.tajpata.com/",       "TJ", 0xFFCA8A04));
        SERVERS.add(new Server("Cloud Movie",       "Kloud Tech",           "http://movies.kloud.com.bd/",    "CM", 0xFF2563EB));
        SERVERS.add(new Server("Binodonmela",       "AmberIT",              "http://binodonmela.net/",        "BM", 0xFFCA8A04));
        SERVERS.add(new Server("City Cloud",        "BDIX IP Server",       "http://103.102.253.250/",        "CC", 0xFF059669));
        SERVERS.add(new Server("iHut FTP",          "BDIX Server",          "http://103.204.244.70/",         "IH", 0xFF0891B2));
        SERVERS.add(new Server("New Hub FTP",       "BDIX IP Server",       "http://103.14.27.182/",          "NH", 0xFF059669));
        SERVERS.add(new Server("On BDIX FTP",       "BDIX Server",          "http://onbdix.com/",             "OB", 0xFF2563EB));
        SERVERS.add(new Server("BossBD FTP",        "Movies & Series",      "http://www.bossbd.net/",         "BB", 0xFF2563EB));
        SERVERS.add(new Server("Mojaloss FTP",      "Streaming FTP",        "http://www.mojaloss.net/",       "ML", 0xFFE11D48));
        SERVERS.add(new Server("Intro Vision",      "Media Server",         "http://theintrovision.com/",     "IV", 0xFF7C3AED));
        SERVERS.add(new Server("File Ebox",         "Fileserver EBOX",      "http://fileserver.ebox.live/",   "FE", 0xFF9333EA));
        SERVERS.add(new Server("IT Based FTP",      "BDIX Server",          "http://itbasebd.net/",           "IB", 0xFF0891B2));
        SERVERS.add(new Server("EM DFNBD",          "DFNBD Media",          "http://em.dfnbd.net/",           "ED", 0xFF0891B2));
        SERVERS.add(new Server("Dhaka Movie",       "Antaranga Dot Com",    "http://www.dhakamovie.com/",     "DM", 0xFFEA580C));
        SERVERS.add(new Server("Real Movie FTP",    "Direct IP Server",     "http://103.195.1.50/",           "RM", 0xFF059669));
        SERVERS.add(new Server("Panda Club",        "BDIX Server",          "http://pandaclubbd.com/",        "PC", 0xFF4F46E5));
        SERVERS.add(new Server("Asian FTP",         "Asian Net",            "http://asianftp.com/",           "AF", 0xFF16A34A));
        SERVERS.add(new Server("Meta FTP",          "Direct IP Server",     "http://103.76.196.90/",          "MF", 0xFF059669));
        SERVERS.add(new Server("Gen Video",         "Streaming Platform",   "https://genvideos.org/",         "GV", 0xFFE11D48));
        SERVERS.add(new Server("Mango Gamer",       "Gaming & Media",       "https://mangogamers.com/",       "MG", 0xFFCA8A04));
        SERVERS.add(new Server("BD LAN FTP",        "Media Server",         "http://www.bdlan.net/",          "BL", 0xFF2563EB));
        SERVERS.add(new Server("Spark Net",         "BDIX Server",          "https://sparknetbd.com/",        "SN", 0xFF4F46E5));
        SERVERS.add(new Server("BD Net FTP",        "Business Network",     "http://www.bnet-bd.com/",        "BN", 0xFF2563EB));
        SERVERS.add(new Server("Yes Hub FTP",       "FTPBD Node",           "http://103.58.73.9/",            "YH", 0xFF2563EB));
        SERVERS.add(new Server("Media FTPBD",       "Media Server",         "http://media.ftpbd.net/",        "MF", 0xFF2563EB));
        SERVERS.add(new Server("FTPBD Server 1",    "Server Node 1",        "http://server1.ftpbd.net/",      "F1", 0xFF2563EB));
        SERVERS.add(new Server("FTPBD Server 4",    "Server Node 4",        "http://server4.ftpbd.net/",      "F4", 0xFF2563EB));
        SERVERS.add(new Server("SamBD",             "SAM Online",           "https://sambd.com/",             "SB", 0xFF7C3AED));
        SERVERS.add(new Server("RK Hub",            "LAN Server 1",         "http://172.16.50.4/",            "RK", 0xFF059669));
        SERVERS.add(new Server("RK2 FTP",           "LAN Server 2",         "http://172.16.50.5/",            "R2", 0xFF059669));
        SERVERS.add(new Server("DDnBD",             "Digital Dot Net",      "https://ddnbd.com/",             "DD", 0xFFDC2626));
        SERVERS.add(new Server("DDnBD Fun",         "Digital Dot Fun",      "http://www.ddnbd.fun/",          "DF", 0xFFDC2626));
        SERVERS.add(new Server("CTG Fun",           "Media CTG",            "http://media.ctgfun.com/",       "CF", 0xFFDC2626));
        SERVERS.add(new Server("Play Box FTP",      "Play Ebox",            "http://play.ebox.live/",         "PB", 0xFF9333EA));
        SERVERS.add(new Server("Circle Net",        "Circle Broadband",     "https://circlenetworkbd.net/",   "CN", 0xFF2563EB));
        SERVERS.add(new Server("Circle2 FTP",       "LAN IP Server",        "http://15.1.1.1/",               "C2", 0xFF059669));
        SERVERS.add(new Server("MyBD FTP",          "LAN IP Server",        "http://15.1.1.4/",               "MB", 0xFF059669));
        SERVERS.add(new Server("Circle4 FTP",       "Circle Node 4",        "http://ftp4.circleftp.net/",     "C4", 0xFF2563EB));
        SERVERS.add(new Server("Circle3 HD",        "Circle HD Node",       "http://hd.circleftp.net/",       "C3", 0xFF2563EB));
        SERVERS.add(new Server("Discovery Net",     "Discovery Broadband",  "https://www.discoverynetbd.com/","DN", 0xFF0284C7));
        SERVERS.add(new Server("Discovery IP",      "Discovery Node",       "http://103.120.165.196/",        "DI", 0xFF0284C7));
        SERVERS.add(new Server("DFLIX Discovery",   "DFlix Server",         "http://dflix.discoveryftp.net/", "DD", 0xFF0284C7));
        SERVERS.add(new Server("CDS1 Discovery",    "CDS Node 1",           "http://cds1.discoveryftp.net/",  "C1", 0xFF0284C7));
        SERVERS.add(new Server("CDS2 Discovery",    "CDS Node 2",           "http://cds2.discoveryftp.net/",  "C2", 0xFF0284C7));
        SERVERS.add(new Server("CDS3 Discovery",    "CDS Node 3",           "http://cds3.discoveryftp.net/",  "C3", 0xFF0284C7));
        SERVERS.add(new Server("File Khulna",       "KhulnaFlix File",      "http://file.khulnaflix.net/",    "FK", 0xFFEA580C));
        SERVERS.add(new Server("Exord Online",      "Exord Broadband",      "http://www.exordonline.com/",    "EO", 0xFF9333EA));
        SERVERS.add(new Server("RK Box",            "BDIX IP Server",       "http://103.49.168.107/",         "RB", 0xFF059669));
        SERVERS.add(new Server("ICC Communication", "ICC Broadband",        "https://icc.com.bd/",            "IC", 0xFF0891B2));
        SERVERS.add(new Server("ANT BD",            "Antaranga Broadband",  "http://www.antbd.net/",          "AB", 0xFFEA580C));
        SERVERS.add(new Server("BD Net IP",         "BDIX IP Node",         "http://103.237.37.181/",         "BI", 0xFF059669));
    }

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
