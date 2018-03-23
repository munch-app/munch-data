package munch.data.price;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 17/12/2017
 * Time: 8:09 PM
 * Project: munch-data
 */
class PriceExtractorTest {

    @Test
    void extract() throws Exception {
        List<Double> price = PriceExtractor.extract("$10.1t g $15adnd $15 $16.0");
        System.out.println(price);
    }
}