package munch.data.linking;

import munch.data.website.WebsiteNormalizer;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 23/3/18
 * Time: 9:50 PM
 * Project: munch-data
 */
@Singleton
public final class LinkingExtractor {
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


        PLATFORMS = map;
    }


    // eatigo.com
    // foodpanda
    // honestbee
    // deliveroo
    // ubereats.com

    /**
     * @param url url
     * @return unique link to place, this is not url
     */
    public String extract(String url) {
        url = WebsiteNormalizer.normalize(url);
        String tld = getTLD(url);

        if (StringUtils.isBlank(tld)) return null;
        return PLATFORMS.get(tld).get(url);
    }

    public String getTLD(String url) {
        String domain = WebsiteNormalizer.getDomain(url);
        if (domain == null) return null;

        int periods = StringUtils.countMatches(domain, '.');
        if (periods < 2) return domain;

        if (domain.endsWith(".com.sg")) return domain;

        String[] parts = domain.split("\\.");
        if (parts.length < 2) return domain;
        return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }

    public static class FacebookPlatform implements Platform {

        @Override
        public String get(String url) {
            // TODO
            return null;
        }
    }

    public static class ChopePlatform implements Platform {

        @Override
        public String get(String url) {
            // https://book.chope.co/booking?rid=rafflesgrill1509rfg&source=sethlui
            // TODO
            return null;
        }
    }

    public static class InstagramPlatform implements Platform {

    }

    public static class GooglePlatform implements Platform {

    }

    public static class BurpplePlatform implements Platform {

    }

    public static class FoursquarePlatform implements Platform {

    }

    public static class QuandooPlatform implements Platform {

    }

    public static class HungrygowherePlatform implements Platform {

    }

    public static class YelpPlatform implements Platform {

    }

    public static class ZomatoPlatform implements Platform {

    }

    public static class OddlePlatform implements Platform {

    }

    public static class EatigoPlatform implements Platform {

    }

    public static class FoodpandaPlatform implements Platform {

    }

    public static class HonestBeePlatform implements Platform {

    }

    public static class DeliverooPlatform implements Platform {

    }

    public static class UberEatsPlatform implements Platform {

    }

    public interface Platform {
        String get(String url);
    }
}
