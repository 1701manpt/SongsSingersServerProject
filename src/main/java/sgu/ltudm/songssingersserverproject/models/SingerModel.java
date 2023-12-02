package sgu.ltudm.songssingersserverproject.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedList;

public class SingerModel {
    private String name;
    private String dateOfBirth;
    private String placeOfBirth;
    private LinkedList<String> aliases;
    private String biography;
    private String songs;
    private LinkedList<SongModel> songList;

    public SingerModel() {
    }

    public LinkedList<SongModel> getSongList() {
        return songList;
    }

    public void setSongList(LinkedList<SongModel> songList) {
        this.songList = songList;
    }

    public void log() {
        System.out.println("Tên: " + name);
        System.out.println("Ngày sinh: " + dateOfBirth);
        System.out.println("Nơi sinh: " + placeOfBirth);
        System.out.println("Tiểu sử và sự nghiệp: " + biography);
        System.out.println("Danh sách bài hát: ");
        for (SongModel song : songList) {
            System.out.println(song.getTitle());
        }
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

    public SingerModel toObject(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(json, this.getClass());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public LinkedList<String> getAliases() {
        return aliases;
    }

    public void setAliases(LinkedList<String> aliases) {
        this.aliases = aliases;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getSongs() {
        return songs;
    }

    public void setSongs(String songs) {
        this.songs = songs;
    }
}
