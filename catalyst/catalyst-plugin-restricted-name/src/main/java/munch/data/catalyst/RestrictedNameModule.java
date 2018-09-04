package munch.data.catalyst;

import catalyst.airtable.AirtableModule;
import catalyst.plugin.PluginRunner;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:32 PM
 * Project: munch-data
 */
public final class RestrictedNameModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new AirtableModule(getAirtableKey()));
    }

    private String getAirtableKey() {
        AWSSimpleSystemsManagement client = AWSSimpleSystemsManagementClientBuilder.defaultClient();
        GetParameterResult result = client.getParameter(new GetParameterRequest()
                .withName("munch-corpus.AirtableKey")
                .withWithDecryption(true));

        return result.getParameter().getValue();
    }


    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new RestrictedNameModule());
        injector.getInstance(PluginRunner.class).run(
                injector.getInstance(RestrictedNamePlugin.class)
        );
    }
}
