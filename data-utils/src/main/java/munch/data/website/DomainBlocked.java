package munch.data.website;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
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

        int periods = StringUtils.countMatches(domain, '.');
        if (periods < 2) return domain;

        if (domain.endsWith(".com.sg")) return domain;

        String[] parts = domain.split("\\.");
        if (parts.length < 2) return domain;
        return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }
}
