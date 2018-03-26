package munch.data.linking;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:27 AM
 * Project: munch-data
 */
public interface Platform {

    /**
     * @param url url to parse into linking id
     * @return nullable
     */
    String parse(PlatformUrl url);

    default String wrap(String prefix, String id) {
        if (StringUtils.isAnyBlank(prefix, id)) {
            return null;
        }
        return prefix + id;
    }

    default String wrap(String prefix, String separator, String... parts) {
        if (StringUtils.isBlank(prefix) || StringUtils.isAnyBlank(parts)) {
            return null;
        }
        return prefix + Joiner.on(separator).join(parts);
    }

    class PlatformUrl {

        private final URI uri;
        private final List<String> paths;
        private final List<NameValuePair> params;

        public PlatformUrl(String url) throws URISyntaxException {
            this.uri = new URI(url);
            this.params = URLEncodedUtils.parse(uri, Charset.forName("UTF-8"));
            this.paths = Splitter.on('/').omitEmptyStrings().splitToList(uri.getPath());
        }

        String getDomain() {
            return uri.getHost();
        }

        List<String> getDomainParts() {
            return Splitter.on('.').omitEmptyStrings().splitToList(getDomain());
        }

        /**
         * @return path, e.g. http://munchapp.co/name, /name will be returned
         */
        String getPath() {
            return uri.getPath();
        }

        /**
         * @param name name of query string
         * @return query string
         */
        String getQueryString(String name) {
            for (NameValuePair param : params) {
                if (param.getName().equals(name)) {
                    return param.getValue();
                }
            }
            return null;
        }

        /**
         * @return paths
         */
        List<String> getPaths() {
            return paths;
        }

        /**
         * @param index index of path
         * @return path fragment, index of path
         */
        String getPath(int index) {
            if (paths.size() > index) return paths.get(index);
            return null;
        }

        /**
         * @param length   length of path
         * @param sections sections to check
         * @return true is section and length all match
         */
        boolean hasPath(int length, String... sections) {
            if (paths.size() != length) return false;

            return hasPath(sections);
        }

        boolean hasPath(String... sections) {
            for (int i = 0; i < sections.length; i++) {
                if (!paths.get(i).equals(sections[i])) return false;
            }
            return true;
        }
    }
}
