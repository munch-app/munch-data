package munch.data.assumption;

import munch.data.structure.SearchQuery;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 26/2/18
 * Time: 6:43 PM
 * Project: munch-data
 */
public final class AssumptionQuery {
    private String text;
    private String location;

    private List<AssumptionToken> tokens;
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

    public List<AssumptionToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<AssumptionToken> tokens) {
        this.tokens = tokens;
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
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
