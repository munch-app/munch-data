package munch.data.place;

import com.fasterxml.jackson.databind.ObjectMapper;
import corpus.airtable.AirtableApi;
import munch.data.clients.PlaceClient;
import munch.restful.core.JsonUtils;

/**
 * Created by: Fuxing
 * Date: 4/3/2018
 * Time: 2:55 AM
 * Project: munch-data
 */
public class AirtableDatabase {

    private final ObjectMapper mapper = JsonUtils.objectMapper;

    private final PlaceClient placeClient;
    private final AirtableApi.Base airtableBase;
    private final AirtableApi.Table indexTable;

    public AirtableDatabase(PlaceClient placeClient, AirtableApi airtableApi) {
        this.placeClient = placeClient;
        this.airtableBase = airtableApi.base("appyZ1jrMU3w0G53V");
        this.indexTable = airtableBase.table("Index");
    }
}
