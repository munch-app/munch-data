package munch.awards;

/**
 * Created by: Fuxing
 * Date: 4/3/2018
 * Time: 9:41 PM
 * Project: munch-data
 */
public class PlaceAward {
    private String collectionId;
    private String collectionAwardId;
    private String awardName;

    /**
     * @return collectionId, can be used to query CollectionPlaceClient
     */
    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    /**
     * @return CollectionAwardId
     */
    public String getCollectionAwardId() {
        return collectionAwardId;
    }

    public void setCollectionAwardId(String collectionAwardId) {
        this.collectionAwardId = collectionAwardId;
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
}
