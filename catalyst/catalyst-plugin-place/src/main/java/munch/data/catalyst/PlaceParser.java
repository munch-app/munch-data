package munch.data.catalyst;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import edit.utils.website.DomainBlocked;
import munch.data.place.Place;
import munch.data.resolver.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 11/8/18
 * Time: 6:59 AM
 * Project: munch-data
 */
@Singleton
public final class PlaceParser {

    private final DomainBlocked domainBlocked;

    private final TagResolver tagResolver;
    private final StatusResolver statusResolver;
    private final LocationResolver locationResolver;

    private final MenuResolver menuResolver;
    private final PriceResolver priceResolver;
    private final BrandResolver brandResolver;

    private final HourResolver hourResolver;
    private final ImageResolver imageResolver;

    private final RankingResolver rankingResolver;
    private final CreatedMillisResolver createdMillisResolver;

    @Inject
    public PlaceParser(DomainBlocked domainBlocked, StatusResolver statusResolver, TagResolver tagResolver, LocationResolver locationResolver, MenuResolver menuResolver, PriceResolver priceResolver, BrandResolver brandResolver, HourResolver hourResolver, ImageResolver imageResolver, RankingResolver rankingResolver, CreatedMillisResolver createdMillisResolver) {
        this.domainBlocked = domainBlocked;
        this.statusResolver = statusResolver;
        this.tagResolver = tagResolver;
        this.locationResolver = locationResolver;
        this.menuResolver = menuResolver;
        this.priceResolver = priceResolver;
        this.brandResolver = brandResolver;
        this.hourResolver = hourResolver;
        this.imageResolver = imageResolver;
        this.rankingResolver = rankingResolver;
        this.createdMillisResolver = createdMillisResolver;
    }

    /**
     * @param mutation to parse into munch.data.Place
     * @return Place
     */
    public Place parse(PlaceMutation mutation) throws LocationSupportException, ResolverHaltException {
        Place place = new Place();
        place.setPlaceId(mutation.getPlaceId());
        place.setStatus(statusResolver.resolve(mutation));

        place.setName(parseStringFirst(mutation.getName()));
        place.setNames(parseStringAll(mutation.getName()));
        place.setTags(tagResolver.resolve(mutation));

        place.setPhone(parseStringFirst(mutation.getPhone()));
        place.setWebsite(parseWebsite(mutation.getWebsite()));
        place.setDescription(parseStringFirst(mutation.getDescription()));

        place.setLocation(locationResolver.resolve(mutation));

        place.setMenu(menuResolver.resolve(mutation));
        place.setPrice(priceResolver.resolve(mutation));
        place.setBrand(brandResolver.resolve(mutation));

        place.setHours(hourResolver.resolve(mutation));
        place.setImages(imageResolver.resolve(mutation));

        place.setCreatedMillis(createdMillisResolver.resolve(mutation));
        place.setUpdatedMillis(mutation.getMillis());

        place.setRanking(rankingResolver.resolve(place, mutation));
        return place;
    }

    @Nullable
    public String parseStringFirst(List<MutationField<String>> fields) {
        if (fields.isEmpty()) return null;
        return StringUtils.trimToNull(fields.get(0).getValue());
    }

    @Nullable
    public String parseWebsite(List<MutationField<String>> fields) {
        for (MutationField<String> field : fields) {
            if (domainBlocked.isBlockedUrl(field.getValue())) continue;
            return field.getValue();
        }
        return null;
    }

    public Set<String> parseStringAll(List<MutationField<String>> fields) {
        return fields.stream()
                .map(MutationField::getValue)
                .collect(Collectors.toSet());
    }
}
