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
    public enum Type {
        Location,
        Tag,
        Price,
        Timing
    }

    private final Type type;
    private final String token;
    private final String tag;
    private final Consumer<SearchQuery> queryConsumer;

    protected Assumption(Type type, String token, String tag, Consumer<SearchQuery> queryConsumer) {
        this.type = type;
        this.token = token;
        this.tag = tag;
        this.queryConsumer = queryConsumer;
    }

    /**
     * @return type used to identify which type of assumption is it
     */
    public Type getType() {
        return type;
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

    public static Assumption of(Type type, String token, String tag, Consumer<SearchQuery> consumer) {
        return new Assumption(type, token, tag, consumer);
    }
}
