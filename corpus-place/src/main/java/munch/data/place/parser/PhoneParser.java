package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;

import javax.inject.Singleton;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 18/12/2017
 * Time: 10:56 AM
 * Project: munch-data
 */
@Singleton
public final class PhoneParser extends AbstractParser<String> {
    private static final Pattern PHONE_PATTERN = Pattern.compile(".*(65)?\\s?(?<g1>[0-9]{4})\\s?(?<g2>[0-9]{4}).*", Pattern.CASE_INSENSITIVE);

    @Override
    public String parse(Place place, List<CorpusData> list) {
        String phone = collectMax(list, PlaceKey.phone);
        if (phone == null) return null;

        Matcher matcher = PHONE_PATTERN.matcher(phone);
        if (!matcher.matches()) return null;

        String g1 = matcher.group("g1");
        String g2 = matcher.group("g2");
        return "+65 " + g1 + " " + g2;
    }
}
