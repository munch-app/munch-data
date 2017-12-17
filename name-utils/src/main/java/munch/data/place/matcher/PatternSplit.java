package munch.data.place.matcher;

import javax.annotation.RegEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 22/5/2017
 * Time: 6:43 AM
 * Project: article-corpus
 */
public class PatternSplit {
    private final Pattern pattern;

    /**
     * @param pattern pattern for the splitter
     */
    private PatternSplit(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * This methods mirrors Pattern#matcher
     *
     * @param input The character sequence to be matched
     * @return A new matcher for this pattern
     * @see Pattern#matcher(CharSequence)
     */
    public Matcher matcher(CharSequence input) {
        return pattern.matcher(input);
    }

    /**
     * @param input string input
     * @return List of result object
     */
    public List<String> split(CharSequence input) {
        return split(input, 0);
    }

    /**
     * @param input string input
     * @param limit limit
     * @return List of result object
     */
    public List<String> split(CharSequence input, int limit) {
        return split(input, limit, s -> s).stream()
                .map(o -> (String) o).collect(Collectors.toList());
    }

    /**
     * @param input         string input
     * @param limit         limit
     * @param delimitMapper delimiter function for delimited value
     * @return List of result object
     */
    public List<Object> split(CharSequence input, int limit, DelimitMapper delimitMapper) {
        return split(input, limit, s -> s, delimitMapper);
    }

    /**
     * @param input         string input
     * @param limit         limit
     * @param splitMapper   splitter function for split value
     * @param delimitMapper delimiter function for delimited value
     * @return List of result object
     */
    public List<Object> split(CharSequence input, int limit, SplitMapper splitMapper, DelimitMapper delimitMapper) {
        return split(input, limit, splitMapper, (Matcher matcher) ->
                delimitMapper.apply(input.subSequence(matcher.start(), matcher.end()).toString())
        );
    }

    /**
     * @param input       string input
     * @param limit       limit
     * @param splitMapper splitter function for split value
     * @param matchMapper delimiter function for delimited value
     * @return List of result object
     */
    public List<Object> split(CharSequence input, int limit, SplitMapper splitMapper, MatchMapper matchMapper) {
        int index = 0;
        boolean matchLimited = limit > 0;
        List<Object> matchList = new ArrayList<>();
        Matcher m = pattern.matcher(input);

        // Add segments before each match found
        while (m.find()) {
            if (!matchLimited || matchList.size() < limit - 1) {
                if (index == 0 && index == m.start() && m.start() == m.end()) {
                    // no empty leading substring included for zero-width match
                    // at the beginning of the input char sequence.
                    continue;
                }
                Object split = splitMapper.apply(input.subSequence(index, m.start()).toString());
                if (split != null) matchList.add(split);
                Object apply = matchMapper.apply(m);
                if (apply != null) matchList.add(apply);
                index = m.end();
            } else if (matchList.size() == limit - 1) { // last one
                Object split = splitMapper.apply(input.subSequence(index, m.start()).toString());
                if (split != null) matchList.add(split);
                Object apply = matchMapper.apply(m);
                if (apply != null) matchList.add(apply);
                index = m.end();
            }
        }

        // If no match was found, return this
        if (index == 0)
            return Collections.singletonList(input.toString());

        // Add remaining segment
        if (!matchLimited || matchList.size() < limit) {
            Object split = splitMapper.apply(input.subSequence(index, input.length()).toString());
            if (split != null) matchList.add(split);
        }

        // Construct result
        int resultSize = matchList.size();
        if (limit == 0)
            while (resultSize > 0 && matchList.get(resultSize - 1).equals(""))
                resultSize--;
        return matchList.subList(0, resultSize);
    }

    /**
     * @param pattern regex string
     * @param flags   pattern flags
     * @return PatternSplit
     */
    public static PatternSplit compile(@RegEx String pattern, int flags) {
        return new PatternSplit(Pattern.compile(pattern, flags));
    }

    /**
     * @param pattern regex string
     * @return PatternSplit
     */
    public static PatternSplit compile(@RegEx String pattern) {
        return new PatternSplit(Pattern.compile(pattern));
    }

    @FunctionalInterface
    public interface SplitMapper extends Function<String, Object> {
    }

    @FunctionalInterface
    public interface DelimitMapper extends Function<String, Object> {
    }

    @FunctionalInterface
    public interface MatchMapper extends Function<Matcher, Object> {
    }
}
