package munch.data;

import catalyst.aws.AWSQueueUrlProvider;
import catalyst.aws.AbstractQueue;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 18/10/18
 * Time: 4:27 PM
 * Project: munch-feed
 */
@Singleton
public final class PlaceQueue extends AbstractQueue<PlaceQueue.Body> {

    @Inject
    protected PlaceQueue(AmazonSQS amazonSQS, AWSQueueUrlProvider provider) {
        super(amazonSQS, provider.get("MunchData_PlaceQueue"));
    }

    protected boolean consume(Consumer<Body> consumer) {
        return super.consume(2, consumer, Body.class);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private String placeId;
        private Type type;
        private Long millis;

        public String getPlaceId() {
            return placeId;
        }

        public void setPlaceId(String placeId) {
            this.placeId = placeId;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Long getMillis() {
            return millis;
        }

        public void setMillis(Long millis) {
            this.millis = millis;
        }

        public enum Type {
            Put,
            Delete,
        }
    }
}
