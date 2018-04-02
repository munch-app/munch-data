package munch.data.place.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Injector;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.data.DocumentClient;
import munch.data.place.graph.PlaceGraphTestModule;
import munch.data.place.graph.PlaceTree;
import munch.data.place.graph.RootPlaceTree;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 2/4/2018
 * Time: 5:13 PM
 * Project: munch-data
 */
public class ElasticIndexTest {
    private static final Logger logger = LoggerFactory.getLogger(ElasticIndexTest.class);

    private final CorpusClient corpusClient;
    private final DocumentClient documentClient;
    private final ElasticClient elasticClient;

    @Inject
    public ElasticIndexTest(CorpusClient corpusClient, DocumentClient documentClient, ElasticClient elasticClient) {
        this.corpusClient = corpusClient;
        this.documentClient = documentClient;
        this.elasticClient = elasticClient;
    }

    public List<CorpusData> search(PlaceTree placeTree, JsonNode... filters) {
        return elasticClient.search(placeTree, filters);
    }

    public void index(String corpusName, int size) {
        int i = 0;
        Iterator<CorpusData> iterator = corpusClient.list(corpusName);
        while (iterator.hasNext() && i++ < size) {
            CorpusData next = iterator.next();
            RootPlaceTree rootTree = get(next.getCatalystId());
            elasticClient.put(100, next, rootTree != null ? rootTree.getTree() : null);
            if (i % 100 == 0) logger.info("Indexed {} {} data", i, corpusName);
        }

        logger.info("Finished Indexed {} {} data", i - 1, corpusName);
    }

    private RootPlaceTree get(String placeId) {
        JsonNode node = documentClient.get("Sg.Munch.Place.Tree.V2", placeId, "0");
        if (node == null) return null;

        return JsonUtils.toObject(node, RootPlaceTree.class);
    }

    public static void main(String[] args) {
        Injector injector = PlaceGraphTestModule.getInjector();
        ElasticIndexTest indexTest = injector.getInstance(ElasticIndexTest.class);

        //        indexTest.index("Global.MunchArticle.Article", 20_000);

        List<CorpusData> results = indexTest.search(new PlaceTree(), ElasticClient.filterTerm("Place.Location.postal", "389468"));
        for (CorpusData result : results) {
            System.out.println(result);
        }
    }
}
