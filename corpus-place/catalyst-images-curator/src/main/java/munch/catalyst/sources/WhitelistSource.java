package munch.catalyst.sources;


import corpus.data.CorpusData;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 6/8/2017
 * Time: 5:37 PM
 * Project: munch-corpus
 */
abstract class WhitelistSource {
    protected final String id;
    protected final double boost;

    protected WhitelistSource(String id, double boost) {
        this.id = id;
        this.boost = boost;
    }

    public String getId() {
        return id;
    }

    public double getBoost() {
        return boost;
    }

    public abstract List<SourcedImage> extract(CorpusData data);
}
