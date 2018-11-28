package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import munch.data.named.NamedQuery;
import munch.restful.server.dynamodb.RestfulDynamoHashRangeService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 28/11/18
 * Time: 9:44 AM
 * Project: munch-data
 */
@Singleton
public final class NamedQueryService extends RestfulDynamoHashRangeService<NamedQuery> {

    @Inject
    public NamedQueryService(DynamoDB dynamoDB) {
        super(dynamoDB.getTable("munch-data.NamedQuery"), NamedQuery.class,
                "slug", "version");
    }

    @Override
    public void route() {
        PATH("/named/query/:slug/:version", () -> {
            GET("", this::get);
            PUT("", this::put);
        });
    }
}
