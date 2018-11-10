package catalyst.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.typesafe.config.ConfigFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 27/7/18
 * Time: 7:22 PM
 * Project: catalyst
 */
@Singleton
public class AWSQueueUrlProvider {

    private final AmazonSQS amazonSQS;
    private final boolean local;

    @Inject
    public AWSQueueUrlProvider(AmazonSQS amazonSQS) {
        this.amazonSQS = amazonSQS;
        this.local = isLocal();
    }

    @NotNull
    public String get(String name) {
        String url = amazonSQS.getQueueUrl(name).getQueueUrl();
        if (!local) return Objects.requireNonNull(url);
        return url.replace("localstack", "localhost");
    }

    private static boolean isLocal() {
        if (ConfigFactory.load().hasPath("services.sqs.url")) {
            String url = ConfigFactory.load().getString("services.sqs.url");
            return url.contains("//localhost");
        }
        return false;
    }
}
