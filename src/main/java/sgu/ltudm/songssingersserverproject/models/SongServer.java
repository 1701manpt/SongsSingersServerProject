package sgu.ltudm.songssingersserverproject.models;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sgu.ltudm.songssingersserverproject.encryptions.AESEncryption;

import javax.crypto.SecretKey;
import java.io.BufferedWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongServer {
    private SecretKey keyAES;

    public SongServer() {
    }

    public SecretKey getKeyAES() {
        return keyAES;
    }

    public void setKeyAES(SecretKey keyAES) {
        this.keyAES = keyAES;
    }

    public SongModel getSong(String title) throws Exception {
        SongModel song = new SongModel();

        GoogleSearch ggSearch = new GoogleSearch(title, null, null);
        String songTitle = ggSearch.getKeywordCorrect("bài hát");
        song.setTitle(songTitle);

        String songId = getSongIdBySongTitle(songTitle);
        LinkedHashMap<String, String> info = getSongTitleAndSingerNameBySongId(songId);
        song.setArtists(info.get("singerName"));

        int index = 0;
        String linkWiki = getLinkWikipediaBySongTitle(songTitle, index);
        String composer = getComposerFromWikipedia(linkWiki);
        song.setComposer(composer);

//        String linkYoutube = getLinkYoutubeFromGoogleSearch(songTitle);
        String linkYoutube = getLinkYoutubeFromYoutubeSearch(songTitle, info.get("singerName"));
        LinkedHashMap<String, String> urlList = getLinkVideoAndLinkAudioByLinkYoutube(linkYoutube);
        song.setUrlVideo(urlList.get("urlVideo"));

        String urlAudio = getUrlAudioBySongIdFromZingMp3(songId);
        if (!urlAudio.isEmpty()) {
            song.setUrlAudio(urlAudio);
        } else {
            song.setUrlAudio(urlList.get("urlAudio"));
        }

        String lyricFromZingMp3 = getLyricBySongIdFromZingMp3(songId);
        String lyricFromGoogleSearch = getLyricBySongTitleFromGoogleSearch(songTitle);

        if (!lyricFromGoogleSearch.isEmpty()) {
            song.setLyric(lyricFromGoogleSearch);
        } else if (!lyricFromZingMp3.isEmpty()) {
            song.setLyric(lyricFromZingMp3);
        } else {
            song.setLyric("");
        }

        song.log();

        return song;
    }

    public void requestSongsByTitle(String songTitleKeyword, BufferedWriter out) throws Exception {
        String json = getSong(songTitleKeyword).toJson();

        out.write(AESEncryption.encrypt(json, keyAES));
        out.newLine();
        out.flush();
    }

    private LinkedHashMap<String, String> getLinkVideoAndLinkAudioByLinkYoutube(String linkYoutube) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        String urlVideo = "";
        String urlAudio = "";

        try {
            // https://line.1010diy.com/web/free-mp3-finder/detail?url=https://www.youtube.com/watch?v=nK6bIhw_YTw

            VideoApi videoApi = new VideoApi(linkYoutube);
            urlVideo = videoApi.getUrlVideo();
            urlAudio = videoApi.getUrlAudio();

            list.put("urlVideo", urlVideo);
            list.put("urlAudio", urlAudio);

            return list;
        } catch (Exception e) {
            list.put("urlVideo", urlVideo);
            list.put("urlAudio", urlAudio);
            return list;
        }
    }

    // có lỗi khi mục đầu tiên là video quảng cáo
    private String getLinkYoutubeFromYoutubeSearch(String songTitle, String singerName) {
        String linkYoutube = "";

        try {
            // https://www.youtube.com/results?search_query=b%C3%A0i+h%C3%A1t+%C4%90%C3%B4i+m%C6%B0%C6%A1i+c%E1%BB%A7a+Ho%C3%A0ng+D%C5%A9ng&sp=EgIQAQ%253D%253D
            String q = "bài hát " + songTitle + " của " + singerName + " MV Official";

            q = URLEncoder.encode(q, StandardCharsets.UTF_8);

            String url = "https://www.youtube.com/results?search_query=" + q + "&sp=EgIQAQ%253D%253D";

            System.out.println(url);

            Document document = Jsoup.connect(url).get();

            Pattern pattern = Pattern.compile("\"videoId\":\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(document.html());

            String videoId = "";
            if (matcher.find()) {
                videoId = matcher.group(1);
            } else {
                throw new Exception("Không tìm thấy trên youtube");
            }

            linkYoutube = "https://www.youtube.com/watch?v=" + videoId;

            return linkYoutube;
        } catch (Exception e) {
            return linkYoutube;
        }
    }

    // k hề có lỗi, nhưng k lấy đúng bài hát thực sự
    private String getLinkYoutubeFromGoogleSearch(String songTitle) {
        String linkYoutube = "";

        try {
            String q = "bài hát " + songTitle + " MV Official";

            GoogleSearch ggSearch = new GoogleSearch(q, "https://www.youtube.com", TbmGoogleSearch.VIDEO);
            Document document = ggSearch.getDocument();

            Element rsoElement = document.getElementById("rso");

            Element hrefElement = rsoElement.select("a").first();

            linkYoutube = hrefElement.attr("href");

            return linkYoutube;
        } catch (Exception e) {
            return linkYoutube;
        }
    }

    private String getLyricBySongTitleFromGoogleSearch(String songTitle) {
        String lyric = "";

        try {
            // https://www.google.gg/search?q=l%E1%BB%9Di%20b%C3%A0i%20h%C3%A1t%20b%C3%ADch%20ph%C6%B0%C6%A1ng%20chuy%E1%BB%87n%20c%C5%A9%20b%E1%BB%8F%20qua
            String q = "lời bài hát " + songTitle;

            Document document = (new GoogleSearch(q, null, null)).getDocument();

            Element searchElement = document.getElementById("search");

            Element elementsWithDataLyricid = searchElement.select("[data-lyricid]").first();

            Elements elementLyrics = elementsWithDataLyricid.children().first().children().get(1).children();

            LinkedList<String> lyrics = new LinkedList<>();
            for (Element element : elementLyrics) {
                LinkedList<String> lyricChild = new LinkedList<>();
                for (Element e : element.children()) {
                    if (!e.text().isEmpty()) {
                        lyricChild.add(e.text().trim());
                    }
                }

                lyrics.add(String.join("\n", lyricChild));
            }

            lyric = String.join("\n\n", lyrics);

            return lyric;
        } catch (Exception e) {
            return lyric;
        }
    }

    private String getLyricBySongIdFromZingMp3(String songId) {
        String lyric = "";

        try {
            ZingMp3Api api = new ZingMp3Api();
            String json = api.getLyric(songId);
            JSONObject object = new JSONObject(json);
            JSONObject data = object.optJSONObject("data");
            JSONArray sentences = data.getJSONArray("sentences");

            LinkedList<String> sentenceList = new LinkedList();
            for (int i = 0; i < sentences.length(); i++) {
                JSONArray words = sentences.optJSONObject(i).optJSONArray("words");
                LinkedList<String> wordList = new LinkedList<>();
                for (int j = 0; j < words.length(); j++) {
                    wordList.add(words.optJSONObject(j).optString("data"));
                }
                sentenceList.add(String.join(" ", wordList));
            }

            lyric = String.join("\n", sentenceList);

            return lyric;
        } catch (Exception e) {
            return lyric;
        }
    }

    private String getSongIdBySongTitle(String songTitle) throws Exception {
        ZingMp3Api api = new ZingMp3Api();
        String json = api.searchSong(songTitle);
        JSONObject object = new JSONObject(json);
        JSONObject data = object.optJSONObject("data");
        JSONArray items = data.optJSONArray("items");
        JSONObject item = items.optJSONObject(0);

        return item.optString("encodeId");
    }

    private LinkedHashMap<String, String> getSongTitleAndSingerNameBySongId(String songId) throws Exception {
        LinkedHashMap<String, String> info = new LinkedHashMap();

        ZingMp3Api api = new ZingMp3Api();

        JSONObject object = new JSONObject(api.getSong(songId));

        info.put("songTitle", object.optJSONObject("data").optString("title"));
        info.put("singerName", object.optJSONObject("data").optString("artistsNames"));

        return info;
    }

    private String getUrlAudioBySongIdFromZingMp3(String songId) {
        String urlAudio = "";

        try {
            ZingMp3Api api = new ZingMp3Api();
            String json = api.getSongStreaming(songId);

            JSONObject object = new JSONObject(json);
            JSONObject data = object.optJSONObject("data");

            String audio128 = data.optString("128", "");

            if (!audio128.isEmpty()) {
                urlAudio = audio128;
            }

            return urlAudio;
        } catch (Exception e) {
            return urlAudio;
        }
    }

    private String getLinkWikipediaBySongTitle(String songTitle, int index) {
        String linkWikipedia = "";
        try {
            Document document = (new GoogleSearch(songTitle, "https://vi.wikipedia.org", null)).getDocument();
            Elements elements = document.getElementById("rso").children();

            Element element = elements.get(index);

            Element aElement = element.select("a").first();

            linkWikipedia = aElement.attr("href");

            System.out.println("Link Wikipedia: " + linkWikipedia);

            return linkWikipedia;
        } catch (Exception e) {
            return linkWikipedia;
        }
    }

    private String getComposerFromWikipedia(String linkWiki) {
        String composer = "";
        try {
            Document document = Jsoup.connect(linkWiki).get();
            Element infobox = document.getElementsByClass("infobox").first();
            Elements trElements = infobox.select("tr");

            for (Element tr : trElements) {
                if (tr != null) {
                    if (tr.text().toLowerCase().contains("sáng tác")) {
                        composer = tr.select("a").get(1).text().trim();
                        break;
                    }
                }
            }

            return composer;
        } catch (Exception e) {
            return composer;
        }
    }
}
