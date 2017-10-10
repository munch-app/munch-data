package munch.catalyst;

import catalyst.CatalystEngine;
import catalyst.CatalystModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.data.DataModule;
import munch.catalyst.street.StreetNameModule;

/**
 * Created by: Fuxing
 * Date: 28/7/2017
 * Time: 1:48 AM
 * Project: munch-corpus
 */
public class LocationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new CatalystModule());
        install(new DataModule());
        install(new StreetNameModule());
        bind(CatalystEngine.class).to(LocationCatalyst.class);
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new LocationModule());
        injector.getInstance(LocationCatalyst.class).run();
    }
}
