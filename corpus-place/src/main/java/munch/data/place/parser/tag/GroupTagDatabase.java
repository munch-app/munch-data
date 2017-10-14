package munch.data.place.parser.tag;

import corpus.data.CorpusClient;
import corpus.field.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 15/10/2017
 * Time: 2:11 AM
 * Project: munch-data
 */
@Singleton
public final class GroupTagDatabase {
    private static final Logger logger = LoggerFactory.getLogger(GroupTagDatabase.class);

    private final CorpusClient corpusClient;
    private Map<String, GroupTag> tagMap = new HashMap<>();

    @Inject
    public GroupTagDatabase(CorpusClient corpusClient) {
        this.corpusClient = corpusClient;
        sync();

        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(this::sync, 24, 24, TimeUnit.HOURS);
    }

    /**
     * @param tags tags
     * @return Set of GroupTag
     */
    public Set<GroupTag> findTags(List<String> tags) {
        return tags.stream()
                .map(name -> tagMap.get(name.toLowerCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void sync() {
        Map<String, GroupTag> tagMap = new HashMap<>();
        corpusClient.list("Sg.MunchSheet.PlaceTag").forEachRemaining(data -> {
            GroupTag tag = new GroupTag();
            tag.setName(FieldUtils.getValue(data, "PlaceTag.name"));
            tag.setSynonyms(FieldUtils.getAllValue(data, "PlaceTag.synonym")
                    .stream().map(String::toLowerCase)
                    .collect(Collectors.toSet()));

            tag.setOrder(FieldUtils.getValue(data, "PlaceTag.order",
                    values -> Integer.parseInt(values.get(0))));
            tag.setGroupNo(FieldUtils.getValue(data, "PlaceTag.groupNo",
                    values -> Integer.parseInt(values.get(0))));

            Objects.requireNonNull(tag.getName());
            tagMap.put(tag.getName().toLowerCase(), tag);
            tag.getSynonyms().forEach(tagName -> tagMap.put(tagName.toLowerCase(), tag));
        });

        this.tagMap = tagMap;
    }
}
