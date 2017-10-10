package munch.data;

import com.google.inject.AbstractModule;
import munch.data.dynamodb.DynamoModule;
import munch.data.elastic.ElasticModule;

/**
 * Created by: Fuxing
 * Date: 10/10/17
 * Time: 8:06 PM
 * Project: munch-data
 */
public final class MunchDataModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new DynamoModule());
        install(new ElasticModule());
    }
}
