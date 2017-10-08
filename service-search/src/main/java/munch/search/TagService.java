package munch.search;

import com.fasterxml.jackson.databind.JsonNode;
import munch.data.Tag;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonService;
import munch.search.elastic.ElasticIndex;

import javax.inject.Inject;

/**
 * Created by: Fuxing
 * Date: 27/8/2017
 * Time: 4:47 PM
 * Project: munch-core
 */
public class TagService implements JsonService {
    private final ElasticIndex index;

    @Inject
    public TagService(ElasticIndex index) {
        this.index = index;
    }

    @Override
    public void route() {
        PATH("/tags", () -> {
            PUT("/:cycleNo/:id", this::put);
            DELETE("/:cycleNo/before", this::deleteBefore);
            DELETE("/:cycleNo/:id", this::delete);
        });
    }

    /**
     * @param call json call
     * @return 200 = saved
     */
    private JsonNode put(JsonCall call) throws Exception {
        long cycleNo = call.pathLong("cycleNo");
        Tag tag = call.bodyAsObject(Tag.class);
        index.put(tag, cycleNo);
        return Meta200;
    }

    /**
     * @param call json call
     * @return 200 = deleted
     */
    private JsonNode delete(JsonCall call) throws Exception {
        String id = call.pathString("id");
        index.delete("tag", id);
        return Meta200;
    }

    /**
     * @param call json call
     * @return 200 = deleted
     */
    private JsonNode deleteBefore(JsonCall call) throws Exception {
        long cycleNo = call.pathLong("cycleNo");
        index.deleteBefore("tag", cycleNo);
        return Meta200;
    }
}
