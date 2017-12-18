package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 18/12/2017
 * Time: 10:58 AM
 * Project: munch-data
 */
@Singleton
public final class DescriptionParser extends AbstractParser<String> {
    private static final Pattern WEBSITE_PATTERN = Pattern.compile("^(https?://)|(www\\.)[0-9a-z\\-]+\\.com$", Pattern.CASE_INSENSITIVE);

    @Override
    public String parse(Place place, List<CorpusData> list) {
        String description = collectMax(list, PlaceKey.description);
        if (StringUtils.isBlank(description)) return null;
        // Cannot be too short
        if (description.length() < 15) return null;
        // Cannot be a website
        if (description.length() < 40 && WEBSITE_PATTERN.matcher(description).matches()) return null;

        return description.replaceAll(" {2,}", " ");
    }
}
