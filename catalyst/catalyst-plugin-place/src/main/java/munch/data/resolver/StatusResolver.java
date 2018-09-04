package munch.data.resolver;

import catalyst.edit.StatusEdit;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.place.Place;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 11/8/18
 * Time: 7:00 AM
 * Project: munch-data
 */
@Singleton
public final class StatusResolver {

    public Place.Status resolve(PlaceMutation mutation) {
        List<MutationField<StatusEdit>> fields = mutation.getStatus();

        if (fields.isEmpty()) {
            Place.Status status = new Place.Status();
            status.setType(Place.Status.Type.open);
            return status;
        }

        StatusEdit statusEdit = fields.get(0).getValue();
        Place.Status status = new Place.Status();
        status.setType(parseType(statusEdit.getType()));
        return status;
    }

    private Place.Status.Type parseType(StatusEdit.Type type) {
        switch (type) {
            case closedHidden:
                throw new ResolverHaltException("Closed Hidden");
            case spam:
                throw new ResolverHaltException("Spam");
            case error:
                throw new ResolverHaltException("Error");

            case closed:
                return Place.Status.Type.closed;
            case open:
                return Place.Status.Type.open;
            case renovation:
                return Place.Status.Type.renovation;
            case moved:
                return Place.Status.Type.moved;
            default:
                throw new IllegalArgumentException(type.name() + " type not found.");
        }
    }
}
