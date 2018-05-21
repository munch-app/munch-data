package munch.data.place.graph.seeder;

import catalyst.utils.exception.DateCompareUtils;
import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.MetaKey;
import corpus.field.PlaceKey;
import munch.data.place.graph.PlaceTree;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 31/3/18
 * Time: 2:59 PM
 * Project: munch-data
 */
public final class DecaySeeder implements Seeder {
    private static final AbstractKey[] TIMESTAMP_KEYS = {AbstractKey.of("Article.timestamp"), MetaKey.createdDate};

    public static final Set<String> DECAY_DATES = Set.of(
            "Sg.MunchSheet.PlaceInfo2",
            "Sg.MunchUGC.PlaceSuggest",
            "Sg.Munch.PlaceAward",
            "Sg.Hpb.HealthyEating",
            "Sg.Muis.Halal",
            "Global.MunchArticle.Article",
            "Sg.Munch.Place.Decaying.Stop"
    );

    private static final Set<String> STATUS_DELETE = Set.of("delete", "deleted", "duplicate", "not food place", "does not exist");
    private static final Set<String> STATUS_CLOSE = Set.of("close", "closed", "permanently closed");
    private static final Set<String> STATUS_OPEN = Set.of("open", "opened");

    private final DecayTracker decayTracker;

    @Inject
    public DecaySeeder(DecayTracker decayTracker) {
        this.decayTracker = decayTracker;
    }

    @Override
    public Result trySeed(String placeId, PlaceTree placeTree) {
        List<CorpusData> dataList = placeTree.getCorpusDataList();
        DecayTracker.Status status = decayTracker.find(dataList);

        switch (getDecayStatus(dataList, status)) {
            case Decaying:
                // In the process of decaying
                return Result.Proceed;

            case Proceed:
                if (status != null) decayTracker.stop(placeId, status);
                return Result.Proceed;

            case Delete:
                if (status != null) decayTracker.stop(placeId, status);
                return Result.Block;

            case Decayed:
                if (status != null) decayTracker.stop(placeId, status);
                // Sg.Munch.Place.Decay
                return Result.Decayed;

            case StartFastDecay:
                // Basically, how fast data can be re-amalgamated
                decayTracker.start(placeId, "StartFastDecay11", Duration.ofDays(11));
                return Result.Proceed;

            case StartSlowDecay:
                decayTracker.start(placeId, "StartSlowDecay90", Duration.ofDays(90));
                return Result.Proceed;

            default:
                throw new IllegalStateException();
        }
    }

    private Status getDecayStatus(List<CorpusData> dataList, DecayTracker.Status status) {
        Date latestOpen = new Date(0);
        Date latestClose = new Date(0);
        Date latestDecay = new Date(0);

        for (CorpusData data : dataList) {
            Date date = getDate(data);

            for (CorpusData.Field field : PlaceKey.status.getAll(data)) {
                if (STATUS_DELETE.contains(field.getValue().toLowerCase())) return Status.Delete;

                if (STATUS_OPEN.contains(field.getValue().toLowerCase())) {

                    if (latestOpen.compareTo(date) < 0) {
                        latestOpen = date;
                    }
                } else if (STATUS_CLOSE.contains(field.getValue().toLowerCase())) {
                    if (latestClose.compareTo(date) < 0) {
                        latestClose = date;
                    }
                }
            }

            if (DECAY_DATES.contains(data.getCorpusName())) {
                if (latestDecay.compareTo(date) < 0) {
                    latestDecay = date;
                }
            }
        }

        Set<String> names = dataList.stream().map(CorpusData::getCorpusName).collect(Collectors.toSet());

        if (names.contains("Sg.Nea.TrackRecord.Deleted")) {
            if (!names.contains("Sg.Nea.TrackRecord")) {
                if (status != null) {
                    if (status.isDecayed()) return Status.Decayed;
                    return Status.Decaying;
                }
                return Status.StartFastDecay;
            }
        }

        // Ability to start decay naturally
        if (latestOpen.getTime() != 0 || latestClose.getTime() != 0) {
            // If latest close is greater means Decayed
            if (latestOpen.compareTo(latestClose) < 0) {
                if (status != null) {
                    if (status.isDecayed()) return Status.Decayed;
                    return Status.Decaying;
                }
                return Status.StartFastDecay;
            } else {
                return Status.Proceed;
            }
        }

        if (names.contains("Sg.Nea.TrackRecord")) return Status.Proceed;
        if (DateCompareUtils.after(latestDecay, Duration.ofDays(90))) {
            return Status.StartSlowDecay;
        }

        return Status.Proceed;
    }

    public enum Status {
        StartFastDecay,
        StartSlowDecay,
        Decaying,
        Decayed,
        Delete,
        Proceed
    }

    /**
     * @param data corpus data
     * @return date of this status
     */
    private Date getDate(CorpusData data) {
        for (AbstractKey key : TIMESTAMP_KEYS) {
            String value = key.getValue(data);
            if (value == null) continue;
            return new Date(Long.parseLong(value));
        }

        return data.getUpdatedDate();
    }
}
