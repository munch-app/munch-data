package munch.data.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 23/5/2017
 * Time: 7:33 PM
 * Project: article-corpus
 */
public class PatternTexts extends LinkedList<Object> {

    public PatternTexts(String first) {
        add(first);
    }

    /**
     * string = "123423"
     * pattern = "2"
     * replace = hello
     * results = "1" + hello "34" + hello + "3"
     *
     * @param splitter pattern split replace
     * @param with     object to replace with
     */
    public void replace(PatternSplit splitter, Object with) {
        replace(splitter, m -> with);
    }

    /**
     * @param splitter split pattern
     * @param mapper   replacer logic, func return null = ignore
     */
    public void replace(PatternSplit splitter, PatternSplit.MatchMapper mapper) {
        ListIterator<Object> iterator = listIterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            // Pattern split function on string only
            if (o instanceof String) {
                iterator.remove();
                splitter.split((String) o, -1, StringUtils::trimToNull, mapper)
                        .forEach(iterator::add);
            }
        }
    }

    /**
     * side + middle + side
     * if match = replace with
     *
     * @param left     left class type
     * @param middle   middle class type
     * @param right    right class type
     * @param replacer replacer logic, func return null = ignore
     */
    public void replace(Class left, Class middle, Class right, Function<Triple<Object, Object, Object>, Object> replacer) {
        ListIterator<Object> iterator = listIterator();
        while (iterator.hasNext()) {
            Object start = iterator.next();
            if (!iterator.hasNext()) break;
            Object between = iterator.next();
            if (!iterator.hasNext()) break;
            Object end = iterator.next();

            if (left.isInstance(start) && middle.isInstance(between) && right.isInstance(end)) {
                Object apply = replacer.apply(Triple.of(start, between, end));
                if (apply != null) {
                    // Remove end and move back to middle
                    iterator.remove();
                    iterator.previous();

                    // Remove middle and move back to start
                    iterator.remove();
                    iterator.previous();

                    // Replace start with new
                    iterator.set(apply);
                    continue;
                }
            }

            // Move back to middle -> start
            iterator.previous();
            iterator.previous();
        }
    }

    /**
     * side + side
     * if match = replace with
     *
     * @param left     left class type
     * @param right    right class type
     * @param replacer replacer logic, func return null = ignore
     */
    public void replace(Class left, Class right, Function<Pair<Object, Object>, Object> replacer) {
        ListIterator<Object> iterator = listIterator();
        while (iterator.hasNext()) {
            Object start = iterator.next();
            if (!iterator.hasNext()) break;
            Object end = iterator.next();

            if (left.isInstance(start) && right.isInstance(end)) {
                Object apply = replacer.apply(Pair.of(start, end));
                if (apply != null) {
                    // Remove end and move back to middle
                    iterator.remove();
                    iterator.previous();

                    // Replace start with new
                    iterator.set(apply);
                    continue;
                }
            }

            // Move back to start
            iterator.previous();
        }
    }

    /**
     * @param type type to collect
     * @param <R>  type
     * @return list of data collected that is given type
     */
    @SuppressWarnings("unchecked")
    public <R> List<R> collect(Class<R> type) {
        return stream().filter(type::isInstance).map(o -> (R) o).collect(Collectors.toList());
    }
}
