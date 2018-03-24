package munch.data.hour.tokens;

import munch.data.hour.OpenHour;
import munch.data.utils.PatternSplit;
import munch.data.utils.PatternTexts;

import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 11:51 AM
 * Project: munch-data
 */
public enum DayToken {
    Mon, Tue, Wed, Thu, Fri, Sat, Sun, Ph, EvePh;

    public static DayToken[] daily() {
        return new DayToken[]{Mon, Tue, Wed, Thu, Fri, Sat, Sun};
    }

    static final PatternSplit PatternMon = PatternSplit.compile("\\b(mon|mondays?)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternTue = PatternSplit.compile("\\b(tue|tues|tuesdays?)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternWed = PatternSplit.compile("\\b(wed|wednesdays?)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternThu = PatternSplit.compile("\\b(thu|thurs?|thursdays?)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternFri = PatternSplit.compile("\\b(fri|fridays?)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternSat = PatternSplit.compile("\\b(sat|saturdays?)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternSun = PatternSplit.compile("\\b(sun|sundays?)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternPh = PatternSplit.compile("\\b(ph|public holidays?|holidays?)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternEvePh = PatternSplit.compile("\\b(public holidays? eve|eveph|eve of (ph|public holidays?|holidays?))\\b", Pattern.CASE_INSENSITIVE);

    public static void parse(PatternTexts texts) {
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
