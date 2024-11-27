package camp.xit.jacod.provider;

import static java.time.Duration.ofMinutes;
import static java.util.Optional.ofNullable;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BatchDataProvider implements DataProvider, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(BatchDataProvider.class);

    protected final static Duration DEFAULT_HOLD_VALUES_TIMEOUT = ofMinutes(1);

    protected Map<String, List<EntryData>> shortTermCache;

    protected final Duration holdValuesTimeout;
    private long lastReadTime;

    private ScheduledExecutorService refreshScheduler;
    private ScheduledFuture<Void> refreshFuture;


    public BatchDataProvider() {
        this(DEFAULT_HOLD_VALUES_TIMEOUT);
    }


    public BatchDataProvider(Duration holdValuesTimeout) {
        this.holdValuesTimeout = holdValuesTimeout;
        refreshScheduler = Executors.newScheduledThreadPool(1);
    }


    /**
     * Use this method, if you prefer to read all codelist values in one batch.
     *
     * @return all codelist values. should alwas be not null.
     */
    protected abstract Map<String, List<EntryData>> readEntriesBatch();


    @Override
    public Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        if (shortTermCache == null) {
            synchronized (this) {
                if (shortTermCache == null) {
                    shortTermCache = readEntriesBatch();
                    lastReadTime = System.currentTimeMillis();
                    var currentRefreshFuture = refreshFuture; // different reference than refreshFuture pointing to instance future varieble object
                    refreshFuture = refreshScheduler.schedule(() -> refreshCache(currentRefreshFuture), holdValuesTimeout.toMillis(), TimeUnit.MILLISECONDS);
                    LOG.debug("first refresh activity scheduled: {}", refreshFuture);
                }
            }
        }
        return ofNullable(shortTermCache.get(codelist));
    }


    private synchronized Void refreshCache(ScheduledFuture<Void> previousRefreshFuture) {
        try {
            if (previousRefreshFuture != null && ! previousRefreshFuture.isDone()) {
                previousRefreshFuture.cancel(false);
                LOG.warn("previous refresh activity ({} ms) cancelled: {}", lastReadTime, previousRefreshFuture);
            }        
            shortTermCache = readEntriesBatch();
            lastReadTime = System.currentTimeMillis();
            LOG.debug("short term cache for batch data provider {} was refreshed by scheduled job after {}. previous successful refresh time was {}ms.",
                getName(), holdValuesTimeout, lastReadTime);
        } catch (Exception ise) {
            LOG.error("unable to refresh cache", ise);
        } finally {
            var currentRefreshFuture = refreshFuture; // different reference than refreshFuture pointing to instance future varieble object
            refreshFuture = refreshScheduler.schedule(() -> refreshCache(currentRefreshFuture), holdValuesTimeout.toMillis(), TimeUnit.MILLISECONDS);
            LOG.debug("refresh activity scheduled: {}", refreshFuture);
        }
        return null;
    }


    @Override
    public void close() throws IOException {
        refreshScheduler.shutdownNow();
    }
}
