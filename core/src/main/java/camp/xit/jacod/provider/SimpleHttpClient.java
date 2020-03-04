package camp.xit.jacod.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import static java.time.Duration.ofMinutes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class SimpleHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpClient.class);

    private final HttpClient httpClient;

    private static volatile SimpleHttpClient instance = null;


    private SimpleHttpClient() {
        httpClient = HttpClient.newHttpClient();
    }


    public <T> T doGet(final URI uri, ClientCallback<T> callback) throws IOException {
        return doGet(uri, -1, callback);
    }


    public <T> T doGet(final URI uri, long lastReadTime, ClientCallback<T> callback) throws IOException {

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_1_1)
                    .uri(uri).GET().timeout(ofMinutes(10));
            LOG.trace("Reading URL: {} LastReadTime: {}", uri, lastReadTime);
            if (lastReadTime > -1) {
                ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastReadTime), ZoneId.systemDefault());
                requestBuilder.header("If-Modified-Since", date.format(DateTimeFormatter.RFC_1123_DATE_TIME));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("URI: {}, If-Modified-Since: {}", uri, date.format(DateTimeFormatter.RFC_1123_DATE_TIME));
                }
            }
            HttpRequest request = requestBuilder.build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            LOG.debug("Request {}, Status: {}", uri, response.statusCode());
            switch (response.statusCode()) {
            case 200:
                try (InputStream in = response.body()) {
                return callback.process(in);
            }
            case 304:
                throw new ResourceNotChangedException(uri.toString());
            case 404:
                throw new ResourceNotFoundException(uri.toString());
            default:
                throw new IOException("Invalid http response from server " + response.statusCode() + ", URL: " + uri);
            }
        } catch (InterruptedException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }


    public String doGetString(URI url) throws IOException {
        return doGet(url, in -> new Scanner(in, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next());
    }

    public static interface ClientCallback<T> {

        T process(InputStream in) throws IOException, SAXException, ParserConfigurationException;
    }


    public static SimpleHttpClient httpClient() {
        if (instance == null) {
            synchronized (SimpleHttpClient.class) {
                if (instance == null) {
                    instance = new SimpleHttpClient();
                }
            }
        }

        return instance;
    }
}
