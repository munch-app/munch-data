package munch.data.location;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 28/3/18
 * Time: 10:32 AM
 * Project: munch-data
 */
@Singleton
public final class AddressGrouping {
    enum Type {
        StreetSuffix,
        Postal
    }


    private final Set<String> KNOWN_PREFIX = Set.of("address:", "location:", "direction:", "address", "location", "direction", ":");
    private final StreetSuffixDatabase suffixDatabase;

    @Inject
    public AddressGrouping(StreetSuffixDatabase suffixDatabase) {
        this.suffixDatabase = suffixDatabase;
    }

    public Set<List<String>> group(String text) {
        List<String> tokens = CityParser.WHITESPACE_PATTERN.split(text.toLowerCase());

        Set<List<String>> groups = new HashSet<>();

        ListIterator<String> iterator = tokens.listIterator();
        while (iterator.hasNext()) {
            String start = iterator.next();
            Type type = getType(start);

            // Check if start is important
            if (type != null) {
                // If it is, capture and most iterator cursor
                List<String> capturedTokens = capture(type, start, iterator);
                if (capturedTokens != null) groups.add(capturedTokens);
            }
        }

        return groups;
    }

    /**
     * @param text text
     * @return whether text is part of address
     */
    private Type getType(String text) {
        if (StringUtils.isBlank(text)) return null;
        if (suffixDatabase.is(text)) return Type.StreetSuffix;
        if (NumberUtils.isDigits(text) && text.length() >= 5) return Type.Postal;
        return null;
    }

    private List<String> capture(Type type, String start, ListIterator<String> iterator) {
        List<String> list;
        switch (type) {
            case StreetSuffix:
                list = new ArrayList<>();
                list.addAll(getPrefix(22, iterator));
                list.addAll(getPostfix(22, iterator));
                return list;
            case Postal:
                list = new ArrayList<>();
                list.addAll(getPrefix(22, iterator));
                list.addAll(getPostfix(8, iterator));
                return list;
        }
        return null;
    }

    private List<String> getPrefix(int size, ListIterator<String> iterator) {
        int actual = 0;
        for (int i = 0; i < size; i++) {
            if (iterator.hasPrevious()) {
                if (KNOWN_PREFIX.contains(iterator.previous())) {
                    break;
                }
                actual++;
            } else {
                break;
            }
        }

        List<String> list = new ArrayList<>();
        for (int i = 0; i < actual; i++) {
            list.add(iterator.next());
        }
        return list;
    }

    private List<String> getPostfix(int size, ListIterator<String> iterator) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (iterator.hasNext()) {
                list.add(iterator.next());
            } else {
                return list;
            }
        }

        return list;
    }

    private static boolean isDash(String text) {
        return text.contains("-") || text.contains("–") || text.contains("—");
    }

    private static boolean containDivider(String text) {
        // |\\n\\r|\\r\\n|\\n|\\r|-|–|—|:|@|\||\\
        return text.contains(":") ||
                text.contains("|") ||
                text.contains("\\") ||
                text.contains("/") ||
                text.contains("@");
    }
}
