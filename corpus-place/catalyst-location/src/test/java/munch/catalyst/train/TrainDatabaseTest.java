package munch.catalyst.train;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created By: Fuxing Loh
 * Date: 2/9/2017
 * Time: 4:27 PM
 * Project: munch-corpus
 */
class TrainDatabaseTest {

    TrainDatabase database;

    @BeforeEach
    void setUp() throws Exception {
        database = new TrainDatabase(ConfigFactory.load());
        database.sync();
    }

    @Test
    void latLng() throws Exception {
        TrainStation nearest = database.findNearest(1.297886, 103.787107);
        System.out.println(nearest);
    }
}