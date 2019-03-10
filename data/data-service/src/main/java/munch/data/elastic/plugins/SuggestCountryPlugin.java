package munch.data.elastic.plugins;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.elastic.ElasticObject;
import munch.data.elastic.SuggestObject;
import munch.data.location.Country;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 2019-03-10
 * Time: 19:25
 * Project: munch-data
 */
public final class SuggestCountryPlugin implements ElasticPlugin {
    private static final Pattern pattern = Pattern.compile("[^a-z0-9]");

    @Override
    public void serialize(ElasticObject elasticObject, ObjectNode node) {
        if (!(elasticObject instanceof SuggestObject)) return;
        SuggestObject object = (SuggestObject) elasticObject;
        SuggestObject.Context context = object.getSuggestContext();
        if (context == null) return;

        getFields(context).forEach(fieldName -> {
            ObjectNode suggest = node.putObject(fieldName);

            // Weight
            suggest.put("weight", context.getWeight());

            // Input
            ArrayNode array = suggest.putArray("input");
            getNames(object).forEach(array::add);

            // Contexts: DataType (Required)
            ObjectNode contexts = suggest.putObject("contexts");
            contexts.putArray("dataType")
                    .add(elasticObject.getDataType().name());


            // Contexts: LatLng (Optional)
            if (context.getLatLng() != null) {
                contexts.putArray("latLng").add(context.getLatLng());
            }
        });
    }

    private static Set<String> getFields(SuggestObject.Context context) {
        return context.getCountries().stream()
                .map(Country::getSuggestField)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    private static Set<String> getNames(SuggestObject object) {
        if (object instanceof SuggestObject.Names) {
            return getNames(object.getName(), ((SuggestObject.Names) object).getNames());
        }

        return getNames(object.getName(), null);
    }

    /**
     * @return all possible names from the suggest object
     */
    public static Set<String> getNames(String objectName, @Nullable Set<String> objectNames) {
        Set<String> names = new HashSet<>();
        // Normal Name
        names.add(StringUtils.lowerCase(objectName));

        // Strip Accents
        String name = StringUtils.stripAccents(objectName);
        names.add(StringUtils.lowerCase(name));

        // Strip All Symbols
        names.add(pattern.matcher(name).replaceAll(""));

        // Add all other names
        if (objectNames != null) {
            objectNames.forEach(s -> names.add(StringUtils.lowerCase(s)));
        }

        // Remove all blank string
        names.removeIf(StringUtils::isBlank);
        return names;
    }
}
