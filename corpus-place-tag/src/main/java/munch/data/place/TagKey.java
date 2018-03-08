package munch.data.place;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 8/3/18
 * Time: 8:26 PM
 * Project: munch-data
 */
public final class TagKey extends AbstractKey {

    public static final TagKey implicits = new TagKey("implicits", true);
    public static final TagKey explicits = new TagKey("explicits", true);
    public static final TagKey predicts = new TagKey("predicts", true);

    protected TagKey(String name, boolean multi) {
        super("Tag." + name, multi);
    }

    public List<CorpusData.Field> createFields(List<String> tags) {
        List<CorpusData.Field> fields = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            fields.add(createField(tags.get(i), "Sort", String.valueOf(i)));
        }
        return fields;
    }
}
