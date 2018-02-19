package munch.data.place.suggest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.typesafe.config.Config;
import munch.restful.client.RestfulClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 19/2/2018
 * Time: 8:29 PM
 * Project: munch-data
 */
@Singleton
public final class PredictTagClient extends RestfulClient {

    @Inject
    public PredictTagClient(Config config) {
        this(config.getString("services.tag-predict.url"));
    }

    public PredictTagClient(String url) {
        super(url);
    }

    public Map<String, Double> predict(List<String> texts) {
        String text = Joiner.on(" ").join(texts);
        return predict(text);
    }

    public Map<String, Double> predict(String texts) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("texts", texts);

        JsonNode dataNode = doPost("/predict")
                .body(texts)
                .asResponse()
                .getDataNode();

        Map<String, Double> labels = new HashMap<>();
        dataNode.fields().forEachRemaining(entry -> {
            labels.put(entry.getKey(), entry.getValue().asDouble());
        });
        return labels;
    }
}
