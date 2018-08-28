package munch.data.catalyst;

import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.restful.core.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by: Fuxing
 * Date: 28/8/2018
 * Time: 2:55 PM
 * Project: munch-data
 */
class RestrictedNamePluginTest {

    @Test
    void validate() throws Exception {
        Pattern pattern = Pattern.compile("(^|\\s|[^a-z0-9])" + "contain" + "($|\\s|[^a-z0-9])",Pattern.CASE_INSENSITIVE);
        Assertions.assertTrue(pattern.matcher("contain").find());
        Assertions.assertTrue(pattern.matcher("the contain is").find());
        Assertions.assertTrue(pattern.matcher("contain a").find());
        Assertions.assertTrue(pattern.matcher("contain abd").find());
        Assertions.assertTrue(pattern.matcher("cdontain abd contain").find());

        Assertions.assertTrue(pattern.matcher("cdontain abd contain's").find());

        Assertions.assertFalse(pattern.matcher("cdontain abd contains").find());
    }

    @Test
    void queryString() throws Exception {
        ObjectNode queryString = RestrictedNamePlugin.queryString(List.of("equals"), List.of("contains"));
        String string = JsonUtils.objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(queryString);
        System.out.println(string);
    }
}