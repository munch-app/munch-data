package munch.data.place;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<String, Pair<String, Place.Brand>> brandMap = new HashMap<>();

    @Inject
    public AirtableBrandMapper(AirtableApi api) {
        this.brandTable = api.base("appDcx5b3vgkhcYB5").table("Brand");
    }

    public JsonNode mapField(Place.Brand brand) {
        ArrayNode fields = JsonUtils.createArrayNode();
        if (brand == null) return fields;
        fields.add(brandMap.computeIfAbsent(brand.getBrandId(), s -> find(brand)).getLeft());
        return fields;
    }

    private Pair<String, Place.Brand> find(Place.Brand brand) {
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

        if (record.getId() == null) {
            record = brandTable.post(record);
        } else {
            record = brandTable.patch(record);
        }

        logger.info("Updated Brand: {}", brand.getBrandId());
        sleep();

        return Pair.of(record.getId(), brand);
    }

    private static boolean equals(Place.Brand brand, AirtableRecord record) {
        if (!record.getField("brandId").asText().equals(brand.getBrandId())) return false;
        if (!record.getField("name").asText().equals(brand.getName())) return false;
        return true;
    }

    private static void sleep() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
