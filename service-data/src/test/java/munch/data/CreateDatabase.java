package munch.data;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by: Fuxing
 * Date: 21/8/2017
 * Time: 8:04 AM
 * Project: munch-core
 */
public class CreateDatabase {

    public static void main(String[] args) {
        System.setProperty("postgres.url", "jdbc:postgresql://localhost:5432/munch");
        System.setProperty("postgres.username", "munch");
        System.setProperty("postgres.password", "");
        System.setProperty("postgres.autoCreate", "false");

        Injector injector = Guice.createInjector(new DataModule());
        injector.getInstance(DataApi.class).start();
    }
}
