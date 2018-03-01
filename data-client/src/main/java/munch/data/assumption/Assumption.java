package munch.data.assumption;

import munch.data.structure.SearchQuery;

import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 23/2/18
 * Time: 8:51 PM
 * Project: munch-data
 */
public class Assumption {
    private final boolean explicit;
    private final String token;
    private final String tag;
    private final Consumer<SearchQuery> queryConsumer;

    protected Assumption(boolean explicit, String token, String tag, Consumer<SearchQuery> queryConsumer) {
        this.explicit = explicit;
        this.token = token;
        this.tag = tag;
        this.queryConsumer = queryConsumer;
    }

    public boolean isExplicit() {
        return explicit;
    }

    /**
     * @return token used to identify assumption
     */
    public String getToken() {
        return token;
    }

    /**
     * @return visual text used to present assumption
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param query search query to apply assumption on
     */
    public void apply(SearchQuery query) {
        queryConsumer.accept(query);
    }

    public static Assumption of(String token, String tag, Consumer<SearchQuery> consumer) {
        return new Assumption(false, token, tag, consumer);
    }

    public static Assumption of(boolean explicit, String token, String tag, Consumer<SearchQuery> consumer) {
        return new Assumption(explicit, token, tag, consumer);
    }
}
