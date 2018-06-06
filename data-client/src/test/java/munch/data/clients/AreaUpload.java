package munch.data.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Resources;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import munch.restful.core.JsonUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Created by: Fuxing
 * Date: 5/6/18
 * Time: 1:52 AM
 * Project: munch-data
 */
public class AreaUpload {

    public static void main(String[] args) throws IOException, InterruptedException {
        URL url = Resources.getResource("areas.json");
        String json = IOUtils.toString(url, "utf-8");

        JsonNode areas = JsonUtils.readTree(json);

        AirtableApi airtableApi = new AirtableApi("");
        AirtableApi.Base base = airtableApi.base("appERO4wuQ5oJSTxO");
        AirtableApi.Table table = base.table("Area");

        for (JsonNode area : areas) {
            AirtableRecord record = new AirtableRecord();
            area.fields().forEachRemaining(entry -> {
               record.putField(entry.getKey(), entry.getValue());
            });
            table.post(record);
//            Thread.sleep(250);
        }
    }
}
