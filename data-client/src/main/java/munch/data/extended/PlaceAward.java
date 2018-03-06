package munch.data.extended;

import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 4/3/2018
 * Time: 9:41 PM
 * Project: munch-data
 */
public final class PlaceAward implements ExtendedData {

    // User Id & CollectionId point to where the collection is at in the collection API
    private String userId;
    private String collectionId;

    // Sort key is also the collection award id
    private String sortKey;
    private String awardName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return collectionId, can be used to query CollectionPlaceClient
     */
    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    @Override
    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    /**
     * @return name of the Award given to Place
     */
    public String getAwardName() {
        return awardName;
    }

    public void setAwardName(String awardName) {
        this.awardName = awardName;
    }

    @Override
    public boolean equals(ExtendedData data) {
        return equals((Object) data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceAward that = (PlaceAward) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(collectionId, that.collectionId) &&
                Objects.equals(sortKey, that.sortKey) &&
                Objects.equals(awardName, that.awardName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, collectionId, sortKey, awardName);
    }
}
