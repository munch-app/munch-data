package munch.data.location.container;

import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.data.DataModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by: Fuxing
 * Date: 12/5/18
 * Time: 8:03 PM
 * Project: munch-data
 */
class ContainerUtilsTest {

    @Test
    void name() {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");
        Injector injector = Guice.createInjector(new DataModule());

        CorpusClient client = injector.getInstance(CorpusClient.class);
        CorpusData data = client.get("Sg.Munch.Location.Container", "d5a37e7c-76e0-4fe5-afd8-7ddd6526cbcf");
        System.out.println(ContainerUtils.createContainer(data));
    }
}