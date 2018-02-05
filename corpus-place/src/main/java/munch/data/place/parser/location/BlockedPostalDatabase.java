package munch.data.place.parser.location;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 5/2/2018
 * Time: 6:30 PM
 * Project: munch-data
 */
@Singleton
public final class BlockedPostalDatabase {

    private final Set<String> postalSet;

    @Inject
    public BlockedPostalDatabase() throws IOException {
        URL url = Resources.getResource("location-blocked-postal.txt");
        this.postalSet = ImmutableSet.copyOf(Resources.readLines(url, Charset.forName("UTF-8")));
    }

    public boolean isBlocked(String postal) {
        return postalSet.contains(postal);
    }
}
