package munch.data.assumption;

import munch.data.clients.LocationUtils;
import munch.data.elastic.ElasticIndex;
import munch.data.structure.Container;
import munch.data.structure.Location;
import munch.data.structure.SearchQuery;
import munch.data.structure.Tag;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 23/2/18
 * Time: 8:50 PM
 * Project: munch-data
 */
public class AssumptionDatabase {
    private static final List<Assumption> EXPLICIT_ASSUMPTION = List.of(
            // Location Assumption
            Assumption.of("nearby", "Nearby", applyLocation(null)),
            Assumption.of("near me", "Near Me", applyLocation(null)),
            Assumption.of("singapore", "Singapore", applyLocation(LocationUtils.SINGAPORE)),
            Assumption.of("anywhere", "Anywhere", applyLocation(LocationUtils.SINGAPORE)),

            // Price Range Assumption
            // Future: Cheap, Budget, Expensive

            // Timing Assumption
            Assumption.of("open now", "Open Now", applyHour("Open Now", null, null)),
            Assumption.of("breakfast", "Breakfast", applyHour("Breakfast", null, null)),
            Assumption.of("lunch", "Lunch", applyHour("Lunch", null, null)),
            Assumption.of("dinner", "Dinner", applyHour("Dinner", null, null)),
            Assumption.of("supper", "Supper", applyHour("Supper", null, null))

            // Tag Assumption
    );

    private final ElasticIndex elasticIndex;

    @Inject
    public AssumptionDatabase(ElasticIndex elasticIndex) {
        this.elasticIndex = elasticIndex;
    }

    public Map<String, Assumption> get() {
        Map<String, Assumption> assumptionMap = new HashMap<>();
        EXPLICIT_ASSUMPTION.forEach(assumption -> assumptionMap.put(assumption.getToken(), assumption));

        Iterator<Container> containers = elasticIndex.scroll("Container", "2m");
        containers.forEachRemaining(container -> {
            String token = container.getName().toLowerCase();
            assumptionMap.putIfAbsent(token, Assumption.of(token, container.getName(), applyContainer(container)));
        });

        Iterator<Location> locations = elasticIndex.scroll("Location", "2m");
        locations.forEachRemaining(location -> {
            String token = location.getName().toLowerCase();
            assumptionMap.putIfAbsent(token, Assumption.of(token, location.getName(), applyLocation(location)));
        });

        Iterator<Tag> tags = elasticIndex.scroll("Tag", "2m");
        tags.forEachRemaining(tag -> {
            String token = tag.getName().toLowerCase();
            assumptionMap.putIfAbsent(token, Assumption.of(token, tag.getName(), applyTag(token)));
        });
        return assumptionMap;
    }

    public static Consumer<SearchQuery> applyLocation(Location location) {
        return query -> {
            if (query.getFilter() == null) query.setFilter(new SearchQuery.Filter());
            query.getFilter().setLocation(location);
            query.getFilter().setContainers(List.of());
        };
    }

    public static Consumer<SearchQuery> applyContainer(Container container) {
        return query -> {
            if (query.getFilter() == null) query.setFilter(new SearchQuery.Filter());
            query.getFilter().setContainers(List.of(container));
            query.getFilter().setLocation(null);
        };
    }

    public static Consumer<SearchQuery> applyTag(String tag) {
        return query -> {
            if (query.getFilter() == null) query.setFilter(new SearchQuery.Filter());
            if (query.getFilter().getTag() == null) query.getFilter().setTag(new SearchQuery.Filter.Tag());
            if (query.getFilter().getTag().getPositives() == null)
                query.getFilter().getTag().setPositives(new HashSet<>());
            query.getFilter().getTag().getPositives().add(tag);
        };
    }

    public static Consumer<SearchQuery> applyHour(String tag, String open, String close) {
        return query -> {
            if (query.getFilter() == null) query.setFilter(new SearchQuery.Filter());
            SearchQuery.Filter.Hour hour = new SearchQuery.Filter.Hour();
            hour.setName(tag);
            hour.setDay(query.getUserInfo().getDay());
            hour.setOpen(open);
            hour.setClose(close);
            query.getFilter().setHour(hour);
        };
    }
}
