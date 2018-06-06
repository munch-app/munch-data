package munch.data;

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

/**
 * Created by: Fuxing
 * Date: 5/6/18
 * Time: 10:35 AM
 * Project: munch-data
 */
public final class LocationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new CorpusModule());
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
        Injector injector = Guice.createInjector(new LocationModule());
        EngineGroup.start(
                injector.getInstance(LandmarkBridge.class),
                injector.getInstance(AreaBridge.class)
        );
    }
}
