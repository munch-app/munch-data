package munch.data.container.matcher;

import com.google.common.collect.ImmutableList;
import corpus.data.CorpusData;
import corpus.field.ContainerKey;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 10/10/17
 * Time: 6:59 PM
 * Project: munch-corpus
 */
public final class PostalContainerMatcher {

    private Map<String, Set<Matched>> postalMap = new HashMap<>();

    public void put(CorpusData container) {
        String postal = ContainerKey.Location.postal.getValueOrThrow(container);
        postalMap.computeIfAbsent(postal, s -> new HashSet<>())
                .add(new Matched(container));
    }

    public List<CorpusData> find(String postal, String catalystId, long cycleNo) {
        Set<Matched> matchedSet = postalMap.get(postal);
        if (matchedSet == null || matchedSet.isEmpty()) return Collections.emptyList();

        return matchedSet.stream()
                .map(matched -> matched.createPlace(catalystId, cycleNo))
                .collect(Collectors.toList());
    }

    private class Matched {
        private final ImmutableList<CorpusData.Field> fields;

        public Matched(CorpusData data) {
            this.fields = ImmutableList.copyOf(data.getFields());
        }

        /**
         * @param cycleNo cycleNo
         * @return newly created Sg.MunchSheet.FranchisePlace
         */
        public CorpusData createPlace(String catalystId, long cycleNo) {
            CorpusData data = new CorpusData("Sg.Munch.ContainerPlace", catalystId, cycleNo);
            data.setCatalystId(catalystId);
            data.setFields(fields);
            return data;
        }
    }
}
