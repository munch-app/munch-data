package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceTree;
import munch.data.website.DomainBlocked;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 31/3/2018
 * Time: 3:33 AM
 * Project: munch-data
 */
@Singleton
public final class WebsiteMatcher implements Matcher, Searcher {

    private final DomainBlocked domainBlocked;

    @Inject
    public WebsiteMatcher(DomainBlocked domainBlocked) {
        this.domainBlocked = domainBlocked;
    }

    @Override
    public List<CorpusData> search(ElasticClient elasticClient, PlaceTree placeTree) {
        FieldCollector fieldCollector = placeTree.getFieldCollector(PlaceKey.website);
        String website = fieldCollector.collectMax();
        if (website == null) return List.of();

        String domain = DomainBlocked.getTLD(website);
        if (domainBlocked.isBlocked(domain)) return List.of();
        return elasticClient.search(placeTree, ElasticClient.filterTerm("Place.website", domain));
    }

    @Override
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        Set<String> leftDomains = PlaceKey.website.getAllValue(left).stream()
                .map(DomainBlocked::getTLD)
                .filter(s -> !domainBlocked.isBlocked(s))
                .collect(Collectors.toSet());
        if (leftDomains.isEmpty()) return Map.of();

        Set<String> rightDomains = PlaceKey.website.getAllValue(right).stream()
                .map(DomainBlocked::getTLD)
                .filter(s -> !domainBlocked.isBlocked(s))
                .collect(Collectors.toSet());

        if (rightDomains.isEmpty()) return Map.of();

        for (String rightDomain : rightDomains) {
            if (!leftDomains.contains(rightDomain)) {
                return Map.of("Place.website", -1);
            }
        }

        return Map.of("Place.website", 1);
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of("Place.website");
    }

    @Override
    public void normalize(CorpusData.Field field) {
        if (!field.getKey().equals("Place.website")) return;

        String domain = DomainBlocked.getTLD(field.getValue());
        if (domain != null) {
            field.setValue(domain);
        }
    }
}
