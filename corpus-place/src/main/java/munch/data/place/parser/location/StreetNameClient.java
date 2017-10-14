package munch.data.place.parser.location;

import catalyst.utils.exception.Retriable;
import catalyst.utils.exception.SleepRetriable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.dudie.nominatim.client.NominatimClient;
import fr.dudie.nominatim.model.Address;
import fr.dudie.nominatim.model.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Fuxing
 * Date: 6/7/2017
 * Time: 5:14 AM
 * Project: munch-corpus
 */
@Singleton
public final class StreetNameClient {
    private static final Logger logger = LoggerFactory.getLogger(StreetNameClient.class);
    private final Retriable retriable = new SleepRetriable(10, TimeUnit.SECONDS, 15);

    private final NominatimClient nominatimClient;

    @Inject
    public StreetNameClient(NominatimClient nominatimClient) {
        this.nominatimClient = nominatimClient;
    }

    /**
     * @param lat lat of place
     * @param lng lng of place
     * @return Street Name if found
     */
    @Nullable
    public String getStreet(double lat, double lng) {
        try {
            Address address = retriable.loop(() -> nominatimClient.getAddress(lng, lat));
            if (address == null) return null;

            Element[] elements = address.getAddressElements();
            if (elements == null || elements.length == 0) return null;

            // Find road value
            for (Element element : address.getAddressElements()) {
                if (element.getKey().equals("road")) return element.getValue();
            }

            return null;
        } catch (IOException ioe) {
            logger.error("Failed to reverse geocode", ioe);
            throw new RuntimeException(ioe);
        }
    }
}
