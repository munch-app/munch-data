package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;
import munch.data.website.DomainBlocked;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 6/3/2018
 * Time: 12:07 AM
 * Project: munch-data
 */
@Singleton
public class MenuParser extends WebsiteParser {

    @Inject
    public MenuParser(DomainBlocked domainBlocked) {
        super(domainBlocked);
    }

    @Override
    public String parse(Place place, List<CorpusData> list) {
        return fix(collectMax(list, PlaceKey.menu));
    }

}
