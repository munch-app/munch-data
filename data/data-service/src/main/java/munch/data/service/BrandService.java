package munch.data.service;

import munch.data.brand.Brand;
import munch.data.elastic.ElasticIndex;
import munch.restful.core.KeyUtils;
import munch.restful.core.exception.ValidationException;
import munch.restful.server.JsonCall;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Bing Hwang
 * Date: 9/7/18
 * Time: 10:45 AM
 * Project: munch-data
 */
@Singleton
public final class BrandService extends PersistenceService<Brand> {

    @Inject
    public BrandService(PersistenceMapping persistenceMapping, ElasticIndex elasticIndex) {
        super(persistenceMapping, elasticIndex, Brand.class);
    }

    @Override
    public void route() {
        PATH("/brands", () -> {
            GET("", this::list);
            GET("/:brandId", this::get);

            POST("", this::post);
            PUT("/:brandId", this::put);
            DELETE("/:brandId", this::delete);
        });
    }

    private Brand post(JsonCall call) {
        Brand brand = call.bodyAsObject(Brand.class);
        brand.setBrandId(KeyUtils.randomUUID());
        return put(brand);
    }

    @Override
    public Brand put(Brand object) {
        // Location Country is required not to be blank because Brand Plugin requires it
        ValidationException.requireNonNull("location.country", object.getLocation().getCountry());
        return super.put(object);
    }
}
