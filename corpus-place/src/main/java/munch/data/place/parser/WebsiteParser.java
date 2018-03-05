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
public class WebsiteParser extends AbstractParser<String> {
    protected static final Pattern HTTP_PATTERN = Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);
    protected static final Set<String> BLOCKED_HOST = Set.of("facebook.com", "instagram.com", "fb.com", "google.com", "burpple.com", "foursquare.com", "hungrygowhere.com", "yelp.com", "zomato.com", "oddle.com", "eatigo.com", "chope.com", "cho.pe");

    @Override
    public String parse(Place place, List<CorpusData> list) {
        List<String> websites = collectSorted(list, PlaceKey.website);
        if (websites.isEmpty()) return null;

        return search(websites);
    }

    protected static String search(List<String> urls) {
        for (String url : urls) {
            if (isBlocked(url)) continue;
            if (HTTP_PATTERN.matcher(url).matches()) return url;
            return "http://" + url;
        }
        return null;
    }

    protected static boolean isBlocked(String website) {
        website = website.toLowerCase();
        for (String host : BLOCKED_HOST) {
            if (website.contains(host)) return true;
        }
        return false;
    }
}
