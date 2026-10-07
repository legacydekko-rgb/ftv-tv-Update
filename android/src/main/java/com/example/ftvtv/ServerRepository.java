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
        
        // --- 1. POPULAR BDIX & FTP SERVERS ---
        SERVERS.add(new Server("FTPBD",             "Business Network",   "http://ftpbd.net/",              "FB", 0xFF2563EB));
        SERVERS.add(new Server("FTPBD Media",       "Emby Media Server",  "http://media.ftpbd.net:8096/",   "FM", 0xFF2563EB));
        SERVERS.add(new Server("Circle FTP",        "Circle Network",     "http://circleftp.net/",          "CF", 0xFF1D4ED8));
        SERVERS.add(new Server("Circle Emby",       "Emby Media Server",  "http://emby.circleftp.net:8096/","CE", 0xFF1D4ED8));
        SERVERS.add(new Server("SamOnline FTP",     "Movies & Series",    "http://samftp.com/",             "SO", 0xFF7C3AED));
        SERVERS.add(new Server("SamOnline Emby",    "Emby Media Server",  "http://movie.sambd.net:8096/",   "SE", 0xFF7C3AED));
        SERVERS.add(new Server("KhulnaFlix",        "Cogent Broadband",   "http://khulnaflix.net/",         "KF", 0xFF059669));
        SERVERS.add(new Server("Bokasoka",          "KhulnaFlix NAS",     "http://bokasoka.net/",           "BS", 0xFF059669));
        SERVERS.add(new Server("Discovery FTP",     "Discovery Net",      "http://discoveryftp.net/",       "DF", 0xFF0284C7));
        SERVERS.add(new Server("DFLIX",             "Discovery Movies",   "http://dflix.discoveryftp.net/", "DM", 0xFF0284C7));
        SERVERS.add(new Server("ShowTime BD",       "OneNet Telecom",     "http://www.showtimebd.com/",     "ST", 0xFFD97706));
        SERVERS.add(new Server("CTG Movies",        "Digital Dot Net",    "http://ctgmovies.com/",          "CM", 0xFFDC2626));
        SERVERS.add(new Server("CrazyCTG",          "CTG Media Server",   "http://crazyctg.com/",           "CC", 0xFFDC2626));
        SERVERS.add(new Server("E-BOX Live",        "Exord Online",       "http://play.ebox.live/",         "EB", 0xFF9333EA));
        SERVERS.add(new Server("Dhaka Movie",       "Antaranga Dot Com",  "http://dhakamovie.com/",         "DM", 0xFFEA580C));
        SERVERS.add(new Server("NaturalBD",         "X-Press Tech",       "http://www.naturalbd.com/",      "NB", 0xFF0891B2));
        SERVERS.add(new Server("TimePassBD",        "Skyview Online",     "http://www.timepassbd.live/",    "TP", 0xFF65A30D));
        SERVERS.add(new Server("Nagordola",         "Carnival Internet",  "http://www.nagordola.com.bd/",   "ND", 0xFF4F46E5));
        SERVERS.add(new Server("Elaach",            "Triangle Services",  "http://www.elaach.com/",         "EL", 0xFF059669));
        SERVERS.add(new Server("Unique Download",   "Unique Internet",    "http://www.uniquedownloadbd.com/","UD", 0xFFD97706));
        SERVERS.add(new Server("Mojaloss",          "Streaming FTP",      "https://www.mojaloss.stream/",   "ML", 0xFFE11D48));
        SERVERS.add(new Server("FNF Movies",        "FNF Online",         "http://movies.fnfonlinebd.com/", "FM", 0xFF0284C7));
        SERVERS.add(new Server("iHUB Live",         "Inspire Broadband",  "http://ihub.live/",              "IH", 0xFF7C3AED));
        SERVERS.add(new Server("SmartFlix",         "Chittagong Online",  "http://www.smartflix.digital/",  "SF", 0xFF0891B2));
        SERVERS.add(new Server("C1 Movies",         "CloudOne Internet",  "http://www.c1movies.com/",       "C1", 0xFF2563EB));
        SERVERS.add(new Server("LinkFTP",           "Link Technologies",  "http://play.linkftp.com/",       "LF", 0xFF16A34A));
        SERVERS.add(new Server("Tajpata",           "Tajpata Media",      "http://tajpata.com/",            "TJ", 0xFFCA8A04));
        SERVERS.add(new Server("Mazedanet FTP",     "Mazeda Network",     "http://ftpweb.mazedanetworks.net/","MN", 0xFFDC2626));
        SERVERS.add(new Server("Mazeda Emby",       "Mazeda Media",       "http://172.22.22.105:8096/",     "ME", 0xFFDC2626));
        SERVERS.add(new Server("Sunplex",           "Vision Tech",        "https://sunplex.net/",           "SP", 0xFF0D9488));
        SERVERS.add(new Server("MovieMela",         "Future.Net",         "http://www.moviemela.live/",     "MM", 0xFFDB2777));
        SERVERS.add(new Server("DhakaFTP",          "AmarNet System",     "http://dhakaftp.com/",           "DF", 0xFF4F46E5));
        SERVERS.add(new Server("Free Download BD",  "Local FTP",          "http://www.freedownloadbd.com/", "FD", 0xFF2563EB));
        SERVERS.add(new Server("DFN Media",         "Dhaka Fiber Net",    "http://media.dfnbd.net/",        "DM", 0xFF0891B2));
        SERVERS.add(new Server("CandyBD",           "Planet 3 Com",       "http://www.candybd.net/",        "CB", 0xFFE11D48));
        SERVERS.add(new Server("MCN FTP",           "Millennium Computers","http://ftp.mcnbd.com/",          "MC", 0xFF16A34A));
        SERVERS.add(new Server("PollyFlix",         "Pollyflix Net",      "http://pollyflix.com/",          "PF", 0xFF9333EA));
        SERVERS.add(new Server("Timenai",           "Speed Tech",         "http://timenai.com/",            "TN", 0xFFD97706));
        SERVERS.add(new Server("DNet Drive",        "DNetBD",             "https://dnetdrive.com/",         "DD", 0xFF0284C7));
        SERVERS.add(new Server("Rangdhanu Live",    "Rangdhanu Net",      "https://www.rangdhanu.live/",    "RL", 0xFFCA8A04));
        SERVERS.add(new Server("Moviedom",          "Race Online",        "http://moviedom.live/",          "MD", 0xFFDC2626));
        SERVERS.add(new Server("MovieHaat",         "Race Online CDN",    "http://moviehaat.net/",          "MH", 0xFFDC2626));
        SERVERS.add(new Server("DFlix",             "Dot Internet",       "http://dflix.live/",             "DL", 0xFF2563EB));
        SERVERS.add(new Server("MovieMaja",         "Skyinfo Online",     "http://moviemaja.net/",          "MM", 0xFF7C3AED));
        SERVERS.add(new Server("IBDPlex",           "City Online",        "https://ibdplex.net/",           "IP", 0xFF0891B2));
        SERVERS.add(new Server("Flixhub",           "Radisson Tech",      "http://flixhub.net/",            "FH", 0xFFE11D48));
        SERVERS.add(new Server("ABCMovies",         "Empowering Net",     "http://abcmoviesbd.com/",        "AM", 0xFF059669));
        SERVERS.add(new Server("CTGOZ",             "ZeroOne Online",     "http://ctgoz.com/",              "CZ", 0xFFD97706));
        SERVERS.add(new Server("SmileDotNet",       "Brisk Systems",      "http://smiledotnet.xyz/",        "SN", 0xFF4F46E5));
        SERVERS.add(new Server("AlphaMediaZone",    "Alpha Broadway",     "http://ftp.alphamediazone.com/", "AZ", 0xFF0284C7));
        SERVERS.add(new Server("AsianFTP",          "Asian Communication","http://asianftp.com/",           "AF", 0xFF16A34A));
        SERVERS.add(new Server("Binodonmela",       "AmberIT",            "http://www.binodonmela.net/",    "BM", 0xFFCA8A04));
        SERVERS.add(new Server("AKKadukka",         "Niloy Net",          "http://akkadukka.com/",          "AK", 0xFF9333EA));
        SERVERS.add(new Server("AKTube",            "AK Networks",        "http://aktube.live/",            "AT", 0xFFDC2626));
        SERVERS.add(new Server("BossBD",            "BossBD Network",     "http://bossbd.live/",            "BB", 0xFF2563EB));
        SERVERS.add(new Server("CinemaBazar",       "SK Traders",         "http://cinemabazar.net/",        "CB", 0xFF0891B2));
        SERVERS.add(new Server("KhulnaPlex",        "Daulatpur Online",   "http://khulnaplex.net/",         "KP", 0xFFEA580C));
        SERVERS.add(new Server("EvoNet",            "Evolution Net",      "http://fs.evonetbd.com/",        "EN", 0xFF059669));
        SERVERS.add(new Server("PlexBD",            "Extreme Net",        "http://plexbd.net/",             "PB", 0xFF7C3AED));
        SERVERS.add(new Server("CTGFlix",           "E-Village Internet", "http://www.ctgflix.com/",        "CF", 0xFFD97706));
        SERVERS.add(new Server("FunTimeBD",         "Reign ICT",          "http://funtimebd.com/",          "FB", 0xFFE11D48));
        SERVERS.add(new Server("FastPlex",          "Fastnet BD",         "https://fastplex.net/",          "FP", 0xFF4F46E5));
        SERVERS.add(new Server("S-Flix",            "FRC Broadband",      "http://s-flix.live/",            "SF", 0xFF0284C7));
        SERVERS.add(new Server("Hasikhushi",        "BDConnect",          "https://hasikhushi.net/",        "HK", 0xFF16A34A));
        SERVERS.add(new Server("Halum",             "Halum Net",          "http://halum.net/",              "HL", 0xFFCA8A04));
        SERVERS.add(new Server("iBoxBD",            "iCommunication",     "http://iboxbd.online/",          "IB", 0xFF9333EA));
        SERVERS.add(new Server("Kloud Movies",      "Kloud Technologies", "http://movies.kloud.com.bd/",    "KM", 0xFFDC2626));
        SERVERS.add(new Server("MyMovieBazar",      "Media Online",       "http://mymoviebazar.net/",       "MB", 0xFF2563EB));
        SERVERS.add(new Server("MovieBoxBD",        "BD Networks",        "http://movieboxbd.com/",         "MB", 0xFF0891B2));
        SERVERS.add(new Server("MidiPlex",          "MidiPlex Net",       "http://midiplex.net/",           "MP", 0xFFEA580C));
        SERVERS.add(new Server("MLWBD",             "MLWBD Media",        "https://mlwbd.mobi/",            "ML", 0xFF059669));
        SERVERS.add(new Server("NBox Live",         "Novus Network",      "http://nbox.live/",              "NB", 0xFF7C3AED));
        SERVERS.add(new Server("Jhakkas Live",      "NMS Technologies",   "http://jhakkas.live/",           "JL", 0xFFD97706));
        SERVERS.add(new Server("CinemaCity",        "Rainbow Network",    "http://cinemacity.live/",        "CC", 0xFFE11D48));
        SERVERS.add(new Server("StarPlexBD",        "Stargate Com",       "http://starplexbd.com/",         "SB", 0xFF4F46E5));

        // --- 2. LAN & DIRECT IP SERVERS ---
        SERVERS.add(new Server("Local Server 1",    "LAN IP 10.16.x.x",   "http://10.16.100.244/",          "L1", 0xFF059669));
        SERVERS.add(new Server("Local Server 2",    "LAN IP 172.16.x.x",  "http://172.16.50.4/",            "L2", 0xFF059669));
        SERVERS.add(new Server("Local Server 3",    "LAN IP 11.11.x.x",   "http://11.11.11.11/",            "L3", 0xFF059669));
        SERVERS.add(new Server("Local Server 4",    "LAN IP 172.27.x.x",  "http://172.27.27.1/",            "L4", 0xFF059669));
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
