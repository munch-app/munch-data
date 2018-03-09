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
import munch.data.utils.ScheduledThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 7:33 PM
 * Project: munch-data
 */
public class PlaceSuggestModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(PlaceSuggestModule.class);

    @Override
    protected void configure() {
        install(new DynamoModule());
        install(new CorpusModule());
        install(new ElasticModule());
        install(new DataModule());

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
        Injector injector = Guice.createInjector(new PlaceSuggestModule());
        EngineGroup.start(
                // TODO
        );
        ScheduledThreadUtils.shutdown();
    }
}
