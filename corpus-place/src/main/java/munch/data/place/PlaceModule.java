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
import corpus.CorpusModule;
import corpus.airtable.AirtableModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import io.searchbox.client.JestClient;
import munch.data.dynamodb.DynamoModule;
import munch.data.place.elastic.GraphElasticModule;
import munch.data.place.graph.ProcessingCorpus;
import munch.data.place.graph.matcher.MatcherModule;
import munch.data.place.graph.seeder.DecayAirtableCorpus;
import munch.data.place.parser.ParserModule;
import munch.data.utils.ScheduledThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 7:39 PM
 * Project: munch-data
 */
public class PlaceModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(PlaceModule.class);

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

    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new PlaceModule());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(ProcessingCorpus.class),
                injector.getInstance(DecayAirtableCorpus.class),
                injector.getInstance(PlaceAirtableCorpus.class)
        );
        ScheduledThreadUtils.shutdown();
        injector.getInstance(JestClient.class).shutdownClient();
        injector.getInstance(Key.get(JestClient.class, Names.named("munch.data.place.jest"))).shutdownClient();

        logger.info("Corpus should shutdown.");
        Thread.getAllStackTraces().forEach((thread, stackTraceElements) -> {
            logger.error("Thread: {} {}", thread.getName(), Arrays.toString(stackTraceElements));
        });
    }
}
