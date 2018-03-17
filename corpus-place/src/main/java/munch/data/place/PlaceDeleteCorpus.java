package munch.data.place;

import catalyst.utils.exception.ExceptionRetriable;
import catalyst.utils.exception.Retriable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import corpus.engine.CatalystEngine;
import munch.data.clients.PlaceClient;
import munch.data.elastic.query.BoolQuery;
import munch.data.exceptions.ElasticException;
import munch.data.structure.Place;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static munch.data.place.PlaceCorpus.createCorpusData;

/**
 * Created by: Fuxing
 * Date: 9/3/18
 * Time: 6:53 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceDeleteCorpus extends CatalystEngine<Place> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceDeleteCorpus.class);
    private static final Retriable retriable = new ExceptionRetriable(4);
    private static final ObjectMapper mapper = JsonUtils.objectMapper;

    private final PlaceClient placeClient;

    @Inject
    public PlaceDeleteCorpus(PlaceClient placeClient) {
        super(logger);
        this.placeClient = placeClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofDays(1);
    }

    @Override
    protected Iterator<Place> fetch(long cycleNo) {
        // Track data that are older then 6 months
        // Track data that are below a certain ranking
        return Iterators.concat(
                // 30 * 6
                getExpirePlace(1000, Duration.ofDays(1)).iterator(),
                getBeforeRanking(1000, 500).iterator()
        );
    }

    @Override
    protected void process(long cycleNo, Place data, long processed) {
        String placeId = Objects.requireNonNull(data.getId());

        Objects.requireNonNull(placeId);

        // Delete if exist only
        Place existing = placeClient.get(placeId);
        if (existing != null) {
            try {
                corpusClient.put("Sg.Munch.Place.Deleted", existing.getId(), createCorpusData(existing));
                corpusClient.delete("Sg.Munch.Place", placeId);
                retriable.loop(() -> placeClient.delete(placeId));

                logger.info("Deleted: {}", JsonUtils.toString(existing));
                counter.increment("Deleted");
            } catch (ElasticException e) {
                if (e.getCode() == 404) logger.info("Already Deleted: {}", JsonUtils.toString(existing));
            }
        }
    }

    public List<Place> getExpirePlace(int size, Duration duration) {
        ObjectNode root = mapper.createObjectNode();
        root.put("from", 0);
        root.put("size", size);

        // Bool
        ObjectNode boolQuery = mapper.createObjectNode();
        ArrayNode filterArray = mapper.createArrayNode();
        filterArray.add(BoolQuery.filterTerm("dataType", "Place"));
        filterArray.add(BoolQuery.filterTerm("open", false));

        long beforeRange = System.currentTimeMillis() - duration.toMillis();
        filterArray.add(BoolQuery.filterRange("updatedDate", "lte", beforeRange));

        // Filter Array
        boolQuery.set("filter", filterArray);
        root.putObject("query").set("bool", boolQuery);
        return placeClient.getSearchClient().search(root);
    }

    public List<Place> getBeforeRanking(int size, double beforeRanking) {
        ObjectNode root = mapper.createObjectNode();
        root.put("from", 0);
        root.put("size", size);

        // Bool
        ObjectNode boolQuery = mapper.createObjectNode();
        ArrayNode filterArray = mapper.createArrayNode();
        filterArray.add(BoolQuery.filterTerm("dataType", "Place"));
        filterArray.add(BoolQuery.filterTerm("open", false));
        filterArray.add(BoolQuery.filterRange("ranking", "lte", beforeRanking));

        // Filter Array
        boolQuery.set("filter", filterArray);
        root.putObject("query").set("bool", boolQuery);
        return placeClient.getSearchClient().search(root);
    }
}
