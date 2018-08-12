package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import munch.data.place.PlaceAward;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonResult;
import munch.restful.server.dynamodb.RestfulDynamoHashRangeService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 4/8/18
 * Time: 1:16 AM
 * Project: munch-data
 */
@Singleton
public final class PlaceAwardService extends RestfulDynamoHashRangeService<PlaceAward> {

    private final Index sortIndex;

    @Inject
    PlaceAwardService(DynamoDB dynamoDB) {
        super(dynamoDB.getTable("munch-data.v4.PlaceAward"), PlaceAward.class, "placeId", "awardId");
        this.sortIndex = table.getIndex("sort");
    }

    @Override
    public void route() {
        PATH("/places/:placeId/awards", () -> {
            GET("", this::list);
            GET("/:awardId", this::get);
            PUT("/:awardId", this::put);
            DELETE("/:awardId", this::delete);
        });
    }

    public JsonResult list(JsonCall call) {
        return list(sortIndex, hashName, "sort", call);
    }
}
