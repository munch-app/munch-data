package munch.data.catalyst;

import catalyst.plugin.PluginRunner;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:32 PM
 * Project: munch-data
 */
public final class RestrictedAreaPluginModule extends AbstractModule {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new RestrictedAreaPluginModule());
        injector.getInstance(PluginRunner.class).run(
                injector.getInstance(RestrictedAreaPlugin.class)
        );
    }
}
