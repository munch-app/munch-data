package munch.data.elastic.plugins;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.Hour;
import munch.data.elastic.ElasticObject;
import munch.data.elastic.TimingObject;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 2019-03-10
 * Time: 19:29
 * Project: munch-data
 */
@Singleton
public final class TimingPlugin implements ElasticPlugin {
    private static final Logger logger = LoggerFactory.getLogger(TimingPlugin.class);

    @Override
    public void serialize(ElasticObject object, ObjectNode node) {
        if (!(object instanceof TimingObject)) return;

        List<Hour> hours = ((TimingObject) object).getHours();
        node.set("hour", parseHour(hours));
    }

    private static ObjectNode parseHour(List<Hour> hours) {
        ObjectNode objectNode = JsonUtils.createObjectNode();
        if (hours.isEmpty()) return objectNode;

        objectNode.set("mon", collectDay(Hour.Day.mon, hours));
        objectNode.set("tue", collectDay(Hour.Day.tue, hours));
        objectNode.set("wed", collectDay(Hour.Day.wed, hours));
        objectNode.set("thu", collectDay(Hour.Day.thu, hours));
        objectNode.set("fri", collectDay(Hour.Day.fri, hours));
        objectNode.set("sat", collectDay(Hour.Day.sat, hours));
        objectNode.set("sun", collectDay(Hour.Day.sun, hours));
        return objectNode;
    }

    private static ArrayNode collectDay(Hour.Day day, List<Hour> hours) {
        ArrayNode arrayNode = JsonUtils.createArrayNode();

        for (Hour hour : hours) {
            if (day.equals(hour.getDay())) {
                try {
                    arrayNode.addObject()
                            .putObject("open_close")
                            .put("gte", serializeTime(hour.getOpen()))
                            .put("lte", serializeTime(hour.getClose()));
                } catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException e) {
                    logger.error("Time parse error", e);
                }
            }
        }
        return arrayNode;
    }

    public static int serializeTime(String time) {
        String[] hourMin = time.split(":");
        return Integer.parseInt(hourMin[0]) * 60 + Integer.parseInt(hourMin[1]);
    }
}
