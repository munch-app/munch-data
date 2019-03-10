package munch.data.elastic.plugins;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.elastic.ElasticObject;
import munch.data.elastic.SuggestObject;
import munch.data.place.Place;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 2019-03-10
 * Time: 19:26
 * Project: munch-data
 */
@Singleton
public final class SuggestPlacePlugin implements ElasticPlugin {

    @Override
    public void serialize(ElasticObject object, ObjectNode node) {
        if (!(object instanceof Place)) return;
        Place place = (Place) object;
        SuggestObject.Context context = place.getSuggestContext();

        ObjectNode suggest = node.putObject("suggest_places");

        // Weight
        suggest.put("weight", context != null ? context.getWeight() : 1);

        // Input
        ArrayNode array = suggest.putArray("input");
        SuggestCountryPlugin.getNames(place.getName(), place.getNames()).forEach(array::add);

        // Contexts: City
        ObjectNode contexts = suggest.putObject("contexts");
        contexts.putArray("city")
                .add(place.getLocation().getCity().name());
    }
}
