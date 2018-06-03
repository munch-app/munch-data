package munch.data.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import munch.data.tag.Tag;
import munch.restful.client.dynamodb.NextNodeList;
import munch.restful.client.dynamodb.RestfulDynamoHashClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:59 PM
 * Project: munch-data
 */
@Singleton
public final class TagClient extends RestfulDynamoHashClient<Tag> {

    @Inject
    public TagClient() {
        this(ConfigFactory.load().getString("services.munch-data.url"));
    }

    TagClient(String url) {
        super(url, Tag.class, "tagId");
    }

    public Tag get(String tagId) {
        return get("/tags/:tagId", tagId);
    }

    public NextNodeList<Tag> list(String nextTagId, int size) {
        return list("/tags", nextTagId, size);
    }

    public Iterator<Tag> list() {
        return list("/tags");
    }

    public Tag post(Tag tag) {
        return doPost("/tags")
                .body(tag)
                .asDataObject(Tag.class);
    }

    public void put(Tag tag) {
        String tagId = Objects.requireNonNull(tag.getTagId());
        put("/tags/:tagId", tagId, tag);
    }

    public Tag delete(String tagId) {
        return delete("/tags/:tagId", tagId);
    }

    public void patch(String tagId, JsonNode body) {
        doPatch("/tags/:tagId")
                .path("tagId", tagId)
                .body(body)
                .asResponse();
    }
}
