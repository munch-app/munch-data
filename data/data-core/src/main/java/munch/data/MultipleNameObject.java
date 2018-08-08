package munch.data;

import java.util.Set;

/**
 * Implement this if object contains multiple name, all names will be added to suggest input fields
 *
 * @see SuggestObject
 * <p>
 * Created by: Fuxing
 * Date: 1/6/18
 * Time: 3:57 PM
 * Project: munch-data
 */
public interface MultipleNameObject {

    /**
     * @return correctable names
     */
    Set<String> getNames();
}
