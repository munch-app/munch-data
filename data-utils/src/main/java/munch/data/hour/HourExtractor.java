package munch.data.hour;

import munch.data.utils.PatternSplit;
import munch.data.utils.PatternTexts;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static munch.data.hour.HourExtractor.Day.*;


/**
 * Created by: Fuxing
 * Date: 21/5/2017
 * Time: 6:02 AM
 * Project: article-corpus
 */
public class HourExtractor {

    public List<OpenHour> extract(String text) {
        if (StringUtils.isBlank(text)) return List.of();

        // Create texts and parse all: must be in order
        PatternTexts texts = parse(text);

        // Collect all days and times
        List<Days> days = texts.collect(Days.class);
        List<TimeRanges> times = texts.collect(TimeRanges.class);
        if (days.isEmpty() && times.isEmpty()) return Collections.emptyList();

        // Check if there is only single time range, if so form up it as daily
        if (times.size() == 1 && days.size() == 1) {
            return times.get(0).set.stream()
                    .flatMap(range -> days.get(0).set.stream().map(range::toFields))
                    .collect(Collectors.toList());
        }

        if (times.size() == 1 && days.isEmpty()) {
            return times.get(0).set.stream()
                    .flatMap(range -> Arrays.stream(daily()).map(range::toFields))
                    .collect(Collectors.toList());
        }
        return parseFields(texts);
    }

    PatternTexts parse(String text) {
        // Create texts and parse all: must be in order
        PatternTexts texts = new PatternTexts(text);
        And.parse(texts);
        Range.parse(texts);
        Day.parse(texts);

        Time.parse(texts);
        Closed.parse(texts);
        Removes.parse(texts);

        Days.parse(texts);
        TimeRange.parse(texts);
        TimeRanges.parse(texts);
        texts.removeIf(o -> o instanceof And);
        return texts;
    }

    List<OpenHour> parseFields(PatternTexts texts) {
        List<OpenHour> fields = new ArrayList<>();
        ListIterator<Object> iterator = texts.listIterator();
        while (iterator.hasNext()) {
            Object days = iterator.next();
            if (!iterator.hasNext()) break;
            Object time = iterator.next();

            if (days instanceof Days && time instanceof TimeRanges) {
                for (Day day : ((Days) days).set) {
                    for (TimeRange timeRange : ((TimeRanges) time).set) {
                        fields.add(timeRange.toFields(day));
                    }
                }
            } else {
                iterator.previous();
            }
        }
        return fields;
    }

    static class Removes {
        static final PatternSplit PatternMeal = PatternSplit.compile("\\b(breakfast|lunch|dinner|supper|brunch)s?\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternSymbol = PatternSplit.compile("[:()]", Pattern.CASE_INSENSITIVE);

        static void parse(PatternTexts texts) {
            texts.replace(PatternMeal, m -> null);
            texts.replace(PatternSymbol, m -> null);
        }
    }

    static class Closed {
        static final PatternSplit PatternClosed = PatternSplit.compile("\\b(closed? on alternat(e|ive)|closed? on|closed?)\\b", Pattern.CASE_INSENSITIVE);

        static void parse(PatternTexts texts) {
            texts.replace(PatternClosed, new Closed());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 1155194720;
        }

        @Override
        public String toString() {
            return "(Closed)";
        }
    }

    static class Range {
        static final PatternSplit PatternRange = PatternSplit.compile("(\\bto\\b|-|–|—)", Pattern.CASE_INSENSITIVE);

        static void parse(PatternTexts texts) {
            texts.replace(PatternRange, new Range());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 2082814191;
        }

        @Override
        public String toString() {
            return "(Range)";
        }
    }

    static class And {
        static final PatternSplit PatternAnd = PatternSplit.compile("\\band\\b|&|,|;", Pattern.CASE_INSENSITIVE);

        static void parse(PatternTexts texts) {
            texts.replace(PatternAnd, new And());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 378517922;
        }

        @Override
        public String toString() {
            return "(And)";
        }
    }

    enum Day {
        Mon, Tue, Wed, Thu, Fri, Sat, Sun, Ph, EvePh;

        static Day[] daily() {
            return new Day[]{Mon, Tue, Wed, Thu, Fri, Sat, Sun};
        }

        static final PatternSplit PatternMon = PatternSplit.compile("\\b(mon|mondays?)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternTue = PatternSplit.compile("\\b(tue|tues|tuesdays?)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternWed = PatternSplit.compile("\\b(wed|wednesdays?)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternThu = PatternSplit.compile("\\b(thu|thurs?|thursdays?)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternFri = PatternSplit.compile("\\b(fri|fridays?)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternSat = PatternSplit.compile("\\b(sat|saturdays?)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternSun = PatternSplit.compile("\\b(sun|sundays?)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternPh = PatternSplit.compile("\\b(ph|public holidays?|holidays?)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternEvePh = PatternSplit.compile("\\beve of (ph|public holidays?|holidays?)\\b", Pattern.CASE_INSENSITIVE);

        static void parse(PatternTexts texts) {
            texts.replace(PatternMon, Mon);
            texts.replace(PatternTue, Tue);
            texts.replace(PatternWed, Wed);
            texts.replace(PatternThu, Thu);
            texts.replace(PatternFri, Fri);
            texts.replace(PatternSat, Sat);
            texts.replace(PatternSun, Sun);
            texts.replace(PatternEvePh, EvePh);
            texts.replace(PatternPh, Ph);
        }

        /**
         * @return as Hour.Day
         */
        OpenHour.Day toDay() {
            switch (this) {
                case Mon:
                    return OpenHour.Day.Mon;
                case Tue:
                    return OpenHour.Day.Tue;
                case Wed:
                    return OpenHour.Day.Wed;
                case Thu:
                    return OpenHour.Day.Thu;
                case Fri:
                    return OpenHour.Day.Fri;
                case Sat:
                    return OpenHour.Day.Sat;
                case Sun:
                    return OpenHour.Day.Sun;
                case Ph:
                    return OpenHour.Day.Ph;
                case EvePh:
                    return OpenHour.Day.EvePh;
                default:
                    throw new IllegalStateException("Day not found.");
            }
        }
    }

    static class Days {
        static final PatternSplit PatternDaily = PatternSplit.compile("\\b(open daily|daily|everyday|allday|all day)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternWeekday = PatternSplit.compile("\\bweekdays?\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternWeekend = PatternSplit.compile("\\bweekends?\\b", Pattern.CASE_INSENSITIVE);
        private static final List<Day> dayLoops = List.of(Mon, Tue, Wed, Thu, Fri, Sat, Sun,
                Mon, Tue, Wed, Thu, Fri, Sat, Sun);

        private final Set<Day> set;

        Days(Set<Day> set) {
            this.set = set;
        }

        Days(Day... days) {
            this(Arrays.stream(days).collect(Collectors.toSet()));
        }

        /**
         * @return first day in set else null
         */
        Day first() {
            if (set.isEmpty()) return null;
            return set.iterator().next();
        }

        int size() {
            return set.size();
        }

        void addAll(Days days) {
            set.addAll(days.set);
        }

        void remove(Days days) {
            set.removeAll(days.set);
        }

        /**
         * @param start start day
         * @param end   end day
         * @return get all days in between start and end
         */
        static Days between(Day start, Day end) {
            Set<Day> days = new HashSet<>();
            boolean started = false;
            for (Day day : dayLoops) {
                if (day == start) started = true;
                if (started) {
                    days.add(day);
                    if (day == end) break;
                }
            }
            return new Days(days);
        }

        static void parse(PatternTexts texts) {
            texts.replace(PatternDaily, new Days(Mon, Tue, Wed, Thu, Fri, Sat, Sun));
            texts.replace(PatternWeekday, new Days(Mon, Tue, Wed, Thu, Fri));
            texts.replace(PatternWeekend, new Days(Sat, Sun));

            // Map all day to days
            texts.replaceAll(obj -> obj instanceof Day ? new Days((Day) obj) : obj);

            // Map all Day + Range + Day to days
            texts.replace(Days.class, Range.class, Days.class, triple -> {
                Days left = (Days) triple.getLeft();
                Days right = (Days) triple.getRight();
                // To build range both must be size of 1 day
                if (left.size() != 1 || right.size() != 1) return null;
                // Both cannot be ph
                if (left.first() == Ph && right.first() == Ph) return null;

                // Form up set range and return
                return Days.between(left.first(), right.first());
            });

            // Map all Day + And + Day to days
            texts.replace(Days.class, And.class, Days.class, triple -> {
                Days left = (Days) triple.getLeft();
                Days right = (Days) triple.getRight();
                left.addAll(right);
                return left;
            });

            // Convert closed + days to reverse days
            texts.replace(Closed.class, Days.class, pair -> {
                Days days = new Days(daily());
                days.remove((Days) pair.getRight());
                return days;
            });

            // Convert days + closed to reverse days
            texts.replace(Days.class, Closed.class, pair -> {
                Days days = new Days(daily());
                days.remove((Days) pair.getLeft());
                return days;
            });
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Days days = (Days) o;

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

    static class Time {
        static final PatternSplit PatternNoon = PatternSplit.compile("\\b(afternoon|12 ?noon|noon|12[:.]00|12 ?pm)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit PatternMidnight = PatternSplit.compile("\\b(midnight|24[:.]00|12 ?am)\\b", Pattern.CASE_INSENSITIVE);

        static final PatternSplit PatternAmPm = PatternSplit.compile("\\b(?<hour>0?[0-9]|1[0-2])([:.]?(?<min>[0-5][0-9]))? ?(?<period>am|pm)\\b", Pattern.CASE_INSENSITIVE);
        static final PatternSplit Pattern24 = PatternSplit.compile("\\b(?<hour>0?[0-9]|1[0-9]|2[0-3])([:.]?(?<min>[0-5][0-9]))?\\b", Pattern.CASE_INSENSITIVE);

        private final LocalTime time;

        Time(LocalTime time) {
            this.time = time;
        }

        static void parse(PatternTexts texts) {
            texts.replace(PatternNoon, new Time(LocalTime.NOON));
            texts.replace(PatternMidnight, new Time(LocalTime.MIDNIGHT));

            texts.replace(PatternAmPm, matcher -> {
                String hour = matcher.group("hour");
                String min = matcher.group("min");
                String period = matcher.group("period");
                return parse(hour, min, period);
            });

            texts.replace(Pattern24, matcher -> {
                String hour = matcher.group("hour");
                String min = matcher.group("min");
                return parse(hour, min, null);
            });
        }

        static Time parse(String hourS, String minS, @Nullable String period) {
            int hour = Integer.parseInt(hourS);
            hour += hour != 12 && StringUtils.equalsIgnoreCase(period, "pm") ? 12 : 0;
            int min = minS == null ? 0 : Integer.parseInt(minS);
            return new Time(LocalTime.of(hour, min));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Time time1 = (Time) o;

            return time.equals(time1.time);
        }

        @Override
        public int hashCode() {
            return time.hashCode();
        }

        @Override
        public String toString() {
            return "(Time:" + time.format(TimeRange.hourFormat) + ")";
        }
    }

    static class TimeRange {
        static final DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern("HH:mm");

        private final LocalTime open;
        private final LocalTime close;

        TimeRange(LocalTime open, LocalTime close) {
            this.open = open;
            this.close = close;
        }

        /**
         * @param day day of time range
         * @return hour field
         */
        OpenHour toFields(Day day) {
            OpenHour hour = new OpenHour();
            hour.setOpen(open.format(hourFormat));
            hour.setClose(close.format(hourFormat));
            hour.setDay(day.toDay());
            return hour;
        }

        static void parse(PatternTexts texts) {
            // Map all Time + Range + Time to TimeRange
            texts.replace(Time.class, Range.class, Time.class, triple -> {
                Time left = (Time) triple.getLeft();
                Time right = (Time) triple.getRight();
                return new TimeRange(left.time, right.time);
            });
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TimeRange timeRange = (TimeRange) o;
            if (!open.equals(timeRange.open)) return false;
            return close.equals(timeRange.close);
        }

        @Override
        public int hashCode() {
            int result = open.hashCode();
            result = 31 * result + close.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "(TimeRange:" + open.format(hourFormat) + "-" + close.format(hourFormat) + ")";
        }
    }

    static class TimeRanges {
        private final Set<TimeRange> set;

        TimeRanges(Set<TimeRange> set) {
            this.set = set;
        }

        TimeRanges(TimeRange... ranges) {
            this(Arrays.stream(ranges).collect(Collectors.toSet()));
        }

        void addAll(TimeRanges ranges) {
            set.addAll(ranges.set);
        }

        static void parse(PatternTexts texts) {
            // Map all TimeRange to TimeRanges
            texts.replaceAll(obj -> obj instanceof TimeRange ? new TimeRanges((TimeRange) obj) : obj);

            // Map all Day + And + Day to days
            texts.replace(TimeRanges.class, And.class, TimeRanges.class, triple -> {
                TimeRanges left = (TimeRanges) triple.getLeft();
                TimeRanges right = (TimeRanges) triple.getRight();
                left.addAll(right);
                return left;
            });
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TimeRanges that = (TimeRanges) o;
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
}
