package munch.data.place.collector;

import com.google.common.base.Joiner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 15/2/2018
 * Time: 7:08 PM
 * Project: munch-data
 */
public class TextDataCollector extends DataCollector {

    @Inject
    public TextDataCollector() throws IOException {
        super("corpus-place-tag/tag-model/data/tag-text-data-2.csv",
                "corpus-place-tag/tag-model/data/tag-text-mapping-2.json");
    }


    public void put(DataGroup group) throws IOException {
        List<String> texts = group.getTexts();
        if (texts.size() == 1) return;
        csvWriter.printRecord(Joiner.on(" ").join(texts), group.getTags());
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        run(TextDataCollector.class);
    }
}
