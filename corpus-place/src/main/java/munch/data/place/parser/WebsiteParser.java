package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;

import javax.inject.Singleton;
import java.util.List;
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

    @Override
    public String parse(Place place, List<CorpusData> list) {
        String website = collectMax(list, PlaceKey.website);
        if (website == null) return null;

        // Website cannot be facebook.com
        if (website.contains("facebook.com")) return null;
        if (HTTP_PATTERN.matcher(website).matches()) return website;
        return "http://" + website;
    }
}
