package munch.data.place;

import catalyst.utils.LatLngUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.engine.AbstractEngine;
import munch.data.client.ElasticClient;
import munch.data.elastic.ElasticUtils;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 24/7/18
 * Time: 2:00 PM
 * Project: munch-data
 */
@Singleton
public final class GongChaBubbleTeaBridge extends AbstractEngine<GongChaBubbleTeaBridge.ContentData> {
    private static final Logger logger = LoggerFactory.getLogger(GongChaBubbleTeaBridge.class);

    private final ElasticClient elasticClient;

    private final AirtableApi.Table placeTable;
    private final AirtableApi.Table gongChaTable;

    @Inject
    public GongChaBubbleTeaBridge(ElasticClient elasticClient, AirtableApi api) {
        super(logger);
        this.elasticClient = elasticClient;
        this.placeTable = api.base("appDcx5b3vgkhcYB5").table("Place");
        this.gongChaTable = api.base("appDcx5b3vgkhcYB5").table("Gong Cha & Bubble Tea");
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<ContentData> fetch(long cycleNo) {
        List<Place> gongChas = searchGongCha();

        Iterator<Iterator<ContentData>> iterators = Iterators.transform(gongChas.iterator(), gongCha -> {
            Objects.requireNonNull(gongCha);
            String gongChaId = getId(gongCha);
            if (gongChaId == null) return Collections.emptyIterator();


            return Iterators.transform(searchBubbleTea(gongCha.getLocation().getLatLng()).iterator(), bubbleTea -> {
                Objects.requireNonNull(bubbleTea);
                String bubbleTeaId = getId(bubbleTea);
                if (bubbleTeaId == null) return null;
                if (bubbleTeaId.equals(gongChaId)) return null;

                ContentData data = new ContentData();
                data.gongCha = gongCha;
                data.gongChaId = gongChaId;
                data.bubbleTea = bubbleTea;
                data.bubbleTeaId = bubbleTeaId;
                return data;
            });
        });

        return Iterators.concat(iterators);
    }

    private List<Place> searchGongCha() {
        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", 0);
        root.put("size", 500);

        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticUtils.match("name", "Gong Cha"));
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticUtils.filterTerm("dataType", "Place"))
        );
        root.putObject("query").set("bool", bool);

        List<Place> places = elasticClient.searchHitsHits(root);
        places.removeIf(place -> !place.getName().equals("Gong Cha"));
        logger.info("Found {} Gong Cha", places.size());
        return places;
    }

    private List<Place> searchBubbleTea(String latLng) {
        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", 0);
        root.put("size", 500);

        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticUtils.mustMatchAll());
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticUtils.filterTerm("dataType", "Place"))
                .add(ElasticUtils.filterTerm("tags.name", "Bubble Tea".toLowerCase()))
                .add(ElasticUtils.filterDistance("location.latLng", latLng, 500))
        );
        root.putObject("query").set("bool", bool);

        List<Place> places = elasticClient.searchHitsHits(root);
        logger.info("Found {} Bubble Tea", places.size());
        return places;
    }

    @Override
    protected void process(long cycleNo, ContentData data, long processed) {
        if (data == null) return;
        List<AirtableRecord> records = gongChaTable.find("Key", data.getKey());
        if (!records.isEmpty()) return;

        AirtableRecord record = new AirtableRecord();
        record.setFields(new HashMap<>());
        record.putField("Key", data.getKey());
        record.putField("Distance", data.getDistance());
        record.putField("Gong Cha", JsonUtils.createArrayNode().add(data.gongChaId));
        record.putField("Bubble Tea", JsonUtils.createArrayNode().add(data.bubbleTeaId));
        gongChaTable.post(record);
        sleep(200);
    }

    private String getId(Place place) {
        List<AirtableRecord> records = placeTable.find("placeId", place.getPlaceId());
        if (records.isEmpty()) return null;
        return records.get(0).getId();
    }

    public static class ContentData {
        private Place gongCha;
        private Place bubbleTea;

        private String gongChaId;
        private String bubbleTeaId;

        private String getKey() {
            return gongCha.getPlaceId() + "_" + bubbleTea.getPlaceId();
        }

        private double getDistance() {
            String left = gongCha.getLocation().getLatLng();
            String right = bubbleTea.getLocation().getLatLng();
            return LatLngUtils.parse(left).distance(LatLngUtils.parse(right));
        }
    }
}
