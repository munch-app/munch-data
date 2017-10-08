package munch.search;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by: Fuxing
 * Date: 12/9/2017
 * Time: 11:57 AM
 * Project: munch-core
 */
class SearchModuleTest {

    public static void main(String[] args) {
        System.setProperty("services.elastic.url", "http://localhost:9200");
        Injector injector = Guice.createInjector(new SearchModule());
        injector.getInstance(SearchApi.class).start();
    }
}