package munch.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import munch.data.database.PostgresModule;
import munch.restful.server.JsonService;

/**
 * Created by: Fuxing
 * Date: 18/4/2017
 * Time: 4:11 PM
 * Project: munch-core
 */
public class DataModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new PostgresModule());
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
        Injector injector = Guice.createInjector(new DataModule());
        injector.getInstance(DataApi.class).start();
    }
}
