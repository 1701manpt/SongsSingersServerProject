package sgu.ltudm.songssingersserverproject.models;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleSearch {
    private final String str;
    private final String url;
    private final Document document;
    private String site = "";
    private TbmGoogleSearch tbm = TbmGoogleSearch.ALL;

    public GoogleSearch(String str, @Nullable String site, @Nullable TbmGoogleSearch tbm) throws Exception {
        String tbmParam = "";
        String siteParam = "";

        this.str = URLEncoder.encode(" " + str, StandardCharsets.UTF_8);

        if (site != null) {
            this.site = site;
            siteParam = "site:" + site;
        }

        if (tbm != null) {
            this.tbm = tbm;
            tbmParam = "&tbm=" + tbm.get();
        }

        url = "https://www.google.gg/search?q=" + siteParam + this.str + tbmParam;

        // Thiết lập tùy chọn cho việc theo dõi phần tử ẩn
        document = Jsoup.connect(url).get();
    }

    public void search() {
        System.out.println("URL search " + url);
    }

    public Document getDocument() {
        search();

        return document;
    }

    public String getKeywordCorrect(String role) throws Exception {
        search();
        String title = "";

        try {
            Element element = document.select("[data-attrid=title]").first();
            Element subtitle = document.select("[data-attrid=subtitle]").first();

            if (subtitle != null) {
                if (!subtitle.text().toLowerCase().contains(role.toLowerCase())) {
                    throw new Exception("Từ khóa tìm kiếm không đúng");
                }
            } else {
                throw new Exception("Từ khóa tìm kiếm không đúng");
            }

            title = element.text().trim().split("\\(")[0].trim();

            System.out.println("Keyword correct: " + title);
        } catch (Exception e) {
            throw new Exception("Từ khóa tìm kiếm không đúng");
        }

        return title;
    }
}
