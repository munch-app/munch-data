package munch.data.linking;

import munch.data.website.DomainBlocked;
import munch.data.website.WebsiteNormalizer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 23/3/18
 * Time: 9:50 PM
 * Project: munch-data
 */
@Singleton
public class LinkingExtractor {
    private static final Map<String, Platform> PLATFORMS;

    static {
        Map<String, Platform> map = new HashMap<>();

        FacebookPlatform facebookPlatform = new FacebookPlatform();
        map.put("facebook.com", facebookPlatform);
        map.put("fb.com", facebookPlatform);

        ChopePlatform chopePlatform = new ChopePlatform();
        map.put("chope.com", chopePlatform);
        map.put("chope.co", chopePlatform);
        map.put("cho.pe", chopePlatform);

        QuandooPlatform quandooPlatform = new QuandooPlatform();
        map.put("quandoo.com", quandooPlatform);
        map.put("quandoo.sg", quandooPlatform);

        map.put("instagram.com", new InstagramPlatform());
        map.put("google.com", new GooglePlatform());
        map.put("burpple.com", new BurpplePlatform());
        map.put("foursquare.com", new FoursquarePlatform());

        HungrygowherePlatform hungrygowherePlatform = new HungrygowherePlatform();
        map.put("hungrygowhere.com", hungrygowherePlatform);
        map.put("hungrygowhere.my", hungrygowherePlatform);

        YelpPlatform yelpPlatform = new YelpPlatform();
        map.put("yelp.com", yelpPlatform);
        map.put("yelp.com.sg", yelpPlatform);

        map.put("zomato.com", new ZomatoPlatform());

        map.put("oddle.me", new OddlePlatform());
        map.put("eatigo.com", new EatigoPlatform());
        map.put("foodpanda.sg", new FoodPandaPlatform());

        HonestBeePlatform honestBeePlatform = new HonestBeePlatform();
        map.put("honestbee.com", honestBeePlatform);
        map.put("honestbee.sg", honestBeePlatform);

        map.put("deliveroo.com.sg", new DeliverooPlatform());
        map.put("ubereats.com", new UberEatsPlatform());

        map.put("feastbump.com", new FeastBumpPlatform());

        PLATFORMS = map;
    }

    /**
     * @param url url
     * @return unique link to place, this is not url
     */
    @Nullable
    public String extract(String url) {
        url = WebsiteNormalizer.normalize(url);
        String tld = DomainBlocked.getTLD(url);

        if (StringUtils.isBlank(tld)) return null;
        Platform platform = PLATFORMS.get(tld);
        if (platform == null) return null;


        try {
            return platform.parse(new Platform.PlatformUrl(url));
        } catch (URISyntaxException e) {
            return null;
        }
    }

}
