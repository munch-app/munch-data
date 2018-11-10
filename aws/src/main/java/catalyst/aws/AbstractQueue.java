package catalyst.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import munch.restful.core.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 25/10/18
 * Time: 7:45 PM
 * Project: catalyst
 */
public abstract class AbstractQueue<T> {

    protected final AmazonSQS amazonSQS;
    protected final String url;

    protected AbstractQueue(AmazonSQS amazonSQS, String url) {
        this.amazonSQS = amazonSQS;
        this.url = url;
    }

    protected void queue(T body) {
        SendMessageRequest request = new SendMessageRequest();
        request.setQueueUrl(url);
        request.setMessageBody(JsonUtils.toString(body));
        request.setDelaySeconds(60);
        amazonSQS.sendMessage(request);
    }

    protected boolean consume(int threads, Consumer<T> consumer, Class<T> clazz) {
        ReceiveMessageResult result = amazonSQS.receiveMessage(
                new ReceiveMessageRequest(url)
                        .withMaxNumberOfMessages(10)
        );
        List<Message> messages = result.getMessages();
        if (messages.isEmpty()) return false;

        ExecutorService service = Executors.newFixedThreadPool(threads);
        List<DeleteMessageBatchRequestEntry> deletes = new ArrayList<>();

        try {
            CompletableFuture[] futures = messages.stream()
                    .map(message -> (Runnable) () -> {
                        consumer.accept(JsonUtils.toObject(message.getBody(), clazz));
                        deletes.add(new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle()));
                    })
                    .map(runnable -> CompletableFuture.runAsync(runnable, service))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
            return true;
        } finally {
            // Always delete those that are completed
            if (!deletes.isEmpty()) {
                amazonSQS.deleteMessageBatch(url, deletes);
            }

            service.shutdown();
        }
    }
}
