package camp.xit.jacod.spring.cache;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.provider.DataProvider;
import org.springframework.cache.Cache;

public class SpringCacheCodelistClient {

    public static class Builder extends camp.xit.jacod.CodelistClient.Builder<Builder> {

        protected boolean reloadReferences = false;
        protected boolean reloadDependencies = true;
        protected Cache cache;


        public Builder(Cache cache) {
            this.cache = cache;
        }


        @Override
        public CodelistClient build() {
            if (dataProvider == null) {
                throw new IllegalArgumentException("No DataProvider provided!");
            }
            if (cache == null) {
                throw new IllegalArgumentException("No Spring Cache provided!");
            }
            if (prefetchedCodelists == null) {
                prefetchedCodelists = dataProvider.getCodelistNames();
            }
            return new SpringCacheCodelistClientImpl(dataProvider, cache, prefetchedCodelists,
                    getMappersReg(), shallowReferences, reloadDependencies);
        }


        /**
         * Set instance of data provider implementation and wrap it with {@link CachedDataProvider}.It caches
         * all provider responses for time defined by cache implementation configuration. Application throws
         * {@link IllegalArgumentException} when no data provider is set. Be aware that provider cache must me
         * different instance than codelist cache.
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
         * Reload all codelists that contain references to changed codelist.This configuration is applied
         * only to {@link SpringCacheCodelistClientImpl}. Default value: false
         *
         * @return
         */
        public Builder reloadReferences() {
            this.reloadReferences = true;
            return this;
        }


        /**
         * Don't reload all referenced codelists (transitive dependencies) from changed codelist.This
         * configuration is applied only to {@link SpringCacheCodelistClientImpl}. Default value: true
         *
         * @return
         */
        public Builder withoutReloadDependecies() {
            this.reloadDependencies = false;
            return this;
        }
    }
}
