package catalyst.aws;

import com.typesafe.config.ConfigFactory;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Created by: Fuxing
 * Date: 18/10/18
 * Time: 2:44 AM
 * Project: catalyst
 */
@Singleton
public final class AWSSNSArnProvider {
    private static final String ARN = "arn:aws:sns:ap-southeast-1:197547471367:";

    @NotNull
    public String get(String name) {
        return ARN + name;
    }

    private static boolean isLocal() {
        if (ConfigFactory.load().hasPath("services.sns.url")) {
            String url = ConfigFactory.load().getString("services.sns.url");
            return url.contains("//localhost");
        }
        return false;
    }
}
