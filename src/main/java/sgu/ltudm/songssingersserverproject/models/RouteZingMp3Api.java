package sgu.ltudm.songssingersserverproject.models;

public enum RouteZingMp3Api {
    GET_SONG("/api/v2/page/get/song"),
    GET_SONG_STREAMING("/api/v2/song/get/streaming"),
    GET_SONG_LIST("/api/v2/song/get/list"),
    GET_PLAYLIST("/api/v2/page/get/playlist"),
    SEARCH("/api/v2/search"),
    GET_ARTIST("/api/v2/page/get/artist"),
    GET_LYRIC("/api/v2/lyric/get/lyric");

    private final String routeValue;

    RouteZingMp3Api(String routeValue) {
        this.routeValue = routeValue;
    }

    public String get() {
        return routeValue;
    }
}
