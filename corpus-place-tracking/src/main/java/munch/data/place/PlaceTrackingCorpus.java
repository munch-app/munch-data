package munch.data.place;

import catalyst.utils.iterators.PaginationIterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.engine.CatalystEngine;
import munch.data.clients.PlaceClient;
import munch.data.elastic.query.BoolQuery;
import munch.data.elastic.query.SortQuery;
import munch.data.structure.Place;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 7:35 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceTrackingCorpus extends CatalystEngine<Place> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceTrackingCorpus.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final ObjectMapper mapper = JsonUtils.objectMapper;
    private final PlaceClient placeClient;
    private final AirtableApi.Table airtable;

    @Inject
    public PlaceTrackingCorpus(PlaceClient placeClient, AirtableApi airtableApi) {
        super(logger);
        this.placeClient = placeClient;
        this.airtable = airtableApi.base("apphY7zE8Tdd525qO").table("New Place");
    }

    @Override
    protected Duration cycleDelay() {
        // Every 12 hours generate one list
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<Place> fetch(long cycleNo) {
        return getNewestPlace();
    }

    @Override
    protected void doCycle(long cycleNo, Iterator<Place> iterator) {
        airtable.replace(Duration.ofSeconds(1), Iterators.transform(iterator, place -> {
            Objects.requireNonNull(place);

            AirtableRecord record = new AirtableRecord();
            Map<String, JsonNode> fields = new HashMap<>();
            fields.put("Place.name", JsonUtils.toTree(place.getName()));
            fields.put("Ranking", JsonUtils.toTree(place.getRanking()));
            fields.put("Place.tag", JsonUtils.toTree(place.getTag().getExplicits().stream()
                    .map(WordUtils::capitalizeFully)
                    .collect(Collectors.joining(", "))));
            fields.put("Place.Location.address", JsonUtils.toTree(place.getLocation().getAddress()));
            fields.put("Place.description", JsonUtils.toTree(place.getDescription()));
            fields.put("Place.website", JsonUtils.toTree(place.getWebsite()));
            fields.put("Place.phone", JsonUtils.toTree(place.getPhone()));
            fields.put("Place.image", JsonUtils.toTree(place.getImages().stream()
                    .flatMap(sourcedImage -> sourcedImage.getImages().entrySet().stream())
                    .max((o1, o2) -> o2.getKey().compareTo(o1.getKey()))
                    .map(Map.Entry::getValue)
                    .orElse("")));
            fields.put("Place.id", JsonUtils.toTree(place.getId()));
            fields.put("CreatedDate", JsonUtils.toTree(DATE_FORMAT.format(place.getCreatedDate())));

            record.setFields(fields);
            return record;
        }));
    }

    @Override
    protected void process(long cycleNo, Place place, long processed) {
    }

    public Iterator<Place> getNewestPlace() {
        return new PaginationIterator<>(integer -> getNewestPlace(integer, 50));
    }

    public List<Place> getNewestPlace(int from, int size) {
        ObjectNode root = mapper.createObjectNode();
        root.put("from", from);
        root.put("size", size);

        // Bool
        ObjectNode boolQuery = mapper.createObjectNode();
        ArrayNode filterArray = mapper.createArrayNode();
        filterArray.add(BoolQuery.filterTerm("dataType", "Place"));

        long beforeRange = System.currentTimeMillis() - Duration.ofDays(30).toMillis();
        filterArray.add(BoolQuery.filterRange("createdDate", "gte", beforeRange));

        // Filter Array
        boolQuery.set("filter", filterArray);
        root.putObject("query").set("bool", boolQuery);

        // Sorting
        ArrayNode sortArray = mapper.createArrayNode();
        sortArray.add(SortQuery.sortField("ranking", "desc"));
        root.set("sort", sortArray);

        return placeClient.getSearchClient().search(root);
    }
}
