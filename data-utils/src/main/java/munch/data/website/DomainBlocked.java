package munch.data.website;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 23/3/18
 * Time: 9:58 PM
 * Project: munch-data
 */
@Singleton
public final class DomainBlocked {
    private final Set<String> blockedDomains;

    @Inject
    public DomainBlocked() throws IOException {
        URL url = Resources.getResource("domain-blocked.txt");
        this.blockedDomains = ImmutableSet.copyOf(Resources.readLines(url, Charset.forName("UTF-8")));
    }

    public boolean isBlockedUrl(String url) {
        return isBlocked(getTLD(url));
    }

    public boolean isBlockedImageUrl(String url) {
        return isBlocked(getTLD(url));
    }

    /**
     * @param domain domain
     * @return whether domain is blocked
     */
    public boolean isBlocked(String domain) {
        if (domain == null || domain.length() < 4) return true;
        return blockedDomains.contains(getTLDFromDomain(domain));
    }

    public static String getTLD(String url) {
        String domain = WebsiteNormalizer.getDomain(url);
        return getTLDFromDomain(domain);
    }

    public static String getTLDFromDomain(String domain) {
        if (domain == null) return null;

        // If only 1 period mean its root domain
        if (StringUtils.countMatches(domain, '.') < 2) return domain;

        List<String> parts = Splitter.on('.').splitToList(domain);

        int size = parts.size();
        if (parts.get(size - 2).equals("com")) {
            if (size == 3) return domain;
            return Joiner.on('.').join(
                    parts.get(size - 3),
                    parts.get(size - 2),
                    parts.get(size - 1)
            );
        }

        return Joiner.on('.').join(parts.get(size - 2), parts.get(size - 1));
    }
}
