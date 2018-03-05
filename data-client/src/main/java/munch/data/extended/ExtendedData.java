package munch.data.extended;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by: Fuxing
 * Date: 6/3/2018
 * Time: 3:05 AM
 * Project: munch-data
 */
public interface ExtendedData {

    /**
     * @return sortKey of data, desc
     */
    String getSortKey();

    /**
     * Overload required to compare ExtendedData
     *
     * @param data extended data
     * @return true if data is same copy, no need to update
     */
    @JsonIgnore
    boolean equals(ExtendedData data);
}
