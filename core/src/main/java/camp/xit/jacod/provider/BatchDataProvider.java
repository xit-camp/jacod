package camp.xit.jacod.provider;

import java.time.Duration;
import static java.time.Duration.ofMinutes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BatchDataProvider implements DataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BatchDataProvider.class);

    protected Map<String, List<EntryData>> shortTermCache;
    protected final Duration holdValuesTimeout;
    private long lastReadTime;
    private final static ScheduledExecutorService CACHE_CLEAN_SCHEDULER = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<Void> cacheCleaner;


    public BatchDataProvider() {
        this(ofMinutes(1));
    }


    public BatchDataProvider(Duration holdValuesTimeout) {
        this.holdValuesTimeout = holdValuesTimeout;
        shortTermCache = new HashMap<>();
    }


    /**
     * Use this method, if you prefer to read all codelist values in one batch.
     *
     * @return all codelist values
     */
    protected abstract Map<String, List<EntryData>> readEntriesBatch();


    @Override
    public final Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        if (!inTime()) readEntriesBatchInternal();
        return ofNullable(shortTermCache.get(codelist));
    }


    private synchronized void readEntriesBatchInternal() {
        shortTermCache = readEntriesBatch();
        if (!cacheCleaner.isDone()) cacheCleaner.cancel(false);
        lastReadTime = System.currentTimeMillis();
        cacheCleaner = CACHE_CLEAN_SCHEDULER.schedule(() -> clearShortTermCache(), holdValuesTimeout.toMillis(), TimeUnit.MILLISECONDS);
    }


    private synchronized Void clearShortTermCache() {
        shortTermCache.clear();
        LOG.info("Short term cache for batch data provider {} was cleared by scheduled job after {}", getName(), holdValuesTimeout);
        return null;
    }


    private boolean inTime() {
        return System.currentTimeMillis() - lastReadTime <= holdValuesTimeout.toMillis();
    }
}
