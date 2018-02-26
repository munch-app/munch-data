package munch.data.assumption;

import munch.data.structure.SearchQuery;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 26/2/18
 * Time: 6:43 PM
 * Project: munch-data
 */
public final class AssumedSearchQuery {
    private String text;
    private List<Token> tokens;
    private SearchQuery query;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public SearchQuery getQuery() {
        return query;
    }

    public void setQuery(SearchQuery query) {
        this.query = query;
    }

    public static class TagToken extends Token {
        @Override
        public String getType() {
            return "tag";
        }
    }

    public static class TextToken extends Token {
        @Override
        public String getType() {
            return "text";
        }
    }

    public static abstract class Token {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public abstract String getType();
    }
}
