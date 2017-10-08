package munch.data.database;

/**
 * Created by: Fuxing
 * Date: 23/8/2017
 * Time: 12:28 AM
 * Project: munch-core
 */
public interface AbstractEntity<T> {

    Long getCycleNo();

    void setCycleNo(Long cycleNo);

    T getData();

    void setData(T data);
}
