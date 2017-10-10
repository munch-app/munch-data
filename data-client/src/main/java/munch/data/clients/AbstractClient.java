package munch.data.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import munch.restful.core.JsonUtils;
import munch.restful.core.exception.JsonException;

import java.io.IOException;

/**
 * Created by: Fuxing
 * Date: 10/10/17
 * Time: 8:11 PM
 * Project: munch-data
 */
abstract class AbstractClient {
    private static final ObjectMapper objectMapper = JsonUtils.objectMapper;


    static String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }
}
