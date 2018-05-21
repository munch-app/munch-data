package munch.data.place.parser;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.structure.Place;
import munch.data.website.DomainBlocked;
import munch.data.website.WebsiteNormalizer;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

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
        FieldCollector fieldCollector = new FieldCollector(PlaceKey.website);
        fieldCollector.addAll(list);
        String priority = fix(fieldCollector.collectMax(priorityCorpus));
        if (priority != null) return priority;

        List<String> websites = new ArrayList<>();
        Map<String, Set<String>> articleWebsites = new HashMap<>();

        Set<String> negatives = new HashSet<>(fieldCollector.collectNegative());

        fieldCollector.getFields().forEach(field -> {
            if (negatives.contains(field.getValue())) return;

            if (field.getCorpusName().equals("Global.MunchArticle.Article")) {
                articleWebsites.compute(field.getValue(), (s, strings) -> {
                    if (strings == null) strings = new HashSet<>();
                    strings.add(field.getCorpusKey());

                    return strings;
                });
            } else {
                websites.add(field.getValue());
            }
        });

        if (!websites.isEmpty()) {
            return HashMultiset.create(websites).entrySet()
                    .stream()
                    .max(Comparator.comparingInt(Multiset.Entry::getCount))
                    .map(entry -> fix(entry.getElement()))
                    .orElse(null);
        }

        if (articleWebsites.isEmpty()) return null;
        return articleWebsites.entrySet()
                .stream()
                .max(Comparator.comparingInt(value -> value.getValue().size()))
                .map(entry -> fix(entry.getKey()))
                .orElse(null);
    }

    protected String fix(String url) {
        if (StringUtils.isEmpty(url)) return null;

        url = WebsiteNormalizer.normalize(url);
        if (isBlocked(url)) return null;
        return url;
    }

    protected boolean isBlocked(String website) {
        String domain = WebsiteNormalizer.getDomain(website);
        if (StringUtils.isBlank(domain)) return true;
        return domainBlocked.isBlocked(domain);
    }
}
