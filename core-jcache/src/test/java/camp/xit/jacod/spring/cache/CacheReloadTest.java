package camp.xit.jacod.spring.cache;

import camp.xit.jacod.jcache.JCacheCodelistClient;
import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.spring.cache.model.Title;
import camp.xit.jacod.spring.cache.test.SimpleCsvDataProvider;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import java.time.Duration;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheReloadTest {

    private static final Logger LOG = LoggerFactory.getLogger(CacheReloadTest.class);
    private static final int CACHE_EXPIRY_TIME = 1;
    private static final int WAIT_TIME = 3;


    @Test
    void reloadCache() throws Exception {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();
        MutableConfiguration<String, Codelist<CodelistEntry>> config = new MutableConfiguration<>();
        Cache<String, Codelist<CodelistEntry>> cache = cacheManager.createCache("reloadCache", config);
        assertNotNull(cache);

        CodelistClient client = new JCacheCodelistClient.Builder(cache)
                .withDataProvider(new SimpleCsvDataProvider())
                .addScanPackages(Title.class.getPackageName())
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
                .addScanPackages(Title.class.getPackageName())
                .withPrefetched("Title").build();
        Title dr1 = client.getCodelist(Title.class).getEntry("ThDr.");
        client.clearCache();
        LOG.info("Waiting for {} seconds", WAIT_TIME);
        Thread.sleep(WAIT_TIME * 1000);
        Title dr2 = client.getCodelist(Title.class).getEntry("ThDr.");
        assertEquals(dr1, dr2);
    }
}
