package munch.search.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 11/7/2017
 * Time: 11:49 PM
 * Project: munch-core
 */
class ElasticMarshallerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void test() throws Exception {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.path("location");
    }
}