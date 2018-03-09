package munch.data.place.collector;

import com.google.common.base.Joiner;
import munch.data.place.text.CollectedText;

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
        super("corpus-place-tag/tag-model/data/tag-text-data-5.csv",
                "corpus-place-tag/tag-model/data/tag-text-mapping-5.json");
    }


    public void put(DataGroup group) throws IOException {
        if (group.getCollectedTexts().size() == 1) {
             if (group.getCollectedTexts().get(0).getFrom() == CollectedText.From.Place) return;
        }
        List<String> texts = group.getTexts();
        csvWriter.printRecord(Joiner.on(" ").join(texts), group.getTags());
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        run(TextDataCollector.class);
    }
}
