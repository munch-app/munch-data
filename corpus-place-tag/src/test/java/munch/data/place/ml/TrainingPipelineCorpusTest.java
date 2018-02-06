package munch.data.place.ml;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 6/2/18
 * Time: 3:14 PM
 * Project: munch-data
 */
class TrainingPipelineCorpusTest {

    @Test
    void removeAll() {
        List<String> list = new ArrayList<>();
        list.add("restaurant");
        list.add("chinese");

        System.out.println(list);

        list.removeAll(Set.of("restaurant"));

        System.out.println(list);
    }
}