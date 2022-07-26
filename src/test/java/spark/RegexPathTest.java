package spark;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import spark.util.SparkTestUtil;

import static spark.Spark.*;

/**
 * @since 2022/07/26.
 */
public class RegexPathTest {
    public static final int PORT = 4567;

    private static final SparkTestUtil http = new SparkTestUtil(PORT);
    private static final Map<String, Book> books = new HashMap<>();

    @BeforeClass
    public static void setup() throws IOException {
        books.put("9999", new Book("Mile Kami", "The Spark"));
        get("~/\\/(\\d+)-([^.]+)\\.book", (request, response) -> {
            Book book = books.get(request.params(1));
            System.out.printf("The book title in the URL is: %s%n", request.params(2));
            if (book != null) {
                System.out.printf("The book title in is: %s%n", book.title);
                return "Title: " + book.getTitle() + ", Author: " + book.getAuthor();
            } else {
                response.status(404); // 404 Not found
                return "Book not found";
            }
        });
        get("~/(?<name>books)\\/(?<book>\\d+)", (request, response) -> {
            Book book = books.get(request.params("book"));
            System.out.printf("The book id is: %s%n", request.params("book"));
            if (book != null) {
                System.out.printf("The book title in is: %s%n", book.title);
                return "Title: " + book.getTitle() + ", Author: " + book.getAuthor();
            } else {
                response.status(404); // 404 Not found
                return "Book not found";
            }
        });
        Spark.awaitInitialization();
    }

    @Test
    public final void groupsShouldBeAvailableInParams() {
        try {
            Map<String, String> requestHeader = new HashMap<>();
            requestHeader.put("Host", "localhost:" + PORT);
            requestHeader.put("User-Agent", "curl/7.55.1");
            String path = "/9999-the-spark.book";
            http.doMethod("GET", path, "", false, "*/*", requestHeader);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public final void namedGroupsShouldBeAvailableInParams() {
        try {
            Map<String, String> requestHeader = new HashMap<>();
            requestHeader.put("Host", "localhost:" + PORT);
            requestHeader.put("User-Agent", "curl/7.55.1");
            String path = "/books/9999";
            http.doMethod("GET", path, "", false, "*/*", requestHeader);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static class Book {

        public String author, title;

        public Book(String author, String title) {
            this.author = author;
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
