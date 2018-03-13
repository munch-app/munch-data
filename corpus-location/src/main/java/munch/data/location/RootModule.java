package munch.data.location;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import corpus.CorpusModule;
import corpus.airtable.AirtableModule;
import corpus.data.DataModule;
import corpus.data.DocumentClient;
import corpus.engine.EngineGroup;
import corpus.images.ImageCachedClient;
import corpus.images.ImageClient;
import io.searchbox.client.JestClient;
import munch.data.dynamodb.DynamoModule;
import munch.data.elastic.ElasticModule;
import munch.data.location.container.ContainerCatalyst;
import munch.data.location.container.ContainerCorpus;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 13/3/18
 * Time: 9:54 PM
 * Project: munch-data
 */
@Singleton
public final class RootModule extends AbstractModule {

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

    @Provides
    @Singleton
    ImageCachedClient provideCachedClient(DocumentClient documentClient, ImageClient imageClient) {
        return new ImageCachedClient("munch-container", documentClient, imageClient);
    }

    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new RootModule());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(LandmarkCorpus.class),
                injector.getInstance(LocationCorpus.class),
                injector.getInstance(ContainerCorpus.class),
                injector.getInstance(ContainerCatalyst.class)
        );

        injector.getInstance(JestClient.class).shutdownClient();
    }
}
