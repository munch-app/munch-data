package munch.data.cleaner;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import munch.restful.server.RestfulServer;

/**
 * Created by: Fuxing
 * Date: 6/6/18
 * Time: 6:11 PM
 * Project: munch-data
 */
public class TagCleanerModule extends AbstractModule {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new TagCleanerModule());
        RestfulServer.start(
                injector.getInstance(TagCleanerService.class)
        );
    }
}
