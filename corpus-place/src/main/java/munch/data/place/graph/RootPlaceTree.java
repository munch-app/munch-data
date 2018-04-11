package munch.data.place.graph;

import java.util.Date;

/**
 * Created by: Fuxing
 * Date: 2/4/2018
 * Time: 3:44 AM
 * Project: munch-data
 */
public class RootPlaceTree {
    private PlaceTree tree;

    private Date updatedDate;

    public RootPlaceTree() {
    }

    public RootPlaceTree(PlaceTree tree) {
        this.updatedDate = new Date();
    }

    public PlaceTree getTree() {
        return tree;
    }

    public void setTree(PlaceTree tree) {
        this.tree = tree;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
