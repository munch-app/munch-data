package munch.data.container;

import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.data.DataModule;
import munch.data.structure.SourcedImage;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 24/12/2017
 * Time: 4:13 PM
 * Project: munch-data
 */
class ContainerCorpusTest {

    @Test
    void data() throws Exception {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");
        Injector injector = Guice.createInjector(new CorpusModule(), new DataModule());
        CorpusClient client = injector.getInstance(CorpusClient.class);
        CorpusData data = client.get("Sg.MunchSheet.Container", "134dd5a6-7a8d-4784-958e-8860d3ebe16c");
        List<SourcedImage> images = ContainerCorpus.collectImages(data);
        System.out.println(images);
    }
}