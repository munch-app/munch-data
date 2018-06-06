package munch.data.cleaner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.restful.core.JsonUtils;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonResult;
import munch.restful.server.JsonService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 6/6/18
 * Time: 6:12 PM
 * Project: munch-data
 */
@Singleton
public class TagCleanerService implements JsonService {

    private final TagCleaner tagCleaner;

    @Inject
    public TagCleanerService(TagCleaner tagCleaner) {
        this.tagCleaner = tagCleaner;
    }

    @Override
    public void route() {
        POST("/clean", this::clean);
    }

    private JsonResult clean(JsonCall call) {
        JsonNode body = call.bodyAsJson();
        List<String> tags = JsonUtils.toList(body.path("tags"), String.class);

        ObjectNode cleaned = JsonUtils.createObjectNode();
        cleaned.set("tags", JsonUtils.toTree(tagCleaner.clean(tags)));
        return result(200, cleaned);
    }
}
