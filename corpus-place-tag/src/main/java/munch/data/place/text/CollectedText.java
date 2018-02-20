package munch.data.place.text;

import corpus.data.CorpusData;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:56 PM
 * Project: munch-data
 */
public class CollectedText {
    public enum From {
        Place,
        Instagram,
        Article
    }

    private From from;
    private CorpusData corpusData;

    private List<String> texts;

    public CorpusData getCorpusData() {
        return corpusData;
    }

    public void setCorpusData(CorpusData corpusData) {
        this.corpusData = corpusData;
    }

    public From getFrom() {
        return from;
    }

    public void setFrom(From from) {
        this.from = from;
    }

    public List<String> getTexts() {
        return texts;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }
}
