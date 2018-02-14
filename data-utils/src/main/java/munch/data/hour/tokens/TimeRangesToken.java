package munch.data.hour.tokens;

import munch.data.utils.PatternTexts;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 11:50 AM
 * Project: munch-data
 */
public class TimeRangesToken {
    public final Set<TimeRangeToken> set;

    TimeRangesToken(Set<TimeRangeToken> set) {
        this.set = set;
    }

    TimeRangesToken(TimeRangeToken... ranges) {
        this(Arrays.stream(ranges).collect(Collectors.toSet()));
    }

    void addAll(TimeRangesToken ranges) {
        set.addAll(ranges.set);
    }

    public static void parse(PatternTexts texts) {
        // Map all TimeRange to TimeRanges
        texts.replaceAll(obj -> obj instanceof TimeRangeToken ? new TimeRangesToken((TimeRangeToken) obj) : obj);

        // Map all Day + And + Day to days
        texts.replace(TimeRangesToken.class, AndToken.class, TimeRangesToken.class, triple -> {
            TimeRangesToken left = (TimeRangesToken) triple.getLeft();
            TimeRangesToken right = (TimeRangesToken) triple.getRight();
            left.addAll(right);
            return left;
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeRangesToken that = (TimeRangesToken) o;
        return set.equals(that.set);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @Override
    public String toString() {
        return "(TimeRanges:" + set + ")";
    }
}
