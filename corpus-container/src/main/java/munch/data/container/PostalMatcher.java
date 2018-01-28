package munch.data.container;

import com.google.common.collect.ImmutableList;
import corpus.data.CorpusData;
import corpus.field.ContainerKey;
import corpus.field.FieldUtils;
import munch.data.structure.Container;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 10/10/17
 * Time: 6:59 PM
 * Project: munch-corpus
 */
public final class PostalMatcher {
    private Map<String, Set<Matched>> postalMap = new HashMap<>();

    public void put(CorpusData sourceData, Container container) {
        ContainerKey.Location.postal.getAll(sourceData).forEach(field -> {
            postalMap.computeIfAbsent(field.getValue(), s -> new HashSet<>())
                    .add(new Matched(sourceData, container));
        });
    }

    public List<CorpusData> find(String postal, String catalystId, long cycleNo) {
        Set<Matched> matchedSet = postalMap.get(postal);
        if (matchedSet == null || matchedSet.isEmpty()) return Collections.emptyList();

        return matchedSet.stream()
                .map(matched -> matched.createPlace(catalystId, cycleNo))
                .collect(Collectors.toList());
    }

    private class Matched {
        private final Container container;
        private final ImmutableList<CorpusData.Field> fields;
        private long matchedPlaces = 0;

        public Matched(CorpusData data, Container container) {
            this.container = container;
            ImmutableList.Builder<CorpusData.Field> builder = new ImmutableList.Builder<>();
            builder.addAll(data.getFields());
            injectFields(data).ifPresent(builder::add);
            this.fields = builder.build();
        }

        private Optional<CorpusData.Field> injectFields(CorpusData data) {
            String type = FieldUtils.getValueOrThrow(data, "Container.type");

            if (type.equalsIgnoreCase("hawker centre")) {
                CorpusData.Field field = new CorpusData.Field("Place.tag", "hawker");
                return Optional.of(field);
            }
            return Optional.empty();
        }

        /**
         * @param cycleNo cycleNo
         * @return newly created Sg.MunchSheet.FranchisePlace
         */
        public CorpusData createPlace(String catalystId, long cycleNo) {
            matchedPlaces++;

            CorpusData data = new CorpusData("Sg.Munch.ContainerPlace", catalystId, cycleNo);
            data.setCatalystId(catalystId);
            data.setFields(fields);
            return data;
        }

        /**
         * @return null if container contains too little data
         */
        @Nullable
        public Container getContainer() {
            container.setCount(matchedPlaces);
            return container;
        }
    }

    public void forEach(Consumer<Container> consumer) {
        postalMap.forEach((s, all) -> {
            all.forEach(matched -> consumer.accept(matched.getContainer()));
        });
    }
}
