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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class SingerServer {
    private final SecretKey keyAES;

    public SingerServer(SecretKey keyAES) {
        this.keyAES = keyAES;
    }

    public void requestSingerByName(String singerNameKeyword, BufferedWriter out) throws Exception {
        SingerModel singer = new SingerModel();

        GoogleSearch ggSearch = new GoogleSearch(singerNameKeyword, null, null);
        String singerName = ggSearch.getKeywordCorrect("ca sĩ");
        singer.setName(singerName);

        int index = 0;
        String linkWikipedia = getLinkWikipediaBySingerName(singerName, index);
        String titleWiki = getTitleWikipediaByLinkWikipedia(linkWikipedia);

        String wikibaseItem = getWikibaseItemByTitleWikipedia(titleWiki);

        LinkedHashMap<String, String> infoFromWiki = getInfo(wikibaseItem);
        singer.setDateOfBirth(infoFromWiki.get("dateOfBirth"));
        singer.setPlaceOfBirth(infoFromWiki.get("placeOfBirth"));

        LinkedHashMap<String, String> info = getArtistId(singerName);

        String artistId = info.get("artistId");

        LinkedList<SongModel> songs = getSongListByArtistIdFromZingMp3(artistId, singerName);
        singer.setSongList(songs);

        String biography = "";
        boolean hasDesc = isSameSinger(linkWikipedia, getTitleByTitleWikipedia(titleWiki), getInfoArtistByAlias(info.get("alias")).get("realname"));

        if (hasDesc) {
            biography = getDescriptionByTitleWikipedia(titleWiki);
            biography = getBiography(biography);
        }

        if (!biography.isEmpty()) {
            singer.setBiography(biography);
        } else {
            singer.setBiography(getInfoArtistByAlias(info.get("alias")).get("biography"));
        }

        String json = singer.toJson();

        out.write(AESEncryption.encrypt(json, keyAES));
        out.newLine();
        out.flush();

        singer.log();
    }

    private String getBiography(String description) {
        String biography = "";

        LinkedList<String> list = new LinkedList<>();
        list.add(description.trim().split("==")[0].trim());

        list.add(description.trim().split("==.*==")[1].trim());

        biography = String.join("\n", list);

        biography = description;
        return biography;
    }

    private LinkedHashMap<String, String> getArtistId(String singerName) throws Exception {
        try {
            LinkedHashMap<String, String> info = new LinkedHashMap<>();
            String artistId = "";

            String json = (new ZingMp3Api()).searchArtist(singerName);
            JSONObject object = new JSONObject(json);

            JSONObject data = object.optJSONObject("data");
            JSONArray items = data.optJSONArray("items");

            JSONObject item = items.getJSONObject(0);

            info.put("artistId", item.optString("id"));
            info.put("alias", item.optString("alias"));

            return info;
        } catch (Exception e) {
            throw new Exception("Không tìm thấy ca sĩ");
        }
    }

    private LinkedList<SongModel> getSongListByArtistIdFromZingMp3(String artistId, String singerName) {
        LinkedList<SongModel> songList = new LinkedList<>();

        try {
            JSONObject object = new JSONObject((new ZingMp3Api()).getSongList(artistId, null, null));

            JSONObject dataJsonObject = object.getJSONObject("data");
            int total = dataJsonObject.getInt("total");

            object = new JSONObject((new ZingMp3Api()).getSongList(artistId, null, total));

            dataJsonObject = object.getJSONObject("data");

            JSONArray itemsJsonArray = dataJsonObject.getJSONArray("items");

            for (int i = 0; i < itemsJsonArray.length(); i++) {
                JSONObject item = itemsJsonArray.getJSONObject(i);
                String title = item.optString("title");

                SongModel songModel = new SongModel();
                songModel.setTitle(title);
                songModel.setArtists(singerName);

                songList.add(songModel);
            }

            List<SongModel> songsToRemove = new ArrayList<>();

            // Duyệt qua danh sách các bài hát
            for (SongModel song : songList) {
                for (SongModel otherSong : songList) {
                    if (song.getTitle() != otherSong.getTitle() && song.getTitle().contains(otherSong.getTitle()) && song.getTitle().contains("(") && song.getTitle().contains(")")) {
                        songsToRemove.add(song);
                        break; // Bỏ qua các bài hát còn lại chứa tên bài hát này
                    }
                }
            }

            // Xóa các bài hát lặp lại
            songList.removeAll(songsToRemove);

            return songList;
        } catch (Exception e) {
            return songList;
        }
    }

    //     phương thức này không lấy đủ các bài hát vì có đoạn phải thực hiện js trong nút "hiển thị thêm"
    private String getSongListFromGoogleBySingerName(String singerName) throws Exception {
        LinkedList<String> songs = new LinkedList<>();

        String q = "các bài hát của " + singerName;

        Document document = (new GoogleSearch(q, null, null)).getDocument();

        System.out.println(document.html());

        Element elementSearch = document.getElementById("search");

        Element element = elementSearch.select("div[jsname][jsslot]").first();

        if (element != null) {
            Elements gExpandableContentElements = element.select("g-expandable-content");

            System.out.println(gExpandableContentElements.size());

            System.out.println(gExpandableContentElements.get(1).select(".title").text());

            for (Element gExpandableContentElement : gExpandableContentElements) {

                if (gExpandableContentElement.hasAttr("style")) {
                    // Xóa thuộc tính style
                    System.out.println("Có style");
                    gExpandableContentElement.attr("aria-hidden", "false");
                    gExpandableContentElement.removeAttr("style");
                    System.out.println(gExpandableContentElement.html());
                }

                Elements titleElements = gExpandableContentElement.select(".title");

                System.out.println(titleElements.size());

                for (Element titleElement : titleElements) {
                    if (titleElement != null) {
                        System.out.println(titleElement.text().trim());
                    }
                }
            }
        }

        return String.join("\n", songs);
    }

    private LinkedHashMap<String, String> getInfoArtistByAlias(String alias) throws Exception {
        LinkedHashMap<String, String> info = new LinkedHashMap<>();

        String json = (new ZingMp3Api()).getArtist(alias);
        JSONObject object = new JSONObject(json);
        JSONObject data = object.optJSONObject("data");

        String[] list = (String.join("", data.optString("biography").split("\n")).split("\\."));

        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].trim();
        }

        String biography = String.join(".\n", list) + ".";

        info.put("biography", biography);
        info.put("realname", data.optString("realname"));

        return info;
    }

    // kiểm tra xem link wikipedia này có phải là cùng ca sĩ trên ZingMp3 không?
    // (để nếu wiki k tồn tại thì sẽ lấy tiểu sử trên ZingMp3, còn mặc định sẽ lấy trên wiki)
    private boolean isSameSinger(String urlWikipedia, String title, String realName) throws Exception {
        boolean isSameSinger = false;

        Document document = Jsoup.connect(urlWikipedia).get();
        Element infobox = document.getElementsByClass("infobox").first();
        Elements nickNames = infobox.getElementsByClass("nickname");

        // kiểm tra cả title lẫn nickname trên wiki, vì đôi khi cùng 1 người nhưng trên wiki hiển thị khác

        for (Element nickName : nickNames) {
            if (nickName != null) {
                if (realName.equalsIgnoreCase(nickName.text())) {
                    isSameSinger = true;
                    break;
                }
            }
        }

        if (title != null) {
            if (realName.equalsIgnoreCase(title)) {
                isSameSinger = true;
            }
        }

        return isSameSinger;
    }

    private String getLinkWikipediaBySingerName(String singerName, int index) throws Exception {
        String linkWikipedia = "";

        Document document = (new GoogleSearch(singerName, "https://vi.wikipedia.org", null)).getDocument();
        Elements elements = document.getElementById("rso").children();

        Element element = elements.get(index);

        Element aElement = element.select("a").first();

        linkWikipedia = aElement.attr("href");

        System.out.println("Link Wikipedia: " + linkWikipedia);

        return linkWikipedia;
    }

    private String getTitleWikipediaByLinkWikipedia(String linkWikipedia) {
        String titleWikipedia = "";

        titleWikipedia = linkWikipedia.split("/")[linkWikipedia.split("/").length - 1];

        return titleWikipedia;
    }

    private String getTitleByTitleWikipedia(String titleWikipedia) throws Exception {
        String title = "";

        // https://vi.wikipedia.org/api/rest_v1/page/summary/S%C6%A1n_T%C3%B9ng_M-TP

        String url = "https://vi.wikipedia.org/api/rest_v1/page/summary/" + titleWikipedia;

        System.out.println("Url lấy wikibase item: " + url);

        Document document = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).get();
        JSONObject object = new JSONObject(document.body().text());
        title = object.optString("title");

        return title;
    }

    private String getWikibaseItemByTitleWikipedia(String titleWikipedia) throws Exception {
        String wikibaseItem = "";

        // https://vi.wikipedia.org/api/rest_v1/page/summary/S%C6%A1n_T%C3%B9ng_M-TP

        String url = "https://vi.wikipedia.org/api/rest_v1/page/summary/" + titleWikipedia;

        System.out.println("Url lấy wikibase item: " + url);

        Document document = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).get();
        JSONObject object = new JSONObject(document.body().text());
        wikibaseItem = object.optString("wikibase_item");

        return wikibaseItem;
    }

    private String getPageIdByTitleWikipedia(String titleWikipedia) throws Exception {
        String pageId = "";

        // https://vi.wikipedia.org/api/rest_v1/page/summary/S%C6%A1n_T%C3%B9ng_M-TP

        String url = "https://vi.wikipedia.org/api/rest_v1/page/summary/" + titleWikipedia;

        System.out.println("Url lấy page id: " + url);

        Document document = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).get();
        JSONObject object = new JSONObject(document.body().text());
        pageId = object.optString("pageid");

        return pageId;
    }

    private String getDescriptionByTitleWikipedia(String titleWikipedia) throws Exception {
        String description = "";

        // https://vi.wikipedia.org/w/api.php?action=query&prop=extracts&explaintext=0&exsectionformat=wiki&format=json&titles=Soobin_Ho%C3%A0ng_S%C6%A1n

        String url = "https://vi.wikipedia.org/w/api.php?action=query&prop=extracts&explaintext=0&exsectionformat=wiki&format=json&titles=" + titleWikipedia;

        System.out.println("Url lấy thông tin ca sĩ: " + url);

        Document document = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).get();
        JSONObject object = new JSONObject(document.body().text());

        JSONObject queryJsonObject = object.optJSONObject("query", null);
        JSONObject pagesJsonObject = queryJsonObject.optJSONObject("pages", null);
        JSONObject pageJsonObject = pagesJsonObject.optJSONObject(getPageIdByTitleWikipedia(titleWikipedia), null);
        description = pageJsonObject.optString("extract");

        return description;
    }

    private LinkedHashMap<String, String> getInfo(String wikibaseItem) {
        LinkedHashMap<String, String> info = new LinkedHashMap<>();

        try {
            // https://www.wikidata.org/wiki/Q17450386
            String url = "https://www.wikidata.org/wiki/" + wikibaseItem;

            System.out.println("Url lấy thông tin ca sĩ trên wiki: " + url);

            Document document = Jsoup.connect(url).ignoreContentType(true).get();

            // id này là date of birth và place of birth
            Element dateOfBirthElement = document.getElementById("P569");
            Element placeOfBirthElement = document.getElementById("P19");

            Element dateOfBirthEl = dateOfBirthElement.select(".wikibase-snakview-body").first();
            Element placeOfBirthEl = placeOfBirthElement.select(".wikibase-snakview-body").first();

            String dateOfBirth = "";
            String placeOfBirth = "";

            dateOfBirth = convertDateStringToString(dateOfBirthEl.text());
            placeOfBirth = standardizedPlace(placeOfBirthEl.text());

            if (dateOfBirth.isEmpty()) {
                dateOfBirth = dateOfBirthEl.text();
            }

            if (placeOfBirth.isEmpty()) {
                placeOfBirth = placeOfBirthEl.text();
            }

            info.put("dateOfBirth", dateOfBirth);
            info.put("placeOfBirth", placeOfBirth);

            return info;
        } catch (Exception e) {
            return info;
        }
    }

    private String convertDateStringToString(String input) {
        String formattedDate = "";

        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            LocalDate date = LocalDate.parse(input, inputFormatter);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("'ngày' dd 'tháng' MM 'năm' yyyy");
            formattedDate = date.format(outputFormatter);

            return formattedDate;
        } catch (Exception e) {
            return formattedDate;
        }
    }

    private String standardizedPlace(String input) {
        String place = "";

        try {
            String q = input;

            GoogleSearch ggSearch = new GoogleSearch(q, null, null);

            place = ggSearch.getKeywordCorrect("");

            return place;
        } catch (Exception e) {
            return place;
        }
    }
}
