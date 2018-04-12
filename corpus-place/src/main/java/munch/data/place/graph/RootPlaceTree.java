package munch.data.place.graph;

import java.util.Date;

/**
 * Created by: Fuxing
 * Date: 2/4/2018
 * Time: 3:44 AM
 * Project: munch-data
 */
public final class RootPlaceTree extends PlaceTree {
    /*
    Ideally, should move all the helper method here and enable caching?
     */
    private Date updatedDate;

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
