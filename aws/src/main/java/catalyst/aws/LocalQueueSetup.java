package catalyst.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 25/7/18
 * Time: 1:03 AM
 * Project: catalyst
 */
@Singleton
public final class LocalQueueSetup {
    private static final Logger logger = LoggerFactory.getLogger(LocalQueueSetup.class);

    private final AmazonSQS amazonSQS;

    @Inject
    public LocalQueueSetup(AmazonSQS amazonSQS) {
        this.amazonSQS = amazonSQS;
    }

    public void setup() {
        create("MunchData_PlaceQueue");
    }

    private void create(String queueName) {
        try {
            GetQueueUrlResult result = amazonSQS.getQueueUrl(queueName);
            logger.info("Found Queue: url: {}", result.getQueueUrl());
        } catch (QueueDoesNotExistException e) {
            try {
                amazonSQS.createQueue(new CreateQueueRequest(queueName)
                        .withAttributes(Map.of(
                                "DelaySeconds", "180", // 180 seconds before processed
                                "MessageRetentionPeriod", "1209600", // Message retention is 14 days
                                "VisibilityTimeout", "3600" // 30 minutes visibility timeout for message in flight
                        ))
                );
            } catch (AmazonSQSException exception) {
                if (exception.getMessage().contains("Queue already exists")) return;
                throw exception;
            }
        }
    }
}
