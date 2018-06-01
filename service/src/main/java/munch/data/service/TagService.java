package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.fasterxml.jackson.databind.JsonNode;
import munch.data.elastic.ElasticIndex;
import munch.data.tag.Tag;
import munch.data.tag.TagConfig;
import munch.restful.core.JsonUtils;
import munch.restful.core.KeyUtils;
import munch.restful.server.JsonCall;
import munch.restful.server.dynamodb.RestfulDynamoHashService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:41 PM
 * Project: munch-data
 */
@Singleton
public final class TagService extends RestfulDynamoHashService<Tag> {

    private final ElasticIndex elasticIndex;
    private final ConfigService configService;

    @Inject
    public TagService(DynamoDB dynamoDB, ElasticIndex elasticIndex) {
        super(dynamoDB.getTable("munch-data.Tag"), Tag.class, "tagId", 100);
        this.elasticIndex = elasticIndex;
        this.configService = new ConfigService(dynamoDB);
    }

    @Override
    public void route() {
        PATH("/tags", () -> {
            GET("", this::list);
            POST("", this::post);

            GET("/:tagId", this::get);
            PUT("/:tagId", this::put);
            DELETE("/:tagId", this::delete);

            // Config Service
            configService.route();
        });
    }

    public JsonNode list(JsonCall call) {
        return super.list(call);
    }

    public Tag get(JsonCall call) {
        return super.get(call);
    }

    private JsonNode post(JsonCall call) {
        String tagId = KeyUtils.randomUUIDBase64();
        Tag tag = call.bodyAsObject(Tag.class);
        tag.setTagId(tagId);
        tag.setCreatedMillis(System.currentTimeMillis());
        tag.setUpdatedMillis(System.currentTimeMillis());
        return put(tag);
    }

    public JsonNode put(JsonCall call) {
        String tagId = call.pathString("tagId");
        Tag tag = call.bodyAsObject(Tag.class);
        tag.setTagId(tagId);
        tag.setUpdatedMillis(System.currentTimeMillis());

        return put(tag);
    }

    public Tag delete(JsonCall call) {
        String tagId = call.pathString("tagId");
        elasticIndex.delete("Tag", tagId);
        return super.delete(tagId);
    }

    private JsonNode put(Tag tag) {
        // Auto add name to names for first put
        if (tag.getNames() == null) tag.setNames(new HashSet<>());
        tag.getNames().add(tag.getName());

        elasticIndex.put(tag);
        return super.put(tag.getTagId(), JsonUtils.toTree(tag));
    }

    private static class ConfigService extends RestfulDynamoHashService<TagConfig> {

        private ConfigService(DynamoDB dynamoDB) {
            super(dynamoDB.getTable("munch-data.TagConfig"), TagConfig.class, "tagId");
        }

        @Override
        public void route() {
            GET("/:tagId/config", this::get);
            PUT("/:tagId/config", this::put);
            DELETE("/:tagId/config", this::delete);
        }
    }
}
