package camp.xit.jacod.jcache;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.DataProvider;
import java.time.Duration;
import javax.cache.Cache;

public class JCacheCodelistClient {

    public static class Builder extends camp.xit.jacod.CodelistClient.Builder<Builder> {

        protected boolean reloadReferences = false;
        protected boolean reloadDependencies = true;
        protected Cache<String, Codelist<CodelistEntry>> cache;
        protected Duration expiryTime = Duration.ofMinutes(10);


        public Builder(Cache cache) {
            this.cache = cache;
        }


        @Override
        public CodelistClient build() {
            if (dataProvider == null) {
                throw new IllegalArgumentException("No DataProvider provided!");
            }
            if (cache == null) {
                throw new IllegalArgumentException("No JCache provided!");
            }
            if (prefetchedCodelists == null) {
                prefetchedCodelists = dataProvider.readAllNames();
            }
            return new JCacheCodelistClientImpl(dataProvider, cache, expiryTime, prefetchedCodelists,
                    whitelistPackages, shallowReferences, reloadDependencies);
        }


        /**
         * Set instance of data provider implementation and wrap it with {@link CachedDataProvider}.It caches
         * all provider responses for time defined by {@link #withExpiryTime(java.time.Duration)} method.This
         * is mandatory attribute. Application throws {@link IllegalArgumentException} when no data provider
         * is set. Be aware that provider cache must me different instance than codelist cache.
         *
         * @param dataProvider data provider
         * @param providerCache
         * @return builder
         */
        public Builder withCachedDataProvider(DataProvider dataProvider, Cache providerCache) {
            this.dataProvider = new CachedDataProvider(dataProvider, providerCache);
            return this;
        }


        /**
         * Set expiry time.Default value is 10 minutes.It means that every codelist expired in this time and
         * will be refreshed when it was changed.This setting is applicable only for
         * {@link Cache2kCodelistClientImpl} implementation.
         *
         * @param expiryTime
         * @return builder
         */
        public Builder withExpiryTime(Duration expiryTime) {
            this.expiryTime = expiryTime;
            return this;
        }


        /**
         * Reload all codelists that contain references to changed codelist.This configuration is applied
         * only to {@link JCacheCodelistClientImpl}. Default value: false
         *
         * @return
         */
        public Builder reloadReferences() {
            this.reloadReferences = true;
            return this;
        }


        /**
         * Don't reload all referenced codelists (transitive dependencies) from changed codelist.This
         * configuration is applied only to {@link JCacheCodelistClientImpl}. Default value: true
         *
         * @return
         */
        public Builder withoutReloadDependecies() {
            this.reloadDependencies = false;
            return this;
        }
    }
}
