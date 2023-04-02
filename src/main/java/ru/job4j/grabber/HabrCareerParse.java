package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private static String retrieveDescription(String link) throws IOException {
        StringBuilder description = new StringBuilder();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        description.append(document.select(".vacancy-description__text").text());
        return description.toString();
    }

    public Post getPost(Element element) throws IOException {
        Post post = new Post();
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element dateElement = element.select(".vacancy-card__date").first().child(0);
        String vacancyName = titleElement.text();
        String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String date = dateElement.attr("datetime");
        LocalDateTime dateTime = dateTimeParser.parse(date);
        String descriptions = retrieveDescription(link);
        post.setTitle(vacancyName);
        post.setLink(link);
        post.setDescription(descriptions);
        post.setCreated(dateTime);
        return post;
    }

    @Override
    public List<Post> list(String link) throws IOException {
        var listPosts = new ArrayList<Post>();
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(link + i);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                try {
                    listPosts.add(getPost(row));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return listPosts;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> list = habrCareerParse.list(PAGE_LINK);
        for (Post post : list) {
            System.out.println(post);
        }
    }
}