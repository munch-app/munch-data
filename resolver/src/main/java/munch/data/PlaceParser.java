package munch.data;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import edit.utils.website.DomainBlocked;
import munch.data.place.Place;
import munch.data.resolver.*;
import munch.data.resolver.name.NameResolver;
import munch.data.resolver.tag.TagResolver;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 11/8/18
 * Time: 6:59 AM
 * Project: munch-data
 */
@Singleton
public final class PlaceParser {

    private final DomainBlocked domainBlocked;

    private final NameResolver nameResolver;
    private final TagResolver tagResolver;
    private final StatusResolver statusResolver;
    private final LocationResolver locationResolver;

    private final MenuResolver menuResolver;
    private final PriceResolver priceResolver;
    private final BrandResolver brandResolver;

    private final HourResolver hourResolver;
    private final ImageResolver imageResolver;
    private final TasteResolver tasteResolver;

    private final RankingResolver rankingResolver;
    private final CreatedMillisResolver createdMillisResolver;

    @Inject
    public PlaceParser(DomainBlocked domainBlocked, NameResolver nameResolver, StatusResolver statusResolver, TagResolver tagResolver, LocationResolver locationResolver, MenuResolver menuResolver, PriceResolver priceResolver, BrandResolver brandResolver, HourResolver hourResolver, ImageResolver imageResolver, TasteResolver tasteResolver, RankingResolver rankingResolver, CreatedMillisResolver createdMillisResolver) {
        this.domainBlocked = domainBlocked;
        this.nameResolver = nameResolver;
        this.statusResolver = statusResolver;
        this.tagResolver = tagResolver;
        this.locationResolver = locationResolver;
        this.menuResolver = menuResolver;
        this.priceResolver = priceResolver;
        this.brandResolver = brandResolver;
        this.hourResolver = hourResolver;
        this.imageResolver = imageResolver;
        this.tasteResolver = tasteResolver;
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

        place.setName(nameResolver.resolve(mutation));
        place.setNames(nameResolver.resolveAll(mutation));
        place.setTags(tagResolver.resolve(mutation));

        place.setPhone(getFirst(mutation.getPhone()));
        place.setWebsite(parseWebsite(mutation.getWebsite()));
        place.setDescription(getFirst(mutation.getDescription()));

        place.setLocation(locationResolver.resolve(mutation));

        place.setMenu(menuResolver.resolve(mutation));
        place.setPrice(priceResolver.resolve(mutation));
        place.setBrand(brandResolver.resolve(mutation));

        place.setHours(hourResolver.resolve(mutation));
        place.setImages(imageResolver.resolve(mutation));

        place.setCreatedMillis(createdMillisResolver.resolve(mutation));
        place.setUpdatedMillis(mutation.getMillis());

        place.setRanking(rankingResolver.resolve(place, mutation));
        place.setTaste(tasteResolver.resolve(place));

        // Areas, is decided & populated by the data-service itself
        place.setAreas(List.of());
        return place;
    }

    @Nullable
    public String getFirst(List<MutationField<String>> fields) {
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
}
