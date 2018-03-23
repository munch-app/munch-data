package munch.data.website;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 23/3/18
 * Time: 10:11 PM
 * Project: munch-data
 */
public class WebsiteNormalizer {
    protected static final Pattern HTTP_PATTERN = Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);

    public static String normalize(String url) {
        if (HTTP_PATTERN.matcher(url).matches()) return url;
        return "http://" + url;
    }

    public static String getDomain(String website) throws MalformedURLException {
        URL url = new URL(website);
        return url.getHost();
    }
}
