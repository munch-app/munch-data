package munch.data.place;

import catalyst.utils.exception.ExceptionRetriable;
import catalyst.utils.exception.Retriable;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.exception.NotFoundException;
import corpus.field.PlaceKey;
import munch.data.clients.PlaceClient;
import munch.data.exceptions.ElasticException;
import munch.data.place.amalgamate.Amalgamate;
import munch.data.structure.Place;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.*;

/**
 * This is the main corpus:
 * It does theses:
 * 1. Maintain Existing Links
 * 2. Add Links from PostalCorpus
 * 3. Add Links from SpatialCorpus
 * 4. Amalgamate data to create Sg.Munch.Place
 * 5. Push to DynamoDB (munch-data:data-client)
 * 6. Push to Elasticsearch (munch-data:data-client)
 * <p>
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 7:39 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceCorpus.class);
    private static final Retriable retriable = new ExceptionRetriable(4);
    private static final Set<String> BLOCKED_CORPUS = Set.of("Sg.Munch.Place");

    private final Amalgamate amalgamate;
    private final PlaceClient placeClient;
    private final PlaceParser placeParser;

    @Inject
    public PlaceCorpus(Amalgamate amalgamate, PlaceClient placeClient, PlaceParser placeParser) {
        super(logger);
        this.amalgamate = amalgamate;
        this.placeClient = placeClient;
        this.placeParser = placeParser;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(1);
    }

    @Override
    protected long loadCycleNo() {
        return System.currentTimeMillis();
    }

    /**
     * @param cycleNo current cycleNo
     * @return all data to maintain
     */
    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list(corpusName);
    }

    /**
     * Maintain Each(Place)
     * 1. Validate()
     * 2. Add()
     *
     * @param cycleNo   cycleNo current cycleNo
     * @param placeData each data to process
     * @param processed processed data count
     */
    @Override
    protected void process(long cycleNo, CorpusData placeData, long processed) {
        try {
            if (amalgamate.maintain(placeData)) {
                List<CorpusData> list = new ArrayList<>();
                catalystClient.listCorpus(placeData.getCatalystId()).forEachRemaining(data -> {
                    if (!BLOCKED_CORPUS.contains(data.getCorpusName())) {
                        list.add(data);
                    }
                });

                Place place = placeParser.parse(new Place(), list);
                // Null = parsing failed
                if (place == null) {
                    logger.info("Failed to parse place id: {}", placeData.getCorpusKey());
                    deleteIf(placeData.getCorpusKey());
                } else {
                    putIf(place);
                    corpusClient.put("Sg.Munch.Place", place.getId(), createCorpusData(place));
                    count(list, place);
                }
            } else {
                deleteIf(placeData.getCorpusKey());
                corpusClient.delete(placeData.getCorpusName(), placeData.getCorpusKey());
            }

            sleep(150);
            if (processed % 1000 == 0) logger.info("Processed {} places", processed);
        } catch (NotFoundException e) {
            logger.warn("Amalgamate Conflict Error catalystId: {}", placeData.getCatalystId(), e);
        }
    }

    private void count(List<CorpusData> list, Place place) {
        counter.increment("Counts.Places");

        if (StringUtils.isNotBlank(place.getPhone())) counter.increment("Counts.Phone");
        if (StringUtils.isNotBlank(place.getWebsite())) counter.increment("Counts.Website");
        if (StringUtils.isNotBlank(place.getDescription())) counter.increment("Counts.Description");

        if (place.getTag().getExplicits().size() > 2) counter.increment("Counts.Tag.Explicits>1");
        if (place.getTag().getImplicits().size() > 2) counter.increment("Counts.Tag.Implicits>1");

        if (place.getPrice() != null) counter.increment("Counts.Price");
        if (place.getReview() != null) counter.increment("Counts.Review.Total");

        if (!place.getHours().isEmpty()) counter.increment("Counts.Hours");
        if (place.getImages().size() > 0) counter.increment("Counts.Images>0");
        if (place.getImages().size() > 1) counter.increment("Counts.Images>1");
        if (place.getImages().size() > 2) counter.increment("Counts.Images>2");

        if (has(list, "Global.MunchArticle.Article")) counter.increment("Counts.Article");
        if (has(list, "Global.Facebook.Place")) counter.increment("Counts.FacebookPlace");
        if (has(list, "Sg.MunchSheet.FranchisePlace")) counter.increment("Counts.Franchise");
    }

    /**
     * Data put only if actually changed
     *
     * @param place non null place
     */
    private void putIf(Place place) {
        Objects.requireNonNull(place.getId());

        // Put if data is changed only
        Place existing = placeClient.get(place.getId());
        if (!place.equals(existing)) {
            try {
                retriable.loop(() -> placeClient.put(place));

                logger.info("Updated: updated: {} existing: {}",
                        JsonUtils.toString(place),
                        JsonUtils.toString(existing)
                );
                counter.increment("Updated");
            } catch (Exception e) {
                logger.error("Error: updated: {}", JsonUtils.toString(place));
                throw e;
            }
        }
    }

    private void deleteIf(String placeId) {
        Objects.requireNonNull(placeId);

        // Delete if exist only
        Place existing = placeClient.get(placeId);
        if (existing != null) {
            try {
                retriable.loop(() -> placeClient.delete(placeId));
                corpusClient.put("Sg.Munch.PlaceDeleted", existing.getId(), createCorpusData(existing));

                logger.info("Deleted: {}",
                        JsonUtils.toString(existing)
                );
                counter.increment("Deleted");
            } catch (ElasticException e) {
                if (e.getCode() == 404) {
                    logger.info("Already Deleted: {}",
                            JsonUtils.toString(existing)
                    );
                }
            }

        }
    }

    private CorpusData createCorpusData(Place place) {
        // Put to corpus client
        CorpusData placeData = new CorpusData(System.currentTimeMillis());
        // Max name is put twice
        placeData.put(PlaceKey.name, place.getName());
        place.getAllNames().forEach(name -> placeData.put(PlaceKey.name, name));

        placeData.put(PlaceKey.phone, place.getPhone());
        placeData.put(PlaceKey.website, place.getWebsite());
        placeData.put(PlaceKey.description, place.getDescription());

        placeData.put(PlaceKey.Location.street, place.getLocation().getStreet());
        placeData.put(PlaceKey.Location.address, place.getLocation().getAddress());
        placeData.put(PlaceKey.Location.unitNumber, place.getLocation().getUnitNumber());

        placeData.put(PlaceKey.Location.city, place.getLocation().getCity());
        placeData.put(PlaceKey.Location.country, place.getLocation().getCountry());

        placeData.put(PlaceKey.Location.postal, place.getLocation().getPostal());
        placeData.put(PlaceKey.Location.latLng, place.getLocation().getLatLng());
        return placeData;
    }

    /**
     * @param list       list to check
     * @param corpusName corpus name
     * @return true is has corpus name
     */
    private static boolean has(List<CorpusData> list, String corpusName) {
        for (CorpusData data : list) {
            if (data.getCorpusName().equals(corpusName)) {
                return true;
            }
        }
        return false;
    }
}
