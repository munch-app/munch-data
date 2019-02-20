package munch.data;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.airtable.AirtableModule;
import corpus.engine.EngineGroup;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:08 PM
 * Project: munch-data
 */
public final class InterfaceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FakeV2CatalystModule());
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
        Injector injector = Guice.createInjector(new InterfaceModule());
        EngineGroup.start(
                injector.getInstance(TagBridge.class),
                injector.getInstance(LandmarkBridge.class),
                injector.getInstance(AreaBridge.class)

//                injector.getInstance(PlaceBridge.class)
        );
    }
}
