package munch.data.assumption;

import com.google.inject.ImplementedBy;
import munch.data.clients.LocationUtils;
import munch.data.elastic.ElasticIndex;
import munch.data.structure.Container;
import munch.data.structure.Location;
import munch.data.structure.SearchQuery;
import munch.data.structure.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 23/2/18
 * Time: 8:50 PM
 * Project: munch-data
 */
@Singleton
@ImplementedBy(CachedAssumptionDatabase.class)
public class AssumptionDatabase {
    protected static final Consumer<SearchQuery> ASSUMPTION_OPEN_NOW = query -> {
        if (query.getFilter() == null) query.setFilter(new SearchQuery.Filter());
        SearchQuery.Filter.Hour hour = new SearchQuery.Filter.Hour();

        hour.setName("Open Now");
        hour.setDay(query.getUserInfo().getDay());
        hour.setOpen(query.getUserInfo().getTime());
        if (query.getUserInfo().getTime().startsWith("23:")) {
            hour.setClose("23:59");
        } else {
            hour.setClose(SearchQuery.Filter.Hour.addMin(query.getUserInfo().getTime(), 30));
        }
        query.getFilter().setHour(hour);
    };

    protected static final List<Assumption> EXPLICIT_ASSUMPTION = List.of(
            // Location Assumption
            Assumption.of(Assumption.Type.Location, "nearby", "Nearby", applyLocation(null)),
            Assumption.of(Assumption.Type.Location, "nearby me", "Nearby", applyLocation(null)),
            Assumption.of(Assumption.Type.Location, "near me", "Near Me", applyLocation(null)),
            Assumption.of(Assumption.Type.Location, "around me", "Around Me", applyLocation(null)),
            Assumption.of(Assumption.Type.Location, "anywhere", "Anywhere", applyLocation(LocationUtils.SINGAPORE)),

            // Price Range Assumption
            // Future: Cheap, Budget, Expensive
            // From 50 to 60 Dollar
            // Under 70 Dollars

            // Timing Assumption
            // Add Open Now
            Assumption.of(Assumption.Type.Timing, "open now", "Open Now", ASSUMPTION_OPEN_NOW),

            // Tag Assumption
            Assumption.of(Assumption.Type.Tag, "bar", "Bars & Pubs", applyTag("Bars & Pubs")),
            Assumption.of(Assumption.Type.Tag, "bars", "Bars & Pubs", applyTag("Bars & Pubs")),
            Assumption.of(Assumption.Type.Tag, "pub", "Bars & Pubs", applyTag("Bars & Pubs")),
            Assumption.of(Assumption.Type.Tag, "pubs", "Bars & Pubs", applyTag("Bars & Pubs"))
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
            assumptionMap.putIfAbsent(token, Assumption.of(Assumption.Type.Location, token, container.getName(), applyContainer(container)));
        });

        Iterator<Location> locations = elasticIndex.scroll("Location", "2m");
        locations.forEachRemaining(location -> {
            String token = location.getName().toLowerCase();
            assumptionMap.putIfAbsent(token, Assumption.of(Assumption.Type.Location, token, location.getName(), applyLocation(location)));
        });

        Iterator<Tag> tags = elasticIndex.scroll("Tag", "2m");
        tags.forEachRemaining(tag -> {
            String token = tag.getName().toLowerCase();
            assumptionMap.putIfAbsent(token, Assumption.of(Assumption.Type.Tag, token, tag.getName(), applyTag(tag.getName())));
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
}
