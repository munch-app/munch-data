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
        status.setMoved(getMoved(statusEdit));
        status.setRenamed(getRenamed(statusEdit));
        status.setRedirected(getRedirected(statusEdit));
        return status;
    }

    private Place.Status.Type parseType(StatusEdit.Type type) {
        switch (type) {
            case open:
                return Place.Status.Type.open;

            case closed:
                return Place.Status.Type.closed;

            case renovation:
                return Place.Status.Type.renovation;

            case closedHidden:
            case spam:
            case error:
            case deleted:
                return Place.Status.Type.deleted;

            case moved:
                return Place.Status.Type.moved;

            case renamed:
                return Place.Status.Type.renamed;

            case merged:
                return Place.Status.Type.redirected;
            default:
                throw new IllegalArgumentException(type.name() + " type not found.");
        }
    }

    private Place.Status.Redirected getRedirected(StatusEdit statusEdit) {
        if (statusEdit.getType() != StatusEdit.Type.merged) return null;

        Place.Status.Redirected redirected = new Place.Status.Redirected();
        redirected.setPlaceId(statusEdit.getPlaceId());
        return redirected;
    }

    private Place.Status.Moved getMoved(StatusEdit statusEdit) {
        if (statusEdit.getType() != StatusEdit.Type.moved) return null;

        Place.Status.Moved moved = new Place.Status.Moved();
        moved.setPlaceId(statusEdit.getPlaceId());
        return moved;
    }

    private Place.Status.Renamed getRenamed(StatusEdit statusEdit) {
        if (statusEdit.getType() != StatusEdit.Type.renamed) return null;

        Place.Status.Renamed renamed = new Place.Status.Renamed();
        renamed.setPlaceId(statusEdit.getPlaceId());
        return renamed;
    }
}
