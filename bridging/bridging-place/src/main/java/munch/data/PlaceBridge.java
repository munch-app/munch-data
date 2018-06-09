package munch.data;

import com.google.common.collect.Iterators;
import corpus.engine.AbstractEngine;
import munch.data.cleaner.TagCleanerClient;
import munch.data.client.PlaceClient;
import munch.data.place.Place;
import munch.data.resolver.LandmarkResolverClient;
import munch.file.Image;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 6/6/18
 * Time: 3:00 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceBridge extends AbstractEngine<Object> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceBridge.class);

    private final munch.data.client.PlaceClient newClient;
    private final munch.data.clients.PlaceClient oldClient;

    private final TagCleanerClient tagClient;
    private final LandmarkResolverClient landmarkClient;

    @Inject
    public PlaceBridge(PlaceClient newClient, munch.data.clients.PlaceClient oldClient, TagCleanerClient tagClient, LandmarkResolverClient landmarkClient) {
        super(logger);
        this.newClient = newClient;
        this.oldClient = oldClient;
        this.tagClient = tagClient;
        this.landmarkClient = landmarkClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(24);
    }

    @Override
    protected Iterator<Object> fetch(long cycleNo) {
        return Iterators.concat(oldClient.list(), newClient.list());
    }

    @Override
    protected void process(long cycleNo, Object data, long processed) {
        if (data instanceof munch.data.structure.Place) {
            // From OLD
            Place converted = convert((munch.data.structure.Place) data);
            newClient.put(converted);
            sleep(200);
        } else {
            // From NEW
            Place place = (Place) data;
            // Don't exists anymore
            if (oldClient.get(place.getPlaceId()) == null) {
                newClient.delete(place.getPlaceId());
            }
        }
    }

    private munch.data.place.Place convert(munch.data.structure.Place old) {
        Place place = new Place();
        place.setPlaceId(old.getId());
        place.setName(old.getName());
        place.setNames(old.getAllNames());
        place.setWebsite(old.getWebsite());
        place.setDescription(old.getDescription());
        place.setRanking(old.getRanking());
        place.setCreatedMillis(old.getCreatedDate().getTime());
        place.setUpdatedMillis(old.getUpdatedDate().getTime());

        place.setLocation(convert(old.getLocation()));
        place.setHours(convertHours(old.getHours()));
        place.setTags(convertTags(old.getTag().getImplicits()));
        place.setImages(convertImages(old.getImages()));

        place.setStatus(new Place.Status());
        place.getStatus().setType(old.isOpen() ? Place.Status.Type.open : Place.Status.Type.closed);
        place.getStatus().setUpdatedMillis(System.currentTimeMillis());

        if (old.getPrice() != null && old.getPrice().getMiddle() != null) {
            Place.Price price = new Place.Price();
            price.setPerPax(old.getPrice().getMiddle());
            place.setPrice(price);
        }

        if (old.getMenuUrl() != null) {
            Place.Menu menu = new Place.Menu();
            menu.setUrl(old.getMenuUrl());
            place.setMenu(menu);
        }

        return place;
    }

    private Location convert(munch.data.structure.Place.Location old) {
        Location location = new Location();
        location.setAddress(old.getAddress());
        location.setStreet(old.getStreet());
        location.setUnitNumber(old.getUnitNumber());
        location.setNeighbourhood(old.getNeighbourhood());

        location.setCity(old.getCity());
        location.setCountry(old.getCountry());
        location.setPostcode(old.getPostal());
        location.setLatLng(old.getLatLng());
        location.setLandmarks(landmarkClient.resolve(old.getLatLng()));
        return location;
    }

    private List<Place.Tag> convertTags(List<String> tags) {
        return tagClient.clean(tags);
    }

    private List<Hour> convertHours(List<munch.data.structure.Place.Hour> oldList) {
        return oldList.stream()
                .map(old -> {
                    Hour.Day day = EnumUtils.getEnum(Hour.Day.class, old.getDay());
                    if (day == null) return null;

                    Hour hour = new Hour();
                    hour.setOpen(old.getOpen());
                    hour.setClose(old.getClose());
                    hour.setDay(day);
                    return hour;
                })
                .collect(Collectors.toList());
    }

    private List<Image> convertImages(List<munch.data.structure.SourcedImage> oldList) {
        return oldList.stream()
                .map(sourcedImage -> {
                    List<Image.Size> sizes = new ArrayList<>();
                    sourcedImage.getImages().forEach((wh, url) -> {
                        Image.Size size = new Image.Size();
                        size.setUrl(url);

                        if (wh.equals("original")) {
                            // Temporary Solution
                            size.setWidth(1000);
                            size.setHeight(1000);
                        }else {
                            String[] widthHeight = wh.split("x");
                            size.setWidth(Integer.parseInt(widthHeight[0]));
                            size.setHeight(Integer.parseInt(widthHeight[1]));
                        }
                        sizes.add(size);
                    });

                    Image.Profile profile = new Image.Profile();
                    profile.setId(sourcedImage.getSourceId());
                    profile.setName(sourcedImage.getSourceName());
                    profile.setType(sourcedImage.getSource());

                    Image image = new Image();
                    image.setSizes(sizes);
                    image.setProfile(profile);
                    return image;
                })
                .collect(Collectors.toList());
    }
}
