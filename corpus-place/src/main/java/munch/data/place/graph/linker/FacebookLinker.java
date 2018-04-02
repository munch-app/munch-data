package munch.data.place.graph.linker;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.linking.LinkingUtils;
import munch.data.place.graph.PlaceTree;

import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 10:20 PM
 * Project: munch-data
 */
public class FacebookLinker implements Linker {
    @Override
    public String getName() {
        return "FacebookLinker";
    }

    @Override
    public boolean link(String placeId, PlaceTree left, Map<String, Integer> matchers, CorpusData right) {
        if (left.getCorpusData().getCorpusName().equals("Global.Facebook.Place")) {
            List<String> facebookLinks = LinkingUtils.getPrefix("facebook.com/", PlaceKey.linking.getAllValue(left.getCorpusData()));
            if (facebookLinks.isEmpty()) return false;

            List<String> rightFbLinks = LinkingUtils.getPrefix("facebook.com/", PlaceKey.linking.getAllValue(right));
            if (rightFbLinks.isEmpty()) return false;

            String facebook = facebookLinks.get(0);
            for (String rightLink : rightFbLinks) {
                if (!facebook.equals(rightLink)) return false;
            }
            return true;
        }
        return false;
    }
}
