package munch.data.cleaner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import munch.data.place.Place;
import munch.restful.client.RestfulClient;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 7/6/18
 * Time: 11:02 AM
 * Project: munch-data
 */
@Singleton
public final class TagCleanerClient extends RestfulClient {

    @Inject
    public TagCleanerClient() {
        super(ConfigFactory.load().getString("services.cleaner.tag.url"));
    }

    /**
     * @param tags to clean
     * @return List of Place.Tag
     */
    public List<Place.Tag> clean(List<String> tags) {
        ObjectNode body = JsonUtils.createObjectNode();
        JsonNode dataNode = doPost("/clean")
                .body(body)
                .asResponse()
                .getDataNode();

        return JsonUtils.toList(dataNode.path("tags"), Place.Tag.class);
    }
}
