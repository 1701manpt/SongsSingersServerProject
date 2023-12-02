package sgu.ltudm.songssingersserverproject.models;

public enum TbmGoogleSearch {
    VIDEO("vid"),
    ALL("");

    private final String value;

    TbmGoogleSearch(String value) {
        this.value = value;
    }

    public String get() {
        return value;
    }
}
