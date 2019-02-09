package munch.data.service;

import munch.data.elastic.ElasticIndex;
import munch.data.tag.Tag;
import munch.restful.core.KeyUtils;
import munch.restful.server.JsonCall;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:41 PM
 * Project: munch-data
 */
@Singleton
public final class TagService extends PersistenceService<Tag> {

    @Inject
    public TagService(PersistenceMapping persistenceMapping, ElasticIndex elasticIndex) {
        super(persistenceMapping, elasticIndex, Tag.class);
    }

    @Override
    public void route() {
        PATH("/tags", () -> {
            GET("", this::list);
            GET("/:tagId", this::get);

            POST("", this::post);
            PUT("/:tagId", this::put);
            DELETE("/:tagId", this::delete);
            PATCH("/:tagId", call -> patch(call, "count"));
        });
    }

    private Tag post(JsonCall call) {
        Tag tag = call.bodyAsObject(Tag.class);
        tag.setTagId(KeyUtils.randomUUID());
        return put(tag);
    }
}
