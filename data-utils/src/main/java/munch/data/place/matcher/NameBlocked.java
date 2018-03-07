package munch.data.place.matcher;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 7/3/2018
 * Time: 11:20 PM
 * Project: munch-data
 */
@Singleton
public final class NameBlocked {
    private static final Pattern TRAILING_PATTERN = Pattern.compile(": *$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIME_PATTERN = Pattern.compile("^(0?[1-9]|1[0-2])[ap]m$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^((t|phone|tel|tele|telephone): *)?(\\+?65)?\\s*([0-9]{4})\\s{0,2}([0-9]{4})$", Pattern.CASE_INSENSITIVE);

    private final Set<String> blockedNames;

    /**
     * See resources/name-blocked.txt for the file
     * resource file must contains lowercase location names for cleaning
     *
     * @throws IOException error loading file
     */
    @Inject
    public NameBlocked() throws IOException {
        URL url = Resources.getResource("name-blocked.txt");
        this.blockedNames = ImmutableSet.copyOf(Resources.readLines(url, Charset.forName("UTF-8")));
    }

    public boolean isBlocked(String name) {
        if (StringUtils.isBlank(name)) return true;

        name = TRAILING_PATTERN.matcher(name.toLowerCase()).replaceAll("").trim();
        if (StringUtils.isBlank(name)) return true;
        if (name.length() < 3) return true;
        if (isTiming(name)) return true;
        if (isPhone(name)) return true;
        return blockedNames.contains(name);
    }

    public boolean isTiming(String name) {
        return TIME_PATTERN.matcher(name.trim()).matches();
    }

    public boolean isPhone(String name) {
        return PHONE_PATTERN.matcher(name.trim()).matches();
    }
}
