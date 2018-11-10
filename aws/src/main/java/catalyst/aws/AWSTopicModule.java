package catalyst.aws;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 18/10/18
 * Time: 4:22 PM
 * Project: catalyst
 */
public final class AWSTopicModule extends AbstractModule {

    @Provides
    @Singleton
    AmazonSNS provideSNS() {
        return AmazonSNSClientBuilder.defaultClient();
    }
}
