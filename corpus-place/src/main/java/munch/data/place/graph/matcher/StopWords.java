package munch.data.place.graph.matcher;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 5/8/2017
 * Time: 9:21 PM
 * Project: munch-corpus
 */
@Singleton
public final class StopWords {

    private final Set<String> words;

    @Inject
    public StopWords() throws IOException {
        URL resource = Resources.getResource("stopwords.txt");
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (String line : Resources.readLines(resource, Charset.forName("utf-8"))) {
            if (StringUtils.isNotBlank(line)) {
                builder.add(line.trim().toLowerCase());
            }
        }
        words = builder.build();
    }

    /**
     * @param text text to break in token and remove all stop words
     * @return then all the token is all joined back together
     */
    public String clean(String text) {
        if (StringUtils.isBlank(text)) return text;
        List<String> tokenList = Arrays.stream(text.toLowerCase().split(" "))
                .collect(Collectors.toList());
        tokenList.removeIf(words::contains);
        return Joiner.on(" ").join(tokenList);
    }
}
