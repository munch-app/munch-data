package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;

import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 18/12/2017
 * Time: 10:57 AM
 * Project: munch-data
 */
@Singleton
public final class WebsiteParser extends AbstractParser<String> {
    private static final Pattern HTTP_PATTERN = Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);
    private static final Set<String> BLOCKED_HOST = Set.of("facebook.com", "instagram.com", "fb.com", "google.com", "burpple.com", "foursquare.com", "hungrygowhere.com", "yelp.com", "zomato.com", "oddle.com", "eatigo.com");

    @Override
    public String parse(Place place, List<CorpusData> list) {
        List<String> websites = collectSorted(list, PlaceKey.website);
        if (websites.isEmpty()) return null;

        for (String website : websites) {
            if (isBlocked(website)) continue;
            if (HTTP_PATTERN.matcher(website).matches()) return website;
            return "http://" + website;
        }

        return null;
    }

    private static boolean isBlocked(String website) {
        website = website.toLowerCase();
        for (String host : BLOCKED_HOST) {
            if (website.contains(host)) return true;
        }
        return false;
    }
}
