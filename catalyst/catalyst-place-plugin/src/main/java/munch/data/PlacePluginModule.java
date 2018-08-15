package munch.data;

import catalyst.plugin.PluginRunner;
import catalyst.source.SourceReporter;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import munch.data.catalyst.PlacePlugin;
import munch.data.catalyst.PlaceValidator;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:32 PM
 * Project: munch-data
 */
public final class PlacePluginModule extends AbstractModule {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new PlacePluginModule());

        PluginRunner.run("data.munch.space",
                injector.getInstance(SourceReporter.class),
                injector.getInstance(PlacePlugin.class)
        );

        PlaceValidator validator = injector.getInstance(PlaceValidator.class);
        validator.run();
    }
}
