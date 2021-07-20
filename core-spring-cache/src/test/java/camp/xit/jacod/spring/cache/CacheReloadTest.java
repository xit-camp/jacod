package camp.xit.jacod.spring.cache;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.spring.cache.model.Title;
import camp.xit.jacod.spring.cache.test.SimpleCsvDataProvider;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.caffeine.CaffeineCacheManager;

public class CacheReloadTest {

    private static final Logger LOG = LoggerFactory.getLogger(CacheReloadTest.class);
    private static final Duration CACHE_EXPIRY_TIME = Duration.ofSeconds(1);
    private static final int WAIT_TIME = 3;


    @Test
    void reloadCache() throws Exception {
        Caffeine caffeine = Caffeine.newBuilder().expireAfterWrite(CACHE_EXPIRY_TIME).maximumSize(100);
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);

        CodelistClient client = new SpringCacheCodelistClient.Builder(cacheManager.getCache("reloadCache"))
                .withDataProvider(new SimpleCsvDataProvider())
                .addScanPackages(Title.class.getPackage())
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
                .addScanPackages(Title.class.getPackage())
                .withPrefetched("Title").build();
        Title dr1 = client.getCodelist(Title.class).getEntry("ThDr.");
        client.clearCache();
        LOG.info("Waiting for {} seconds", WAIT_TIME);
        Thread.sleep(WAIT_TIME * 1000);
        Title dr2 = client.getCodelist(Title.class).getEntry("ThDr.");
        assertEquals(dr1, dr2);
    }
}
