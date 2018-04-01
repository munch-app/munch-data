package munch.data.place.graph;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.DataModule;
import munch.data.place.elastic.GraphElasticModule;
import munch.data.place.graph.matcher.MatcherModule;
import munch.data.place.parser.ParserModule;

/**
 * Created by: Fuxing
 * Date: 31/3/2018
 * Time: 2:48 AM
 * Project: munch-data
 */
class PlaceGraphTest extends AbstractModule {
    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());

        install(new GraphElasticModule());
        install(new ParserModule());
        install(new MatcherModule());
    }

    public static void main(String[] args) {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");
        System.setProperty("services.elastic.url", "http://localhost:9200");
        System.setProperty("services.location.url", "http://localhost:9200");

        Injector injector = Guice.createInjector(new PlaceGraphTest());

        String placeId = "81bf2e1c-14bb-4032-9d19-8d7ee5f9964f";

        CorpusClient corpusClient = injector.getInstance(CorpusClient.class);
        CatalystClient catalystClient = injector.getInstance(CatalystClient.class);
        PlaceGraph placeGraph = injector.getInstance(PlaceGraph.class);

        PlaceTree tree = new PlaceTree("seed", corpusClient.get("Sg.MunchSheet.PlaceInfo2", "recj3OR54YTb5nnws"));
        PlaceGraph.Result result = placeGraph.search(placeId, tree, Lists.newArrayList(catalystClient.listCorpus(placeId)));

        for (PlaceGraph.Action action : result.actions) {
            System.out.println(action);
        }
    }
}