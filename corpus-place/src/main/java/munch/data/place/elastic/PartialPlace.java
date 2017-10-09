package munch.data.place.elastic;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 9/10/2017
 * Time: 4:24 AM
 * Project: munch-data
 */
public class PartialPlace {
    private String corpusName;
    private String corpusKey;

    private List<String> name;
    private List<String> postal;

    public String getCorpusName() {
        return corpusName;
    }

    public void setCorpusName(String corpusName) {
        this.corpusName = corpusName;
    }

    public String getCorpusKey() {
        return corpusKey;
    }

    public void setCorpusKey(String corpusKey) {
        this.corpusKey = corpusKey;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<String> getPostal() {
        return postal;
    }

    public void setPostal(List<String> postal) {
        this.postal = postal;
    }
}
