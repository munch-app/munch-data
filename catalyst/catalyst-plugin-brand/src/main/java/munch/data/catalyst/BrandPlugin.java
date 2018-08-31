package munch.data.catalyst;

import catalyst.edit.PlaceEdit;
import catalyst.elastic.ElasticQueryUtils;
import catalyst.elastic.ElasticSearchBuilder;
import catalyst.link.PlaceLink;
import catalyst.mutation.PlaceMutation;
import catalyst.plugin.LinkPlugin;
import munch.data.brand.Brand;
import munch.data.client.BrandClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 15/8/18
 * Time: 11:36 PM
 * Project: munch-data
 */
@Singleton
public final class BrandPlugin extends LinkPlugin<Brand> {
    private static final Logger logger = LoggerFactory.getLogger(BrandPlugin.class);

    private final BrandClient brandClient;
    private final BrandComparator brandComparator;
    private final PlaceEditMapper brandEditParser;

    @Inject
    public BrandPlugin(BrandClient brandClient, BrandComparator brandComparator, PlaceEditMapper brandEditParser) {
        this.brandClient = brandClient;
        this.brandComparator = brandComparator;
        this.brandEditParser = brandEditParser;
    }

    @Override
    public String getSource() {
        return "brand.data.munch.space";
    }

    @Override
    protected Iterator<Brand> objects() {
        return brandClient.iterator();
    }

    @Override
    protected String getId(Brand object) {
        return object.getBrandId();
    }

    @Nullable
    @Override
    protected PlaceEdit receive(Brand brand, PlaceMutation placeMutation, @Nullable PlaceLink placeLink, @Nullable PlaceEdit placeEdit) {
        if (placeMutation == null) {
            logger.warn("PlaceMutation is null, PlaceLink: {}", placeLink);
            return null;
        }
        if (brandComparator.match(brand, placeMutation)) {
            return brandEditParser.parse(brand);
        }
        return null;
    }

    @Override
    public Iterator<PlaceMutation> search(Brand brand) {
        if (!brand.getPlace().isAutoLink()) return Collections.emptyIterator();

        List<String> names = getNames(brand);
        List<String> points = getPoints(brand);
        if (names.isEmpty() || points == null) {
            logger.warn("Names or Points is empty for Brand: {}", brand);
            return Collections.emptyIterator();
        }

        ElasticSearchBuilder<PlaceMutation> builder = placeMutationClient.searchBuilder();
        builder.withFilterPolygon("latLng.value", points);
        if (names.size() == 1) {
            builder.withFilterTerm("name.value", names.get(0));
        }else if (names.size() > 1) {
            builder.withBoolOption("minimum_should_match", 1);
            names.forEach(s -> builder.withShould(ElasticQueryUtils.match("name.value", s)));
        }

        return builder.asIterator();
    }

    private static List<String> getPoints(Brand brand) {
        if (brand.getLocation().getCountry() == null) return null;
        return brand.getLocation().getCountry().getPoints();
    }

    private static List<String> getNames(Brand brand) {
        List<String> names = new ArrayList<>();
        names.add(brand.getName());

        for (String name : brand.getNames()) {
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }
}
