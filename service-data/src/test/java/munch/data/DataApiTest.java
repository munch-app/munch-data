package munch.data;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by: Fuxing
 * Date: 26/8/2017
 * Time: 10:50 AM
 * Project: munch-core
 */
class DataApiTest {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new DataModule());
        injector.getInstance(DataApi.class).start(8700);
    }
}