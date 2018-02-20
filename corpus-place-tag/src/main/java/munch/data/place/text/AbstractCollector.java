package munch.data.place.text;

import corpus.data.CorpusData;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:30 PM
 * Project: munch-data
 */
public abstract class AbstractCollector {

    public abstract List<CollectedText> collect(String placeId, List<CorpusData> list);

    protected CollectedText mapField(CorpusData corpusData, CorpusData.Field field, CollectedText.From from) {
        String text = field.getValue();
        if (StringUtils.isBlank(text)) return null;

        CollectedText collectedText = new CollectedText();
        collectedText.setFrom(from);
        collectedText.setCorpusData(corpusData);
        collectedText.setTexts(List.of(text));
        return collectedText;
    }

    protected CollectedText mapField(List<String> texts, CollectedText.From from) {
        if (texts.isEmpty()) return null;

        CollectedText collectedText = new CollectedText();
        collectedText.setFrom(from);
        collectedText.setTexts(texts);
        return collectedText;
    }
}
