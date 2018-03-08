package munch.data.place;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.airtable.AirtableModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import io.searchbox.client.JestClient;
import munch.data.dynamodb.DynamoModule;
import munch.data.elastic.ElasticModule;
import munch.data.place.elastic.ElasticSyncCorpus;
import munch.data.place.predict.PredictTagModule;
import munch.data.utils.ScheduledThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:21 PM
 * Project: munch-data
 */
public final class PlaceTagModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(PlaceTagModule.class);

    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());

        install(new DynamoModule());
        install(new ElasticModule());

        install(new PredictTagModule());

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
        Injector injector = Guice.createInjector(new PlaceTagModule());
        EngineGroup.start(
                injector.getInstance(PlaceTagCorpus.class),
                injector.getInstance(ElasticSyncCorpus.class)
        );
        ScheduledThreadUtils.shutdown();

        System.exit(0);
        logger.info("Corpus exit status: 0.");

        injector.getInstance(JestClient.class).shutdownClient();
    }
}
