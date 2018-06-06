package munch.data.service;

import munch.data.elastic.ElasticIndex;
import munch.data.tag.Tag;
import munch.restful.core.KeyUtils;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonResult;

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

    private JsonResult post(JsonCall call) {
        Tag tag = call.bodyAsObject(Tag.class);
        tag.setTagId(KeyUtils.randomUUIDBase64());
        return put(tag);
    }

    @Override
    protected JsonResult put(Tag tag) {
        // Auto add name to names for first put
        if (tag.getNames() == null) tag.setNames(new HashSet<>());
        tag.getNames().add(tag.getName());
        return super.put(tag);
    }
}
