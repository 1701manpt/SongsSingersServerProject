package sgu.ltudm.songssingersserverproject.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedList;

public class SongModel {
    private String title;
    private String artists;
    private String composer;
    private String lyric;
    private String urlAudio;
    private String urlVideo;
    private LinkedList<String> urlVideoList;
    private LinkedList<String> urlAudioList;

    public SongModel() {

    }

    public void log() {
        System.out.println("Tên bài hát: " + title);
        System.out.println("Nhạc sĩ sáng tác: " + composer);
//        System.out.println("Ca sĩ: " + artists);
        System.out.println("Link audio: " + urlAudio);
        System.out.println("Link video: " + urlVideo);
        System.out.println("Lời bài hát: \n" + lyric);
    }

    public String toJson() {
        String json = "";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    public SongModel toObject(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(json, this.getClass());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public String getUrlAudio() {
        return urlAudio;
    }

    public void setUrlAudio(String urlAudio) {
        this.urlAudio = urlAudio;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }
}
