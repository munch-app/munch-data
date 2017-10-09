package munch.data.clients;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import munch.data.structure.SearchQuery;
import munch.data.structure.SearchResult;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 10/7/2017
 * Time: 6:29 PM
 * Project: munch-core
 */
@Singleton
public class SearchClient {

    @Inject
    public SearchClient() {

    }

    /**
     * @param query query object
     * @return List of SearchResult
     * @see SearchQuery
     */
    public List<SearchResult> search(SearchQuery query) {
        return null;
    }

    /**
     * Suggest place data based on name
     *
     * @param size size per list
     * @param text text query
     * @return List of SearchResult
     */
    public List<SearchResult> suggest(String text, @Nullable String latLng, int size) {
        return null;
    }
}
