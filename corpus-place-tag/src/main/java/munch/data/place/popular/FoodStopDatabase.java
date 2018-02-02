package munch.data.place.popular;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 2/2/18
 * Time: 5:13 PM
 * Project: munch-data
 */
@Singleton
public final class FoodStopDatabase {
    private final Set<String> tags = new HashSet<>();

    @Inject
    public FoodStopDatabase() throws IOException {
        URL resource = Resources.getResource("stopword-food.txt");
        FileUtils.readLines(new File(resource.getFile()), "utf-8").forEach(s -> {
            tags.add(s.toLowerCase());
        });
    }

    public boolean is(String text) {
        return tags.contains(text.toLowerCase());
    }
}
