package camp.xit.jacod.cache2k;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.provider.DataProvider;
import java.time.Duration;

public class Cache2kCodelistClient {

    public static class Builder extends camp.xit.jacod.CodelistClient.Builder<Builder> {

        protected Duration expiryTime = Duration.ofMinutes(10);
        protected boolean reloadReferences = false;
        protected boolean reloadDependencies = true;
        protected int loaderThreadCount = 0;


        @Override
        public CodelistClient build() {
            if (dataProvider == null) {
                throw new IllegalArgumentException("No DataProvider provided!");
            }
            if (prefetchedCodelists == null) {
                prefetchedCodelists = dataProvider.getCodelistNames();
            }
            return new Cache2kCodelistClientImpl(dataProvider, prefetchedCodelists, expiryTime,
                    whitelistPackages, shallowReferences, reloadReferences, reloadDependencies,
                    loaderThreadCount);
        }


        /**
         * Set instance of data provider implementation and wrap it with {@link CachedDataProvider}. It caches
         * all provider responses for time defined by {@link #withExpiryTime(java.time.Duration)} method. This
         * is mandatory attribute. Application throws {@link IllegalArgumentException} when no data provider
         * is set.
         *
         * @param dataProvider data provider
         * @return builder
         */
        @Override
        public Builder withDataProvider(DataProvider dataProvider) {
            this.dataProvider = new CachedDataProvider(dataProvider, expiryTime);
            return this;
        }


        /**
         * Set instance of data provider implementation and wrap it with {@link CachedDataProvider}. It caches
         * all provider responses for defined time period. This is mandatory attribute. Application throws
         * {@link IllegalArgumentException} when no data provider is set.
         *
         * @param dataProvider data provider
         * @return builder
         */
        public Builder withDataProvider(DataProvider dataProvider, Duration expiryTime) {
            this.dataProvider = new CachedDataProvider(dataProvider, expiryTime);
            return this;
        }


        /**
         * Set expiry time. Default value is 10 minutes. It means that every codelist expired in this time and
         * will be refreshed when it was changed. This setting is applicable only for
         * {@link Cache2kCodelistClientImpl} implementation.
         *
         * @param time time value
         * @param unit time unit
         * @return builder
         */
        public Builder withExpiryTime(Duration expiryTime) {
            this.expiryTime = expiryTime;
            return this;
        }


        /**
         * Reload all codelists that contain references to changed codelist. This configuration is applied
         * only to
         * {@link Cache2kCodelistClientImpl}. Default value: false
         */
        public Builder reloadReferences() {
            this.reloadReferences = true;
            return this;
        }


        /**
         * Don't reload all referenced codelists (transitive dependencies) from changed codelist. This
         * configuration is applied only to {@link Cache2kCodelistClientImpl}. Default value: true
         */
        public Builder withoutReloadDependecies() {
            this.reloadDependencies = false;
            return this;
        }


        public Builder withLoaderThreadCount(int loaderThreadCount) {
            this.loaderThreadCount = loaderThreadCount;
            return this;
        }
    }
}
