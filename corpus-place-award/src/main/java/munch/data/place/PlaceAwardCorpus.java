package munch.data.place;

import com.google.common.collect.Lists;
import corpus.engine.CatalystEngine;
import munch.awards.PlaceAwardClient;
import munch.collections.CollectionClient;
import munch.collections.CollectionPlaceClient;
import munch.collections.PlaceCollection;
import munch.data.clients.SearchClient;
import munch.data.location.PostalParser;
import munch.data.place.matcher.NameNormalizer;
import munch.data.structure.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 7:35 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceAwardCorpus extends CatalystEngine<AwardCollection> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceAwardCorpus.class);
    private static final String AWARD_USER_ID = "munch_award";

    private final SearchClient searchClient;
    private final AirtableDatabase airtableDatabase;

    private final CollectionClient collectionClient;
    private final CollectionPlaceClient collectionPlaceClient;
    private final PlaceAwardClient placeAwardClient;

    private final NameNormalizer nameNormalizer;

    @Inject
    public PlaceAwardCorpus(SearchClient searchClient, AirtableDatabase airtableDatabase, CollectionClient collectionClient,
                            CollectionPlaceClient collectionPlaceClient, PlaceAwardClient placeAwardClient, NameNormalizer nameNormalizer) {
        super(logger);
        this.searchClient = searchClient;
        this.airtableDatabase = airtableDatabase;
        this.collectionClient = collectionClient;
        this.collectionPlaceClient = collectionPlaceClient;
        this.placeAwardClient = placeAwardClient;
        this.nameNormalizer = nameNormalizer;
    }

    @Override
    protected Duration cycleDelay() {
        // Sync every 18 hours
        return Duration.ofHours(18);
    }

    @Override
    protected Iterator<AwardCollection> fetch(long cycleNo) {
        return airtableDatabase.list();
    }

    @Override
    protected void process(long cycleNo, AwardCollection awardCollection, long processed) {
        awardCollection.getAwardPlaces().forEach(awardPlace -> {
            awardPlace.tryLink((name, address) -> {
                sleep(1000);
                name = nameNormalizer.normalize(name);
                return search(name, address);
            });
        });

        String collectionId = new UUID(awardCollection.getCollectionId(), 0).toString();
        // Always update PlaceCollection
        PlaceCollection collection = new PlaceCollection();
        collection.setUserId(AWARD_USER_ID);
        collection.setCollectionId(collectionId);
        collection.setSortKey(awardCollection.getCollectionId());
        collection.setName(awardCollection.getCollectionName());
        collectionClient.put(collection);

        // Award Place List
        List<AwardCollection.AwardPlace> awardList = awardCollection.getAwardPlaces()
                .stream()
                .filter(awardPlace -> awardPlace.getMunchId() != null && AwardCollection.AwardPlace.STATUS_LINKED.equals(awardPlace.getStatus()))
                .collect(Collectors.toList());

        // Current Added Collection List
        List<PlaceCollection.AddedPlace> addedList = Lists.newArrayList(collectionPlaceClient.list(AWARD_USER_ID, collectionId));
        for (PlaceCollection.AddedPlace place : addedList) {
            // Place to delete
            if (notContains(awardList, place, (award, added) -> award.getMunchId().equals(added.getPlaceId()))) {
                logger.info("Removed {} with id: {}", awardCollection.getCollectionName(), place.getPlaceId());
                collectionPlaceClient.remove(AWARD_USER_ID, collectionId, place.getPlaceId());
            }
        }

        for (AwardCollection.AwardPlace place : awardList) {
            // Place to add
            if (notContains(addedList, place, (added, award) -> award.getMunchId().equals(added.getPlaceId()))) {
                logger.info("Added {} to {} with id: {}", awardCollection.getCollectionName(), place.getName(), place.getMunchId());
                collectionPlaceClient.add(AWARD_USER_ID, collectionId, place.getMunchId());
                placeAwardClient.put(place.getMunchId(), awardCollection.getCollectionId(), place.getAwardId(),
                        awardCollection.getCollectionName());
            }
        }

        sleep(15000);
    }

    private <L, R> boolean notContains(List<L> compareList, R compareObject, BiFunction<L, R, Boolean> compare) {
        for (L left : compareList) {
            if (compare.apply(left, compareObject)) return false;
        }
        return true;
    }

    @Nullable
    private Place search(String name, String address) {
        String postal = PostalParser.parse(address);
        if (postal == null) return null;

        List<Place> placeList = searchClient.search(List.of("Place"), name, null, 0, 100);
        placeList = placeList.stream()
                .filter(place -> place.getLocation().getPostal().equals(postal))
                .filter(place -> nameNormalizer.equals(place.getName(), name))
                .collect(Collectors.toList());

        if (placeList.isEmpty()) {
            logger.info("Conflict: Not Found for name: {}", name);
            return null;
        }
        if (placeList.size() > 1) {
            logger.info("Conflict: Multiple for name: {}", name);
            return null;
        }
        return placeList.get(0);
    }
}
