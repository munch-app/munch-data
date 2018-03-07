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
    private String location;
    private long resultCount;

    private List<Token> tokens;
    private SearchQuery searchQuery;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * @return resultCount of total possible results for this search query
     */
    public long getResultCount() {
        return resultCount;
    }

    public void setResultCount(long resultCount) {
        this.resultCount = resultCount;
    }

    public static class TagToken extends Token {

        public TagToken(String text) {
            setText(text);
        }

        @Override
        public String getType() {
            return "tag";
        }
    }

    public static class TextToken extends Token {
        public TextToken(String text) {
            setText(text);
        }

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

        @Override
        public String toString() {
            return getType() + "[" + text + "]";
        }
    }

    @Override
    public String toString() {
        return "AssumedSearchQuery{" +
                "text='" + text + '\'' +
                ", tokens=" + tokens +
                ", searchQuery=" + searchQuery +
                '}';
    }
}
