package munch.data.elastic;

import munch.data.Hour;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 17/6/18
 * Time: 4:17 PM
 * Project: munch-data
 */
public interface TimingObject {

    /**
     * @return hours object, providing it makes hours searchable
     */
    List<Hour> getHours();
}
