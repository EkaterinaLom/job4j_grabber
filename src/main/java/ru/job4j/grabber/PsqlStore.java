package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.username"),
                    cfg.getProperty("jdbc.password")
            );
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement =
                cnn.prepareStatement("INSERT INTO post (name, text, link, created) VALUES (?, ?, ?, ?)"
                        + " ON CONFLICT (link) DO NOTHING", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.executeUpdate();
            try (ResultSet key = statement.getGeneratedKeys()) {
                while (key.next()) {
                    post.setId(key.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(new Post(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("link"),
                            resultSet.getString("text"),
                            resultSet.getTimestamp("created").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) throws SQLException {
        var resultPost = new Post();
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM post WHERE ID = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultPost.setId(resultSet.getInt("id"));
                    resultPost.setTitle(resultSet.getString("name"));
                    resultPost.setDescription(resultSet.getString("text"));
                    resultPost.setLink(resultSet.getString("link"));
                    resultPost.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return resultPost;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> listPost = habrCareerParse.list("https://career.habr.com/vacancies/java_developer?page=");
        try (InputStream inputStream = new FileInputStream("src/main/resources/post.properties")) {
            Properties config = new Properties();
            config.load(inputStream);
            PsqlStore store = new PsqlStore(config);
            for (Post post : listPost) {
                store.save(post);
            }
            List<Post> post = store.getAll();
            post.forEach(System.out::println);
            var postFound = store.findById(624);
            System.out.println(postFound);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
