package munch.data.place;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import munch.data.brand.Brand;
import munch.data.client.BrandClient;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Fuxing
 * Date: 19/8/18
 * Time: 11:20 PM
 * Project: munch-data
 */
@Singleton
public final class AirtableBrandMapper {
    private static final Logger logger = LoggerFactory.getLogger(AirtableBrandMapper.class);

    private final AirtableApi.Table brandTable;
    private final AirtableTagMapper tagMapper;

    private final LoadingCache<String, Brand> loadingCache;

    private final Map<String, Pair<String, Brand>> brandMap = new HashMap<>();

    @Inject
    public AirtableBrandMapper(AirtableApi api, AirtableTagMapper tagMapper, BrandClient brandClient) {
        this.brandTable = api.base("appDcx5b3vgkhcYB5").table("Brand");
        this.tagMapper = tagMapper;
        this.loadingCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(CacheLoader.from(brandClient::get));
    }

    public JsonNode mapField(Place.Brand brand) {
        ArrayNode fields = JsonUtils.createArrayNode();
        if (brand == null) return fields;
        fields.add(brandMap.computeIfAbsent(brand.getBrandId(), s -> find(brand)).getLeft());
        return fields;
    }

    private Pair<String, Brand> find(Place.Brand placeBrand) {
        Brand brand = loadingCache.getUnchecked(placeBrand.getBrandId());
        List<AirtableRecord> records = brandTable.find("brandId", brand.getBrandId());

        AirtableRecord record;
        if (records.size() == 0) record = new AirtableRecord();
        else record = records.get(0);

        for (int i = 1; i < records.size(); i++) {
            // Auto Cleanup
            brandTable.delete(records.get(i).getId());
            sleep();
        }

        if (equals(brand, record)) return Pair.of(record.getId(), brand);

        record.setFields(new HashMap<>());
        record.putField("brandId", brand.getBrandId());
        record.putField("name", brand.getName());
        record.putField("tags", tagMapper.mapField(brand));
        record.putFieldDate("updatedMillis", brand.getUpdatedMillis());

        if (record.getId() == null) {
            record = brandTable.post(record);
        } else {
            record = brandTable.patch(record);
        }

        logger.info("Updated Brand: {}", brand.getBrandId());
        sleep();

        return Pair.of(record.getId(), brand);
    }

    private static boolean equals(Brand brand, AirtableRecord record) {
        Date date = record.getFieldDate("updatedMillis");
        if (date == null) return false;
        return date.getTime() == brand.getUpdatedMillis();
    }

    private static void sleep() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
