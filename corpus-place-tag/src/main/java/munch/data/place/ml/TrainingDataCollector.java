package munch.data.place.ml;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.CorpusClient;
import corpus.data.DataModule;
import munch.restful.core.JsonUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Set<String> outputSet = new HashSet<>();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("corpus-place-tag/tag-model/tag-data.txt"), "UTF-8"
        ));

        client.list("Sg.Munch.PlaceTagTraining").forEachRemaining(data -> {
            ObjectNode objectNode = JsonUtils.objectMapper.createObjectNode();
            objectNode.put("placeId", data.getCorpusKey());
            ArrayNode topic = objectNode.putArray("topic");
            ArrayNode label = objectNode.putArray("label");

            TrainingPipelineKey.input.getAllValue(data).forEach(topic::add);
            List<String> output = TrainingPipelineKey.output.getAllValue(data);
            output.forEach(label::add);
            outputSet.addAll(output);
            try {
                writer.write(JsonUtils.toString(objectNode));
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        writer.close();

        BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("corpus-place-tag/tag-model/output_class.txt"), "UTF-8"
        ));
        for (String s : outputSet) {
            outputWriter.write(s);
            outputWriter.newLine();
        }
        outputWriter.close();
    }


}
