package munch.data.place.graph;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.DataModule;
import munch.data.place.elastic.GraphElasticModule;
import munch.data.place.graph.matcher.MatcherModule;
import munch.data.place.parser.ParserModule;

/**
 * Created by: Fuxing
 * Date: 2/4/2018
 * Time: 4:44 PM
 * Project: munch-data
 */
public class PlaceGraphTestModule extends AbstractModule {

    @Override
    protected void configure() {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");
        System.setProperty("services.elastic.url", "http://localhost:9200");
        System.setProperty("services.location.url", "http://localhost:9200");

        install(new CorpusModule());
        install(new DataModule());

        install(new GraphElasticModule());
        install(new ParserModule());
        install(new MatcherModule());
    }

    public static Injector getInjector() {
        return Guice.createInjector(new PlaceGraphTestModule());
    }
}
