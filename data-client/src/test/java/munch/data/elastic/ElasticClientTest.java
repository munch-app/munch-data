package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.core.JsonUtils;
import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 28/2/2018
 * Time: 5:49 PM
 * Project: munch-data
 */
class ElasticClientTest {

    @Test
    void parseError() throws Exception {
        JsonNode jsonNode = JsonUtils.objectMapper.readTree("{\"error\":{\"root_cause\":[{\"type\":\"parse_exception\",\"reason\":\"illegal latitude value [269.9999986588955] for [GeoDistanceSort] for field [location.latLng].\"}],\"type\":\"search_phase_execution_exception\",\"reason\":\"all shards failed\",\"phase\":\"query\",\"grouped\":true,\"failed_shards\":[{\"shard\":0,\"index\":\"munch2\",\"node\":\"pgu-6z8RSt6OITe-Ya5O4Q\",\"reason\":{\"type\":\"parse_exception\",\"reason\":\"illegal latitude value [269.9999986588955] for [GeoDistanceSort] for field [location.latLng].\"}}]},\"status\":400}");
        System.out.println(jsonNode);
    }
}