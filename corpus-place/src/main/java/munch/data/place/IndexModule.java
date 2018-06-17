package munch.data.place;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import corpus.CorpusModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import io.searchbox.client.JestClient;
import munch.data.dynamodb.DynamoModule;
import munch.data.place.elastic.GraphElasticModule;
import munch.data.place.graph.matcher.MatcherModule;
import munch.data.place.parser.ParserModule;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 7:39 PM
 * Project: munch-data
 */
public class IndexModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());
        install(new DynamoModule());
        install(new munch.data.elastic.ElasticModule());

        install(new GraphElasticModule());
        install(new ParserModule());
        install(new MatcherModule());
    }

    public static void start(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new IndexModule());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(IndexCorpus.class)
        );
        ScheduledThreadUtils.shutdown();
        injector.getInstance(JestClient.class).shutdownClient();
        injector.getInstance(Key.get(JestClient.class, Names.named("munch.data.place.jest"))).shutdownClient();
    }
}
