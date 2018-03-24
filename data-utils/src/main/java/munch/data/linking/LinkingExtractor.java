package munch.data.linking;

import com.google.common.base.Splitter;
import munch.data.website.WebsiteNormalizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

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

        QuandooPlatform quandooPlatform = new QuandooPlatform();
        map.put("quandoo.com", quandooPlatform);
        map.put("quandoo.sg", quandooPlatform);

        map.put("instagram.com", new InstagramPlatform());
        map.put("google.com", new GooglePlatform());
        map.put("burpple.com", new BurpplePlatform());
        map.put("foursquare.com", new FoursquarePlatform());
        map.put("hungrygowhere.com", new HungrygowherePlatform());

        map.put("yelp.com", new YelpPlatform());
        map.put("zomato.com", new ZomatoPlatform());

        map.put("oddle.com", new OddlePlatform());
        map.put("eatigo.com", new EatigoPlatform());
        map.put("foodpanda.sg", new FoodPandaPlatform());

        HonestBeePlatform honestBeePlatform = new HonestBeePlatform();
        map.put("honestbee.com", honestBeePlatform);
        map.put("honestbee.sg", honestBeePlatform);

        map.put("deliveroo.com.sg", new DeliverooPlatform());
        map.put("ubereats.com", new UberEatsPlatform());

        PLATFORMS = map;
    }

    /**
     * @param url url
     * @return unique link to place, this is not url
     */
    public String extract(String url) {
        url = WebsiteNormalizer.normalize(url);
        String tld = getTLD(url);

        if (StringUtils.isBlank(tld)) return null;
        return PLATFORMS.get(tld).parse(url);
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
        public String parse(String url) {
            return "facebook.com/place/" + getPath(url, 0);
        }
    }

    public static class ChopePlatform implements Platform {

        @Override
        public String parse(String url) {
            String id = getQueryString(url, "rid");
            return "chope.co/booking/" + id;
        }
    }

    public static class InstagramPlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class GooglePlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class BurpplePlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class FoursquarePlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class QuandooPlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class HungrygowherePlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class YelpPlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class ZomatoPlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class OddlePlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class EatigoPlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class FoodPandaPlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class HonestBeePlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public static class DeliverooPlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }

    }

    public static class UberEatsPlatform implements Platform {

        @Override
        public String parse(String url) {
            return null;
        }
    }

    public interface Platform {
        String parse(String url);

        /**
         * @param url  url
         * @param name name of query string
         * @return query string
         */
        default String getQueryString(String url, String name) {
            try {
                List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), Charset.forName("UTF-8"));
                for (NameValuePair param : params) {
                    if (param.getName().equals(name)) {
                        return param.getValue();
                    }
                }
                return null;
            } catch (URISyntaxException e) {
                return null;
            }
        }

        /**
         * @param url url
         * @return path, e.g. http://munchapp.co/name, /name will be returned
         */
        default String getPath(String url) {
            try {
                return new URI(url).getPath();
            } catch (URISyntaxException e) {
                return null;
            }
        }

        /**
         * @param url   url
         * @param index index of path
         * @return path fragment, index of path
         */
        default String getPath(String url, int index) {
            String path = getPath(url);

            List<String> paths = Splitter.on("/").omitEmptyStrings().splitToList(path);
            if (paths.size() > index) return paths.get(index);
            return null;
        }
    }
}
