package munch.data;

import catalyst.plugin.PluginRunner;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import munch.data.catalyst.PlacePlugin;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:32 PM
 * Project: munch-data
 */
public final class PlacePluginModule extends AbstractModule {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new PlacePluginModule());
        injector.getInstance(PluginRunner.class).run(
                injector.getInstance(PlacePlugin.class)
        );

        System.exit(0);
    }
}
