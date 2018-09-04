package munch.data.catalyst;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

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
}