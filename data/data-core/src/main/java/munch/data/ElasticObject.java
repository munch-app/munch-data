package munch.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:11 PM
 * Project: munch-data
 */
public interface ElasticObject {

    /**
     * @return data type to identify the type of data
     */
    @JsonProperty("dataType")
    String getDataType();

    /**
     * @return id to identify the data
     */
    @JsonIgnore
    String getDataId();

    @NotNull
    @JsonProperty("updatedMillis")
    Long getUpdatedMillis();

    void setUpdatedMillis(Long updatedMillis);

    @NotNull
    @JsonProperty("createdMillis")
    Long getCreatedMillis();

    void setCreatedMillis(Long createdMillis);
}
