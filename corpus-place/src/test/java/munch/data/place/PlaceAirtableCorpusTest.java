package munch.data.place;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by: Fuxing
 * Date: 10/3/2018
 * Time: 1:49 AM
 * Project: munch-data
 */
class PlaceAirtableCorpusTest {

    @Test
    void name() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        System.out.println(
                simpleDateFormat.format(new Date())
        );
    }
}