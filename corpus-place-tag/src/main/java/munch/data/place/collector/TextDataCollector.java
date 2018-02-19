package munch.data.place.ml;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by: Fuxing
 * Date: 15/2/2018
 * Time: 7:08 PM
 * Project: munch-data
 */
public class TextDataCollector extends DataCollector {
    private static final Logger logger = LoggerFactory.getLogger(TopicDataCollector.class);

    @Inject
    public TextDataCollector() throws IOException {
        super("corpus-place-tag/tag-model/data/tag-text-data-1.csv",
                "corpus-place-tag/tag-model/data/tag-text-mapping-1.json");
    }


    public void put(DataGroup group) throws IOException {
        csvWriter.printRecord(Joiner.on(" ").join(group.getTexts()), group.getTags());
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        run(TextDataCollector.class);
    }
}
