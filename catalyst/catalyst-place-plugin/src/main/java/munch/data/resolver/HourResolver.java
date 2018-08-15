package munch.data.resolver;

import catalyst.edit.HourEdit;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.Hour;

import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 12/8/18
 * Time: 2:14 PM
 * Project: munch-data
 */
@Singleton
public final class HourResolver {

    public List<Hour> resolve(PlaceMutation mutation) {
        List<MutationField<List<HourEdit>>> fields = mutation.getHour();
        if (fields.isEmpty()) return List.of();

        List<HourEdit> hours = fields.get(0).getValue();
        return hours.stream()
                .map(this::parseHour)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Hour parseHour(HourEdit hourEdit) {
        Hour.Day day = parseDay(hourEdit.getDay());
        if (day == null) return null;

        Hour hour = new Hour();
        hour.setDay(day);
        hour.setOpen(hourEdit.getOpen());
        hour.setClose(hourEdit.getClose());
        return hour;
    }

    private Hour.Day parseDay(HourEdit.Day day) {
        switch (day) {
            case mon:
                return Hour.Day.mon;
            case tue:
                return Hour.Day.tue;
            case wed:
                return Hour.Day.wed;
            case thu:
                return Hour.Day.thu;
            case fri:
                return Hour.Day.fri;
            case sat:
                return Hour.Day.sat;
            case sun:
                return Hour.Day.sun;
        }

        return null;
    }
}
