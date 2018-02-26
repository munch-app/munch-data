package munch.data.assumption;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import munch.data.elastic.ElasticIndex;
import munch.data.utils.ScheduledThreadUtils;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Fuxing
 * Date: 26/2/18
 * Time: 6:51 PM
 * Project: munch-data
 */
public class CachedAssumptionDatabase extends AssumptionDatabase {

    private final LoadingCache<String, Map<String, Assumption>> cache = CacheBuilder.newBuilder()
            .maximumSize(2)
            .build(new CacheLoader<String, Map<String, Assumption>>() {
                public Map<String, Assumption> load(String key) {
                    return CachedAssumptionDatabase.super.get();
                }
            });

    @Inject
    public CachedAssumptionDatabase(ElasticIndex elasticIndex) {
        super(elasticIndex);
        cache.refresh("cache");
        ScheduledThreadUtils.schedule(() -> cache.refresh("cache"), 8, TimeUnit.HOURS);
    }

    @Override
    public Map<String, Assumption> get() {
        try {
            return cache.get("cache");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
