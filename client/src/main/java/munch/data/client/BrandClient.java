package munch.data.client;

import com.typesafe.config.ConfigFactory;
import munch.data.brand.Brand;
import munch.restful.core.NextNodeList;
import munch.restful.client.dynamodb.RestfulDynamoHashClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Created by: Bing Hwang
 * Date: 9/7/18
 * Time: 12:27 PM
 * Project: munch-data
 */
@Singleton
public final class BrandClient extends RestfulDynamoHashClient<Brand> {

    @Inject
    public BrandClient() {
        this(ConfigFactory.load().getString("services.munch-data.url"));
    }

    public BrandClient(String url) {
        super(url, Brand.class, "brandId");
    }

    public Brand get(String brandId) {
        return doGet("/brands/:brandId", brandId);
    }

    public NextNodeList<Brand> list(String nextBrandId, int size) {
        return doList("/v4.0/brands", nextBrandId, size);
    }

    public Iterator<Brand> iterator() {
        return doIterator("/brands", 30);
    }

    public Brand post(Brand brand) {
        return doPost("/brands")
                .body(brand)
                .asDataObject(Brand.class);
    }

    public void put(Brand brand) {
        String BrandId = Objects.requireNonNull(brand.getBrandId());
        doPut("/brands/:brandId", BrandId, brand);
    }

    public Brand delete(String brandId) {
        return doDelete("/brands/:brandId", brandId);
    }
}
