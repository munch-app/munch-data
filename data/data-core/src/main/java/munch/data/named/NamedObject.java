package munch.data.named;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Created by: Fuxing
 * Date: 28/11/18
 * Time: 9:50 AM
 * Project: munch-data
 */
public interface NamedObject {

    /**
     * Because NamedObject is used in the front-end, slug is the id and must adhered to the strict pattern
     *
     * @return slug, the id of the object, lowercase validated
     */
    @NotNull
    @Pattern(regexp = "[a-z0-9-]{1,255}")
    String getSlug();

    void setSlug(String slug);

    /**
     * Named Object requires version-ing to allow smooth front-end migration
     *
     * @return version in yyyy-mm-dd format
     */
    @NotNull
    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}")
    String getVersion();

    void setVersion(String version);

    /**
     * @return last updated millis
     */
    @NotNull
    Long getUpdatedMillis();

    void setUpdatedMillis(Long updatedMillis);
}
