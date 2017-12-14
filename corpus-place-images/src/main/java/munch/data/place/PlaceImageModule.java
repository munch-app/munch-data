package munch.data.place;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import munch.finn.FinnClient;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:21 PM
 * Project: munch-data
 */
public final class PlaceImageModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    FinnClient provideFinnClient(Config config) {
        return new FinnClient(config.getString("services.finn.url"));
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new PlaceImageModule());
        injector.getInstance(PlaceImageCorpus.class).run();
    }
}
