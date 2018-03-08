package munch.data.place.collector;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import munch.data.place.group.PlaceTagDatabase;
import munch.data.place.group.PlaceTagGroup;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 8/3/18
 * Time: 8:41 PM
 * Project: munch-data
 */
@Singleton
public final class SynonymTagMapping {

    protected final Supplier<Map<String, Set<PlaceTagGroup>>> supplier;

    @Inject
    public SynonymTagMapping(PlaceTagDatabase database) {
        this.supplier = Suppliers.memoizeWithExpiration(() -> {
            Map<String, Set<PlaceTagGroup>> map = new HashMap<>();

            for (PlaceTagGroup group : database.getAll()) {
                for (String synonym : group.getSynonyms()) {
                    map.compute(synonym.toLowerCase(), (s, set) -> {
                        if (set == null) set = new HashSet<>();
                        set.add(group);
                        return set;
                    });
                }
            }

            return map;
        }, 1, TimeUnit.HOURS);
    }

    public Set<PlaceTagGroup> resolveAll(Set<String> tags) {
        Map<String, Set<PlaceTagGroup>> mapping = supplier.get();
        return tags.stream()
                .flatMap(name -> mapping.getOrDefault(name.toLowerCase(), Set.of()).stream())
                .collect(Collectors.toSet());
    }
}
