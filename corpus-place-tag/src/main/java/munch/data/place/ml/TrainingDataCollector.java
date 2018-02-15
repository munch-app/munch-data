package munch.data.place.ml;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.CorpusClient;
import corpus.data.DataModule;
import munch.restful.core.JsonUtils;

import java.io.*;

/**
 * Created by: Fuxing
 * Date: 15/2/2018
 * Time: 7:08 PM
 * Project: munch-data
 */
public class TrainingDataCollector {

    public static void main(String[] args) throws IOException {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");

        Injector injector = Guice.createInjector(new CorpusModule(), new DataModule());
        CorpusClient client = injector.getInstance(CorpusClient.class);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("corpus-place-tag/tag-model/tag-data.txt"), "UTF-8"
        ));

        client.list("Sg.Munch.PlaceTagTraining").forEachRemaining(data -> {
            ObjectNode objectNode = JsonUtils.objectMapper.createObjectNode();
            ArrayNode outputs = objectNode.putArray("outputs");
            ObjectNode inputs = objectNode.putObject("inputs");

            TrainingPipelineKey.output.getAllValue(data).forEach(outputs::add);
            TrainingPipelineKey.input.getAll(data).forEach(field -> {
                inputs.put(field.getValue(), Integer.parseInt(field.getMetadata().get("count")));
            });
            try {
                writer.write(JsonUtils.toString(objectNode));
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        writer.close();
    }


}
