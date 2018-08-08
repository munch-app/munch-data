package munch.data;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:32 PM
 * Project: munch-data
 */
public final class PlacePluginModule extends AbstractModule {

    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new PlacePluginModule());
        // TODO
    }
}
