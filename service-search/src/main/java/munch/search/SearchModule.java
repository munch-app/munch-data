package munch.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import munch.restful.server.JsonService;
import munch.search.elastic.ElasticModule;

/**
 * Created by: Fuxing
 * Date: 6/7/2017
 * Time: 6:16 AM
 * Project: munch-core
 */
public class SearchModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new ElasticModule());
    }

    @Provides
    Config provideConfig() {
        return ConfigFactory.load();
    }

    @Provides
    @Singleton
    ObjectMapper provideObjectMapper() {
        return JsonService.objectMapper;
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new SearchModule());
        injector.getInstance(SearchApi.class).start();
    }
}

