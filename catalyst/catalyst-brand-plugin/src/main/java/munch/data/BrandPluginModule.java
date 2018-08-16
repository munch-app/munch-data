package munch.data;

import catalyst.plugin.PluginRunner;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import munch.data.catalyst.BrandPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by: Fuxing
 * Date: 15/8/18
 * Time: 11:36 PM
 * Project: munch-data
 */
public final class BrandPluginModule extends AbstractModule {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new BrandPluginModule());

        injector.getInstance(PluginRunner.class).run(
                injector.getInstance(BrandPlugin.class)
        );

        System.exit(0);
    }
}
