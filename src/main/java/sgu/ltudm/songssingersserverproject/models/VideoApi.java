package sgu.ltudm.songssingersserverproject.models;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class VideoApi {
    private final String url;
    private final String json;
    private String urlVideo = "";
    private String urlAudio = "";

    public VideoApi(String link) throws Exception {
        this.url = "https://line.1010diy.com/web/free-mp3-finder/detail?url=" + link;
        System.out.println(this.url);
        Document document = Jsoup.connect(url).timeout(100000).ignoreContentType(true).get();

        json = document.text();
    }

    public String getJson() {
        return json;
    }

    public String getUrlVideo() throws Exception {
        try {
            JSONObject object = new JSONObject(json);

            JSONObject dataJsonObject = object.optJSONObject("data", null);
            JSONArray videosJsonArray = dataJsonObject.optJSONArray("videos");

            for (int i = 0; i < videosJsonArray.length(); i++) {
                JSONObject video = videosJsonArray.getJSONObject(i);
                boolean audioExist = video.getBoolean("audio_exist");
                String formatNote = video.getString("formatNote");
                if (audioExist) {
                    urlVideo = video.getString("url");
                    break;
                }
            }

            return urlVideo;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public String getUrlAudio() throws Exception {
        try {
            JSONObject object = new JSONObject(json);

            JSONObject dataJsonObject = object.optJSONObject("data", null);
            JSONArray videosJsonArray = dataJsonObject.optJSONArray("videos");

            for (int i = 0; i < videosJsonArray.length(); i++) {
                JSONObject video = videosJsonArray.getJSONObject(i);
                boolean audioExist = video.getBoolean("audio_exist");
                String formatNote = video.getString("formatNote");
                if (!audioExist) {
                    urlAudio = video.getString("orgin_audio_url");
                    break;
                }
            }

            return urlAudio;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}
