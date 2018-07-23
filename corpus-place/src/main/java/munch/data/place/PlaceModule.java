package munch.data.place;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.typesafe.config.ConfigFactory;
import corpus.CorpusModule;
import corpus.airtable.AirtableModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import io.searchbox.client.JestClient;
import munch.data.dynamodb.DynamoModule;
import munch.data.place.elastic.GraphElasticModule;
import munch.data.place.graph.matcher.MatcherModule;
import munch.data.place.graph.seeder.DecayAirtableCorpus;
import munch.data.place.parser.ParserModule;
import munch.data.utils.ScheduledThreadUtils;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 7:39 PM
 * Project: munch-data
 */
public class PlaceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());
        install(new DynamoModule());
        install(new munch.data.elastic.ElasticModule());

        install(new GraphElasticModule());
        install(new ParserModule());
        install(new MatcherModule());

        install(new AirtableModule(getAirtableKey()));
    }

    private String getAirtableKey() {
        AWSSimpleSystemsManagement client = AWSSimpleSystemsManagementClientBuilder.defaultClient();
        GetParameterResult result = client.getParameter(new GetParameterRequest()
                .withName("munch-corpus.AirtableKey")
                .withWithDecryption(true));

        return result.getParameter().getValue();
    }

    public static void start(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new PlaceModule());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(PlaceCorpus.class),
                injector.getInstance(ValidationCorpus.class),
                injector.getInstance(DecayAirtableCorpus.class)
        );
        ScheduledThreadUtils.shutdown();
        injector.getInstance(JestClient.class).shutdownClient();
        injector.getInstance(Key.get(JestClient.class, Names.named("munch.data.place.jest"))).shutdownClient();
    }

    public static void main(String[] args) throws InterruptedException {
        String type = ConfigFactory.load().getString("corpus.type");
        switch (type.toLowerCase()) {
            case "place":
                start(args);
                return;
            case "index":
                IndexModule.start(args);
                return;

            default:
                throw new IllegalArgumentException("corpus.type must be 'place' or 'index'");
        }
    }
}
