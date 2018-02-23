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
import munch.data.dynamodb.DynamoModule;
import munch.data.elastic.ElasticModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 7:33 PM
 * Project: munch-data
 */
public class PlaceTrackingModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(PlaceTrackingModule.class);

    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());
        install(new DynamoModule());
        install(new ElasticModule());

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
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logger.error("Uncaught Exceptions: ", e.getCause());
            logger.error("Trace: {}", (Object) e.getStackTrace());
        });

        Injector injector = Guice.createInjector(new PlaceTrackingModule());
        EngineGroup.start(
                injector.getInstance(PlaceTrackingCorpus.class)
        );
    }
}