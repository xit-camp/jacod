package camp.xit.jacod.cache2k;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.cache2k.model.Title;
import camp.xit.jacod.cache2k.test.SimpleCsvDataProvider;
import camp.xit.jacod.model.Codelist;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheReloadTest {

    private static final Logger LOG = LoggerFactory.getLogger(CacheReloadTest.class);
    private static final int CACHE_EXPIRY_TIME = 1;
    private static final int WAIT_TIME = 3;


    @Test
    void reloadFiles() throws IOException {
        System.out.println("URLS: " + loadResources("META-INF/codelist-mappers", null));
    }


    @Test
    void reloadCache() throws Exception {
        CodelistClient client = new Cache2kCodelistClient.Builder()
                .withDataProvider(new SimpleCsvDataProvider())
                .codelists(Title.class)
                .withExpiryTime(Duration.ofSeconds(CACHE_EXPIRY_TIME))
                .withPrefetched("Title").build();

        Codelist<Title> titles = client.getCodelist(Title.class);
        LOG.info("Waiting for {} seconds", WAIT_TIME);
        Thread.sleep(WAIT_TIME * 1000);
        titles = client.getCodelist(Title.class);
        assertThat(titles.size(), greaterThan(0));
    }


    @Test
    void equalEntries() throws Exception {
        CodelistClient client = new CodelistClient.Builder()
                .withDataProvider(new SimpleCsvDataProvider())
                .codelists(Title.class)
                .withPrefetched("Title").build();
        Title dr1 = client.getCodelist(Title.class).getEntry("ThDr.");
        client.clearCache();
        LOG.info("Waiting for {} seconds", WAIT_TIME);
        Thread.sleep(WAIT_TIME * 1000);
        Title dr2 = client.getCodelist(Title.class).getEntry("ThDr.");
        assertEquals(dr1, dr2);
    }


    public static List<URL> loadResources(final String name, final ClassLoader classLoader) throws IOException {
        final List<URL> list = new ArrayList<>();
        final ClassLoader cl = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        final Enumeration<URL> systemResources = cl.getResources(name);
        while (systemResources.hasMoreElements()) {
            list.add(systemResources.nextElement());
        }
        return list;
    }
}
