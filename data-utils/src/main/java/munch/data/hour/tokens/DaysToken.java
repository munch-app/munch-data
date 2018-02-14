package munch.data.hour.tokens;

import munch.data.utils.PatternSplit;
import munch.data.utils.PatternTexts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static munch.data.hour.tokens.DayToken.*;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 11:50 AM
 * Project: munch-data
 */
public class DaysToken {
    static final PatternSplit PatternDaily = PatternSplit.compile("\\b(open daily|daily|everyday|allday|all day)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternWeekday = PatternSplit.compile("\\bweekdays?\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternWeekend = PatternSplit.compile("\\bweekends?\\b", Pattern.CASE_INSENSITIVE);
    private static final List<DayToken> dayLoops = List.of(Mon, Tue, Wed, Thu, Fri, Sat, Sun,
            Mon, Tue, Wed, Thu, Fri, Sat, Sun);

    public final Set<DayToken> set;

    DaysToken(Set<DayToken> set) {
        this.set = set;
    }

    DaysToken(DayToken... days) {
        this(Arrays.stream(days).collect(Collectors.toSet()));
    }

    /**
     * @return first day in set else null
     */
    DayToken first() {
        if (set.isEmpty()) return null;
        return set.iterator().next();
    }

    int size() {
        return set.size();
    }

    void addAll(DaysToken days) {
        set.addAll(days.set);
    }

    void remove(DaysToken days) {
        set.removeAll(days.set);
    }

    /**
     * @param start start day
     * @param end   end day
     * @return get all days in between start and end
     */
    static DaysToken between(DayToken start, DayToken end) {
        Set<DayToken> days = new HashSet<>();
        boolean started = false;
        for (DayToken day : dayLoops) {
            if (day == start) started = true;
            if (started) {
                days.add(day);
                if (day == end) break;
            }
        }
        return new DaysToken(days);
    }

    public static void parse(PatternTexts texts) {
        texts.replace(PatternDaily, new DaysToken(Mon, Tue, Wed, Thu, Fri, Sat, Sun));
        texts.replace(PatternWeekday, new DaysToken(Mon, Tue, Wed, Thu, Fri));
        texts.replace(PatternWeekend, new DaysToken(Sat, Sun));

        // Map all day to days
        texts.replaceAll(obj -> obj instanceof DayToken ? new DaysToken((DayToken) obj) : obj);

        // Map all Day + Range + Day to days
        texts.replace(DaysToken.class, RangeToken.class, DaysToken.class, triple -> {
            DaysToken left = (DaysToken) triple.getLeft();
            DaysToken right = (DaysToken) triple.getRight();
            // To build range both must be size of 1 day
            if (left.size() != 1 || right.size() != 1) return null;
            // Both cannot be ph
            if (left.first() == Ph && right.first() == Ph) return null;

            // Form up set range and return
            return DaysToken.between(left.first(), right.first());
        });

        // Map all Day + And + Day to days
        texts.replace(DaysToken.class, AndToken.class, DaysToken.class, triple -> {
            DaysToken left = (DaysToken) triple.getLeft();
            DaysToken right = (DaysToken) triple.getRight();
            left.addAll(right);
            return left;
        });

        // Convert closed + days to reverse days
        texts.replace(ClosedToken.class, DaysToken.class, pair -> {
            DaysToken days = new DaysToken(daily());
            days.remove((DaysToken) pair.getRight());
            return days;
        });

        // Convert days + closed to reverse days
        texts.replace(DaysToken.class, ClosedToken.class, pair -> {
            DaysToken days = new DaysToken(daily());
            days.remove((DaysToken) pair.getLeft());
            return days;
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DaysToken days = (DaysToken) o;

        return set.equals(days.set);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @Override
    public String toString() {
        return "(Days:" + set + ")";
    }
}
