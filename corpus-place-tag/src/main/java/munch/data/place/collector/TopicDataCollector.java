package munch.data.place.collector;

import com.google.common.base.Joiner;
import munch.data.place.topic.TopicAnalysis;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 15/2/2018
 * Time: 7:08 PM
 * Project: munch-data
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class TopicDataCollector extends DataCollector {

    private final TopicAnalysis topicAnalysis;

    @Inject
    public TopicDataCollector(TopicAnalysis topicAnalysis) throws IOException {
        super("corpus-place-tag/tag-model/data/tag-topic-data-1.csv",
                "corpus-place-tag/tag-model/data/tag-topic-mapping-1.json");
        this.topicAnalysis = topicAnalysis;
    }


    public void put(DataGroup group) throws IOException {
        List<Map<String, Integer>> topics = topicAnalysis.apply(group.getTexts(), 1, 100);
        if (topics.isEmpty()) return;
        if (topics.get(0).size() < 2) return;

        csvWriter.printRecord(Joiner.on(" ").join(topics.get(0).keySet()), group.getTags());
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        run(TopicDataCollector.class);
    }
}
