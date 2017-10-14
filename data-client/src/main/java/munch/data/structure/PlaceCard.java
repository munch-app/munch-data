package munch.data.structure;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * contains 2 fields: id, data
 * <pre>
 * {
 *      "id": "type_Name_version",
 *      "fields": ...
 * }
 * </pre>
 * <p>
 * Data in the card should be at its most primitive form.
 * String, Double, Integer, Set no POJO
 * <p>
 * Created by: Fuxing
 * Date: 6/9/2017
 * Time: 12:35 AM
 * Project: munch-core
 */
public interface PlaceCard<T> {

    /**
     * Id format:
     * type_Name_version(yyyymmdd)
     * E.g. basic_Banner_20170609
     * E.g. vendor_FacebookReview_20171205
     * E.g. vendor_InstagramMedia_20160101
     * <p>
     * Version of the card is usually a result of data structure update
     * Rarely it can be based on incremental design changes as well
     *
     * @return id of the card
     */
    @JsonProperty("_cardId")
    String getCardId();

    /**
     * @return data
     */
    T getData();
}
