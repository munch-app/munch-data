package munch.data.resolver;

import catalyst.edit.StatusEdit;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.place.Place;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 12/8/18
 * Time: 2:07 PM
 * Project: munch-data
 */
@Singleton
public final class RankingResolver {

    public double resolve(Place place, PlaceMutation mutation) {
        if (isClosed(mutation)) return 0;

        List<MutationField<Double>> ranking = mutation.getRanking();
        if (ranking.isEmpty()) return 100;

        if (place.getImages().isEmpty()) {
            return ranking.get(0).getValue();
        }

        return ranking.get(0).getValue() + 1500;
    }

    private boolean isClosed(PlaceMutation mutation) {
        for (MutationField<StatusEdit> field : mutation.getStatus()) {
            switch (field.getValue().getType()) {
                case closed:
                case moved:
                case error:
                case spam:
                case closedHidden:
                case renovation:
                    return true;

            }
        }
        return false;
    }
}
