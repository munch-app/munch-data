package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;
import munch.data.website.DomainBlocked;
import munch.data.website.WebsiteNormalizer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 18/12/2017
 * Time: 10:57 AM
 * Project: munch-data
 */
@Singleton
public class WebsiteParser extends AbstractParser<String> {

    private final DomainBlocked domainBlocked;

    @Inject
    public WebsiteParser(DomainBlocked domainBlocked) {
        this.domainBlocked = domainBlocked;
    }

    @Override
    public String parse(Place place, List<CorpusData> list) {
        List<String> websites = collectSorted(list, PlaceKey.website);
        if (websites.isEmpty()) return null;

        return search(websites);
    }

    protected String search(List<String> urls) {
        for (String url : urls) {
            url = WebsiteNormalizer.normalize(url);
            if (isBlocked(url)) continue;

            return url;
        }
        return null;
    }

    protected boolean isBlocked(String website) {
        try {
            String domain = WebsiteNormalizer.getDomain(website);
            return domainBlocked.isBlocked(domain);
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
