package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 12/2/2018
 * Time: 6:09 PM
 * Project: munch-data
 */
@Singleton
public final class StatusParser extends AbstractParser<Boolean> {

    @Override
    public Boolean parse(Place place, List<CorpusData> list) {
        List<String> statusList = collectValue(list, PlaceKey.status);
        for (String status : statusList) {
            if (status.equalsIgnoreCase("delete")) {
                return false;
            }
            if (status.equalsIgnoreCase("close")) {
                return false;
            }
        }
        return true;
    }
}
