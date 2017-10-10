package munch.catalyst;

import catalyst.CatalystEngine;
import catalyst.CatalystModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.blob.ImageModule;
import corpus.data.DataModule;

/**
 * Created by: Fuxing
 * Date: 28/7/2017
 * Time: 1:48 AM
 * Project: munch-corpus
 */
public class ImageCuratorModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new CatalystModule());
        install(new DataModule());
        install(new ImageModule());
        bind(CatalystEngine.class).to(ImageCuratorCatalyst.class);
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ImageCuratorModule());
        injector.getInstance(ImageCuratorCatalyst.class).run();
    }
}
