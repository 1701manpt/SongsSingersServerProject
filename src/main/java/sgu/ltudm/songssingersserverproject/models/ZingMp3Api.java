package sgu.ltudm.songssingersserverproject.models;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sgu.ltudm.songssingersserverproject.encryptions.Hash;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class ZingMp3Api {
    final private String apiKey = "X5BM3w8N7MKozC0B85o4KMlzLZKhV00y";
    private final String thumbSize = "600_600";
    private final String version = "1.9.88";
    private final String host = "https://zingmp3.vn";
    private RouteZingMp3Api route;
    private String url;
    private String json;
    private String ctime;

    public ZingMp3Api() {
    }

    public static void main(String[] args) throws Exception {
        ZingMp3Api api = new ZingMp3Api();
//        System.out.println(api.searchSong("cat doi noi sau"));
        System.out.println(api.getSongStreaming("Z6FWCOO0"));
    }

    public String getSongStreaming(String songId) throws Exception {
        route = RouteZingMp3Api.GET_SONG_STREAMING;

        LinkedHashMap<String, String> params = new LinkedHashMap();
        params.put("id", "id=" + songId);

        generateUrlApi(params);

        return getJson();
    }

    public String getArtist(String alias) throws Exception {
        route = RouteZingMp3Api.GET_ARTIST;

        LinkedHashMap<String, String> params = new LinkedHashMap();
        params.put("alias", "alias=" + alias);

        generateUrlApi(params);

        return getJson();
    }

    public String searchArtist(String q) throws Exception {
        route = RouteZingMp3Api.SEARCH;

        q = URLEncoder.encode(q, StandardCharsets.UTF_8);

        LinkedHashMap<String, String> params = new LinkedHashMap();
        params.put("q", "q=" + q);
        params.put("type", "type=" + "artist");

        generateUrlApi(params);

        return getJson();
    }

    public String searchSong(String q) throws Exception {
        route = RouteZingMp3Api.SEARCH;

        q = URLEncoder.encode(q, StandardCharsets.UTF_8);

        LinkedHashMap<String, String> params = new LinkedHashMap();
        params.put("q", "q=" + q);
        params.put("type", "type=" + "song");

        generateUrlApi(params);

        return getJson();
    }

    public String getLyric(String songId) throws Exception {
        route = RouteZingMp3Api.GET_LYRIC;

        LinkedHashMap<String, String> params = new LinkedHashMap();
        params.put("id", "id=" + songId);

        generateUrlApi(params);

        return getJson();
    }

    public String getSong(String songId) throws Exception {
        route = RouteZingMp3Api.GET_SONG;

        LinkedHashMap<String, String> params = new LinkedHashMap();
        params.put("id", "id=" + songId);
        params.put("thumbSize", "thumbSize=" + thumbSize);

        generateUrlApi(params);

        return getJson();
    }

    public String getPlaylist(String playlistId) throws Exception {
        route = RouteZingMp3Api.GET_PLAYLIST;

        LinkedHashMap<String, String> params = new LinkedHashMap();
        params.put("id", "id=" + playlistId);
        params.put("thumbSize", "thumbSize=" + thumbSize);

        generateUrlApi(params);

        return getJson();
    }

    public String getSongList(String artistId, @Nullable Integer page, Integer count) throws Exception {
        route = RouteZingMp3Api.GET_SONG_LIST;

        if (page == null || page.describeConstable().isEmpty()) {
            page = 1;
        }

        if (count == null || count.describeConstable().isEmpty()) {
            count = 20;
        }

        LinkedHashMap<String, String> params = new LinkedHashMap();
        params.put("id", "id=" + artistId);
        params.put("type", "type=artist");
        params.put("page", "page=" + page);
        params.put("count", "count=" + count);
        params.put("sort", "sort=new");

        generateUrlApi(params);

        return getJson();
    }

    private void generateUrlApi(LinkedHashMap<String, String> params) throws Exception {
        ctime = Long.toString(System.currentTimeMillis() / 1000L);

        params.put("ctime", "ctime=" + ctime);
        params.put("version", "version=" + version);
        params.put("sig", "sig=" + generateSignature(route, params));
        params.put("apiKey", "apiKey=" + apiKey);

        String paramsStr = String.join("&", params.values());
        url = host + route.get() + "?" + paramsStr;

        System.out.println("API " + route.get() + " " + url);
    }

    private String getJson() throws Exception {
        Map<String, String> cookies = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).execute().cookies();

        Document document = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).cookies(cookies).execute().parse();
        json = document.text();

        return json;
    }

    private String generateSignature(RouteZingMp3Api route, LinkedHashMap<String, String> params) throws Exception {
        final String key = "acOrvUS15XRW2o9JksiK1KgQ6Vbds8ZW";

        LinkedHashMap<String, String> n = new LinkedHashMap();
        n.put("ctime", params.get("ctime"));
        n.put("version", params.get("version"));

        if (route == RouteZingMp3Api.GET_SONG || route == RouteZingMp3Api.GET_PLAYLIST || route == RouteZingMp3Api.GET_LYRIC || route == RouteZingMp3Api.GET_SONG_STREAMING) {
            n.put("id", params.get("id"));
        }

        if (route == RouteZingMp3Api.GET_SONG_LIST) {
            n.put("id", params.get("id"));
            n.put("page", params.get("page"));
            n.put("count", params.get("count"));
            n.put("type", params.get("type"));
        }

        if (route == RouteZingMp3Api.SEARCH) {
            n.put("type", params.get("type"));
        }

        TreeMap<String, String> sortedN = new TreeMap<>(n);

        String t = Hash.hashSHA256(String.join("", sortedN.values()));

        String m = Hash.hashSHA512(route.get() + t, key);

        return m;
    }
}
