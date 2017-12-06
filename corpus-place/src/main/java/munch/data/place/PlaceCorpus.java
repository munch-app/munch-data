package munch.data.place;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.exception.NotFoundException;
import corpus.field.PlaceKey;
import munch.data.clients.PlaceClient;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
        return Duration.ofMinutes(20);
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
                catalystClient.listCorpus(placeData.getCatalystId()).forEachRemaining(list::add);

                Place place = placeParser.parse(new Place(), list);
                // Null = parsing failed
                if (place == null) {
                    deleteIf(placeData.getCorpusKey());
                } else {
                    putIf(place);
                    putPlaceData(placeData, place);
                    count(list, place);
                }
            } else {
                deleteIf(placeData.getCorpusKey());
                corpusClient.delete(placeData.getCorpusName(), placeData.getCorpusKey());
            }

            sleep(300);
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
        if (place.getReview().getTotal() > 0) counter.increment("Counts.Review.Total");

        if (!place.getHours().isEmpty()) counter.increment("Counts.Hours");
        if (place.getImages().size() > 0) counter.increment("Counts.Images>0");
        if (place.getImages().size() > 1) counter.increment("Counts.Images>1");

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
            placeClient.put(place);
            counter.increment("Updated");
        }
    }

    private void deleteIf(String placeId) {
        Objects.requireNonNull(placeId);

        // Delete if exist only
        Place existing = placeClient.get(placeId);
        if (existing != null) {
            placeClient.delete(placeId);
        }
    }

    private void putPlaceData(CorpusData placeData, Place place) {
        // Put to corpus client
        // CACHED FEEDBACK LOOP, Parser will read from here also
        placeData.replace(PlaceKey.name, place.getName());
        placeData.replace(PlaceKey.Location.postal, place.getLocation().getPostal());
        placeData.replace(PlaceKey.Location.latLng, place.getLocation().getLatLng());
        corpusClient.put(corpusName, placeData.getCorpusKey(), placeData);
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
